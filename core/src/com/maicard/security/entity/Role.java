package com.maicard.security.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


//角色
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Role extends BaseEntity{

	private static final long serialVersionUID = 1L;

	private int parentRoleId;

	private int roleType;//类型:1=组织节点,相当于组,2=节点中的角色，相当于岗位

	/**
	 * 2=商户merchant的角色
	 */
	private int roleLevel;		

	private String roleName;

	private String roleDescription;


	private int flag;

	//非持久化属性	
	private List<Privilege> relatedPrivilegeList;

	@Override
	public Role clone(){
		return (Role)super.clone();
	}


	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"roleId=" + "'" + id + "'," + 
				"roleName=" + "'" + roleName + "'," + 
				"roleType=" + "'" + roleType + "'," + 
				"parentRoleId=" + "'" + parentRoleId + "'," + 
				"currentStatus=" + "'" + currentStatus + "'" + 
				")";
	}


	public int getParentRoleId() {
		return parentRoleId;
	}

	public void setParentRoleId(int parentRoleId) {
		this.parentRoleId = parentRoleId;
	}

	public int getRoleType() {
		return roleType;
	}

	public void setRoleType(int roleType) {
		this.roleType = roleType;
	}

	public int getRoleLevel() {
		return roleLevel;
	}

	public void setRoleLevel(int roleLevel) {
		this.roleLevel = roleLevel;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleDescription() {
		return roleDescription;
	}

	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}


	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}


	public List<Privilege> getRelatedPrivilegeList() {
		return relatedPrivilegeList;
	}

	public void setRelatedPrivilegeList(List<Privilege> relatedPrivilegeList) {
		this.relatedPrivilegeList = relatedPrivilegeList;
	}



}
