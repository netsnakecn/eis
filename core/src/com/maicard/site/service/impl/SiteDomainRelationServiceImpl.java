package com.maicard.site.service.impl;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.entity.SiteDomainRelation;
import com.maicard.core.service.ConfigService;
import com.maicard.site.dao.mapper.SiteDomainRelationMapper;
import com.maicard.site.service.SiteDomainRelationService;
import com.maicard.utils.HttpUtils;

@Service
public class SiteDomainRelationServiceImpl extends AbsGlobalSyncService<SiteDomainRelation,SiteDomainRelationMapper> implements SiteDomainRelationService {
 

	@Resource
	private ConfigService configService;

	//	private final String cacheName = CommonStandard.cacheNameSupport;
	//	private final String cachePrefix = "SiteDomainRelation";

	//private int isInited = 0;

	// 先用List进行排序，再使用LinkedHashSet保证顺序不变, 因此必须使用LinkedHashSet。NetSnake,2016-6-29.
	// 使用静态Set取代cache来缓存数据，因为cache中的数据经常丢失，NetSnake,2016-06-24.
	private static Set<SiteDomainRelation> cache = new LinkedHashSet<SiteDomainRelation>();
	
	@PostConstruct
	public void init(){
		initCache();
	}

	private void initCache(){
		//isInited++;
		//初始化siteDomainRelation数据
		CriteriaMap siteDomainRelationCriteria = new CriteriaMap();
		//Map<String,Object> siteDomainRelationCriteria = new LinkedHashMap<>();
		siteDomainRelationCriteria.put("currentStatus", new int[] {BasicStatus.normal.id,BasicStatus.defaulted.id});
		//siteDomainRelationCriteria.put("siteCode","default.com");
		List<SiteDomainRelation> siteDomainRelationList = mapper.list(siteDomainRelationCriteria);
		int addCount = 0;
		//int existCount = 0;
		logger.debug("当前数据库中的站点关联数据:" + (siteDomainRelationList == null ? "空" : siteDomainRelationList.size()));
		if(siteDomainRelationList != null && siteDomainRelationList.size() > 0){

			Collections.sort(siteDomainRelationList, new Comparator<SiteDomainRelation>(){
				@Override
				public int compare(SiteDomainRelation o1, SiteDomainRelation o2) {
					if(!o1.getDomain().startsWith("^\\*") && o2.getDomain().startsWith("^\\*")){
						return 1;
					} else {
						if(o1.getDomain().replaceAll("^\\*\\.", "").endsWith(o2.getDomain().replaceAll("^\\*\\.", "")) && o1.getDomain().length() > o2.getDomain().length()){
							return -1;
						} else {
							return 1;
						}
					}
				}

			});
			synchronized(this){
				for(SiteDomainRelation siteDomainRelation : siteDomainRelationList){
					cache.add(siteDomainRelation);
					logger.debug("放入站点数据:" + siteDomainRelation);
					addCount++;
				}
			}
			logger.info("共放入" + addCount + "个站点关联数据");			
		} else {
			logger.warn("未找到任何站点关联数据");
		}

	}

	/*private void init(){
		isInited++;
		//初始化siteDomainRelation数据
		CriteriaMap siteDomainRelationCriteria = new CriteriaMap();
		siteDomainRelationCriteria.setCurrentStatus(BasicStatus.normal.id,BasicStatus.relation.id);
		List<SiteDomainRelation> existWordList = siteDomainRelationDao.list(siteDomainRelationCriteria);
		int addCount = 0;
		int existCount = 0;
		logger.debug("当前数据库中的站点关联数据:" + (existWordList == null ? "空" : existWordList.size()));
		if(existWordList != null && existWordList.size() > 0){
			for(SiteDomainRelation siteDomainRelation : existWordList){
				String key = cachePrefix + "#" + siteDomainRelation.getSiteDomainRelationId();
				//logger.info("键值数据:" + key);
				if(cacheService.get(cacheName, key) == null  	|| !(cacheService.get(cacheName,key) instanceof SiteDomainRelation)){
					logger.debug("将站点关联数据[" + siteDomainRelation + "]放入缓存");
					addCount++;
					cacheService.put(cacheName, key, siteDomainRelation);
				} else {
					logger.debug("站点关联数据[" + siteDomainRelation + "]已存在于缓存中");
					existCount++;
				}

			}
			logger.info("共放入" + addCount + "个站点关联数据，已存在" + existCount + "个站点关联数据");
		} else {
			logger.warn("未找到任何站点关联数据");
		}

	}*/

 
   
   



