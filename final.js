[
    {
      $match: {
        type: "acct-casinoCode-areaCode-locnCode",
        bucketSize: "1day"
      }
    },
    {
      $group: {
        _id: "$locnCode",
        headCount: {
          $sum: 1
        },
        areaCode: {
          $first: "$areaCode"
        },
        casinoCode: {
          $first: "$casinoCode"
        }
      }
    },
    {
      $project: {
        locnCode: "$_id",
        headCount: "$headCount",
        areaCode: "$areaCode",
        casinoCode: "$casinoCode"
      }
    },
    {
      $merge: {
        into: "tRatingFinal",
        on: "locnCode",
        whenMatched: "replace",
        whenNotMatched: "insert"
      }
    }
  ]