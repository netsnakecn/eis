package com.maicard.core.service.impl;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.EisError;
import com.maicard.core.dao.mapper.RelationMapper;
import com.maicard.core.entity.Relation;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.core.service.RelationService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;


@Service
public class RelationServiceImpl extends AbsGlobalSyncService<Relation, RelationMapper> implements RelationService {
 
	@Resource
	private CenterDataService centerDataService;
	
	@Resource
	private GlobalUniqueService globalUniqueService;
	

	//final String[] needCachedDynamicType = new String[]{RelationConst.RELATION_TYPE_FAVORITE,RelationConst.RELATION_TYPE_PRAISE, RelationConst.RELATION_TYPE_READ};


 

	public int insert(Relation relation) {
		if(StringUtils.isBlank(relation.getRelationLimit())){
			relation.setRelationLimit(Relation.RELATION_LIMIT_UNIQUE);
		}
		if(relation.getRelationLimit().equalsIgnoreCase(Relation.RELATION_LIMIT_UNIQUE)
				|| relation.getRelationLimit().equalsIgnoreCase(Relation.RELATION_LIMIT_GLOBAL_UNIQUE)){
			CriteriaMap relationCriteria = CriteriaMap.create(relation.getOwnerId());
			relationCriteria.put("fromType", Objects.requireNonNull(relation.getFromType()));
			relationCriteria.put("fromId",relation.getFromId());
			relationCriteria.put("toType",Objects.requireNonNull(relation.getToType()));
			relationCriteria.put("relationType",relation.getRelationType());
			relationCriteria.put("toId",relation.getToId());

			int alreadyRelatedCount = count(relationCriteria);
			if(alreadyRelatedCount > 0){
				logger.error("已存在[" +  relation.getFromType() + "#" + relation.getFromId() + "]与[" + relation.getToType() + "#" + relation.getToId() + "]的关联,关联类型为:" + relation.getRelationType() + ",且关联限制为:" + relation.getRelationLimit() );
				return EisError.dataUniqueConflict.id;
			}
		}


		int rs =  mapper.insert(relation);
		logger.debug("插入对象[" + relation + "]返回结果:" + rs);
		if(rs != 1){
			return rs;
		}
		//检查是否需要更新缓存
		/*for(String relationType : needCachedDynamicType){
			if(relation.getRelationType() != null && relation.getRelationType().equals(relationType)){
				String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + relation.getObjectType() + "#" + relationType + "#" + relation.getObjectId();
				logger.debug("新写入的Relation需要在中央缓存中+1:" + key);
				centerDataService.increaseBy(key, 1, 1, 0);
				break;
			}
		}*/

		return rs;
	}


	public int deleteBy(CriteriaMap relationCriteria) {

		if(relationCriteria == null){
			return -1;
		}
		if(StringUtils.isBlank(relationCriteria.getStringValue("fromType")) || StringUtils.isBlank(relationCriteria.getStringValue("toType"))){
			logger.error("条件删除关联未提供fromType或toType");
			return EisError.REQUIRED_PARAMETER.id;
		}
		if(relationCriteria.getLongValue("fromId") <= 0 && relationCriteria.getLongValue("toId") <= 0){
			logger.error("条件删除关联没有提供fromId也没有toId");
			return EisError.REQUIRED_PARAMETER.id;
		}	

		int rs = mapper.deleteBy(relationCriteria);
		logger.debug("删除关联[" + relationCriteria + "]的结果是:" + rs);
		if(rs != 1){
			return rs;
		}
		//检查是否需要更新缓存
	/*	for(String relationType : needCachedDynamicType){
			String rt = relationCriteria.getStringValue("relationType");
			if(rt != null && rt.equals(relationType)){
				String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + rt + "#" + relationType + "#" + relationCriteria.getLongValue("objectId");
				logger.debug("新删除的Relation需要在中央缓存中-1:" + key);
				centerDataService.increaseBy(key, -1, 1, 0);
				break;
			}
		}*/
		return rs;

	}




	@Override
	public int delete(long relationId) {
		Relation _oldRelation = select(relationId);
		int actualRowsAffected = 0;
		if(_oldRelation != null){
			actualRowsAffected = mapper.delete(relationId);
		}
		logger.debug("删除关联[" + _oldRelation + "]的结果是:" + actualRowsAffected);
		if(actualRowsAffected != 1){
			return actualRowsAffected;
		}
		//检查是否需要更新缓存
		/*for(String relationType : needCachedDynamicType){
			if(_oldRelation.getRelationType() != null && _oldRelation.getRelationType().equals(relationType)){
				String key = USER_RELATION_COUNT_KEY_PREFIX + "#" + _oldRelation.getObjectType() + "#" + relationType + "#" + _oldRelation.getObjectId();
				logger.debug("新删除的Relation需要在中央缓存中-1:" + key);
				centerDataService.increaseBy(key, -1, 1, 0);
				break;
			}
		}*/
		return actualRowsAffected;
	}






}
