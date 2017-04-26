package com.sheng.alarm.query;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.sheng.alarm.result.IMapper;
import com.sheng.alarm.result.impl.ErrorMapper;
import com.sheng.alarm.result.impl.MainMapper;
import com.sheng.alarm.util.PropertiesUtil;

public class ExcuteQuery {

	private static String[] TYPES = new String[] { "main", "error" };
	
	private static String TIME_INTERVAL = "TIME_INTERVAL";
	
	private static String INDEX_TEMPLETE = "nginx-access-%s";

	private static String FILESUFFIX = "es";

	public File[] getTempleFilePath() {
		String dirPath = Thread.currentThread().getContextClassLoader().getResource("").getFile();
		File file = new File(dirPath);
		Collection<File> fileCollection = FileUtils.listFiles(file, new String[]{FILESUFFIX}, true);
		if(fileCollection == null || fileCollection.isEmpty()) return null;
		return fileCollection.toArray(new File[fileCollection.size()]);
	}

	public void excute() throws IOException {/*
		Map<String, Map<String, Object>> result = new HashMap<>();
		File[] templeteFileArray = getTempleFilePath();
		for (File templeteFile : templeteFileArray) {
			String templeteFileName = templeteFile.getName();
			for (String type : TYPES) {
				if (templeteFileName.indexOf(type) == -1)
					continue;

				String content = FileUtils.readFileToString(templeteFile, Charset.forName("utf8"));
				IExcute excute = suffixMap.get(type);
				Map<String, Object> tempResultMap = excute.excute(replaceIndex(), content);
				String prefixFileName = templeteFileName.substring(0, templeteFileName.lastIndexOf("_"));
				if (result.containsKey(prefixFileName)) {
					result.get(prefixFileName).putAll(tempResultMap);
				} else {
					result.put(prefixFileName, tempResultMap);
				}
			}
		}

		System.out.println(result);
	*/}

	private static String replaceIndex() {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
		return String.format(INDEX_TEMPLETE, formatter.format(date));
	}
	
	
	
	
	private static String replaceTime(String content) {
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.HOUR, 0 - Integer.parseInt(PropertiesUtil.getProperTies(TIME_INTERVAL)));
		return String.format(content, calendar.getTime().getTime(), date.getTime());
	}

	public static Map<String, IMapper> suffixMap = new HashMap<>();
	static {
		suffixMap.put("main", new MainMapper());
		suffixMap.put("error", new ErrorMapper());
	}



	public static void main(String[] args) throws IOException {
		new ExcuteQuery().excute();
	}
}
