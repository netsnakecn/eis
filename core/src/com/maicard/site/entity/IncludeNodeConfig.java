package com.maicard.site.entity;

import com.maicard.core.entity.BaseEntity;

/**
 * 自动包含下级栏目的配置
 *
 *
 * @author NetSnake
 * @date 2016年6月29日
 *
 */
public class IncludeNodeConfig extends BaseEntity{

	private static final long serialVersionUID = 5740670921179591088L;

	private int nodeId;		//包含的栏目ID
	
	private int rows;		//获取该栏目下的文章数量
	
	private String documentTypeCode;	//文章类型代码
	
	private String contextType;		//在哪种应用场景下使用该配置，如移动端还是PC端

	public IncludeNodeConfig(){
		
	}
	
	public IncludeNodeConfig(int nodeId, int rows, long ownerId){
		this.nodeId = nodeId;
		this.rows = rows;
		this.ownerId = ownerId;
	}
	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public String getContextType() {
		return contextType;
	}

	public void setContextType(String contextType) {
		this.contextType = contextType;
	}

	public String getDocumentTypeCode() {
		return documentTypeCode;
	}

	public void setDocumentTypeCode(String documentTypeCode) {
		this.documentTypeCode = documentTypeCode;
	}

}
