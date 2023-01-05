package com.maicard.core.service.impl;

import com.maicard.base.CriteriaMap;
import com.maicard.base.ImplNameTranslate;
import com.maicard.core.constants.Constants;
import com.maicard.core.dao.mapper.CacheMapper;
import com.maicard.core.entity.EisCache;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.CacheService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.mb.annotation.IgnoreJmsDataSync;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * 注意：使用BothSync将导致消息不断重复发送
 * 使用SpringCacheService后
 *
 * @author robin
 */
public class CacheServiceImpl extends AbsGlobalSyncService<EisCache, CacheMapper> implements CacheService {

    @Resource
    private CacheManager cacheManager;

    @Resource
    private ApplicationContextService applicationContextService;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public void put(String cacheName, String key, Object value) {
        Assert.notNull(cacheName, "尝试插入的缓存系统，名称不能为空");
        Assert.notNull(key, "尝试插入的缓存键不能为空");
        cacheManager.getCache(cacheName).put(key, value);

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String cacheName, String key) {
        try {
            return (T) cacheManager.getCache(cacheName).get(key).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public <T> T get(String cacheNameAndKey) {
        int firstHash = cacheNameAndKey.indexOf("#");
        String cacheName = cacheNameAndKey.substring(0, firstHash);
        String key = cacheNameAndKey.substring(firstHash + 1);

        return get(cacheName, key);
    }

    @Override
    public List<String> listKeys(String cacheName, String pattern) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.error("系统没有配置缓存:" + cacheName);
            return Collections.emptyList();
        }
        Object nativeCache = cache.getNativeCache();
        if (nativeCache == null) {
            logger.error("缓存{}没有原生缓存", cacheName);
            return Collections.emptyList();
        }

        List<String> keys = null;
        List<String> returnKeys = new ArrayList<String>();
        if (nativeCache instanceof RedisCacheManager) {
            RedisCacheManager redisCache = (RedisCacheManager) nativeCache;
        } else {
            logger.error("不支持的缓存类型:{}", nativeCache);
        }
        if (keys == null || keys.size() < 1) {
            logger.debug("原生缓存:{}中的key总数是0", cacheName);
            return Collections.emptyList();
        }
        if (pattern == null) {
            return keys;
        }
        for (String key : keys) {
            if (key.matches(pattern)) {
                returnKeys.add(key);
            }
        }
        logger.debug("原生缓存:{}中的key总数是:{},过滤后的数量是:{}", cacheName, keys.size(), returnKeys.size());
        return returnKeys;
		/*@SuppressWarnings("unchecked")
		List<Object> keys = cacheManager.getCache(cacheName).();
		if(keys == null || keys.size() < 1){
			return null;
		}
		List<String> returnKeys = new ArrayList<String>();
		for(Object key : keys){			
			if(pattern == null || key.toString().matches(pattern)){
				returnKeys.add(key.toString());
			}
		}
		return returnKeys;*/
    }

