package com.maicard.flow.dao.mybatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.maicard.base.BaseDao;
import com.maicard.base.CriteriaMap;
import com.maicard.flow.dao.RouteDao;
import com.maicard.flow.entity.Route;

@Repository
public class RouteDaoImpl extends BaseDao implements RouteDao {

	public void insert(Route route) throws DataAccessException {
		getSqlSessionTemplate().insert("com.maicard.flow.sql.Route.insert", route);
	}

	public int update(Route route) throws DataAccessException {
		return getSqlSessionTemplate().update("com.maicard.flow.sql.Route.update", route);
	}

	public int delete(long routeId) throws DataAccessException {
		return getSqlSessionTemplate().delete("com.maicard.flow.sql.Route.delete", routeId);
	}

	public Route select(long routeId) throws DataAccessException {
		return (Route) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.Route.select", routeId);
	}

	public List<Route> list(CriteriaMap routeCriteria) throws DataAccessException {
		Assert.notNull(routeCriteria, "routeCriteria must not be null");
		
		return getSqlSessionTemplate().selectList("com.maicard.flow.sql.Route.list", routeCriteria);
	} 
	public int count(CriteriaMap routeCriteria) throws DataAccessException {
		Assert.notNull(routeCriteria, "routeCriteria must not be null");
		
		return getSqlSessionTemplate().selectOne("com.maicard.flow.sql.Route.count", routeCriteria);
	}

}
