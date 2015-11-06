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
  //encrypt the password
  var hash = bcrypt.hashSync(req.body.password);
  req.body.password=hash;
  var user = new User(req.body);
  user.save(function(err) {
    if (err) {
      return res.send(err);
    }
    res.send('User Added');
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
      //if you are modifing the password field encrypt it
      if(prop === "password")
        user[prop]=bcrypt.hashSync(req.body[prop]);
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
  User.findOne({ username: req.body.username}, function(err, user) {
    if (err) {
      return res.send(err);
    }
    //compare if the encrypted password on the DB is the same as the one insered
    if((user!=null) && bcrypt.compareSync(req.body.password, user.password)){
      req.session.userid=user._id;
      res.send('true');
    }else {
      res.send('false');
    }
  });
});

router.route('/users/logout').post(function(req, res) {
  req.session.destroy();
  res.send('true');
});

module.exports = router;
