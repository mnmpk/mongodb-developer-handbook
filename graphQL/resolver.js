import { PubSub } from 'graphql-subscriptions';
import { getAccountArea15days, getAccountArea3mins, getAccountCasinoArea1day, getAccountCasinoArea3mins, getAccountCasino1day, 
  watchAccountArea15days, watchAccountArea3mins, watchAccountCasinoArea1day, watchAccountCasinoArea3mins, watchAccountCasino1day } from './mongoDBOperation.js'
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
    getAccountArea3mins: async ()=> {
      return await getAccountArea3mins();
    },
    getAccountCasinoArea1day: async ()=> {
      return await getAccountCasinoArea1day();
    },
    getAccountCasinoArea3mins: async () => {
      return await getAccountCasinoArea3mins(); 
    },
    getAccountCasino1day: async () => {
      return await getAccountCasino1day(); 
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
    }
  }
};