package com.maicard.flow.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.Route;
import com.maicard.security.entity.User;

public interface RouteService {

	void insert(Route route);

	int update(Route route);

	int delete(int routeId);
	
	Route select(int routeId);

	List<Route> list(CriteriaMap routeCriteria);

	List<Route> listOnPage(CriteriaMap routeCriteria);

	int count(CriteriaMap routeCriteria);

	boolean havePrivilege(User user, long objectId, Route route);

}
