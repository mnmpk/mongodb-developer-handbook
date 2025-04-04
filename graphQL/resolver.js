import { PubSub } from 'graphql-subscriptions';
import { getAccountArea15days, getAccountArea1day, getAccountArea3mins, getAccountCasinoArea15days, getAccountCasinoArea1day, getAccountCasinoArea3mins, getAccountCasino15days, getAccountCasino1day, getAccountCasino3mins, getCasinoAreaLocation1day,
  watchAccountArea15days, watchAccountArea1day, watchAccountArea3mins, watchAccountCasinoArea15days, watchAccountCasinoArea1day, watchAccountCasinoArea3mins, watchAccountCasino15days, watchAccountCasino1day, watchAccountCasino3mins, watchCasinoAreaLocation1day } from './mongoDBOperation.js'
import GraphQLDecimal from 'graphql-type-decimal';

const pubsub = new PubSub();

// Resolvers define how to fetch the types defined in your schema.
// This resolver retrieves books from the "books" array above.
export const resolvers = {
  Decimal: GraphQLDecimal,
  Query: {
    getAccountArea15days: async ()=> {
      return await getAccountArea15days();
    },
    getAccountArea1day: async ()=> {
      return await getAccountArea1day();
    },
    getAccountArea3mins: async ()=> {
      return await getAccountArea3mins();
    },
    getAccountCasinoArea15days: async ()=> {
      return await getAccountCasinoArea15days();
    },
    getAccountCasinoArea1day: async ()=> {
      return await getAccountCasinoArea1day();
    },
    getAccountCasinoArea3mins: async () => {
      return await getAccountCasinoArea3mins(); 
    },
    getAccountCasino15days: async () => {
      return await getAccountCasino15days(); 
    },
    getAccountCasino1day: async () => {
      return await getAccountCasino1day(); 
    },
    getAccountCasino3mins: async () => {
      return await getAccountCasino3mins(); 
    },
    getCasinoAreaLocation1day: async () => {
      return await getCasinoAreaLocation1day(); 
    }
  },
  Subscription: {
    watchAccountArea15days: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountArea15days", 
            // theNext
            {watchAccountArea15days: theNext}
          );
        }
        watchAccountArea15days(_handler);
        return pubsub.asyncIterator(["watchAccountArea15days"]);
      }
    },
    watchAccountArea1day: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountArea1day", 
            // theNext
            {watchAccountArea1day: theNext}
          );
        }
        watchAccountArea1day(_handler);
        return pubsub.asyncIterator(["watchAccountArea1day"]);
      }
    },
    watchAccountArea3mins: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountArea3mins", 
            // theNext
            {watchAccountArea3mins: theNext}
          );
        }
        watchAccountArea3mins(_handler);
        return pubsub.asyncIterator(["watchAccountArea3mins"]);
      }
    },
    watchAccountCasinoArea15days: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountCasinoArea15days", 
            {watchAccountCasinoArea15days: theNext}
          );
        }
        watchAccountCasinoArea15days(_handler);
        return pubsub.asyncIterator(["watchAccountCasinoArea15days"]);
      }
    },
    watchAccountCasinoArea1day: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountCasinoArea1day", 
            {watchAccountCasinoArea1day: theNext}
          );
        }
        watchAccountCasinoArea1day(_handler);
        return pubsub.asyncIterator(["watchAccountCasinoArea1day"]);
      }
    },
    watchAccountCasinoArea3mins: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountCasinoArea3mins", 
            {watchAccountCasinoArea3mins: theNext}
          );
        }
        watchAccountCasinoArea3mins(_handler);
        return pubsub.asyncIterator(["watchAccountCasinoArea3mins"]);
      }
    },
    watchAccountCasino15days: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountCasino15days", 
            {watchAccountCasino15days : theNext}
          );
        }
        watchAccountCasino15days(_handler);
        return pubsub.asyncIterator(["watchAccountCasino15days"]);
      }
    },
    watchAccountCasino1day: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountCasino1day", 
            {watchAccountCasino1day : theNext}
          );
        }
        watchAccountCasino1day(_handler);
        return pubsub.asyncIterator(["watchAccountCasino1day"]);
      }
    },
    watchAccountCasino3mins: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountCasino3mins", 
            {watchAccountCasino3mins : theNext}
          );
        }
        watchAccountCasino3mins(_handler);
        return pubsub.asyncIterator(["watchAccountCasino3mins"]);
      }
    },
    watchCasinoAreaLocation1day: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchCasinoAreaLocation1day", 
            {watchCasinoAreaLocation1day : theNext}
          );
        }
        watchCasinoAreaLocation1day(_handler);
        return pubsub.asyncIterator(["watchCasinoAreaLocation1day"]);
      }
    }
  }
};