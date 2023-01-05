package com.maicard.security.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsBaseService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.ObjectType;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.DataDefineService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.security.constants.UserRelationConst;
import com.maicard.security.dao.mapper.UserRelationMapper;
import com.maicard.security.entity.UserRelation;
import com.maicard.security.service.UserRelationService;
import com.maicard.utils.NumericUtils;

import static com.maicard.security.constants.UserRelationConst.USER_RELATION_COUNT_KEY_PREFIX;

@Service
public class UserRelationServiceImpl extends AbsGlobalSyncService<UserRelation, UserRelationMapper> implements UserRelationService {
 


	@Resource
	private CenterDataService centerDataService;
	
	@Resource
	private GlobalUniqueService globalUniqueService;
	

	final String[] needCachedDynamicType = new String[]{UserRelationConst.RELATION_TYPE_FAVORITE,UserRelationConst.RELATION_TYPE_PRAISE, UserRelationConst.RELATION_TYPE_READ};




	@Override
	public int getRelationCount(CriteriaMap userRelationCriteria){
		Assert.notNull(userRelationCriteria,"统计关联对象的条件不能为空");
		
		String objectType = userRelationCriteria.getStringValue("objectType");
		String relationType = userRelationCriteria.getStringValue("relationType");

		Assert.notNull(objectType,"统计关联对象的对象类型不能为空");		
		String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + objectType + "#" + (relationType == null ? "" : relationType + "#") + userRelationCriteria.getLongValue("objectId");
		String cachedCount = centerDataService.get(key);
		logger.debug("统计对象[" + key + "]在中央缓存中的总数是:" + cachedCount );
		int count = 0;
		if(cachedCount == null || !NumericUtils.isNumeric(cachedCount)){
			count = count(userRelationCriteria);
			cachedCount = String.valueOf(count);
			centerDataService.setForce(key, cachedCount, -1);
		}
		return Integer.parseInt(cachedCount);

	}

	public int insert(UserRelation userRelation) {
		if(StringUtils.isBlank(userRelation.getRelationLimit())){
			userRelation.setRelationLimit(UserRelationConst.RELATION_LIMIT_UNIQUE);
		}
		if(userRelation.getRelationLimit().equalsIgnoreCase(UserRelationConst.RELATION_LIMIT_UNIQUE) 
				|| userRelation.getRelationLimit().equalsIgnoreCase(UserRelationConst.RELATION_LIMIT_GLOBAL_UNIQUE)){
			CriteriaMap userRelationCriteria = CriteriaMap.create(userRelation.getOwnerId());
			userRelationCriteria.put("uuid",userRelation.getUuid());
			userRelationCriteria.put("objectType",userRelation.getObjectType());
			userRelationCriteria.put("relationType",userRelation.getRelationType());
			userRelationCriteria.put("objectId",userRelation.getObjectId());

			int alreadyRelatedCount = count(userRelationCriteria);
			if(alreadyRelatedCount > 0){
				logger.error("用户[" + userRelation.getUuid() + "]已关联" + userRelation.getObjectType() + "对象#" + userRelation.getObjectId() + ",关联类型为:" + userRelation.getRelationType() + ",且关联限制为:" + userRelation.getRelationLimit() + ",不再关注");
				return -1;
			}
		}
		if(userRelation.getCreateTime() == null){
			userRelation.setCreateTime(new Date());
		}
		if(userRelation.getId() < 1){
			//通过REDIS去获取一个主键而不是使用数据库的自增ID，来保证insert的幂等性,NetSnake,2018-5-7.
			long id = globalUniqueService.incrSequence(ObjectType.userRelation.name());
			userRelation.setId(id);
		}
		int rs =  mapper.insert(userRelation);
		logger.debug("插入对象[" + userRelation + "]返回结果:" + rs);
		if(rs != 1){
			return rs;
		}
		//检查是否需要更新缓存
		for(String relationType : needCachedDynamicType){
			if(userRelation.getRelationType() != null && userRelation.getRelationType().equals(relationType)){
				String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + userRelation.getObjectType() + "#" + relationType + "#" + userRelation.getObjectId();
				logger.debug("新写入的UserRelation需要在中央缓存中+1:" + key);
				centerDataService.increaseBy(key, 1, 1, 0);
				break;
			}
		}

		return rs;
	}


