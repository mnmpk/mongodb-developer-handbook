const mongoose = require("mongoose");
const moment = require("moment");
const userSchema = new mongoose.Schema({
  name: String,
  lastActiveAt: Date,
  version: Number
}, {
  timestamps: true,
  methods: {
    toJsonWithLocalDate() {
      let ret = {...this._doc };
      for (const [key, value] of Object.entries(ret)) {
        if(value instanceof Date){
          console.log(`${key}: ${value}`);
          ret[key] = moment(value).format();
        }
      }
      return ret;
    }
  }
});
module.exports = mongoose.model('User', userSchema);