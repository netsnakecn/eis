package com.maicard.security.entity;

import com.maicard.core.entity.BaseEntity;

/**
 * 菜单与角色[之前的岗位]对应关系
 * 岗位即role中roleType=2的类型为岗位
 * 
 * @author NetSnake
 * @date 2012-9-23
 */
public class MenuRoleRelation extends BaseEntity{

	private static final long serialVersionUID = 1L;

	private int menuRoleRelationId;
	private long roleId;
	private long menuId; 

	public MenuRoleRelation() {
	}

	public MenuRoleRelation(long ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public MenuRoleRelation clone(){
		return (MenuRoleRelation)super.clone();

	}

	public long getMenuId() {
		return menuId;
	}

	public void setMenuId(long menuId) {
		this.menuId = menuId;
	}


	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + menuRoleRelationId;

		return result;
	}


	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MenuRoleRelation other = (MenuRoleRelation) obj;
		if (menuRoleRelationId != other.menuRoleRelationId)
			return false;

		return true;
	}


	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"menuRoleRelationId=" + "'" + menuRoleRelationId + "'," + 
				"menuId=" + "'" + menuId + "'," + 
				"roleId=" + "'" + roleId + "'," + 
				"currentStatus=" + "'" + currentStatus + "'" + 
				")";
	}

	public int getMenuRoleRelationId() {
		return menuRoleRelationId;
	}


	public void setMenuRoleRelationId(int menuRoleRelationId) {
		this.menuRoleRelationId = menuRoleRelationId;
	}


	public long getRoleId() {
		return roleId;
	}


	public void setRoleId(int groupId) {
		this.roleId = groupId;
	}
 


}
