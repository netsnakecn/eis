package com.maicard.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maicard.core.entity.BaseEntity;
 
/*
 * 权限
 * 权限的对象分为三个层次：
 * 1、要访问的对象类型，比如是产品product，还是文档document，还是节点node
 * 2、要访问的对象个体，也就是产品ID、文档ID或节点ID
 * 3、要访问的对象属性和值，比如产品的productName，或文档的currentStatus，或节点的nodeAlias
 * 2和3是平等的关系
 * 
 * 权限需要进行的操作：
 * *：所有操作权限
 * r：所有读取权限，包括list和get
 * w：所有写入权限，包括create、update和delete
 * create：创建新对象
 * update：更新已存在的对象，如需进一步限定能更新的属性，可使用objectAttributePattern
 * delete：删除对象
 * list：列出对象列表
 * get：列出对象数据
 */
public class Privilege extends BaseEntity  {

	private static final long serialVersionUID = 1070800943736441767L;
	private int parentPid;
	private String operateCode;		//权限进行的操作，包括*（所有操作权限）、r（所有读取权限）、w（所有写入权限）、create、update、delete、list、get

	private String privilegeName;

	private String privilegeDesc;

	private String objectTypeCode;		//权限匹配的对象代码，如product、document、node

	private String objectList;		//权限匹配的对象ID列表，如果是product，就是一个product的ID list，以,分割的字符串列表，如果第一个元素是-8，表示*

	private String objectAttributePattern; //权限匹配的对象的属性，如product对象的currentStatus，document的title等

	private String matchPattern;		//匹配方法，保留未使用

	private boolean recursive;		//是否覆盖下级权限，保留未使用

	private boolean inherit;		//是否继承上级权限，保留未使用

	private int flag;



	@Override
	public Privilege clone(){
		return (Privilege)super.clone();
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"privilegeId=" + "'" + id + "'," + 
				"privilegeName=" + "'" + privilegeName + "'," + 
				"objectCodeType=" + "'" + objectTypeCode + "'," + 
				"parentPid=" + "'" + parentPid + "'," + 
				"currentStatus=" + "'" + currentStatus + "'" + 
				")";
	}
 
	public int getParentPid() {
		return parentPid;
	}

	public void setParentPid(int parentPid) {
		this.parentPid = parentPid;
	}

	public String getPrivilegeName() {
		return privilegeName;
	}

	public void setPrivilegeName(String privilegeName) {
		this.privilegeName = privilegeName;
	}

	public String getPrivilegeDesc() {
		return privilegeDesc;
	}

	public void setPrivilegeDesc(String privilegeDesc) {
		this.privilegeDesc = privilegeDesc;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}





	public String getOperateCode() {
		return operateCode;
	}

	public void setOperateCode(String operateCode) {
		if(operateCode == null || operateCode.trim().equals("")){
			return;
		}
		this.operateCode = operateCode;
	}

	public String getObjectTypeCode() {
		return objectTypeCode;
	}

	public void setObjectTypeCode(String objectTypeCode) {
		this.objectTypeCode = objectTypeCode;
	}

	public String getObjectList() {
		return objectList;
	}

	public void setObjectList(String objectList) {
		this.objectList = objectList;
	}

	public String getMatchPattern() {
		return matchPattern;
	}

	public void setMatchPattern(String matchPattern) {
		this.matchPattern = matchPattern;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public boolean isInherit() {
		return inherit;
	}

	public void setInherit(boolean inherit) {
		this.inherit = inherit;
	}

	public String getObjectAttributePattern() {
		return objectAttributePattern;
	}

	public void setObjectAttributePattern(String objectAttributePattern) {
		this.objectAttributePattern = objectAttributePattern;
	}

}