    @Override
    @IgnoreJmsDataSync
    //@ExecOnBothNode
    public int delete(CriteriaMap cacheCriteria) {

        String cacheName = cacheCriteria.getStringValue("cacheName");
        String key = cacheCriteria.getStringValue("key");
        if (cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).evict(key);
        } else {
            logger.error("找不到指定的缓存:" + cacheName + ",无法执行删除操作KEY=" + key);
        }
        return 1;
    }

    @Override
    @IgnoreJmsDataSync
    //@ExecOnBothNode
    public int deleteWithCacheNameAndKey(String cacheNameAndKey) {
        int firstHash = cacheNameAndKey.indexOf("#");
        String cacheName = cacheNameAndKey.substring(0, firstHash);
        String key = cacheNameAndKey.substring(firstHash + 1);

        CriteriaMap params = CriteriaMap.create();
        params.put("cacheName", cacheName);
        params.put("key", key);
        delete(params);
        return 1;
    }


    /**
     * 清除本机所有缓存
     * 用于某些情况下，缓存未能同步
     */
    @Override
    @IgnoreJmsDataSync
    //@ExecOnBothNode
    public int evict(String cacheName) {
        if (cacheName == null) {
            return 1;
        }
		/*if(cacheName.equals("*")){
			logger.debug("当前请求清除所有缓存数据");
			String[] allCacheNames = cacheManager.getCacheNames();
			for(String cn : allCacheNames){
				logger.debug("清空缓存[" + cn + "]");
				cacheManager.getCacheManager().getCache(cn).removeAll();
			}
			return 1;
		}*/

        logger.debug("清空缓存[" + cacheName + "]");

        try {
            // FIXME 可能是配置问题，执行clear会报转换错误
            Optional.of(cacheManager.getCache(cacheName)).get().clear();
            logger.debug("执行正常clear完成，返回");
            return 1;
        }catch (Exception e){
            logger.warn("clear出错:",e);
        }
        if (!(cacheManager instanceof RedisCacheManager)) {
            logger.error("不支持的缓存:" + cacheManager);
            return 0;
        }
        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            final String pattern = cacheName + "*";
            long rs = connection.del(pattern.getBytes(StandardCharsets.UTF_8));
            logger.debug("清空缓存[" + pattern + "]结果:" + rs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //  logger.info("释放连接:" + connection);
            connection.close();
        }

        return 1;

    }

    public List<EisCache> list(CriteriaMap paramMap) {
        if (!(cacheManager instanceof RedisCacheManager)) {
            logger.error("不支持的缓存:" + cacheManager);
            return Collections.emptyList();
        }
        Map<String, Integer> statMap = new HashedMap();

        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            final String pattern = Constants.CACHE_NAME + "*";
            Set<byte[]> keys = connection.keys(pattern.getBytes(Constants.DEFAULT_CHARSET));
            for (byte[] k : keys) {
                String kStr = new String(k).split("::")[0];

                if (!statMap.containsKey(kStr)) {
                    statMap.put(kStr, 1);
                } else {
                    statMap.put(kStr, statMap.get(kStr) + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //  logger.info("释放连接:" + connection);
            connection.close();
        }
        List<EisCache> list = new ArrayList<>();
        if (statMap.size() > 0) {
            for (Map.Entry<String, Integer> entry : statMap.entrySet()) {
                EisCache cache = new EisCache();
                cache.setName(entry.getKey());
                cache.setCacheCount(String.valueOf(entry.getValue()));
                list.add(cache);
            }

        }
        return list;
    }

    @Override
    public int count(String cacheName) {
		/*Cache cache = cacheManager.getCache(cacheName);
		if(cache != null && cache.getKeysNoDuplicateCheck() != null){
			return cache.getKeysNoDuplicateCheck().size();
		}*/
        return 0;
    }

    @Override
    public String[] getCacheNames() {
        Set<String> cacheNames = new HashSet<String>();

        if (cacheManager instanceof RedisCacheManager) {
            JedisConnectionFactory connection = applicationContextService.getBeanGeneric("jedisConnectionFactory");
            try {
                final String pattern = Constants.CACHE_NAME + "*";
                Set<byte[]> keys = connection.getConnection().keys(pattern.getBytes(Constants.DEFAULT_CHARSET));
                for (byte[] k : keys) {
                    String kStr = new String(k).split("::")[0];
                    if (!cacheNames.contains(kStr)) {
                        cacheNames.add(kStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        } else {
            cacheNames = (Set<String>) cacheManager.getCacheNames();
        }
        if (cacheNames == null || cacheNames.size() < 1) {
            return new String[]{};
        }
        String[] names = new String[cacheNames.size()];
        int i = 0;
        for (String name : cacheNames) {
            names[i] = name;
            i++;
        }
        logger.info("从缓存:" + cacheManager.getClass().getName() + "中获取名字:" + Arrays.toString(names));

        return names;

    }


    @Override
    public Object getNativeCache(String cacheName) {
        return cacheManager.getCache(cacheName).getNativeCache();

    }

    @Override
    public int deleteSync(EisCache entity) {
        if (!handleSync() && isMqEnabled()) {
            return 0;
        }
        entity.setSyncFlag(0);
        int rs = evict(entity.getName());
        if (isMqEnabled()) {
            messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "deleteLocal", entity);
        }
        return rs;
    }

    @Override
    public int deleteAsync(EisCache entity) {
        Assert.isTrue(entity.getId() > 0, "Remove object id is zero");
        if (!isMqEnabled()) {
            return evict(entity.getName());
        }

        entity.setSyncFlag(0);
        messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()), "deleteSync", entity);
        return 1;
    }

    @Override
    public int deleteLocal(EisCache entity) {
        return evict(entity.getName());
    }


}
