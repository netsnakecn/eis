package com.maicard.core.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.core.entity.EisCache;


//import com.maicard.annotation.IgnoreJmsDataSync;

public interface CacheService extends GlobalSyncService<EisCache> {
	void put(String cacheName, String key, Object value);

	
	List<String> listKeys(String cacheName, String pattern);

	<T>T get(String cacheName, String key);

	<T>T get(String cacheNameAndKey);


	int evict(String cacheName);




	int count(String cacheName);





	int delete(CriteriaMap cacheCriteria);

	int deleteWithCacheNameAndKey(String cacheNameAndKey);


	String[] getCacheNames();


	/**
	 * 返回一个原生cache类
	 * 
	 * 
	 * @author GHOST
	 * @date 2019-01-17
	 */
	Object getNativeCache(String cacheName);







}
