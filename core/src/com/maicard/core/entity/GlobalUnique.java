package com.maicard.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GlobalUnique extends BaseEntity {


	@JsonIgnore
	public static final String CACHE_PREFIX = "GlobalUnique";
	
	
	private String value;
	private boolean needSave;

	public GlobalUnique() {
	}
	
	public GlobalUnique(String value, long ownerId) {
		this.value = value;
		this.ownerId = ownerId;
		this.needSave = true;
	}
	
	public GlobalUnique(String value, long ownerId, boolean needSave) {
		this.value = value;
		this.ownerId = ownerId;
		this.needSave = needSave;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());

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
		final GlobalUnique other = (GlobalUnique) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"value=" + "'" + value + "'," + 
			"ownerId=" + "'" + ownerId + "'" + 
			")";
	}

	public boolean isNeedSave() {
		return needSave;
	}

	public void setNeedSave(boolean needSave) {
		this.needSave = needSave;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
}
