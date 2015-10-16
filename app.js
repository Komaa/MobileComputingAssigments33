var express = require('express');
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var users = require('./routes/users'); //routes are defined here
var events = require('./routes/events');
var app = express(); //Create the Express app
var mailer = require('express-mailer');

mailer.extend(app, {
  from: 'mobilecalendar33@gmail.com',
  host: 'smtp.gmail.com', // hostname
  secureConnection: true, // use SSL
  port: 465, // port for secure SMTP
  transportMethod: 'SMTP', // default is SMTP. Accepts anything that nodemailer accepts
  auth: {
    user: 'mobilecalendar33@gmail.com',
    pass: 'mobil3calendar33'
  }
});

//connect to our database
//Ideally you will obtain DB details from a config file
var dbName = 'calendarDB';
var connectionString = 'mongodb://localhost:27017/' + dbName;

mongoose.connect(connectionString);

//configure body-parser
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use('/api', users); //This is our route middleware
app.use('/api', events); //This is our route middleware

module.exports = app;
