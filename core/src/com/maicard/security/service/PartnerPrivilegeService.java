package com.maicard.security.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.base.IService;
import com.maicard.security.entity.Privilege;
import com.maicard.security.entity.User;


public interface PartnerPrivilegeService extends GlobalSyncService<Privilege> {


	List<User> getUserByPrivilege( CriteriaMap params);

	List<Privilege> listByRole(CriteriaMap params);

	List<Privilege> listRemainPrivilege(List<Privilege> partnerPrivilegeList);

}
