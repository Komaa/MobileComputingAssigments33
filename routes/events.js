var User = require('../models/user');
var Event = require('../models/event');
var express = require('express');
var router = express.Router();

//get all the event
router.route('/event').get(function(req, res) {
  Event.find(function(err, events) {
    if (err) {
      return res.send(err);
    }
    res.json(events);
  });
});

//get all the events related to a user
router.route('/event/:id').get(function(req, res) {
  Event.find({ id_user: req.params.id},function(err, events) {
    if (err) {
      return res.send(err);
    }
    res.json(events);
  });
});

//insert a event
router.route('/event/:id').post(function(req, res) {
  _ = require("underscore");
  var event = new Event(_.extend({ id_user: req.params.id }, req.body));
  event.save(function(err) {
    if (err) {
      return res.send(err);
    }
    res.send({ message: 'Event Added' });
  });
});

module.exports = router;
