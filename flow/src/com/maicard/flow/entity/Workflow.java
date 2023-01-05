package com.maicard.flow.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Workflow extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private String objectType;
	private String workflowName;

	private String workflowDesc;

	private String targetObjectType;

	private String targetObjectOperateCodeList;
	
	//////////////////////////////////////////////////
	private List<Route> routeList;

	public Workflow() {
	} 

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName == null ? null : workflowName.trim();
	}

	public String getWorkflowDesc() {
		return workflowDesc;
	}

	public void setWorkflowDesc(String workflowDesc) {
		this.workflowDesc = workflowDesc == null ? null : workflowDesc.trim();
	}

	public String getTargetObjectType() {
		return targetObjectType;
	}

	public void setTargetObjectType(String targetObjectType) {
		this.targetObjectType = targetObjectType == null ? null : targetObjectType.trim();
	}

	public String getTargetObjectOperateCodeList() {
		return targetObjectOperateCodeList;
	}

	public void setTargetObjectOperateCodeList(String targetObjectOperateCodeList) {
		this.targetObjectOperateCodeList = targetObjectOperateCodeList == null ? null : targetObjectOperateCodeList.trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)id;

		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Workflow other = (Workflow) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"workflowId=" + "'" + id + "'" + 
			")";
	}

	public List<Route> getRouteList() {
		return routeList;
	}

	public void setRouteList(List<Route> routeList) {
		this.routeList = routeList;
	}
	
}
