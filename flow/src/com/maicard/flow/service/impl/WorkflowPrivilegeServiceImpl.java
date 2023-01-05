package com.maicard.flow.service.impl;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.flow.dao.WorkflowPrivilegeDao;
import com.maicard.flow.entity.WorkflowPrivilege;
import com.maicard.flow.service.RouteService;
import com.maicard.flow.service.WorkflowPrivilegeService;
import com.maicard.utils.PagingUtils;

@Service
public class WorkflowPrivilegeServiceImpl extends BaseService implements WorkflowPrivilegeService {

	

	@Resource
	private WorkflowPrivilegeDao workflowPrivilegeDao;

	@Resource RouteService routeService;

	HashMap<String,String[]> requireWorkflowPrivilegeConfigMap = new HashMap<String,String[]>();



	public int insert(WorkflowPrivilege workflowPrivilege) {
		return workflowPrivilegeDao.insert(workflowPrivilege);
	}

	public int update(WorkflowPrivilege workflowPrivilege) {
		int actualRowsAffected = 0;

		long workflowPrivilegeId = workflowPrivilege.getId();

		WorkflowPrivilege _oldWorkflowPrivilege = workflowPrivilegeDao.select(workflowPrivilegeId);

		if (_oldWorkflowPrivilege != null) {
			actualRowsAffected = workflowPrivilegeDao.update(workflowPrivilege);
		}

		return actualRowsAffected;
	}

	public int delete(int workflowPrivilegeId) {
		int actualRowsAffected = 0;

		WorkflowPrivilege _oldWorkflowPrivilege = workflowPrivilegeDao.select(workflowPrivilegeId);

		if (_oldWorkflowPrivilege != null) {
			actualRowsAffected = workflowPrivilegeDao.delete(workflowPrivilegeId);
		}

		return actualRowsAffected;
	}

	public WorkflowPrivilege select(int workflowPrivilegeId) {
		WorkflowPrivilege workflowPrivilege =  workflowPrivilegeDao.select(workflowPrivilegeId);
		return workflowPrivilege;
	}

	public List<WorkflowPrivilege> list(CriteriaMap workflowPrivilegeCriteria) {		
		List<WorkflowPrivilege> workflowPrivilegeList = workflowPrivilegeDao.list(workflowPrivilegeCriteria);
		
		return workflowPrivilegeList;
	}

	public List<WorkflowPrivilege> listOnPage(CriteriaMap workflowPrivilegeCriteria) {
		PagingUtils.paging(workflowPrivilegeCriteria,0);
		List<WorkflowPrivilege> workflowPrivilegeList = workflowPrivilegeDao.list(workflowPrivilegeCriteria);
		
		return workflowPrivilegeList;
	}

	@Override
	public int count(CriteriaMap workflowPrivilegeCriteria) {
		return workflowPrivilegeDao.count(workflowPrivilegeCriteria);
	}

	

}
