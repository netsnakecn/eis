package com.maicard.flow.service.impl;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.DataName;
import com.maicard.core.service.ConfigService;
import com.maicard.flow.dao.WorkflowDao;
import com.maicard.flow.entity.Workflow;
import com.maicard.flow.service.RouteService;
import com.maicard.flow.service.WorkflowService;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.PagingUtils;
 
@Service
public class WorkflowServiceImpl extends BaseService implements WorkflowService {

	@Resource
	private ConfigService configService;

	@Resource
	private WorkflowDao workflowDao;

	@Resource RouteService routeService;

	//以JSON形式保存在配置中，例如：{"product":["create","update"],"document":["create","update"]}
	HashMap<String,String[]> requireWorkflowConfigMap = new HashMap<String,String[]>();

 


	public int insert(Workflow workflow) {
		return workflowDao.insert(workflow);
	}

	public int update(Workflow workflow) {
		int actualRowsAffected =  workflowDao.update(workflow);
		 

		return actualRowsAffected;
	}

	public int delete(long workflowId) {
		int actualRowsAffected = 0;

		Workflow _oldWorkflow = workflowDao.select(workflowId);

		if (_oldWorkflow != null) {
			actualRowsAffected = workflowDao.delete(workflowId);
		}

		return actualRowsAffected;
	}

	public Workflow select(long workflowId) {
		Workflow workflow =  workflowDao.select(workflowId);
		afterFetch(workflow);
		return workflow;
	}

	public List<Workflow> list(CriteriaMap workflowCriteria) {		
		List<Workflow> workflowList = workflowDao.list(workflowCriteria);
		for(Workflow workflow : workflowList){
			afterFetch(workflow);
		}
		return workflowList;
	}

	public List<Workflow> listOnPage(CriteriaMap criteria) {
		PagingUtils.paging(criteria,0);
		List<Workflow> workflowList = workflowDao.list(criteria);
		for(Workflow workflow : workflowList){
			afterFetch(workflow);
		}
		return workflowList;
	}

	@Override
	public int count(CriteriaMap workflowCriteria) {
		return workflowDao.count(workflowCriteria);
	}

	private void afterFetch(Workflow workflow){
		if(workflow == null){
			return;
		}
		CriteriaMap  routeCriteria = new CriteriaMap();
		routeCriteria.put("workflowId",workflow.getId());
		workflow.setRouteList(routeService.list(routeCriteria));
	}

	@Override
	public boolean requiredWorkflow(String objectType, String action, long ownerId) {
		String requireWorkflowConfig = configService.getValue(DataName.requireWorkflowObject.toString(), ownerId );
		if(requireWorkflowConfig != null){
			ObjectMapper om = JsonUtils.getInstance();
			JavaType javaType = om.getTypeFactory().constructMapType(HashMap.class, String.class, String[].class);   

			try{
				requireWorkflowConfigMap = om.readValue(requireWorkflowConfig, javaType);
			}catch(Exception e){
				logger.error("系统配置的requireWorkflowObject异常:" + ExceptionUtils.getFullStackTrace(e));
			}
		}
		if(requireWorkflowConfigMap == null || requireWorkflowConfigMap.size() < 1){
			logger.warn("系统未定义任何需要工作流处理的对象及操作");
			return false;
		}
		for(String ot : requireWorkflowConfigMap.keySet()){
			if(ot.equalsIgnoreCase(objectType)){
				for(String ac : requireWorkflowConfigMap.get(ot)){
					if(ac.equalsIgnoreCase(action)){
						logger.debug("针对对象[" + objectType + "]进行[" + action + "]操作需要工作流参与");
						return true;
					}
				}
			}
		}
		logger.debug("针对对象[" + objectType + "]进行[" + action + "]操作不需要工作流参与");
		return false;
	}


}
