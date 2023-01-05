package com.maicard.security.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.security.entity.Menu;
import com.maicard.security.entity.MenuRoleRelation;
import com.maicard.security.entity.User;


public interface PartnerMenuRoleRelationService extends IService<MenuRoleRelation> {



	void deleteByGroupId(int groupId);
	

	List<Menu> listInTree(CriteriaMap params);

	List<Menu> listAllByPartner(User partner);

	/**
	 * 列出所有菜单，但是将对应role已经拥有的菜单设置为defaulted
	 */
	List<Menu> listAllByRole(CriteriaMap criteria);


}
