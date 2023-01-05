package com.maicard.core.service.impl;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.annotation.IgnoreDs;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.dao.mapper.GlobalUniqueMapper;
import com.maicard.core.entity.GlobalUnique;
import com.maicard.core.service.CacheService;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.misc.EncryptPropertyPlaceholderConfigurer;
import com.maicard.utils.NumericUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@IgnoreDs
public class GlobalUniqueServiceImpl extends BaseService implements GlobalUniqueService {


	@Resource
	private GlobalUniqueMapper globalUniqueMapper;
	@Resource
	private CacheService cacheService;

	@Resource
	private CenterDataService centerDataService;

	@Resource
	protected EncryptPropertyPlaceholderConfigurer encryptPropertyPlaceholderConfigurer;
	
	final long MAX_ORDER_RAND_NUMBER = 99999999;
	public static final String CACHE_PREFIX = "GlobalUnique";
	private final String cacheName = CacheNames.cacheNameValidate;



	@Override
	public boolean exist(GlobalUnique globalUnique) {
		logger.debug("检查全局唯一数据[" + globalUnique + "]是否存在");
		if(StringUtils.isBlank(globalUnique.getValue())){
			logger.debug("全局唯一数据中的data为空，无法检查，返回不存在");
			return false;
		}
		int distributedCount = 1;//getDistributedCount();

		String key = CACHE_PREFIX + "#" + globalUnique.getOwnerId() + "#" + globalUnique.getValue();

		if(distributedCount > 0){
			if(centerDataService.get(key) == null){
				logger.debug("在中央缓存中未找到键:" + key + "," + globalUnique + "将视为不存在");
				return false;
			}
			logger.debug("在中央缓存中找到了键:" + key + "," + globalUnique + "，数据已存在");
			return true;

		}
		if(globalUnique == null ||StringUtils.isBlank(globalUnique.getValue())){
			return false;
		}
		GlobalUnique existGlobalUnique = null;

		if(!globalUnique.isNeedSave()){
			return false;
		}
		int rs = globalUniqueMapper.count(CriteriaMap.create().put("value",globalUnique.getValue().trim()));
		if (rs == 0){
			logger.debug("从数据库中未找到全局唯一数据[" + globalUnique.getValue() + "]");
			return false;
		}
		cacheService.put(cacheName, CACHE_PREFIX + "#" + key,	globalUnique);
		return true;
	}

	@Override
	public boolean create(GlobalUnique globalUnique) {
		boolean createSuccess = false;
		String key = CACHE_PREFIX + "#" + globalUnique.getOwnerId() + "#" + globalUnique.getValue();
		if(!centerDataService.setIfNotExist(key, globalUnique.getValue(), -1)){
			logger.debug("在中央缓存中未能成功插入键:" + key + "," + globalUnique + "将视为已存在");
			return false;
		}	
		logger.debug("在中央缓存中成功插入键:" + key + "," + globalUnique);
		//plusDistributedCount(1);
		createSuccess = true;

		if(globalUnique == null || StringUtils.isBlank(globalUnique.getValue())){
			return false;
		}

		if(globalUnique.isNeedSave()){
			try{
				if(globalUniqueMapper.insert(globalUnique) == 1){
					logger.debug("成功创建全局唯一数据[" + globalUnique + "]");
				}
				return true;
			}catch(Exception e){
				logger.error(ExceptionUtils.getFullStackTrace(e));
			}
		} else {
			logger.debug("向全局唯一数据缓存[" + cacheName + "]中放入数据: " + CACHE_PREFIX + "#" + key + ":" + globalUnique);
			cacheService.put(cacheName, CACHE_PREFIX + "#" + key,	globalUnique);
			return true;
		}
		return createSuccess;
	}

	@Override
	public void syncDbToDistributed() throws Exception{
		if(centerDataService == null){
			logger.warn("当前系统未配置中央缓存服务");
			return;
		}
		List<GlobalUnique> list = globalUniqueMapper.list(CriteriaMap.create());
		if(list == null || list.size() < 1){
			logger.debug("当前数据库中没有全局唯一约束数据");
			return;
		}
		Map<String,String> map = new HashMap<String,String>();
		for(GlobalUnique globalUnique : list){
			map.put(CACHE_PREFIX + "#" + globalUnique.getOwnerId() + "#" + globalUnique.getValue(),  globalUnique.getValue());
		}
		centerDataService.setBatch(map, -1, true);
		centerDataService.setForce(CACHE_PREFIX + "#COUNT", String.valueOf(map.size()), -1);
		logger.debug("写入" + map.size() + "条全局唯一约束数据到中央缓存");
	}

