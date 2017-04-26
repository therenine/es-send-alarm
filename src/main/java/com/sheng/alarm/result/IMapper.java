package com.sheng.alarm.result;


import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;

public interface IMapper {

	public Map<String, Object> mapper(SearchResponse response);
}
