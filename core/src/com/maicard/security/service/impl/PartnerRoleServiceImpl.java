package com.maicard.security.service.impl;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.service.abs.AbsBaseService;
import com.maicard.security.dao.mapper.PartnerRoleMapper;
import com.maicard.security.entity.Privilege;
import com.maicard.security.entity.Role;
import com.maicard.security.entity.User;
import com.maicard.security.service.PartnerPrivilegeService;
import com.maicard.security.service.PartnerRoleRelationService;
import com.maicard.security.service.PartnerRoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class PartnerRoleServiceImpl extends AbsBaseService<Role, PartnerRoleMapper> implements PartnerRoleService {

	@Resource
	private PartnerPrivilegeService partnerPrivilegeService;

	@Override
	public void afterFetch(Role role){
		if(role == null){
			return;
		}
		if(role.getRelatedPrivilegeList() != null && role.getRelatedPrivilegeList().size() > 0){
			return;
		}
		CriteriaMap criteriaMap = CriteriaMap.create(role.getOwnerId()).put("roleId",role.getId());
		criteriaMap.put("currentStatus", BasicStatus.normal.id);
		criteriaMap.putArray("roleIds", role.getId());
		List<Privilege> privilegeList = partnerPrivilegeService.listByRole(criteriaMap);
		role.setRelatedPrivilegeList(privilegeList);
	}

	

	@Override
	public int[] getValidLevel(User partner, String roleLevelStr) {
		if(partner.getUserExtraTypeId() <= 0) {
			return null;
		}
		return new int[] {partner.getUserExtraTypeId()};
	}




}
