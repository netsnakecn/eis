package com.maicard.flow.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.flow.dao.WorkflowInstanceLogDao;
import com.maicard.flow.entity.WorkflowInstanceLog;
import com.maicard.flow.service.WorkflowInstanceLogService;
import com.maicard.utils.PagingUtils;

@Service
public class WorkflowInstanceLogServiceImpl extends BaseService implements WorkflowInstanceLogService {

	@Resource
	private WorkflowInstanceLogDao workflowInstanceLogDao;

	
	public void insert(WorkflowInstanceLog workflowInstanceLog) {
		workflowInstanceLogDao.insert(workflowInstanceLog);
	}

	public int update(WorkflowInstanceLog workflowInstanceLog) {
		int actualRowsAffected = workflowInstanceLogDao.update(workflowInstanceLog);
		 
		
		return actualRowsAffected;
	}

	public int delete(int workflowInstanceLogId) {
		int actualRowsAffected = 0;
		
		WorkflowInstanceLog _oldWorkflowInstanceLog = workflowInstanceLogDao.select(workflowInstanceLogId);
		
		if (_oldWorkflowInstanceLog != null) {
			actualRowsAffected = workflowInstanceLogDao.delete(workflowInstanceLogId);
		}
		
		return actualRowsAffected;
	}
	
	public WorkflowInstanceLog select(int workflowInstanceLogId) {
		return workflowInstanceLogDao.select(workflowInstanceLogId);
	}

	public List<WorkflowInstanceLog> list(CriteriaMap workflowInstanceLogCriteria) {
		return workflowInstanceLogDao.list(workflowInstanceLogCriteria);
	}
	
	public List<WorkflowInstanceLog> listOnPage(CriteriaMap workflowInstanceLogCriteria) {
		PagingUtils.paging(workflowInstanceLogCriteria,0);
		return workflowInstanceLogDao.list(workflowInstanceLogCriteria);
	}

}
