package com.sheng.alarm.esclient;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class EsClient {
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
			.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.125.145.104"), 9301))
			.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.125.145.105"), 9301))
			;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

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
