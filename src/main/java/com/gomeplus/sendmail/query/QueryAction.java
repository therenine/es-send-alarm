package com.gomeplus.sendmail.query;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation.SingleValue;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;

import com.gomeplus.sendmail.result.IMapper;

public class QueryAction {

	public static Client client = null;

	static {
		try {
			client = TransportClient.builder().build()
			/*
			 * .addTransportAddress(new
			 * InetSocketTransportAddress(InetAddress.getByName
			 * ("10.125.145.101"), 9301)) .addTransportAddress(new
			 * InetSocketTransportAddress
			 * (InetAddress.getByName("10.125.145.102"), 9301))
			 * .addTransportAddress(new
			 * InetSocketTransportAddress(InetAddress.getByName
			 * ("10.125.145.103"), 9301))
			 */
			.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.125.145.104"), 9301)).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.125.145.105"), 9301));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	public static Map<String, Object> get(String index, String source, IMapper mapper) {
		SearchResponse response = client.prepareSearch(index).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setSource(source).execute().actionGet();
		return mapper.mapper(response);
	}

	static class ShutDownWork extends Thread {
		@Override
		public void run() {
			if(client != null) client.close();
		}
	}

	static {
		Runtime.getRuntime().addShutdownHook(new ShutDownWork());
	}
}
