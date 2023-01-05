package com.maicard.flow.dao.mybatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.maicard.base.BaseDao;
import com.maicard.base.CriteriaMap;
import com.maicard.flow.dao.WorkflowInstanceLogDao;
import com.maicard.flow.entity.WorkflowInstanceLog;

@Repository
public class WorkflowInstanceLogDaoImpl extends BaseDao implements WorkflowInstanceLogDao {

	public void insert(WorkflowInstanceLog workflowInstanceLog) throws DataAccessException {
		getSqlSessionTemplate().insert("com.maicard.flow.sql.WorkflowInstanceLog.insert", workflowInstanceLog);
	}

	public int update(WorkflowInstanceLog workflowInstanceLog) throws DataAccessException {
		return getSqlSessionTemplate().update("com.maicard.flow.sql.WorkflowInstanceLog.update", workflowInstanceLog);
	}

	public int delete(int workflowInstanceLogId) throws DataAccessException {
		return getSqlSessionTemplate().delete("com.maicard.flow.sql.WorkflowInstanceLog.delete", new Integer(workflowInstanceLogId));
	}

	public WorkflowInstanceLog select(int workflowInstanceLogId) throws DataAccessException {
		return (WorkflowInstanceLog) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.WorkflowInstanceLog.select", new Integer(workflowInstanceLogId));
	}

	public List<WorkflowInstanceLog> list(CriteriaMap workflowInstanceLogCriteria) throws DataAccessException {
		Assert.notNull(workflowInstanceLogCriteria, "workflowInstanceLogCriteria must not be null");		
		return getSqlSessionTemplate().selectList("com.maicard.flow.sql.WorkflowInstanceLog.list", workflowInstanceLogCriteria);
	}
 

	public int count(CriteriaMap workflowInstanceLogCriteria) throws DataAccessException {
		Assert.notNull(workflowInstanceLogCriteria, "workflowInstanceLogCriteria must not be null");
		
		return ((Integer) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.WorkflowInstanceLog.count", workflowInstanceLogCriteria)).intValue();
	}

}
