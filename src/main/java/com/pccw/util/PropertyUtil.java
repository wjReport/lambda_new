package com.pccw.util;

import java.io.IOException;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.log4j.Logger;

public class PropertyUtil {
	
	private static final Logger log = Logger.getLogger(PropertyUtil.class);
	
	static Properties properties = null;

	public static String getPropertyByKey(String key, String defaultValue) {
		if(properties == null){
			try {
				properties = Resources.getResourceAsProperties("db.properties");
			} catch (IOException e) {
				log.error("解析db.properties出错");
			}
		}
		return properties.getProperty(key, defaultValue);
	} 
	
	public static String getByKey(String key){
		return getPropertyByKey(key,"");
	}
	
	
}
