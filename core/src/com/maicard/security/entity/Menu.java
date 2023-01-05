package com.maicard.security.entity;

import java.util.List;

import com.maicard.core.entity.BaseEntity;


//用户菜单
public class Menu extends BaseEntity{

	private static final long serialVersionUID = 1L;
	
	private String menuName;
	
	private String menuUrl;	
	
	private int parentMenuId;	
	
	private int menuLevel;	
	
	private String parameter;
	
	private int weight;
	
	//对应资源，比如按钮的图标
	private String resourceId;


	//非持久化属性	
	private String parentMenuName;
	private List<Menu> subMenuList;

	
	


	public Menu() {
	}
	
	@Override
	public Menu clone(){
		return (Menu)super.clone();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"menuId=" + "'" + id + "'," + 
			"menuName=" + "'" + menuName + "'," + 
			"parentMenuId=" + "'" + parentMenuId + "'," + 
			"currentStatus=" + "'" + currentStatus + "'" + 
			")";
	}


	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	
	public String getMenuUrl() {
		return menuUrl;
	}

	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
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
		final Menu other = (Menu) obj;
		if (id != other.id)
			return false;

		return true;
	}

	public int getParentMenuId() {
		return parentMenuId;
	}

	public void setParentMenuId(int parentMenuId) {
		this.parentMenuId = parentMenuId;
	}

	public String getParentMenuName() {
		return parentMenuName;
	}

	public void setParentMenuName(String parentMenuName) {
		this.parentMenuName = parentMenuName;
	}

	public int getMenuLevel() {
		return menuLevel;
	}

	public void setMenuLevel(int menuLevel) {
		this.menuLevel = menuLevel;
	}

	public List<Menu> getSubMenuList() {
		return subMenuList;
	}

	public void setSubMenuList(List<Menu> subMenuList) {
		this.subMenuList = subMenuList;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
