package com.gomeplus.sendmail.query;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.gomeplus.sendmail.result.impl.ErrorMapper;
import com.gomeplus.sendmail.result.impl.MainMapper;
import com.gomeplus.sendmail.util.PropertiesUtil;

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

	public void excute() throws IOException {
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
	}

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

	public static Map<String, IExcute> suffixMap = new HashMap<>();
	static {
		suffixMap.put("main", new MainExcute());
		suffixMap.put("error", new ErrorExcute());
	}

	public interface IExcute {
		public Map<String, Object> excute(String index, String content);
	}

	private static class MainExcute implements IExcute {

		@Override
		public Map<String, Object> excute(String index, String content) {
			content = replaceTime(content);
			Map<String, Object> result = QueryAction.get(index, content, new MainMapper());
			return result;
		}
	}

	private static class ErrorExcute implements IExcute {
		@Override
		public Map<String, Object> excute(String index, String content) {
			content = replaceTime(content);
			Map<String, Object> result = QueryAction.get(index, content, new ErrorMapper());
			return result;
		}
	}

	public static void main(String[] args) throws IOException {
		new ExcuteQuery().excute();
	}
}
