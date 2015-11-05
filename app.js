var express = require('express');
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var users = require('./routes/users'); //routes are defined here
var events = require('./routes/events');
var bodyParser = require('body-parser'); // for reading POSTed form data into `req.body`
var expressSession = require('express-session');
var app = express(); //Create the Express app

//connect to our database
//Ideally you will obtain DB details from a config file
var dbName = 'calendarDB';
var connectionString = 'mongodb://localhost:27017/' + dbName;

mongoose.connect(connectionString);

app.use(cookieParser());
app.use(expressSession({secret:'somesecrettokenhere'}));
//configure body-parser
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use('/api', users); //This is our route middleware
app.use('/api', events); //This is our route middleware
app.use(express.static(__dirname + "/public"));

//Handle 404
app.get("/*", function(req, res, next) {
    next("Could not find page");
});

module.exports = app;
