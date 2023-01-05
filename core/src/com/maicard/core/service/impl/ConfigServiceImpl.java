package com.maicard.core.service.impl;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.constants.DataName;
import com.maicard.core.container.CoreContainer;
import com.maicard.core.dao.mapper.ConfigMapper;
import com.maicard.core.entity.Config;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.misc.EncryptPropertyPlaceholderConfigurer;
import com.maicard.utils.CacheUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;


@Service
public class ConfigServiceImpl extends AbsGlobalSyncService<Config, ConfigMapper> implements ConfigService {

	
	@Resource
	private CenterDataService centerDataService;

	@Resource
	private EncryptPropertyPlaceholderConfigurer encryptPropertyPlaceholderConfigurer;


	@Value(value="${systemServerId}")
	private int serverId;

	@Value("${systemCode}")
	private String systemCode;



	static Map<String,Config> localCache = new HashMap<String,Config>();



	
	
	static final String keyPrefix = "CACHE_INIT_TIME_";
	
	@PostConstruct
	public void init() {

		//读取系统的所有properties


		String key = keyPrefix + systemCode;
		//logger.info("将表名:{}注册为缓存:{}的一员", key, CacheNames.cacheNameSupport);
		logger.info("Init system config, mq enabled:" + getBoolProperty(DataName.MQ_ENABLED.name()));
		CacheUtils.registerRelationCache(CacheNames.cacheNameSupport, key);
	}
	
	

	@Override
	public String getProperty(String key){
		return encryptPropertyPlaceholderConfigurer.getProperty(key);
	}

	@Override
	public boolean getBoolProperty(String key){
		String v = getProperty(key);
		if(v == null || (!v.equalsIgnoreCase("true") && !v.equalsIgnoreCase("1"))){
			return false;
		}
		return true;

	}
	private void checkCache(){
		if(CoreContainer.lastReloadTime == 0 || CoreContainer.lastReloadTime < new Date().getTime() - (600 * 1000)) {
			initCache();
		} 

	}


	private void initCache() {

		localCache = new HashMap<>();
		CriteriaMap params = CriteriaMap.create();
		List<Config> existConfigList = list(params);
		logger.info("初始化所有系统配置，当前数据库中的系统配置数据:" + (existConfigList == null ? "空" : existConfigList.size()));
		//logger.info("初始化配置:" + JsonUtils.toStringFull(existConfigList));
		if(existConfigList != null && existConfigList.size() > 0){
			for(Config config : existConfigList){
				if(StringUtils.isNotBlank(config.getConfigName()) && StringUtils.isNotBlank(config.getConfigValue())) {
					localCache.put(config.getConfigName(), config);
				}
			}
		} else {
			logger.error("未找到任何系统配置数据");
		}
		CoreContainer.lastReloadTime = new Date().getTime();
	}





	public Config get(String configName, long ownerId){	
		//long ts = System.currentTimeMillis();
		checkCache();


		//循环
		//Config globalOwnerConfig = null;	//ownerId=0的配置
		Config globalOwnerAndServerConfig = null;	//ownerId和serverId都=0配置
		Config globalServerConfig = null;	//serverId=0的配置
		//Config fixConfig = null;	//ownerId和serverId都匹配的配置

		logger.info("查找配置:" + configName + ",o=" + ownerId + ",缓存数:" + localCache.size());
		return localCache.get(configName);
		/*for(Config config : localCache){
			if(config == null || config.getConfigName() == null || config.getConfigValue() == null){
				logger.warn("配置为空:" + config);
				continue;
			}

			if(config.getConfigName().equalsIgnoreCase(configName)){
				if(config.getOwnerId() == ownerId && config.getServerId() == serverId){
					logger.debug("在缓存中找到了与serverId[" + serverId + "]匹配而且ownerId[" + ownerId + "]也匹配的配置:" + config + ",返回该配置");
					return config;
				} 			
				if(config.getOwnerId() == 0 && config.getServerId() == 0){
					//找到了全局配置，ownerId和serverId都=0
					globalOwnerAndServerConfig = config;
				} else if(config.getServerId() == 0 && config.getOwnerId() == ownerId){
					globalServerConfig = config;
				} *//*else if(config.getOwnerId() == 0){
					globalOwnerAndServerConfig = config;
				}*//*
			}


		}
		//logger.debug("读取缓存:{}耗时:{}ms", configName, (System.currentTimeMillis() - ts));
		if(globalServerConfig != null){
			logger.debug("在缓存中没找到与serverId[" + serverId + "]匹配而且ownerId[" + ownerId + "]也匹配的配置，返回[serverId=0而且ownerId=" + ownerId + "]的配置" + globalServerConfig);
			return globalServerConfig;
		} else if(globalOwnerAndServerConfig != null){
			logger.debug("在缓存中没找到与serverId[" + serverId + "]匹配而且ownerId[" + ownerId + "]也匹配的配置，也没有ownerId=" + ownerId + "或者serverId=" + serverId + "]的配置，返回全局配置:" + globalOwnerAndServerConfig);
			return globalOwnerAndServerConfig;
		} 
		logger.debug("在缓存中没找到名字是:" + configName + "，与serverId[" + serverId + "]匹配或者ownerId[" + ownerId + "]匹配的任何配置或ownerId、serverId为0的准全局或全局配置，放弃在缓存中的查找。");
		//return null;


		CriteriaMap params = CriteriaMap.create();
		params.put("configName",configName);
		params.put("serverId",serverId);
		params.put("ownerId",ownerId);
		Config config = mapper.selectByName(params);
		if(config != null){
			localCache.add(config);
		}
		return config;*/
	}



	@Override
	public boolean getBooleanValue(String configName, long ownerId){
		
		if(configName.equalsIgnoreCase(DataName.MQ_ENABLED.name())) {
			//如果MQ_ENABLED配置了且为false，则返回false
			//否则就是未配置或true，那么返回true
			return getBoolProperty(configName);
		}
		
		Config config = get(configName, ownerId);
		if(config != null){
			try{
				return Boolean.parseBoolean(config.getConfigValue());
			}catch(Exception e){}
		}
		return false;

	}

	@Override
	public int getIntValue(String configName, long ownerId){
		Config config = get(configName, ownerId);
		if(config != null){
			try{
				return Integer.parseInt(config.getConfigValue());
			}catch(Exception e){}
		}
		return 0;

	}

	@Override
	public float getFloatValue(String configName, long ownerId){
		Config config = get(configName, ownerId);
		if(config != null){
			try{
				return Float.parseFloat(config.getConfigValue());
			}catch(Exception e){}
		}
		return 0f;

	}

	@Override
	public int getServerId(){
		return serverId;
	}

	@Override
	public String getSystemCode() {
		return systemCode;
	}

	@Override
	public long getLongValue(String configName, long ownerId) {
		Config config = get(configName, ownerId);
		if(config != null){
			try{
				return Long.parseLong(config.getConfigValue());
			}catch(Exception e){}
		}
		return 0;
	}


	@Override
	public String getValue(String configName, long ownerId) {
		Config config = get(configName, ownerId);
		if(config != null){
			return config.getConfigValue().trim();
		}
		return null;
	}




}
