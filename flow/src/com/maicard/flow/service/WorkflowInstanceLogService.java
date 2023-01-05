package com.maicard.flow.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.WorkflowInstanceLog;

public interface WorkflowInstanceLogService {

	void insert(WorkflowInstanceLog workflowInstanceLog);

	int update(WorkflowInstanceLog workflowInstanceLog);

	int delete(int workflowInstanceLogId);
	
	WorkflowInstanceLog select(int workflowInstanceLogId);

	List<WorkflowInstanceLog> list(CriteriaMap workflowInstanceLogCriteria);

	List<WorkflowInstanceLog> listOnPage(CriteriaMap workflowInstanceLogCriteria);

}