	@Override
	public boolean isValidOwnerAccess(String hostName, long ownerId) {
		//获取域名
		String domainSuffix = HttpUtils.parseDomain(hostName);
		String siteCode = null;
		//根据主机名确定使用哪个站点
		CriteriaMap siteDomainRelationCriteria = new CriteriaMap();
		siteDomainRelationCriteria.put("domain",hostName);
		siteDomainRelationCriteria.put("currentStatus", new int[] {BasicStatus.normal.id, BasicStatus.defaulted.id});
		List<SiteDomainRelation> siteDomainRelationList = list(siteDomainRelationCriteria);
		if(siteDomainRelationList != null && siteDomainRelationList.size() > 0){
			siteCode = siteDomainRelationList.get(0).getSiteCode(); 
			if(siteDomainRelationList.get(0).getOwnerId() != ownerId){
				logger.debug("根据主机名[" + hostName + "]找到的站点是:" + siteCode + "其平台ID[" + siteDomainRelationList.get(0).getOwnerId() + "]与[" + ownerId + "]不一样");		
				return false;
			} else {
				logger.debug("根据主机名[" + hostName + "]找到的站点是:" + siteCode + "其平台ID[" + siteDomainRelationList.get(0).getOwnerId() + "]与[" + ownerId + "]一样");		
				return true;
			}
		}
		if(StringUtils.isNotBlank(domainSuffix)){
			//根据域名确定使用哪个站点
			siteDomainRelationCriteria.clear();
			siteDomainRelationCriteria.put("domain",domainSuffix);
			siteDomainRelationCriteria.put("currentStatus", new int[] {BasicStatus.normal.id, BasicStatus.defaulted.id});
			
			
			siteDomainRelationList = list(siteDomainRelationCriteria);
			if(siteDomainRelationList != null && siteDomainRelationList.size() > 0){
				siteCode = siteDomainRelationList.get(0).getSiteCode(); 
				if(siteDomainRelationList.get(0).getOwnerId() != ownerId){
					logger.debug("根据域名[" + hostName + "]找到的站点是:" + siteCode + "其平台ID[" + siteDomainRelationList.get(0).getOwnerId() + "]与[" + ownerId + "]不一样");		
					return false;
				} else {
					logger.debug("根据域名[" + hostName + "]找到的站点是:" + siteCode + "其平台ID[" + siteDomainRelationList.get(0).getOwnerId() + "]与[" + ownerId + "]一样");		
					return true;
				}
			}
		}
		return false;

	}

	@Override
	public SiteDomainRelation getByHostName(String hostName) {

		hostName = hostName.toLowerCase();

		SiteDomainRelation defaultSiteDomainRelation = null;

		if(cache == null || cache.size() < 1){
			initCache();
		}
		if(cache != null && cache.size() > 0){
			//循环
			for(SiteDomainRelation siteDomainRelation : cache){

				if(siteDomainRelation.getCurrentStatus() == BasicStatus.defaulted.id){
					defaultSiteDomainRelation = siteDomainRelation;
				}
				if(isValidRelation(siteDomainRelation, hostName)){
					return siteDomainRelation;
				}			
			}
			logger.warn("在缓存中没有找到任何与[" + hostName + "]匹配的站点数据");

		} else {
			logger.info("缓存中没有任何数据");
		}

		if(defaultSiteDomainRelation != null){
			logger.info("主机名[" + hostName + "]没有任何直接匹配的数据，返回默认站点数据:" + defaultSiteDomainRelation);
			return defaultSiteDomainRelation;
		}
		logger.info("主机名[" + hostName + "]没有任何直接匹配的数据，系统也没有默认站点数据");



		logger.debug("根据主机名[" + hostName + "]找不到任何站点");	

		return null;
	}

	private boolean isValidRelation(SiteDomainRelation siteDomainRelation, String hostName){
		String domainSuffix = HttpUtils.parseDomain(hostName);
		logger.debug("检查访问主机名[" + hostName + "]与站点数据[" + siteDomainRelation + "]的关联关系.");
		if(siteDomainRelation.getDomain().equalsIgnoreCase(hostName)){
			logger.debug("主机名[" + hostName  + "]与站点数据匹配:" + siteDomainRelation);
			return true;
		}
		if(siteDomainRelation.getDomain().startsWith("*")){
			String host = hostName.replaceAll("^\\w+\\.","*.");
			logger.debug("XXXX Start with *, host=" + host);

			if(siteDomainRelation.getDomain().equalsIgnoreCase("*." + hostName)){
				logger.debug("主机名[" + hostName + "]与泛站点数据匹配:" + siteDomainRelation);
				return true;
			} else if(siteDomainRelation.getDomain().equalsIgnoreCase("*." + domainSuffix)){
				logger.debug("域名[" + domainSuffix + "]与泛站点数据匹配:" + siteDomainRelation);
				return true;
			} else if(siteDomainRelation.getDomain().equalsIgnoreCase(hostName.replaceAll("^\\w+\\.","*."))){	 
				logger.debug("主机名[" + hostName + "]处理后:" + host + ",与泛站点数据匹配:" + siteDomainRelation);
				return true;
			}
		}
		return false;
	}
	

}
