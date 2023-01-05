package com.maicard.security.service;

import java.util.List;

import com.maicard.base.GlobalSyncService;
import com.maicard.base.IService;
import org.springframework.scheduling.annotation.Async;

import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.BaseEntity;
import com.maicard.security.entity.UserRelation;


public interface UserRelationService extends GlobalSyncService<UserRelation> {



	int getRelationCount(CriteriaMap criteria);

	@Async
	void plusCachedRelationCount(UserRelation userRelation);
 
	void setDynamicData(BaseEntity object);


	

}
