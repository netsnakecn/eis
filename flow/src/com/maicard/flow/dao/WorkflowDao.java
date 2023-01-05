package com.maicard.flow.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.Workflow;

public interface WorkflowDao {

	int insert(Workflow workflow) throws DataAccessException;

	int update(Workflow workflow) throws DataAccessException;

	int delete(long workflowId) throws DataAccessException;

	Workflow select(long workflowId) throws DataAccessException;

	List<Workflow> list(CriteriaMap workflowCriteria) throws DataAccessException;
	
	
	int count(CriteriaMap workflowCriteria) throws DataAccessException;

}
