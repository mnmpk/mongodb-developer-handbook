import { PubSub } from 'graphql-subscriptions';
import { getAccountArea, getAccountCasinoArea, getAccountCasino, watchAccountArea, watchAccountCasinoArea, watchAccountCasino } from './mongoDBOperation.js'
import GraphQLDecimal from 'graphql-type-decimal';

const pubsub = new PubSub();

// Resolvers define how to fetch the types defined in your schema.
// This resolver retrieves books from the "books" array above.
export const resolvers = {
  Decimal: GraphQLDecimal,
  Query: {
    getAccountArea: async ()=> {
      return await getAccountArea();
    },
    getAccountCasinoArea: async ()=> {
      return await getAccountCasinoArea();
    },
    getAccountCasino: async () => {
      return await getAccountCasino(); 
    }
  },
  Subscription: {
    watchAccountArea: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountArea", 
            // theNext
            {watchAccountArea: theNext}
          );
        }
        watchAccountArea(_handler);
        return pubsub.asyncIterator(["watchAccountArea"]);
      }
    },
    watchAccountCasinoArea: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountCasinoArea", 
            {watchAccountCasinoArea: theNext}
          );
        }
        watchAccountCasinoArea(_handler);
        return pubsub.asyncIterator(["watchAccountCasinoArea"]);
      }
    },
    watchAccountCasino: {
      subscribe: ()=> {
        const _handler = (theNext)=>{
          
          pubsub.publish("watchAccountCasino", 
            {watchAccountCasino : theNext}
          );
        }
        watchAccountCasino(_handler);
        return pubsub.asyncIterator(["watchAccountCasino"]);
      }
    }
  }
};