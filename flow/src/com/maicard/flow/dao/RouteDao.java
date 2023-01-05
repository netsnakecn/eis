package com.maicard.flow.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.Route;

public interface RouteDao {

	void insert(Route route) throws DataAccessException;

	int update(Route route) throws DataAccessException;

	int delete(long routeId) throws DataAccessException;

	Route select(long routeId) throws DataAccessException;

	List<Route> list(CriteriaMap routeCriteria) throws DataAccessException;
	
 	
	int count(CriteriaMap routeCriteria) throws DataAccessException;

}
