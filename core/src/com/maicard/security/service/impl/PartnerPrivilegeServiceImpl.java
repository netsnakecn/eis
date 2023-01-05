package com.maicard.security.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsBaseService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.security.dao.mapper.PartnerPrivilegeMapper;
import com.maicard.security.entity.Privilege;
import com.maicard.security.entity.User;
import com.maicard.security.service.PartnerPrivilegeService;


@Service
public class PartnerPrivilegeServiceImpl extends AbsGlobalSyncService<Privilege, PartnerPrivilegeMapper> implements PartnerPrivilegeService {


	@Override
	public List<Privilege> listByRole(CriteriaMap partnerPrivilegeCriteria) {
		//根据角色列出权限，那么角色ID就不能是空
	//	Assert.isTrue(partnerPrivilegeCriteria.getRoleIds().length > 0, "根据角色列出权限的角色ID不能是空");
	//	Assert.isTrue(partnerPrivilegeCriteria.getRoleIds()[0] > 0, "根据角色列出权限的角色，第一个角色ID不能是0:" + JsonUtils.toStringFull(partnerPrivilegeCriteria.getRoleIds()));

		List<Privilege> list  =  mapper.listByRole(partnerPrivilegeCriteria);
		
		if(list == null) {
			 return Collections.emptyList();
		 } else {
			 return list;
		 }
	}
	 

	@Override
	public List<User> getUserByPrivilege(CriteriaMap privilegeCriteria) {
		return mapper.getUserByPrivilege(privilegeCriteria);
	}

	@Override
	public List<Privilege> listRemainPrivilege(List<Privilege> partnerPrivilegeList) {
		
		CriteriaMap criteria = CriteriaMap.create();
		criteria.put("currentStatus", new int[] {BasicStatus.normal.id});
		List<Privilege> allPrivilegeList = list(criteria);
		if(allPrivilegeList.size() < 1) {
			logger.warn("当前系统中没有正常状态的权限");
			return allPrivilegeList;
		}
		if(partnerPrivilegeList == null || partnerPrivilegeList.size() < 1) {
			logger.warn("当前系统中所有正常状态的权限有{}条，但是角色本身没有关联任何权限，直接返回所有权限");
			return allPrivilegeList;
		}
		List<Privilege> remainPrivilegeList = new ArrayList<Privilege>();
		for(Privilege p1 : allPrivilegeList) {
			if(partnerPrivilegeList.contains(p1)){
				continue;
			}
			remainPrivilegeList.add(p1);
			
		}
		logger.info("系统共有权限{}条，角色已有权限{}条，剩余权限{}条", allPrivilegeList.size(),partnerPrivilegeList.size(), remainPrivilegeList.size());
		return remainPrivilegeList;
		
	}


}
