package com.maicard.flow.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.flow.entity.WorkflowPrivilege;

public interface WorkflowPrivilegeService {

	int insert(WorkflowPrivilege workflowPrivilege);

	int update(WorkflowPrivilege workflowPrivilege);

	int delete(int workflowPrivilegeId);
	
	WorkflowPrivilege select(int workflowPrivilegeId);

	List<WorkflowPrivilege> list(CriteriaMap workflowPrivilegeCriteria);

	List<WorkflowPrivilege> listOnPage(CriteriaMap workflowPrivilegeCriteria);

	int count(CriteriaMap workflowPrivilegeCriteria);

	
	

}
