var User = require('../models/user');
var Event = require('../models/event');
var express = require('express');
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

//insert a event
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
  var mailOptions = {
    to: email,
    subject: 'Hello',
    locals: {
      title: 'Hello',
      message: 'Welcome to my website'
    }
  }
  // Send email.
  res.send('mail', mailOptions, function (error, response) {
    if (error) {
      console.log(error);
      return res.send(err);
    } else {
      console.log('Message sent: ' + response.message);
      res.json({ message: 'Message sent!' });
    }
  });
});

module.exports = router;
