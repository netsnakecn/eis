package com.maicard.security.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.security.entity.Role;
import com.maicard.security.entity.User;


public interface PartnerRoleService extends IService<Role> {

	int[] getValidLevel(User partner, String roleLevelStr);



}
