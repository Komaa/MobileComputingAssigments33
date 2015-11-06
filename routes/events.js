var User = require('../models/user');
var Event = require('../models/event');
var express = require('express');
var nodemailer = require('nodemailer');
var extend = require('util')._extend;
var router = express.Router();



//get all the events related to a user
router.route('/events/:id').get(function(req, res) {
  Event.find({ id_user: req.params.id},function(err, events) {
    if (err) {
      return res.send(err);
    }
    res.json(events);
  });
});

//get all the event
router.route('/events').get(function(req, res) {
  Event.find(function(err, events) {
    if (err) {
      return res.send(err);
    }
    res.json(events);
  });
});

//insert an event
router.route('/events/:id').post(function(req, res) {
  console.log(req.body);
  _ = require('underscore');
var cordi;
 //send in the format loc = 22.9,-10 in body of Post request
/* if(req.body.loc !== 'undefined')
    cordi = req.body.loc.split(',');
  else{
    cordi[0]=0;
    cordi[1]=0;
  }*/

  var event = new Event(_.extend({ id_user: req.params.id }, req.body/*,{loc:cordi}*/));
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
    //retreive the event
    var newEvent=event;
    //change _id with null
    newEvent._id=null;
    //set the mongoDB variable isNew to true
    newEvent.isNew = true;
    //add the id of the new user
    newEvent.id_user=req.params.id;

    newEvent.save(function(err) {
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
    //for every properties changed in the body
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
  //find events withing a req.query.distance km radius
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
  var email=req.body.email;
  //search if the event exist, if not return
  Event.findOne({ _id:req.body.id_event, id_user:req.params.id_user}, function(err, event) {
    if (err) {
      return res.send(err);
    }
    // search if the inviter exist, if not return
    User.findOne({ _id: req.params.id_user}, function(err, user) {
        if (err) {
          return res.send(err);
        }
        //create the trasporter variable in order to send the email
        var transporter = nodemailer.createTransport({
              service: 'Gmail',
              auth: {
                  user: 'mobilecalendar33@gmail.com', // Your email id
                  pass: 'mobil3calendar33' // Your password
              }
          });
        // create the body of the message
        var text = 'Greeting from Strax Calendar team! \n\n'+
        'The user '+ user.username + ' invited you to the event: ' + event.name
        +'\n\nDetails:\n'+
        '\nDescription:\t'+ event.description
        +'\nStarting date:\t'+ event.start_event
        +'\nEnding Date:\t'+ event.end_event
        +'\n\nPlease click on the following link to accept the invitation:\n\n'
        //the link will point to the web service
        +'www.straxcalendar.com/acceptinvitation/'+ event._id
        +'\n\nBest Regards';
        //create the option of the message
          var mailOptions = {
            from: 'mobilecalendar33@gmail.com', // sender address
            to: email, // list of receivers
            subject: 'Invitation to an event', // Subject line
            text: text // plaintext body
        };
        //send the email
        transporter.sendMail(mailOptions, function(error, info){
          if(error){
              console.log(error);
              res.json(error);
          }else{
              console.log('Message sent: ' + info.response);
              res.json('Message sent: ' + info.response);
          };
        });

    });
  });
});

//syncronizing to google calendar
router.route('/events/syncronize/from/:id_user').get(function(req, res) {


});



module.exports = router;
