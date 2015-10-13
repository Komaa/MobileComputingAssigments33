var mongoose=require('mongoose');
var Schema=mongoose.Schema;

var eventSchema = new Schema({
  name: String,
  id_user: String,
  description: String,
  start_event: Date,
  end_event: Date,
  latitude: Number,
  longitude: Number,
  repetition: Boolean,
  /* 0 for daily
   1 for weekly
   2 for monthly
   3 for yearly */
  when_repetition: Number,
  // if the event needs remainder with notification
  alert: Boolean,
  when_alert: Date,
  // type of the event (i.e work, fun)
  type: String
});


module.exports = mongoose.model('Event', eventSchema);
