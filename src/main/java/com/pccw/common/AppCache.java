package com.pccw.common;

import java.io.InputStream;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class AppCache {
	
	private static Cache cache;
	
	
	public static synchronized  Cache getCache() {
		if(cache == null) initCache();
		return cache;
	}


	private static void initCache() {
		InputStream in = AppCache.class.getClassLoader().getResourceAsStream("ehcache.xml");
		CacheManager cm = CacheManager.create(in);
		cache = cm.getCache("data-cache");
		
	}
	
}