	@Override
	public void syncDistributedToDb() {
		if(centerDataService == null){
			logger.warn("当前系统未配置中央缓存服务");
			return;
		}


		String pattern = CACHE_PREFIX + "*";

		Set<String> keys =centerDataService.listKeys(pattern);
		if(keys == null || keys.size() < 1){
			logger.debug("中央缓存中没有任何全局唯一数据项");
			return;
		}
		logger.debug("中央缓存中的全局唯一数据项有" + keys.size() + "条");
		int addCount = 0;
		for(String key : keys){
			String[] data = key.split("#");
			if(data == null || data.length < 3){
				continue;
			}
			long ownerId = Long.parseLong(data[1]);
			String value = data[2];
			GlobalUnique globalUnique = new GlobalUnique(value, ownerId);
			int rs = globalUniqueMapper.insertIgnore(globalUnique);
			if(rs == 1){
				addCount++;
			}
		}

		logger.debug("写入" + addCount + "条全局唯一约束数据到本机");		
	}

	@Override
	public int getDistributedCount(){
		if(centerDataService == null){
			logger.warn("当前系统未配置中央缓存服务");
			return -1;
		}
		String key = CACHE_PREFIX + "#COUNT";
		String value = centerDataService.get(key);
		if(value == null || !NumericUtils.isNumeric(value)){
			return -1;
		}
		return Integer.parseInt(value);
	}

	@Override
	public long plusDistributedCount(int count){
		return 0;
		//String key = CACHE_PREFIX + "#COUNT";

		//return centerDataService.increaseBy(key, count, count, 0);
	}
	
	@Override
	public long incrOrderSequence(int count){
		return incrSequence("orderSequence");
		/*
		String key = CACHE_PREFIX + "#ORDER_SEQUENCE";

		long rs = centerDataService.increaseBy(key, count, count, 0);
		if(rs > MAX_ORDER_RAND_NUMBER) {
			centerDataService.setForce(key, "1", 0);
			return 1;
		}
		return rs;*/
	}

	@Override
	public int count(CriteriaMap params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(GlobalUnique globalUnique) {
		Assert.notNull(globalUnique, "尝试删除的唯一数据不能为空");
		Assert.notNull(globalUnique.getValue(), "尝试删除的唯一数据不能为空");
		Assert.isTrue(globalUnique.getOwnerId() > 0, "尝试删除的唯一数据的ownerId不能为0");

		logger.debug("删除本地全局唯一数据:" + globalUnique);
		globalUniqueMapper.delete(globalUnique);

		if(globalUnique.getSyncFlag() == 0){
			String key = CACHE_PREFIX + "#" + globalUnique.getOwnerId() + "#" + globalUnique.getValue();
			logger.debug("删除中心缓存数据:" + key);
			centerDataService.delete(key);
		}
		return 1;
	}

	@Override
	public long incrSequence(String prefix) {

		int serverId = NumericUtils.parseInt(encryptPropertyPlaceholderConfigurer.getProperty("systemServerId"));
		String key = CACHE_PREFIX + "_SEQUENCE_" + prefix;
		return Long.parseLong(serverId + "" + new SimpleDateFormat("MMddHHmmss").format(new Date()) + "" + RandomStringUtils.randomNumeric(3));
		//long rand = Long.parseLong(serverId + "" + new DecimalFormat("000").format(Thread.currentThread().getId()) + "" + new SimpleDateFormat("mmss").format(new Date()) + "" + RandomStringUtils.randomNumeric(3));
		//long id = Long.parseLong(String.valueOf(centerDataService.increaseBy(key, 1, backupNumber, 0))  + RandomStringUtils.randomNumeric(4));
		// logger.debug("生成自增ID,key=" + key + ",backNumber=" + backupNumber + ",最后生成ID:" + id);
		//return rand;
	}


}
