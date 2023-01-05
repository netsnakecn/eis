package com.maicard.flow.entity;

import java.util.Date;

import com.maicard.core.entity.BaseEntity;

 
public class WorkflowInstanceLog extends BaseEntity {

	private static final long serialVersionUID = 1L;

 
	private int workflowInstanceId;

	private int step;

	private int priority;

	private String targetObjectType;

	private String targetObjectOperateCode;

	private String targetObjectAttribute;

	private String targetObjectValue;

	private String editable;

	private String processClass;

	private String successPolicy;

	private Date startTime;

	private Date endTime;

	public WorkflowInstanceLog() {
	}
 

	public int getWorkflowInstanceId() {
		return workflowInstanceId;
	}

	public void setWorkflowInstanceId(int workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
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
		this.targetObjectType = targetObjectType;
	}

	public String getTargetObjectOperateCode() {
		return targetObjectOperateCode;
	}

	public void setTargetObjectOperateCode(String targetObjectOperateCode) {
		this.targetObjectOperateCode = targetObjectOperateCode;
	}

	public String getTargetObjectAttribute() {
		return targetObjectAttribute;
	}

	public void setTargetObjectAttribute(String targetObjectAttribute) {
		this.targetObjectAttribute = targetObjectAttribute;
	}

	public String getTargetObjectValue() {
		return targetObjectValue;
	}

	public void setTargetObjectValue(String targetObjectValue) {
		this.targetObjectValue = targetObjectValue;
	}

	public String getEditable() {
		return editable;
	}

	public void setEditable(String editable) {
		this.editable = editable;
	}

	public String getProcessClass() {
		return processClass;
	}

	public void setProcessClass(String processClass) {
		this.processClass = processClass;
	}

	public String getSuccessPolicy() {
		return successPolicy;
	}

	public void setSuccessPolicy(String successPolicy) {
		this.successPolicy = successPolicy;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
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
		final WorkflowInstanceLog other = (WorkflowInstanceLog) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"workflowInstanceLogId=" + "'" + id + "'" + 
			")";
	}
	
}
