package com.maicard.flow.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.WorkflowPrivilege;

public interface WorkflowPrivilegeDao {

	int insert(WorkflowPrivilege workflowPrivilege) throws DataAccessException;

	int update(WorkflowPrivilege workflowPrivilege) throws DataAccessException;

	int delete(long workflowPrivilegeId) throws DataAccessException;

	WorkflowPrivilege select(long workflowPrivilegeId) throws DataAccessException;

	List<WorkflowPrivilege> list(CriteriaMap workflowPrivilegeCriteria) throws DataAccessException;
	
	
	int count(CriteriaMap workflowPrivilegeCriteria) throws DataAccessException;

}
