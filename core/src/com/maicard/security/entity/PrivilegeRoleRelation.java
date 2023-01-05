package com.maicard.security.entity;

import com.maicard.core.entity.BaseEntity;
import lombok.Data;


@Data
//权限与角色的对应关系
public class PrivilegeRoleRelation extends BaseEntity {



	private int privilegeRoleRelationId;

	private long privilegeId;

	private long roleId;
	
	public PrivilegeRoleRelation() {
	}

	public PrivilegeRoleRelation(long ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"frontPrivilegeRoleRelationId=" + "'" + privilegeRoleRelationId + "'" + 
			")";
	}




}
