package com.maicard.flow.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WorkflowInstance extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private long workflowId = 0;

	private int currentStep = 0;

	private String targetObjectType;

	private int targetObjectTypeId;

	private String objectType;

	private long objectId = 0;

	private Date startTime;

	private Date endTime;

	////////////////////////////
	private List<Route> routeList;

	private Route previewsRoute;
	private List<Route> nextRouteList;

	public WorkflowInstance() {
	}
	public WorkflowInstance(long ownerId) {
		this.ownerId = ownerId;
	}


	public void setTargetObjectType(String targetObjectType) {
		this.targetObjectType = targetObjectType == null ? null : targetObjectType.trim();
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
		final WorkflowInstance other = (WorkflowInstance) obj;
		if (id != other.id)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"workflowInstanceId=" + "'" + id + "'" + 
				",workflowId=" + "'" + workflowId + "'" +
				",currentStep=" + "'" + currentStep + "'" + 
				",object=" + "'" + objectType + ":" + objectId + "'" +
				",currentStatus=" + currentStatus + "'" +
				")";
	}



	public Route getCurrentRoute() {
		if(this.routeList == null || this.routeList.size() < 1){
			return null;
		}
		if(this.currentStep < 1){
			return this.routeList.get(0);
		}
		for(Route r : this.routeList){
			if(r.getStep() == this.currentStep){
				return r;
			}
		}
		return null;
	}


}
