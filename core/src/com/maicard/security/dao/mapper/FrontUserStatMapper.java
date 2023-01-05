package com.maicard.security.dao.mapper;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.security.entity.User;
import com.maicard.security.entity.UserStat;

import java.util.List;


public interface FrontUserStatMapper extends IDao<User> {

	List<UserStat> stat(CriteriaMap criteriaMap);
	

}
