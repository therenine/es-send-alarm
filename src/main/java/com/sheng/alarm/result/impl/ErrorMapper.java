package com.sheng.alarm.result.impl;


import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;

import com.sheng.alarm.result.IMapper;

public class ErrorMapper implements IMapper {

	@Override
	public Map<String, Object> mapper(SearchResponse response) {
		Long totalHits = response.getHits().getTotalHits();
		Map<String, Object> result = new HashMap<>();
		result.put("errorTotal", totalHits);
		return result;
	}

}
