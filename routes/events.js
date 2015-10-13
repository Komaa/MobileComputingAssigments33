var User = require('../models/user');
var Event = require('../models/event');
var express = require('express');
var router = express.Router();

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
router.route('/event').post(function(req, res) {
  var event = new Event(req.body);
  Console.log(req.body);
  Console.log("hi");
  Console.log(event);
  event.save(function(err) {
    if (err) {
      return res.send(err);
    }
    res.send({ message: 'Event Added' });
  });
});

module.exports = router;
