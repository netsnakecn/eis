package com.maicard.flow.entity;

import java.io.Serializable;

/**工作流与其他对象的关联关系
 * 任何其他对象的操作,都可以使用工作流进行管理
 * 例如:
 * 交易品（产品），则relatedObjectType==100005(见status_type、status_code表）,relatedObjectId则为产品ID
 * 用户审核，则relatedObjectType==100007，relatedObjectId则可以是空。
 * 
 * @author NetSnake
 * @date 2012-3-27
 */
public class WorkflowRelation implements Serializable {

	private static final long serialVersionUID = 1L;

	private int workflowRelationId;

	private int workflowId;

	private int currentStatus;

	private String statusName;

	private int relatedObjectType;

	private int relatedObjectId;

	//非持久性属性
	private int id;

	private int index;

	public WorkflowRelation() {
	}

	public int getWorkflowRelationId() {
		return workflowRelationId;
	}

	public void setWorkflowRelationId(int workflowRelationId) {
		this.workflowRelationId = workflowRelationId;
	}


	public int getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(int currentStatus) {
		this.currentStatus = currentStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + workflowRelationId;

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
		final WorkflowRelation other = (WorkflowRelation) obj;
		if (workflowRelationId != other.workflowRelationId)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"workflowRelationId=" + "'" + workflowRelationId + "'" + 
				")";
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public int getRelatedObjectType() {
		return relatedObjectType;
	}

	public void setRelatedObjectType(int relatedObjectType) {
		this.relatedObjectType = relatedObjectType;
	}

	public int getRelatedObjectId() {
		return relatedObjectId;
	}

	public void setRelatedObjectId(int relatedObjectId) {
		this.relatedObjectId = relatedObjectId;
	}

	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
