package com.maicard.security.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.security.entity.User;


public interface FrontUserMapper extends IDao<User> {

	int changeUuid(User frontUser);
	

}
