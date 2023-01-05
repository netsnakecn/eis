package com.maicard.flow.dao.mybatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.maicard.base.BaseDao;
import com.maicard.base.CriteriaMap;
import com.maicard.flow.dao.WorkflowPrivilegeDao;
import com.maicard.flow.entity.WorkflowPrivilege;

@Repository
public class WorkflowPrivilegeDaoImpl extends BaseDao implements WorkflowPrivilegeDao {

	public int insert(WorkflowPrivilege workflowPrivilege) throws DataAccessException {
		return (Integer)getSqlSessionTemplate().insert("com.maicard.flow.sql.WorkflowPrivilege.insert", workflowPrivilege);
	}

	public int update(WorkflowPrivilege workflowPrivilege) throws DataAccessException {
		return getSqlSessionTemplate().update("com.maicard.flow.sql.WorkflowPrivilege.update", workflowPrivilege);
	}

	public int delete(long workflowPrivilegeId) throws DataAccessException {
		return getSqlSessionTemplate().delete("com.maicard.flow.sql.WorkflowPrivilege.delete", workflowPrivilegeId);

	}

	public WorkflowPrivilege select(long workflowPrivilegeId) throws DataAccessException {
		return (WorkflowPrivilege) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.WorkflowPrivilege.select", workflowPrivilegeId);
	}

	public List<WorkflowPrivilege> list(CriteriaMap workflowPrivilegeCriteria) throws DataAccessException {
		Assert.notNull(workflowPrivilegeCriteria, "workflowPrivilegeCriteria must not be null");
		
		return getSqlSessionTemplate().selectList("com.maicard.flow.sql.WorkflowPrivilege.list", workflowPrivilegeCriteria);
	}

 
	public int count(CriteriaMap workflowPrivilegeCriteria) throws DataAccessException {
		Assert.notNull(workflowPrivilegeCriteria, "workflowPrivilegeCriteria must not be null");
		
		return ((Integer) getSqlSessionTemplate().selectOne("com.maicard.flow.sql.WorkflowPrivilege.count", workflowPrivilegeCriteria)).intValue();
	}

}
