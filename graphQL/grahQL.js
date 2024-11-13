export const typeDefs = `#graphql
  # Comments in GraphQL strings (such as this one) start with the hash (#) symbol.
  scalar Decimal


  type accountArea {
    _id : String
    type: String
    acct: String
    areaCode: String
    sumBet: Float
    sumCasinoWin: Float
    sumTheorWin: Float
    noOfTxn: Int
  }
  type accountCasinoArea {
    _id : String
    type: String
    acct: String
    casinoCode: String
    areaCode: String
    sumBet: Float
    sumCasinoWin: Float
    sumTheorWin: Float
    noOfTxn: Int
  }
  type accountCasino {
    _id : String
    type: String
    acct: String
    casinoCode: String
    sumBet: Float
    sumCasinoWin: Float
    sumTheorWin: Float
    noOfTxn: Int
  }

  # The "Query" type is special: it lists all of the available queries that
  # clients can execute, along with the return type for each. In this
  # case, the "books" query returns an array of zero or more Books (defined above).
  type Query {
    getAccountArea: [accountArea]
    getAccountCasinoArea: [accountCasinoArea]
    getAccountCasino: [accountCasino]
  }

  type Subscription {
    watchAccountArea: accountArea
    watchAccountCasinoArea: accountCasinoArea
    watchAccountCasino: accountCasino
  }
`;