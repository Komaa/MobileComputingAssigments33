var User = require('../models/user');
var Event = require('../models/event');
var express = require('express');
var nodemailer = require('nodemailer');
var extend = require('util')._extend;
var router = express.Router();

//get all the event
router.route('/events').get(function(req, res) {
  Event.find(function(err, events) {
    if (err) {
      return res.send(err);
    }
    res.json(events);
  });
});

//get all the events related to a user
router.route('/events/:id').get(function(req, res) {
  Event.find({ id_user: req.params.id},function(err, events) {
    if (err) {
      return res.send(err);
    }
    res.json(events);
  });
});

//insert an event
router.route('/events/:id').post(function(req, res) {
  _ = require('underscore');

 //send in the format loc = 22.9,-10 in body of Post request
  var cordi = req.body.loc.split(',');

  var event = new Event(_.extend({ id_user: req.params.id }, req.body,{loc:cordi}));
  event.save(function(err) {
    if (err) {
      return res.send(err);
    }
    res.send({ message: 'Event Added' });
  });
});

//copy an event
router.route('/events/copyevent/:id').post(function(req, res) {
  _ = require('underscore');
  Event.findOne({ _id:req.body.id_event}, function(err, event) {
    if (err) {
      return res.send(err);
    }
    event.id_user=req.params.id;
    console.log(event);
    // specify the transform schema option
    if (!schema.options.toObject) schema.options.toObject = {};
    schema.options.toObject.transform = function (doc, event, options) {
      // remove the _id of every document before returning the result
      delete ret._id;
    }

    console.log(event);
    event.save(function(err) {
    if (err) {
      return res.send(err);
    }
    res.send({ message: 'Event Copied' });
    });
  });
});

//delete an event
router.route('/events/:id').delete(function(req, res) {
  Event.remove({
    _id: req.params.id
  }, function(err, event) {
    if (err) {
      return res.send(err);
    }

    res.json({ message: 'Event successfully deleted' });
  });
});

//modify an event
router.route('/events/:id').put(function(req,res){
  Event.findOne({ _id: req.params.id }, function(err, event) {
    if (err) {
      return res.send(err);
    }

    for (prop in req.body) {
      event[prop] = req.body[prop];
    }
    // save the event
    event.save(function(err) {
      if (err) {
        return res.send(err);
      }

      res.json({ message: 'Event updated!' });
    });
  });
});

//retriving a event by id
router.route('/events/search/:id_user').get(function(req, res) {
  Event.findOne({ _id:req.query.id, id_user:req.params.id_user}, function(err, event) {
    if (err) {
      return res.send(err);
    }
    res.json(event);
  });
});

//retriving a event by name
router.route('/events/search/byname/:id_user').get(function(req, res) {
  Event.find({ name:req.query.name, id_user:req.params.id_user}, function(err, event) {
    if (err) {
      return res.send(err);
    }
    res.json(event);
  });
});

//retriving a event by type
router.route('/events/search/bytype/:id_user').get(function(req, res) {
  Event.find({ type:req.query.type, id_user:req.params.id_user}, function(err, event) {
    if (err) {
      return res.send(err);
    }
    res.json(event);
  });
});

//retriving a event by datetime
router.route('/events/search/bydate/:id_user').get(function(req, res) {
  Event.find({ end_event: { $gt: new Date(req.query.start_time)}, start_event: { $lt: new Date(req.query.end_time)}, id_user:req.params.id_user}, function(err, event) {
    if (err) {
      return res.send(err);
    }
    res.json(event);
  });
});

//retriving a event by location
router.route('/events/search/bylocation/:id_user').get(function(req, res) {

  var coords = req.query.loc.split(',');
  //find events withing a 10 km radius
  var maxDist = req.query.distance / 6371;

  Event.find({ loc: {$near: coords, $maxDistance: maxDist }, id_user:req.params.id_user}, function(err, event) {
    if (err) {
      return res.send(err);
    }
    res.json(event);
  });
});


//send an event by mail
router.route('/events/invite/:id_user').post(function(req, res) {
  var email=req.body.mail;
  Event.findOne({ _id:req.body.id_event, id_user:req.params.id_user}, function(err, event) {
    if (err) {
      return res.send(err);
    }
    console.log(event.name);
    User.findOne({ _id: req.params.id_user}, function(err, user) {
        if (err) {
          return res.send(err);
        }
        console.log(user.username);
        console.log(event.name);
        var transporter = nodemailer.createTransport({
              service: 'Gmail',
              auth: {
                  user: 'mobilecalendar33@gmail.com', // Your email id
                  pass: 'mobil3calendar33' // Your password
              }
          });

        var text = 'Greeting from Strax Calendar team! \n\n'+
        'The user '+ user.username + ' invited you to the event ' + event.name
        +'\n\nPlease click on the following link to accept the invitation:\n\n'+
        'www.straxcalendar.com/acceptinvitation/'+ event._id+
        '\n\nBest Regards';

          var mailOptions = {
            from: 'mobilecalendar33@gmail.com', // sender address
            to: email, // list of receivers
            subject: 'Invitation to an event', // Subject line
            text: text //, // plaintext body
        };

        transporter.sendMail(mailOptions, function(error, info){
          if(error){
              console.log(error);
              res.json(error);
          }else{
              console.log('Message sent: ' + info.response);
              res.json(info.response);
          };
        });

    });
  });
});

module.exports = router;
