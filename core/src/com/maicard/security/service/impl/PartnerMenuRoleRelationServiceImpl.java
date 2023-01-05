package com.maicard.security.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsBaseService;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Service;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.security.dao.mapper.PartnerMenuRoleRelationMapper;
import com.maicard.security.entity.Menu;
import com.maicard.security.entity.MenuRoleRelation;
import com.maicard.security.entity.Role;
import com.maicard.security.entity.User;
import com.maicard.security.service.PartnerMenuRoleRelationService;
import com.maicard.security.service.PartnerMenuService;

@Service
public class PartnerMenuRoleRelationServiceImpl extends AbsBaseService<MenuRoleRelation,PartnerMenuRoleRelationMapper> implements PartnerMenuRoleRelationService, MessageSourceAware {
 
	@Resource
	private PartnerMenuService partnerMenuService;

	private MessageSource messageSource;



	public void deleteByGroupId(int groupId){
		mapper.deleteByGroupId(groupId);
	}






	/*以树形结构列出菜单
	 * 是否返回所有菜单，取决于PartnerMenuRoleRelationCriteria中是否存在roleId
	 */
	public List<Menu> listInTree(CriteriaMap partnerMenuRoleRelationCriteria) {


		Object l = partnerMenuRoleRelationCriteria.get("locale");
		Locale locale = null;
		if(l != null && l instanceof Locale){
			locale = (Locale)l;
		}
		CriteriaMap menuCriteria = CriteriaMap.create();
		long ownerId = partnerMenuRoleRelationCriteria.getLongValue("ownerId");
		menuCriteria.put("currentStatus",BasicStatus.normal.id);
		menuCriteria.put("ownerId",ownerId);
		List<Menu> plateSysMenuList = partnerMenuService.list(menuCriteria);
		logger.debug("ownerId=" + ownerId + "的全部菜单是" + (plateSysMenuList == null ? "空" : plateSysMenuList.size()));
		if(plateSysMenuList == null){
			return null;
		}
		ArrayList<Menu> userMenuList = new ArrayList<Menu>();
		int[] roleIds = partnerMenuRoleRelationCriteria.get("roleIds");
		if(roleIds != null && roleIds.length > 0){
			List<MenuRoleRelation> sysMenuPositionRelationList = mapper.list(partnerMenuRoleRelationCriteria);
			for(int i = 0; i < plateSysMenuList.size(); i++){
				for(int j = 0; j<sysMenuPositionRelationList.size(); j++){
					if(plateSysMenuList.get(i).getId() == sysMenuPositionRelationList.get(j).getMenuId()){
						Menu currentMenu = plateSysMenuList.get(i);
						if(userMenuList.contains(currentMenu)){
							logger.debug("忽略重复菜单:" + currentMenu);
						} else {
							userMenuList.add(currentMenu);
						}
						break;
					}
				}
			}
		} else {
			userMenuList = (ArrayList<Menu>)plateSysMenuList;
		}
		logger.info("过滤后的菜单是:" + (userMenuList == null ? "空" : userMenuList.size()));
		/*
		 * 由于前台菜单空间不能控制部分选择的情况，所以这里要对没有父菜单的菜单，进行修补
		 */
		//ArrayList<Integer> needPathMenuId = new ArrayList<Integer>();
		for(int i = 0; i < userMenuList.size(); i++){
			if(userMenuList.get(i).getParentMenuId() == 0){
				continue;
			}
			boolean orphan = true;
			for(int j = 0; j < userMenuList.size(); j++){				
				if(userMenuList.get(i).getParentMenuId() == userMenuList.get(j).getId()){
					orphan = false;
					break;
				}				
			}
			if(orphan){
				logger.info("菜单[" + userMenuList.get(i).getMenuName() + "/" + userMenuList.get(i).getId() + "]没有父菜单");
				for(int k  = 0; k  < plateSysMenuList.size(); k++){
					if(plateSysMenuList.get(k).getId() == userMenuList.get(i).getParentMenuId()){
						logger.info("把孤儿菜单[" + userMenuList.get(i).getMenuName() + "]的父菜单[" + plateSysMenuList.get(k).getMenuName() + "]自动加入菜单");
						userMenuList.add(plateSysMenuList.get(k));
						break;
					}
				}
			}
		}


		//子菜单设置
		for(int i = 0; i< userMenuList.size(); i++){		
			for(int j = 0; j< userMenuList.size(); j++){
				if(userMenuList.get(j).getParentMenuId() == userMenuList.get(i).getId()){
					if(userMenuList.get(i).getSubMenuList() == null){
						userMenuList.get(i).setSubMenuList(new ArrayList<Menu>());
					}
					Menu menu = i18n(userMenuList.get(j),locale);
					userMenuList.get(i).getSubMenuList().add(menu);
				}
			}

		}
		//形成树形结构
		ArrayList<Menu> topMenuList = new ArrayList<Menu>();
		for(int i = 0; i< userMenuList.size(); i++){	
			if(userMenuList.get(i).getParentMenuId() == 0){
				Menu menu = i18n(userMenuList.get(i),locale);
				topMenuList.add(menu);
			}
		}
		return topMenuList;
	}

