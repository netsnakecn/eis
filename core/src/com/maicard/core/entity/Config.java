package com.maicard.core.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Config extends BaseEntity {


	private String configName;

	private String configValue;

	private String configDescription;

	private int flag;

	private int serverId = 0;

	private String category;	

	private String categoryDescription;

	private String displayLevel;

	public String getDisplayLevel() {
		return displayLevel;
	}

	public void setDisplayLevel(String displayLevel) {
		this.displayLevel = displayLevel;
	}

	public Config() {
	}

	public Config(long ownerId) {
		this.ownerId = ownerId;
	}
	
	public Config(String configName, String configValue, long ownerId) {
		this.configName = configName;
		this.configValue = configValue;
		this.configDescription = configValue;
		this.ownerId = ownerId;
	}

	 


	public void setConfigValue(String configValue) {
		this.configValue = configValue == null ? null : configValue.trim();
	}




	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"configId=" + "'" + id + "'," + 
				"configName=" + "'" + configName + "'," + 
				"configValue=" + "'" + configValue + "'," + 
				"serverId=" + "'" + serverId + "'," + 
				"ownerId=" + "'" + ownerId + "'" + 
				")";
	}


	public void setConfigDescription(String configDescription) {
		this.configDescription = configDescription == null ? null : configDescription.trim();
	}


}
