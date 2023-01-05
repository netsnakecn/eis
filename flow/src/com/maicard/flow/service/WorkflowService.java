package com.maicard.flow.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.Workflow;

public interface WorkflowService {

	int insert(Workflow workflow);

	int update(Workflow workflow);

	int delete(long workflowId);
	
	Workflow select(long workflowId);

	List<Workflow> list(CriteriaMap workflowCriteria);

	List<Workflow> listOnPage(CriteriaMap workflowCriteria);

	int count(CriteriaMap workflowCriteria);
	
	boolean requiredWorkflow(String objectType, String action, long ownerId);

}
