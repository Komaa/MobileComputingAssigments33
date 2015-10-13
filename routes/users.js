var User = require('../models/user');
var express = require('express');
var router = express.Router();

//get all the users
router.route('/users').get(function(req, res) {
  User.find(function(err, users) {
    if (err) {
      return res.send(err);
    }
    res.json(users);
  });
});

//insert a new user
router.route('/users').post(function(req, res) {
  var user = new User(req.body);
  user.save(function(err) {
    if (err) {
      return res.send(err);
    }
    res.send({ message: 'User Added' });
  });
});

//modify an user
router.route('/users/:id').put(function(req,res){
  User.findOne({ _id: req.params.id }, function(err, user) {
    if (err) {
      return res.send(err);
    }

    for (prop in req.body) {
      user[prop] = req.body[prop];
    }

    // save the user
    user.save(function(err) {
      if (err) {
        return res.send(err);
      }

      res.json({ message: 'User updated!' });
    });
  });
});

//retriving a user
router.route('/users/:id').get(function(req, res) {
  User.findOne({ _id: req.params.id}, function(err, user) {
    if (err) {
      return res.send(err);
    }

    res.json(user);
  });
});

//delete a user
router.route('/users/:id').delete(function(req, res) {
  User.remove({
    _id: req.params.id
  }, function(err, user) {
    if (err) {
      return res.send(err);
    }

    res.json({ message: 'Successfully deleted' });
  });
});

//retriving a user
router.route('/users/login').get(function(req, res) {
  User.findOne({ username: req.params.username, password; req.params.password}, function(err, user) {
    if (err) {
      return res.send(err);
    }

    res.json(user);
  });
});

module.exports = router;
