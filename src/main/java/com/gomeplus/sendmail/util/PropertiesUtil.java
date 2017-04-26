package com.gomeplus.sendmail.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	private static Properties prop = new Properties();
	static {
		try {
			InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream("app.properties");
			prop.load(fis);
		} catch (IOException e) {
		}
	}
	
	
	public static String getProperTies(String key) {
		return prop.getProperty(key);
	}
}
