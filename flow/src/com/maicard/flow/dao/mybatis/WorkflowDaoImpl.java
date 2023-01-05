package com.maicard.flow.dao.mybatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.maicard.base.BaseDao;
import com.maicard.base.CriteriaMap;
import com.maicard.flow.dao.WorkflowDao;
import com.maicard.flow.entity.Workflow;

@Repository
public class WorkflowDaoImpl extends BaseDao implements WorkflowDao {

	public int insert(Workflow workflow) throws DataAccessException {
		return getSqlSessionTemplate().insert("com.maicard.flow.sql.Workflow.insert", workflow);
	}

	public int update(Workflow workflow) throws DataAccessException {
		return getSqlSessionTemplate().update("com.maicard.flow.sql.Workflow.update", workflow);
	}

	public int delete(long workflowId) throws DataAccessException {
		return getSqlSessionTemplate().delete("com.maicard.flow.sql.Workflow.delete", workflowId);
	}

	public Workflow select(long workflowId) throws DataAccessException {
		return (Workflow) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.Workflow.select", workflowId);
	}

	public List<Workflow> list(CriteriaMap workflowCriteria) throws DataAccessException {
		Assert.notNull(workflowCriteria, "workflowCriteria must not be null");
		
		return getSqlSessionTemplate().selectList("com.maicard.flow.sql.Workflow.list", workflowCriteria);
	}
 

	public int count(CriteriaMap workflowCriteria) throws DataAccessException {
		Assert.notNull(workflowCriteria, "workflowCriteria must not be null");
		
		return ((Integer) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.Workflow.count", workflowCriteria)).intValue();
	}

}
