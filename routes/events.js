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
  var event = new Event(_.extend({ id_user: req.params.id }, req.body));
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
  console.log(req.query.id);
  Event.findOne({ _id: req.query.id, user_id:req.params.id_user}, function(err, event) {
    if (err) {
      return res.send(err);
    }

    res.json(event);
  });
});

//retriving a event by name
router.route('/events/search/byname/:name').get(function(req, res) {
  Event.findOne({ name: req.params.name}, function(err, event) {
    if (err) {
      return res.send(err);
    }

    res.json(event);
  });
});

//retriving a event by date
router.route('/events/search/bydate/:date').get(function(req, res) {
  Event.findOne({ _id: req.params.id}, function(err, event) {
    if (err) {
      return res.send(err);
    }

    res.json(event);
  });
});

//retriving a event by location
router.route('/events/search/bylocation/:location').get(function(req, res) {
  Event.findOne({ _id: req.params.id}, function(err, event) {
    if (err) {
      return res.send(err);
    }

    res.json(event);
  });
});

//retriving a event by type
router.route('/events/search/bytype/:type').get(function(req, res) {
  Event.findOne({ type: req.params.type}, function(err, event) {
    if (err) {
      return res.send(err);
    }

    res.json(event);
  });
});

module.exports = router;
