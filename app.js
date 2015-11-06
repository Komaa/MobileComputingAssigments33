var express = require('express');
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var users = require('./routes/users'); //routes are defined here
var events = require('./routes/events');
var bodyParser = require('body-parser'); // for reading POSTed form data into `req.body`
var expressSession = require('express-session');
var cookieParser = require('cookie-parser'); // the session is stored in a cookie, so we use this to parse it
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

app.use(function (req, res, next) {
  var fullUrl = req.protocol + '://' + req.get('host') + req.originalUrl;


    var id;
    if(req.session.userid == null)
      id=""
    else
      id=req.session.userid;

    if(req.method === "POST"){
        //console.log(req.body);
        if((req.originalUrl ==="/api/events") || (req.originalUrl ==="/api/events/copyevent") || (req.originalUrl ==="/api/events/invite")){
          req.originalUrl=req.originalUrl+"/"+id;
          req.params.id = id;
          console.log(req.originalUrl);
          res.redirect(307,req.originalUrl);
        }else{
          next();
        }
    }else{
      if((req.originalUrl ==="/api/events") || (req.originalUrl ==="/api/events/search") || (req.originalUrl ==="/api/events/search/byname") ||
      (req.originalUrl ==="/api/events/search/bytype") || (req.originalUrl ==="/api/events/search/bydate") || (req.originalUrl ==="/api/events/search/bylocation")){
        req.originalUrl=req.originalUrl+"/"+id;
        req.params.id = id;
        res.redirect(req.originalUrl);
      }else{
        next();
      }
    }
});

app.use('/api', users); //This is our route middleware
app.use('/api', events); //This is our route middleware
app.use(express.static(__dirname + "/public"));

//Handle 404
app.get("/*", function(req, res, next) {
    next("Could not find page");
});

module.exports = app;
