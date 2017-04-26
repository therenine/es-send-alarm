package com.sheng.alarm.query;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesBuilder;

import com.sheng.alarm.esclient.EsClient;
import com.sheng.alarm.result.IMapper;
import com.sheng.alarm.util.JsonUtil;

public class QueryAction {


	public static Map<String, Object> get(String index, String source, IMapper mapper) {
		SearchResponse response = EsClient.client.prepareSearch(index).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setSource(source).execute().actionGet();
		return mapper.mapper(response);
	}


	public static List<Map<String, Object>> getDashboard(String dashboardId) throws Exception {
		
		SearchResponse response = EsClient.client.prepareSearch(".kibana").setQuery(QueryBuilders.idsQuery("dashboard").addIds(dashboardId))
			.setFrom(0).setSize(1).execute().actionGet();
		
		SearchHit searchHit = response.getHits().getAt(0);
		Map<String, Object> resultHit = JsonUtil.readValue(searchHit.getSourceAsString(), Map.class, String.class, Object.class);
		String panelsJSON = (String)resultHit.get("panelsJSON");
		List<Map<String, Object>> panelsList = JsonUtil.readValue(panelsJSON, List.class, Map.class);
		return panelsList;
	}
	
	public static Map<String, Object> getVisualization(String visualizationId) throws Exception {
		SearchResponse response = EsClient.client.prepareSearch(".kibana").setQuery(QueryBuilders.idsQuery("visualization").addIds(visualizationId))
			.setFrom(0).setSize(1).execute().actionGet();
		SearchHit searchHit = response.getHits().getAt(0);
		Map<String, Object> resultHit = JsonUtil.readValue(searchHit.getSourceAsString(), Map.class, String.class, Object.class);
		return resultHit;
	}
	
	
	public static Map<String, Map<String, Object>> get(String dashboardId, Long[] times) throws Exception {
		Map<String, Map<String, Object>> result = new HashMap<>();
		List<Map<String, Object>> dashboardList = getDashboard(dashboardId);
		for(Map<String, Object> dashboard : dashboardList) {
			String id = (String)dashboard.get("id");
			
			Map<String, Object> visualizationMap = QueryAction.getVisualization(id);
			Map<String, Object> conditionMap = ParseEsl.parser(visualizationMap, times);
			String title = (String)conditionMap.get("title");
			SearchRequestBuilder searchRequestBuilder = EsClient.client.prepareSearch((String)conditionMap.get("index")).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery((QueryBuilder)conditionMap.get("queryBuilder")).setSize(0);
			List<AbstractAggregationBuilder> filteredQueryBuilderList = (List<AbstractAggregationBuilder>)conditionMap.get("filteredQueryBuilderList");
			for(AbstractAggregationBuilder aggregationBuilder : filteredQueryBuilderList) {
				searchRequestBuilder.addAggregation(aggregationBuilder);
			}
//			System.out.println(title + " : " + searchRequestBuilder.toString());
			SearchResponse response = searchRequestBuilder.execute().actionGet();
			
			String prefixFileName = (title.indexOf("监控") != -1) ? title.substring(0, title.indexOf("监控")) : title.substring(0, title.indexOf("失败"));
			String type = title.indexOf("监控") != -1 ? "main" : "error";
			Map<String, Object> mapperResult = ExcuteQuery.suffixMap.get(type).mapper(response);
			if (result.containsKey(prefixFileName)) {
				result.get(prefixFileName).putAll(mapperResult);
			} else {
				result.put(prefixFileName, mapperResult);
			}
			
		}
		
		return result;
	}
	
	
	static class ParseEsl{
		
		public static Map<String, Object> parser(Map<String, Object> resultHit, Long [] times) throws Exception {
			String title = (String)resultHit.get("title");
			String visState = (String)resultHit.get("visState");
			Map<String, Object> kibanaSavedObjectMeta = (Map<String, Object>)resultHit.get("kibanaSavedObjectMeta");
			
			String searchSourceJSON = (String)kibanaSavedObjectMeta.get("searchSourceJSON");
			Map<String, Object> searchSourceJSONMap = JsonUtil.readValue(searchSourceJSON, Map.class, String.class, Object.class);
			Map<String, Object> queryStringMap = (Map<String, Object>)searchSourceJSONMap.get("query");
			Map<String, Object> queryMap = (Map<String, Object>)queryStringMap.get("query_string");
			String index = (String)searchSourceJSONMap.get("index");
			
			
			QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery((String)queryMap.get("query")).analyzeWildcard(true);
			
			BoolQueryBuilder filterBoolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("@timestamp").gte(times[0]).lte(times[1]).format("epoch_millis"));
			
			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBoolQueryBuilder);
			
			Map<String, Object> visStateMap = JsonUtil.readValue(visState, Map.class, String.class, Object.class);
			List<Map<String, Object>> aggsList = (List<Map<String, Object>>)visStateMap.get("aggs");
			List<AbstractAggregationBuilder>  aggregationBuilderList = new ArrayList<AbstractAggregationBuilder>();
			for(Map<String, Object> aggsMap : aggsList) {
				
				String type = (String)aggsMap.get("type");
				if(type.equalsIgnoreCase("count")) continue;
				Method method = AggregationBuilders.class.getMethod(type, String.class);
				Object obj = method.invoke(null, aggsMap.get("id"));
				Map<String, Object> paramMap = (Map<String, Object>)aggsMap.get("params");
				Method[] methods = obj.getClass().getMethods();
				for(Method md : methods) {
					if(paramMap.containsKey(md.getName())) {
						md.invoke(obj, paramMap.get(md.getName()));
					}
				}
				if(type.equals("percentiles")) {
					PercentilesBuilder percentilesBuilder = (PercentilesBuilder)obj;
					List<Integer> list = (List<Integer>)paramMap.get("percents");
					double[] percentiles = new double[list.size()];
					for(int i = 0; i < list.size(); i++) {
						percentiles[i] = Double.parseDouble(list.get(i).toString());
					}
					percentilesBuilder.percentiles(percentiles);
				}
				aggregationBuilderList.add((AbstractAggregationBuilder)obj);
			}
			
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("title", title);
			resultMap.put("index", index);
			resultMap.put("queryBuilder", filteredQueryBuilder);
			resultMap.put("filteredQueryBuilderList", aggregationBuilderList);
			return resultMap;
		}
	}
	
	
	
	
	public static void main(String []args) throws Exception {
		System.out.println(QueryAction.get("BS-408关键接口信息统计", new Long[]{1493136000000L, 1493208000000L}));
//		ParseEsl.parser(QueryAction.getVisualization("-slash-v3-slash-user-slash-loginTokenGenerateAction监控【count、avg、max，95线】"), new Long[]{1493172594032L, 1493186994032L});
	}
	
}
