package com.maicard.site.entity;

import java.util.Date;

import com.maicard.core.entity.BaseEntity;

public class TagObjectRelation extends BaseEntity {

	private static final long serialVersionUID = 9011122198767839685L;


	private long tagId;

	private String objectType;

	private long objectId;

	private Date createTime;


	private int extraStatus;

	public TagObjectRelation() {
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int)(prime * result + id);

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
		final TagObjectRelation other = (TagObjectRelation) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"tagObjectRelationId=" + "'" + id + "'" + 
			",tagId=" + "'" + tagId + "'" + 
			",objectType=" + "'" + objectType + "'" + 
			",objectId=" + "'" + objectId + "'" + 
			",ownerId=" + "'" + ownerId + "'" + 
			")";
	}



	public long getTagId() {
		return tagId;
	}


	public void setTagId(long tagId) {
		this.tagId = tagId;
	}


	public String getObjectType() {
		return objectType;
	}


	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}


	public long getObjectId() {
		return objectId;
	}


	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public int getExtraStatus() {
		return extraStatus;
	}


	public void setExtraStatus(int extraStatus) {
		this.extraStatus = extraStatus;
	}
	
}
