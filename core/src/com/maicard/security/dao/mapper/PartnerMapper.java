package com.maicard.security.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.security.entity.User;


public interface PartnerMapper extends IDao<User> {


	List<User> listOnPage(CriteriaMap params) throws DataAccessException;
	


	List<Long> listPkOnPage(CriteriaMap params)
			throws DataAccessException;

	String listBelowUser(long rootUuid);

 


}
