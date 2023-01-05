package com.maicard.site.entity;

import com.maicard.core.entity.BaseEntity;
import com.maicard.mb.annotation.NeedJmsDataSyncP2P;

@NeedJmsDataSyncP2P
public class Tag extends BaseEntity {


	private static final long serialVersionUID = -6087541110562150023L;

	public static final String REDIS_KEY = "HOT_TAG_";

	private String tagName;		//标签的具体内容

	private String tagCode;
		

	private int objectCount;

	private double hits;			//命中率
	
	

	public Tag() {
	}
	
	public Tag(String tagName, long ownerId) {
		this.tagName = tagName;
		this.ownerId = ownerId;
	}


	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getTagCode() {
		return tagCode;
	}

	public void setTagCode(String tagCode) {
		this.tagCode = tagCode;
	}

	public int getObjectCount() {
		return objectCount;
	}

	public void setObjectCount(int objectCount) {
		this.objectCount = objectCount;
	}

	public double getHits() {
		return hits;
	}

	public void setHits(double hits) {
		this.hits = hits;
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
		final Tag other = (Tag) obj;
		if (id != other.id)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"tagId=" + "'" + id + "'" + 
				"tagName=" + "'" + tagName + "'" + 
				")";
	}


}
