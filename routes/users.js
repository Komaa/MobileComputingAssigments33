var User = require('../models/user');
var express = require('express');
var router = express.Router();
var bcrypt = require('bcryptjs');

//retrive all the users
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
  var hash = bcrypt.hashSync((String)req.body.password);
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
  //check if the user is present
  User.findOne({ _id: req.params.id }, function(err, user) {
    if (err) {
      return res.send(err);
    }
    //for every properties to change in the body
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

//retrive a user
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

//check if the user is present and in case give back his information
router.route('/users/login').post(function(req, res) {
  User.findOne({ username: req.body.username, password: req.body.password}, function(err, user) {
    if (err) {
      return res.send(err);
    }
    res.json(user);
  });
});

module.exports = router;
