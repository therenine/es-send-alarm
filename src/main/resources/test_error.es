{
  "query": {
    "filtered": {
      "query": {
        "query_string": {
          "query": "\"/v3/user/loginTokenGenerateAction\" AND status:\"500\"",
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
  "aggs": {}
}