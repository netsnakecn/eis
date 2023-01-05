package com.maicard.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CacheUtils {

	/**
	 * Spring缓存在redis中的名字
	 */
	public static final String SPRING_CACHE_PREFIX = "SPRING_CACHE_";
	
	public static Map<String,Set<String>> relationCacheMap = new HashMap<String,Set<String>>();
	
	public static String getCacheTableName(String systemCode, String cacheName) {
		return SPRING_CACHE_PREFIX + systemCode + "_" + cacheName;
	}

	/**
	 * 把一个指定的表名字注册为指定的缓存中的一员<br>
	 * 当清除某个缓存时，也将清除该表<br>
	 * 
	 * 
	 * 
	 * 
	 * @author GHOST
	 * @date 2020-02-21
	 */
	public static void registerRelationCache(String cacheName, String relationTableName) {
		
		cacheName = cacheName.trim();
		relationTableName = relationTableName.trim();
		
		if(relationCacheMap == null) {
			initRelationCacheMap();
		}
		if(!relationCacheMap.containsKey(cacheName)) {
			 relationCacheMap.put(cacheName, new HashSet<String>());
		}
		relationCacheMap.get(cacheName).add(relationTableName);
		
	}
	
	private static void initRelationCacheMap() {
		 relationCacheMap = new HashMap<String,Set<String>>();		
	}

	public static Set<String> getRelationCacheTables(String cacheName){
		cacheName = cacheName.trim();
		if(relationCacheMap == null || !relationCacheMap.containsKey(cacheName)) {
			return Collections.emptySet();
		}
		Set<String> set =  relationCacheMap.get(cacheName);
		if(set == null) {
			return Collections.emptySet();
		} else {
			return set;
		}
		
		
	}

}
