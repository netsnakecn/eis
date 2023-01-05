package com.maicard.core.service;

import java.util.List;
import java.util.Map;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.base.IService;
import com.maicard.core.entity.DataDefine;


public interface DataDefineService extends GlobalSyncService<DataDefine> {

	DataDefine select(String dataCode);

	Map<String, DataDefine> map(CriteriaMap params);

}
