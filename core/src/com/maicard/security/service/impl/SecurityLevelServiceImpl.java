package com.maicard.security.service.impl;


import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsBaseService;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.SiteDomainRelation;
import com.maicard.security.constants.SecurityConstants;
import com.maicard.security.dao.mapper.SecurityLevelMapper;
import com.maicard.security.entity.SecurityLevel;
import com.maicard.security.service.SecurityLevelService;


@Service
public class SecurityLevelServiceImpl extends AbsBaseService<SecurityLevel,SecurityLevelMapper> implements SecurityLevelService {

 
	@Resource
	private CacheManager cacheManager;
	
	private final String cacheName = SecurityConstants.cacheName;
	private final String cachePrefix = SecurityConstants.cachePrefix;



	private void initCache(){
		//初始化siteDomainRelation数据
		CriteriaMap params = CriteriaMap.create();
		List<SecurityLevel> existSecurityLevelList = list(params);
		int addCount = 0;
		int existCount = 0;
		logger.info("初始化系统安全级别数据，当前数据库中的系统安全级别数据:" + (existSecurityLevelList == null ? "空" : existSecurityLevelList.size()));
		if(existSecurityLevelList != null && existSecurityLevelList.size() > 0){
			for(SecurityLevel config : existSecurityLevelList){
				String key = cachePrefix + "#" + config.getLevel();
				if(cacheManager.getCache(cacheName).get(key) == null  	|| !(cacheManager.getCache(cacheName).get(key).get() instanceof SiteDomainRelation)){
					logger.debug("将系统安全级别数据[" + config + "]放入缓存");
					addCount++;
					cacheManager.getCache(cacheName).put(key, config);
				} else {
					logger.debug("系统安全级别数据[" + config + "]已存在于缓存中");
					existCount++;
				}

			}
			logger.info("共放入" + addCount + "个系统安全级别数据，已存在" + existCount + "个系统安全级别数据");
		} else {
			logger.error("未找到任何系统配置数据");
		}

	}
	
	
	public int  insert(SecurityLevel securityLevel) {
		int rs = 0;
		try{
			rs = mapper.insert(securityLevel);
		}catch(Exception e){
			logger.error("插入数据失败:" + e.getMessage());
		}
		if(rs == 1){
			initCache();
		}
		return rs;
	}



	

	public int delete(int level) {
		int rs = 0;
		try{
			rs =  mapper.delete(level);
		}catch(Exception e){
			logger.error("删除数据失败:" + e.getMessage());
		}
		if(rs == 1){
			initCache();
		}
		return rs;	
	}

	public SecurityLevel select(int level) {
		SecurityLevel securityLevel =  mapper.select(level);
		if(securityLevel == null){
			logger.error("找不到level=" + level + "的安全级别");
			return null;
		}
		
		return securityLevel;

	}

	public List<SecurityLevel> list(CriteriaMap params) {
		List<SecurityLevel> securityLevelList =  mapper.list(params);
		if(securityLevelList == null){
			return null;
		}
		return securityLevelList;
	}
	


	public List<SecurityLevel> listOnPage(CriteriaMap params) {
		List<SecurityLevel> securityLevelList =  mapper.list(params);
		if(securityLevelList == null){
			return null;
		}
		return securityLevelList;	
	}

	
	public int count(CriteriaMap params){
		return mapper.count(params);
	}


}
