{
  "query": {
    "filtered": {
      "query": {
        "query_string": {
          "query": "\"/v3/ext/rebate/rebateItems\"",
          "analyze_wildcard": true
        }
      },
      "filter": {
        "bool": {
          "must": [
            {
              "range": {
                "@timestamp": {
                  "gte": %d,
                  "lte": %d,
                  "format": "epoch_millis"
                }
              }
            }
          ],
          "must_not": []
        }
      }
    }
  },
  "size": 0,
  "aggs": {
    "avgTotal": {
      "avg": {
        "field": "responsetime"
      }
    },
    "maxTotal": {
      "max": {
        "field": "responsetime"
      }
    },
    "percentTotal": {
      "percentiles": {
        "field": "responsetime",
        "percents": [
          95,
          99
        ]
      }
    }
  }
}