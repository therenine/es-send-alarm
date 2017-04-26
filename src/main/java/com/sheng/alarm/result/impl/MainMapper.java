package com.sheng.alarm.result.impl;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation.SingleValue;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;

import com.sheng.alarm.result.IMapper;

public class MainMapper implements IMapper {

	@Override
	public Map<String, Object> mapper(SearchResponse response) {
		
		Long totalHits = response.getHits().getTotalHits();
		Map<String, Object> result = new HashMap<>();
		result.put("total", totalHits);
		Aggregations aggregations = response.getAggregations();
		
		
		SingleValue avgTotalValue = (SingleValue) aggregations.get("1");
		SingleValue maxTotalValue = (SingleValue) aggregations.get("3");
		Percentiles percentiles = (Percentiles) aggregations.get("4");
		BigDecimal ab = new BigDecimal((avgTotalValue != null && !"NaN".equals(avgTotalValue.value())) ? avgTotalValue.value() : 0);
		BigDecimal mb = new BigDecimal((maxTotalValue != null && !"NaN".equals(maxTotalValue.value()) ) ? maxTotalValue.value() : 0);
		BigDecimal _95b = new BigDecimal((percentiles != null && !"NaN".equals(percentiles.percentile(95.0))) ? percentiles.percentile(95.0) : 0);
		result.put("avgTotal", ab.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
		result.put("maxTotal", mb.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
		result.put("_95percent", _95b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
		return result;
	}

}
