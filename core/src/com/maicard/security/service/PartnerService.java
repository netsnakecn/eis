package com.maicard.security.service;

import java.util.List;

import com.maicard.base.BaseVo;
import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.security.entity.User;
import com.maicard.security.vo.UserVoSimple;


public interface PartnerService extends GlobalSyncService<User> {



	//列出指定partner的所有子账户
	void listAllChildren(List<User> all, long  fatherId);
	
	boolean isValidSubUser(long parentUuid, long childUuid);

	int insertLocal(User partner);


	int updateLocal(User partner);

	//列出指定账户的子孙账户
	List<User> listBelowUser(long rootUuid, long ownerId);
	 

	void correctUserAttributes(User frontUser);

 
	long getHeadUuid(User partner);

	long getHeadUuid(long inviter);

	User fuzzySelect(CriteriaMap criteria);

	void setSubPartner(CriteriaMap criteria, User partner, boolean addSelf);
	
	void evictCache(User user);

	List<User> listSimplePartnerByExtraType(User partner, int... extraTypeIds);

	List<User> listSimplePartner(CriteriaMap criteria);

	/**
	 * 为查询条件设置操作员的条件
	 * 即把查询条件中的用户范围设置为上级用户
	 * 如果没有上级用户，则设置为自己
	 * @param criteria
	 * @param partner
	 */
	void setForOperator(CriteriaMap criteria, User partner);



}
