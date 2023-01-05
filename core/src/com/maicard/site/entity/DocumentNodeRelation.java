package com.maicard.site.entity;

 
import com.maicard.core.entity.BaseEntity;

public class DocumentNodeRelation extends BaseEntity {

	private static final long serialVersionUID = 1L;


	private long udid;

	private long nodeId;

	private int currentStatus;

	
	public DocumentNodeRelation() {
	}


	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
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
		final DocumentNodeRelation other = (DocumentNodeRelation) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"documentNodeRelationId=" + "'" + id + "'" + 
			")";
	}

	public long getUdid() {
		return udid;
	}

	public void setUdid(long udid) {
		this.udid = udid;
	}
	
}
