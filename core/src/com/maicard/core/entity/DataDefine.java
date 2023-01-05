package com.maicard.core.entity;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.views.JsonFilterView;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataDefine extends BaseEntity {


	protected String dataCode;	//数据代码
	
	protected String dataName; //显示名称

	@JsonView(JsonFilterView.Partner.class)
	protected String dataDescription;

	@JsonView(JsonFilterView.Partner.class)
	protected String dataType;	//数据类型，如String

	@JsonView(JsonFilterView.Partner.class)
	protected int displayWeight; //显示时的权重，数值越大越靠前

	@JsonView(JsonFilterView.Partner.class)
	protected String inputLevel;		//从哪个级别输入
	
	@JsonView(JsonFilterView.Partner.class)
	protected String displayLevel; //哪个级别可以显示

	@JsonView(JsonFilterView.Partner.class)
	protected String inputMethod;	//输入方式

	@JsonView(JsonFilterView.Partner.class)
	protected String objectType;

	@JsonView(JsonFilterView.Partner.class)
	protected long objectId = -1;

	@JsonView(JsonFilterView.Partner.class)
	protected long objectExtraId = -1;

	@JsonView(JsonFilterView.Partner.class)
	protected int ttl = 0; //配置存活时间，秒

	@JsonView(JsonFilterView.Partner.class)
	protected Map<String,String> validDataEnum;  //有效的范围
	
	@JsonView(JsonFilterView.Partner.class)
	protected String compareMode;	//比较模式，等于=、大于>、属于in等
	
	@JsonView(JsonFilterView.Partner.class)
	protected int flag;
	
	/**
	 * 当显示给外界键是用dataCode还是用dataName
	 */
	@JsonView(JsonFilterView.Partner.class)
	protected String displayMode;
		
	
	@JsonView(JsonFilterView.Partner.class)
	protected String defaultValue;

	
	@JsonView(JsonFilterView.Partner.class)
	protected boolean readonly;			//是否不可写入

	@JsonView(JsonFilterView.Partner.class)
	protected boolean hidden;				//是否不可显示

	@JsonView(JsonFilterView.Partner.class)
	protected boolean required;			//是否必须输入

	@JsonView(JsonFilterView.Partner.class)
	protected String category;

	public DataDefine() {
	}

	

	public String getDataCode() {
		return dataCode;
	}

	public void setDataCode(String dataCode) {
		if(dataCode != null && !dataCode.trim().equals("")){
			this.dataCode = dataCode.trim();
		}
	}


	public void setDataDescription(String dataDescription) {
		if(dataDescription != null && !dataDescription.trim().equals("")){
			this.dataDescription = dataDescription.trim();
		}
	}


	public void setDataType(String dataType) {
		if(dataType != null && !dataType.trim().equals(""))
			this.dataType = dataType.trim();
	}


	public void setInputMethod(String inputMethod) {
		if(inputMethod != null && !inputMethod.trim().equals(""))
		this.inputMethod = inputMethod.trim();
	}



	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"id=" + "'" + id + "'" + 
				"dataCode=" + "'" + dataCode + "'" + 
				")";
	}



	public void setInputLevel(String inputLevel) {
		if(inputLevel != null && !inputLevel.trim().equals(""))
			this.inputLevel = inputLevel.trim();
	}


	public void setDisplayWeight(int displayWeight) {
 			this.displayWeight = displayWeight;
	}


	public void setObjectType(String objectType) {
		if(objectType != null && !objectType.trim().equals(""))
		this.objectType = objectType.trim();
	}


	public String getDisplayLevel() {
		if(displayLevel == null){
			return "";
		}
		return displayLevel;
	}

	public void setDisplayLevel(String displayLevel) {
		this.displayLevel = displayLevel;
	}

	public String getCompareMode() {
		return compareMode;
	}

	public void setCompareMode(String compareMode) {
		this.compareMode = compareMode;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}


	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}



	



	public long getObjectId() {
		return objectId;
	}



	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}



	public long getObjectExtraId() {
		return objectExtraId;
	}



	public void setObjectExtraId(long objectExtraId) {
		this.objectExtraId = objectExtraId;
	}



	public int getTtl() {
		return ttl;
	}



	public void setTtl(int ttl) {
		this.ttl = ttl;
	}



	public boolean isReadonly() {
		return readonly;
	}



	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}



	public boolean isHidden() {
		return hidden;
	}



	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}



	public boolean isRequired() {
		return required;
	}



	public void setRequired(boolean required) {
		this.required = required;
	}



	public String getDisplayMode() {
		return displayMode;
	}



	public void setDisplayMode(String displayMode) {
		this.displayMode = displayMode;
	}


}
