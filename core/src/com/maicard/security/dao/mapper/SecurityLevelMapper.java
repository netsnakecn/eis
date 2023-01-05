package com.maicard.security.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.security.entity.SecurityLevel;

public interface SecurityLevelMapper extends IDao<SecurityLevel> {


	List<Long> listPkOnPage(CriteriaMap params )
			throws DataAccessException;



}
