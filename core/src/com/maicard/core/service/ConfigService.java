package com.maicard.core.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.core.entity.Config;


public interface ConfigService extends GlobalSyncService<Config> {

    String getProperty(String key);

    boolean getBoolProperty(String key);

	Config get(String configName, long ownerId);


	boolean getBooleanValue(String configName, long ownerId);

	int getIntValue(String configName, long ownerId);
	
	int getServerId();

	String getSystemCode();

	long getLongValue(String configName, long ownerId);

	String getValue(String string, long ownerId);

	float getFloatValue(String configName, long ownerId);







}
