package com.sheng.alarm.util;


import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

/**
 * json公共类
 * 
 * @Description TODO
 * @author zhangchunsheng
 * @date 2015年12月23日 上午11:32:00
 */
public class JsonUtil {
	private static ObjectMapper mapper = new ObjectMapper();
	
	
	public static String writeValueAsString(Object value) throws Exception {
		return mapper.writeValueAsString(value);
	}
	
	public static <T> T readValue(String json, Class cls) throws Exception {
		return readValue(json, cls, null);
	}
	
	/**
	 * 转换json
	 * 
	 * @author zhangchunsheng
	 * @date 2015年12月23日 上午11:32:13
	 * @param json
	 * @param cls
	 * @return
	 * @throws Exception
	 */
	public static <T> T readValue(String json, Class cls, Class<?>... elementClasses) throws Exception {
		Class []elementCls = null;
		if(Map.class == cls && null == elementClasses) {
			elementCls = new Class[]{Object.class, Object.class};
		}
		elementCls = elementClasses !=null ? elementClasses : new Class[]{};
		JavaType JavaType = getCollectionType(cls, elementCls);
		return (T) mapper.readValue(json, JavaType);
	}

	public static JavaType getCollectionType(Class<?> cls, Class<?>... elementClasses) {
		return mapper.getTypeFactory().constructParametricType(cls, elementClasses);
	}
}
