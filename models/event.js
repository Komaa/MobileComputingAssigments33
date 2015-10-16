var mongoose=require('mongoose');
var Schema=mongoose.Schema;

var eventSchema = new Schema({
  name: String,
  id_user: [
      {type: Schema.Types.ObjectId, ref: 'User'}
    ],
  description: String,
  start_event: Date,
  end_event: Date,
  loc: {
    type: [Number],  // [<longitude>, <latitude>]
    index: '2d'      // create the geospatial index
  },
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
eventSchema.set('versionKey', false);


module.exports = mongoose.model('Event', eventSchema);
