package com.maicard.security.entity;

import com.maicard.core.entity.BaseEntity;

/**
 * 用户与角色的对应关系
 * @author NetSnake
 * @date 2012-9-23
 */
public class UserRoleRelation extends BaseEntity {

	private static final long serialVersionUID = -4862227808490738654L;
	private int userRoleRelationId;
	private long uuid;
	private int roleId;

	public UserRoleRelation() {
	}


	public UserRoleRelation(long ownerId) {
		this.ownerId = ownerId;
	}


	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = (int)roleId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + userRoleRelationId;

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
		final UserRoleRelation other = (UserRoleRelation) obj;
		if (userRoleRelationId != other.userRoleRelationId)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"userRoleRelationId=" + "'" + userRoleRelationId + "'" + 
				")";
	}


	public int getUserRoleRelationId() {
		return userRoleRelationId;
	}


	public void setUserRoleRelationId(int userRoleRelationId) {
		this.userRoleRelationId = userRoleRelationId;
	}


	public long getUuid() {
		return uuid;
	}


	public void setUuid(long uuid) {
		this.uuid = uuid;
	}

}
