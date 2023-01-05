package com.maicard.flow.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.WorkflowInstance;

public interface WorkflowInstanceDao {

	int insert(WorkflowInstance workflowInstance) throws DataAccessException;

	int update(WorkflowInstance workflowInstance) throws DataAccessException;

	int delete(long workflowInstanceId) throws DataAccessException;

	WorkflowInstance select(long workflowInstanceId) throws DataAccessException;

	List<WorkflowInstance> list(CriteriaMap workflowInstanceCriteria) throws DataAccessException;
	
	
	int count(CriteriaMap workflowInstanceCriteria) throws DataAccessException;

}
