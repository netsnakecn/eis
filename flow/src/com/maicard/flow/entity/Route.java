package com.maicard.flow.entity;

import java.util.HashMap;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity; 

@JsonIgnoreProperties(ignoreUnknown = true)
public class Route extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private String routeName;

	private int workflowId = 0;

	private int step = 0;
	
	private int nextStep = 0;

	private int priority = 0;

	private String targetObjectType;

	private String targetObjectOperateCode;

	private String targetObjectCheckMode;		//对属性的检查方式，如果是严格模式则除了配置过的属性都不允许输入，如果是宽松模式，则允许输入所有属性仅对已配置属性进行限定
	
	//用于修改当前对象的属性值
	private HashMap<String, Attribute>targetObjectAttributeMap;

	
	private String editable;

	private String postProcess;

	//进行步骤的条件
	private Set<StepCondition> stepConditionSet;

	
	/////////////////////////////////
	private String url;
	private String method;

	public Route() {
	} 

	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName == null ? null : routeName.trim();
	}

	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getTargetObjectType() {
		return targetObjectType;
	}

	public void setTargetObjectType(String targetObjectType) {
		this.targetObjectType = targetObjectType == null ? null : targetObjectType.trim();
	}

	public String getTargetObjectOperateCode() {
		return targetObjectOperateCode;
	}

	public void setTargetObjectOperateCode(String targetObjectOperateCode) {
		this.targetObjectOperateCode = targetObjectOperateCode == null ? null : targetObjectOperateCode.trim();
	}

	public String getEditable() {
		return editable;
	}

	public void setEditable(String editable) {
		this.editable = editable == null ? null : editable.trim();
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
		final Route other = (Route) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"routeId=" + "'" + id + "'" + 
			")";
	}

	public int getNextStep() {
		return nextStep;
	}

	public void setNextStep(int nextStep) {
		this.nextStep = nextStep;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method == null ? null : method.trim();
	}

	public String getTargetObjectCheckMode() {
		return targetObjectCheckMode;
	}

	public void setTargetObjectCheckMode(String targetObjectCheckMode) {
		this.targetObjectCheckMode = targetObjectCheckMode == null ? null : targetObjectCheckMode.trim();
	}

	public HashMap<String, Attribute> getTargetObjectAttributeMap() {
		return targetObjectAttributeMap;
	}

	public void setTargetObjectAttributeMap(HashMap<String, Attribute> targetObjectAttributeMap) {
		this.targetObjectAttributeMap = targetObjectAttributeMap;
	}

	public Set<StepCondition> getStepConditionSet() {
		return stepConditionSet;
	}

	public void setStepConditionSet(Set<StepCondition> stepConditionSet) {
		this.stepConditionSet = stepConditionSet;
	}

	public String getPostProcess() {
		return postProcess;
	}

	public void setPostProcess(String postProcess) {
		this.postProcess = postProcess;
	}

}
