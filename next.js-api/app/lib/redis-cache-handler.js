const createClient = require('redis');

module.exports = class CacheHandler {
  options
  client;
  constructor(options) {
    this.options = options;
    const redisUri = "rediss://clustercfg.tsp-cache.byysaj.ape1.cache.amazonaws.com:6379";
    this.client = createClient({
      url: redisUri
    })
    this.client.connect().catch(console.error)
  }
 
  async get(key) {
    // This could be stored anywhere, like durable storage
    return JSON.parse(await this.client.get(key));
  }
 
  async set(key, data, ctx) {
    // This could be stored anywhere, like durable storage
    await this.client.set(key, JSON.stringify(data));
  } 
}