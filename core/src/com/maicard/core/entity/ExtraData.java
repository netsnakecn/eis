package com.maicard.core.entity;

import java.util.Map.Entry;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.views.JsonFilterView;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtraData extends DataDefine{


	
	protected Object dataValue;

	protected  String parentObjectType;

	protected  long parentObjectId;

	public String getParentObjectType() {
		return parentObjectType;
	}

	public void setParentObjectType(String parentObjectType) {
		this.parentObjectType = parentObjectType;
	}

	public long getParentObjectId() {
		return parentObjectId;
	}

	public void setParentObjectId(long parentObjectId) {
		this.parentObjectId = parentObjectId;
	}

	/**
	 * 便于阅读的属性值
	 */
	protected String readableValue;
		
	@JsonView({JsonFilterView.Partner.class})
	protected long uuid;
	
	@JsonView({JsonFilterView.Partner.class})
	protected String tableName;
	
	
	protected long dataDefineId;
	
	//扩展自父类的objectId和objectType
	
	public ExtraData(){
		
	}
	

	@Override
	public String toString(){
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(extraDataId=" + id + ",dataDefineId=" + dataDefineId + ",dataCode=" + dataCode + ",objectId=" + objectId + ",objectType=" + objectType + ",dataValue=" + dataValue + ")";
	}
	
	public ExtraData(String dataCode, Object value){
		this.dataCode = dataCode;
		this.dataValue = value;
	}
	public ExtraData(DataDefine dataDefine) {
		BeanUtils.copyProperties(dataDefine, this);
		this.id = 0;
		this.dataDefineId = dataDefine.id;
	}

	public ExtraData(String dataCode, String dataName, String inputMethod) {
		this.dataCode = dataCode;
		this.dataName = dataName;
		this.inputMethod = inputMethod;
	}


	@SuppressWarnings("unchecked")
	public <T>T getDataValue() {
		try {
			return (T)dataValue;
		}catch(Exception e) {}
		return null;
	}
	public void setDataValue(Object dataValue) {
		if(dataValue == null) {
			this.dataValue = null;
			return;
		}
		if(dataValue instanceof String) {
			this.dataValue = dataValue.toString().trim();
		} else {
			this.dataValue = dataValue;
		}
	}



	public long getUuid() {
		return uuid;
	}

	public void setUuid(long uuid) {
		this.uuid = uuid;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public long getDataDefineId() {
		return dataDefineId;
	}


	public void setDataDefineId(long dataDefineId) {
		this.dataDefineId = dataDefineId;
	}


	public String getReadableValue() {
		return readableValue;
	}


	public void setReadableValue(String readableValue) {
		this.readableValue = readableValue;
	}

	/**
	 * 根据数值设置可读性数值
	 * 
	 * 
	 * @author GHOST
	 * @date 2019-11-20
	 */
	public void applyReadable() {
		if(this.readableValue != null) {
			return;
		}
		if(this.dataValue == null) {
			return;
		}
		if(this.validDataEnum != null && this.validDataEnum.size() > 0) {
			for(Entry<String,String>entry : this.validDataEnum.entrySet()) {
				if(entry.getKey().equalsIgnoreCase(this.dataValue.toString())) {
					this.readableValue = entry.getValue();
					return;
				}
			}
		}
		if(this.dataType != null && this.dataType.toLowerCase().startsWith("bool")) {
			if(this.dataValue.toString().equalsIgnoreCase("true")) {
				this.readableValue = "是";
			} else {
				this.readableValue = "否";
			}
			return;
		}
		this.readableValue = this.dataValue.toString();
		
		
	}

	
	

}
