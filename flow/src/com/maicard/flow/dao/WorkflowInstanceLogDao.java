package com.maicard.flow.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.WorkflowInstanceLog;

public interface WorkflowInstanceLogDao {

	void insert(WorkflowInstanceLog workflowInstanceLog) throws DataAccessException;

	int update(WorkflowInstanceLog workflowInstanceLog) throws DataAccessException;

	int delete(int workflowInstanceLogId) throws DataAccessException;

	WorkflowInstanceLog select(int workflowInstanceLogId) throws DataAccessException;

	List<WorkflowInstanceLog> list(CriteriaMap workflowInstanceLogCriteria) throws DataAccessException;
	
	
	int count(CriteriaMap workflowInstanceLogCriteria) throws DataAccessException;

}