	private Menu i18n(Menu menu, Locale locale) {
		if(menu.getMenuName() == null){
			return menu;
		}
		menu.setMenuName(messageSource.getMessage(menu.getMenuName().trim(), null, locale));
		return menu;
	}

	@Override
	public List<Menu> listAllByPartner(User partner) {
		CriteriaMap criteria = CriteriaMap.create(partner.getOwnerId());
		criteria.put("currentStatus", new int[] {BasicStatus.normal.id});
		List<Menu> allList0 = partnerMenuService.list(criteria);

		if(allList0.size() < 1) {
			return Collections.emptyList();
		}
		List<Menu> allList = new ArrayList<Menu>();
		for(Menu menu : allList0) {
			allList.add(menu.clone());
		}

		if(partner.getRelatedRoleList() == null || partner.getRelatedRoleList().size() < 1) {
			logger.info("用户:{}没有任何关联角色", partner.getUuid());
			return allList;
		}
		for(Role role : partner.getRelatedRoleList()) {
			criteria.init();
			criteria.put("roleId", role.getId());
			List<MenuRoleRelation> relationList = this.list(criteria);
			if(relationList.size() > 0) {
				for(Menu m : allList) {
					for(MenuRoleRelation mrr : relationList) {
						if(mrr.getMenuId() == m.getId()) {
							m.setCurrentStatus(BasicStatus.defaulted.id);
							break;
						}
					}
				}
			}

		}


		return allList;
	}


	@Override
	public List<Menu> listAllByRole(CriteriaMap criteria) {
		CriteriaMap criteria2 = CriteriaMap.create(criteria.getLongValue("ownerId"));
		criteria2.put("currentStatus", new int[] {BasicStatus.normal.id});
		List<Menu> allList0 = partnerMenuService.list(criteria2);

		if(allList0.size() < 1) {
			return Collections.emptyList();
		}
		List<Menu> allList = new ArrayList<Menu>();
		for(Menu menu : allList0) {
			allList.add(menu.clone());
		}
		

		long[] roleIds = criteria.get("roleIds");
		if(roleIds == null || roleIds.length  <= 0) {
			logger.error("查询条件中必须包含roleIds");
			return allList;
		}
		criteria2.init();
		criteria2.put("roleIds", roleIds);
		List<MenuRoleRelation> relationList = this.list(criteria2);
		
		if(relationList.size() > 0) {
			for(Menu m : allList) {
				for(MenuRoleRelation mrr : relationList) {
					if(mrr.getMenuId() == m.getId()) {
						m.setCurrentStatus(BasicStatus.defaulted.id);
						break;
					}
				}
			}
		}




		return allList;
	}





	@Override
	public void setMessageSource(MessageSource m) {
		this.messageSource = m;

	}
}
