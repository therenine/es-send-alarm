{
  "query": {
    "filtered": {
      "query": {
        "query_string": {
          "query": "\"/v3/combo/groupHomepage_1.1\"",
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