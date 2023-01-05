package com.maicard.security.entity;

import com.maicard.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

//权限的关联对象
@Data
@EqualsAndHashCode(callSuper = true)
public class PrivilegeRelation extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private int privilegeRelationId;
	private int privilegeId;
	private String objectType;
	private int objectTypeId;
	private String objectId;

	//非持久化属性		
	
	public PrivilegeRelation() {
	}




	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"privilegeId=" + "'" + privilegeId + "'" + 
				")";
	}




}
