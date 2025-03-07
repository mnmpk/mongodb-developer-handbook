[
  {
    "$match": {
      "FullPortList": {
        "$in": [
        <#list portCodes as portCode>
        "${portCode}"<#sep>,
        </#list>
        ]
      }
    }
  },
  {
    "$lookup": {
      "from": "tsp_port_info",
      "let": {
        "portCode": "$FullPortList"
      },
      "pipeline": [
        {
          "$match": {
            "$expr": {
              "$eq": [
                "$port_code",
                "$$portCode"
              ]
            }
          }
        }
      ],
      "as": "portInfo"
    }
  },
  {
    "$lookup": {
      "from": "tsp_port_info",
      "let": {
        "portCode": "$FullPortList",
        "portInfo": "$portInfo"
      },
      "pipeline": [
        {
          "$unwind": "$airports"
        },
        {
          "$match": {
            "$expr": {
              "$and": [
                {
                  "$eq": [
                    "$airports.iata_airport_code",
                    {
                      "$cond": [
                        {
                          "$eq": [
                            "$$portCode",
                            "SHA+"
                          ]
                        },
                        "SHA",
                        "$$portCode"
                      ]
                    }
                  ]
                },
                {
                  "$eq": [
                    "$$portInfo",
                    []
                  ]
                }
              ]
            }
          }
        },
        {
          "$replaceRoot": {
            "newRoot": {
              "$mergeObjects": [
                "$airports",
                "$$ROOT"
              ]
            }
          }
        },
        {
          "$unset": [
            "_id",
            "airports"
          ]
        }
      ],
      "as": "airportInfo"
    }
  },
  {
    "$unwind": {
      "path": "$portInfo",
      "preserveNullAndEmptyArrays": true
    }
  },
  {
    "$unwind": {
      "path": "$airportInfo",
      "preserveNullAndEmptyArrays": true
    }
  },
  {
    "$project": {
      "doc": {
        "$mergeObjects": [
          "$portInfo",
          "$airportInfo",
          {
            "_id": "$FullPortList"
          }
        ]
      }
    }
  },
  {
    "$unset": "doc.airports"
  },
  {
    "$group": {
      "_id": "$doc._id",
      "doc": {
        "$mergeObjects": "$doc"
      }
    }
  },
  {
    "$replaceRoot": {
      "newRoot": "$doc"
    }
  },
  {
    "$merge": {
      "into": "tsp_flatten_ports",
      "on": "_id",
      "whenMatched": "replace",
      "whenNotMatched": "insert"
    }
  }
]