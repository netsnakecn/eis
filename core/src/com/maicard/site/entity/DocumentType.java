package com.maicard.site.entity;

import java.util.Map;

import com.maicard.core.entity.BaseEntity;
import com.maicard.core.entity.DataDefine;


public class DocumentType extends BaseEntity {

	private static final long serialVersionUID = 1L;

	
	private String documentTypeCode;

	private String documentTypeName;

	private String documentTypeDescription;
	
	private int flag;
	

	//非持久化属性	
	private Map<String,DataDefine> dataDefineMap;
		
	
	private String statusName;

	public DocumentType() {
	}


	public String getDocumentTypeName() {
		return documentTypeName;
	}

	public void setDocumentTypeName(String documentTypeName) {
		this.documentTypeName = documentTypeName;
	}

	public String getDocumentTypeDescription() {
		return documentTypeDescription;
	}

	public void setDocumentTypeDescription(String documentTypeDescription) {
		this.documentTypeDescription = documentTypeDescription;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
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
		final DocumentType other = (DocumentType) obj;
		if (id != other.id)
			return false;

		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"documentTypeId=" + "'" + id + "'" + 
			")";
	}
	
	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public Map<String, DataDefine> getDataDefineMap() {
		return dataDefineMap;
	}

	public void setDataDefineMap(
			Map<String, DataDefine> documentDataDefineMap) {
		this.dataDefineMap = documentDataDefineMap;
	}

	public String getDocumentTypeCode() {
		return documentTypeCode;
	}

	public void setDocumentTypeCode(String documentTypeCode) {
		this.documentTypeCode = documentTypeCode;
	}

}
