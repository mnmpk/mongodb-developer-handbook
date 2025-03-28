const {connect} = require('./database');

module.exports = class CacheHandler {
  options
  db;
  constructor(options) {
    this.options = options;
    this.db = connect(this.options.database);
    /*this.db.then(db=>{
      db.collection('sessions').findOne().then(r=>console.log(r));
    });*/
  }
 
  async get(key,
    meta) {
    console.log(key);
    // This could be stored anywhere, like durable storage
    return await(await this.db).collection('cache').findOneAndUpdate({ _id: key }, { $set: { expires: new Date(Date.now() + this.options.ttl * 1000) } });
  }
 
  async set(key, data) {
    console.log(key);
    // This could be stored anywhere, like durable storage
    await(await this.db).collection('cache').replaceOne({ _id: key }, { _id: key, v: data, expires: new Date(Date.now() + this.options.ttl * 1000) }, { upsert: true });
  }
  async revalidateTag(tags) {
    // tags is either a string or an array of strings
    tags = [tags].flat()
    
    await(await this.db).collection('cache').deleteOne({_id:{$in:tags}});
  }
}