	public int deleteBy(CriteriaMap userRelationCriteria) {

		if(userRelationCriteria == null){
			return -1;
		}
		if(StringUtils.isBlank(userRelationCriteria.getStringValue("objectType"))){
			logger.error("条件删除关联未提供对象类型objectType");
			return EisError.REQUIRED_PARAMETER.id;
		}
		if(userRelationCriteria.getLongValue("uuid") <= 0 && userRelationCriteria.getLongValue("objectId") <= 0){
			logger.error("条件删除关联没有提供用户ID也没有提供对象ID");
			return EisError.REQUIRED_PARAMETER.id;
		}	
		if(StringUtils.isBlank(userRelationCriteria.getStringValue("relationType"))){
			logger.error("条件删除关联未提供关联的类型relationType");
			return EisError.REQUIRED_PARAMETER.id;
		}
		int rs = mapper.deleteBy(userRelationCriteria);
		logger.debug("删除关联[" + userRelationCriteria + "]的结果是:" + rs);
		if(rs != 1){
			return rs;
		}
		//检查是否需要更新缓存
		for(String relationType : needCachedDynamicType){
			String rt = userRelationCriteria.getStringValue("relationType");
			if(rt != null && rt.equals(relationType)){
				String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + rt + "#" + relationType + "#" + userRelationCriteria.getLongValue("objectId");
				logger.debug("新删除的UserRelation需要在中央缓存中-1:" + key);
				centerDataService.increaseBy(key, -1, 1, 0);
				break;
			}
		}
		return rs;

	}




	@Override
	public int delete(long userRelationId) {
		UserRelation _oldUserRelation = select(userRelationId);
		int actualRowsAffected = 0;
		if(_oldUserRelation != null){
			actualRowsAffected = mapper.delete(userRelationId);
		}
		logger.debug("删除关联[" + _oldUserRelation + "]的结果是:" + actualRowsAffected);
		if(actualRowsAffected != 1){
			return actualRowsAffected;
		}
		//检查是否需要更新缓存
		for(String relationType : needCachedDynamicType){
			if(_oldUserRelation.getRelationType() != null && _oldUserRelation.getRelationType().equals(relationType)){
				String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + _oldUserRelation.getObjectType() + "#" + relationType + "#" + _oldUserRelation.getObjectId();
				logger.debug("新删除的UserRelation需要在中央缓存中-1:" + key);
				centerDataService.increaseBy(key, -1, 1, 0);
				break;
			}
		}
		return actualRowsAffected;
	}

	//在中央缓存中增加一个关联数
	@Override
	@Async
	public void plusCachedRelationCount(UserRelation userRelation) {
		String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + userRelation.getObjectType() + "#" + (userRelation.getRelationType() == null ? "" : userRelation.getRelationType() + "#") + userRelation.getObjectId();
		long relationCount = centerDataService.increaseBy(key, 1, 1, 0);
		logger.debug("对对象[" + userRelation.getObjectType() + "#" + userRelation.getObjectId() + "]强行增加一个关联数量，最新关联数量是:" + relationCount);
	}

	@Override
	public void setDynamicData(BaseEntity object){

		

		String objectType = StringUtils.uncapitalize(object.getClass().getSimpleName());
		//获取阅读数
		for(String relationType : needCachedDynamicType){
			//从缓存中读取对应的KEY
			String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + objectType + "#" + relationType + "#" + object.getId();
			String value = centerDataService.get(key);
			String dataCode = relationType + "Count";
			if(value == null){
				CriteriaMap userRelationCriteria = CriteriaMap.create(object.getOwnerId());
				userRelationCriteria.put("objectType",objectType);
				userRelationCriteria.put("relationType",relationType);
				userRelationCriteria.put("objectId",object.getId());

				//从数据库统计
				int count = count(userRelationCriteria);
				logger.debug("从数据库中统计[" + key + "]的数量是:" + count);
				value = String.valueOf(count);				
				centerDataService.setForce(key, value, -1);
			} else {
				logger.debug("从中央缓存中统计[" + key + "]的数量是:" + value);

			}
			logger.debug("为对象[" + objectType + "#" + object.getId() + "]写入最新的数据:" + dataCode + "=>" + value);
			object.setExtra(dataCode, value);


		}


	}


}
