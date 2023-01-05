package com.maicard.security.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.security.entity.Privilege;
import com.maicard.security.entity.User;


public interface PartnerPrivilegeMapper extends IDao<Privilege> {


    List<Privilege> listByRole(CriteriaMap partnerPrivilegeCriteria);

    List<User> getUserByPrivilege(CriteriaMap privilegeCriteria);
}
