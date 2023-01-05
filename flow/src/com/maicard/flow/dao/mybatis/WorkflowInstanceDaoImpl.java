package com.maicard.flow.dao.mybatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.maicard.base.BaseDao;
import com.maicard.base.CriteriaMap;
import com.maicard.flow.dao.WorkflowInstanceDao;
import com.maicard.flow.entity.WorkflowInstance;

@Repository
public class WorkflowInstanceDaoImpl extends BaseDao implements WorkflowInstanceDao {

	public int insert(WorkflowInstance workflowInstance) throws DataAccessException {
		return (Integer)getSqlSessionTemplate().insert("com.maicard.flow.sql.WorkflowInstance.insert", workflowInstance);
	}

	public int update(WorkflowInstance workflowInstance) throws DataAccessException {
		return getSqlSessionTemplate().update("com.maicard.flow.sql.WorkflowInstance.update", workflowInstance);
	}

	public int delete(long workflowInstanceId) throws DataAccessException {
		return getSqlSessionTemplate().delete("com.maicard.flow.sql.WorkflowInstance.delete",  (workflowInstanceId));

	}

	public WorkflowInstance select(long workflowInstanceId) throws DataAccessException {
		return (WorkflowInstance) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.WorkflowInstance.select",  (workflowInstanceId));
	}

	public List<WorkflowInstance> list(CriteriaMap workflowInstanceCriteria) throws DataAccessException {
		Assert.notNull(workflowInstanceCriteria, "workflowInstanceCriteria must not be null");
		
		return getSqlSessionTemplate().selectList("com.maicard.flow.sql.WorkflowInstance.list", workflowInstanceCriteria);
	}
 

	public int count(CriteriaMap workflowInstanceCriteria) throws DataAccessException {
		Assert.notNull(workflowInstanceCriteria, "workflowInstanceCriteria must not be null");
		
		return ((Integer) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.WorkflowInstance.count", workflowInstanceCriteria)).intValue();
	}

}
