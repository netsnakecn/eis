package com.maicard.core.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.maicard.base.CriteriaMap;
import com.maicard.core.dao.mapper.DataDefineMapper;
import com.maicard.core.entity.DataDefine;
import com.maicard.core.service.DataDefineService;


@Service
public class DataDefineServiceImpl extends AbsGlobalSyncService<DataDefine,DataDefineMapper> implements DataDefineService {


	/**
	 * 如果这些基本数据定义没有将自动创建
	 */
	private static final String[] BASIC_DATA_DEFINE = new String[]{"clientIp"};



	@Override
	public DataDefine selectOne( CriteriaMap params) {
		List<Long> idList = mapper.listPk(params);
		if(idList != null) {
			if(idList.size() == 1){
				return mapper.select(idList.get(0));
			} else if(idList.size() > 0 && params.getLongValue("objectId") > 0) {
				List<DataDefine> dataDefineList = this.list(params);
				for(DataDefine  dd : dataDefineList) {
					if(dd.getObjectId() == params.getLongValue("objectId")) {
						return dd;
					}
				}
			}
		} 
		if(params.getStringValue("dataCode") != null && StringUtils.isNotBlank(params.getStringValue("dataCode"))){
			boolean isBasic = false;
			for(String bd :  BASIC_DATA_DEFINE){
				if(bd.equals(params.getStringValue("dataCode"))){
					isBasic = true;
					break;
				}
			}
			if(isBasic){
				DataDefine dd = new DataDefine();
				dd.setDataCode(params.getStringValue("dataCode"));
				dd.setObjectType(params.getStringValue("objectType"));
				dd.setObjectId(params.getLongValue("objectId"));
				int rs = this.insert(dd);
				logger.debug("自动创建新的基本数据定义:" + dd + ",创建结果:" + rs);
				return dd;

			}
		}
		return null;
	}

	@Override
	public DataDefine select(String dataCode) {
		CriteriaMap params = CriteriaMap.create();
		params.put("dataCode",dataCode);
		List<Long> idList = mapper.listPk(params);
		if(idList != null && idList.size() == 1){
			return mapper.select(idList.get(0));
		}
		return null;
	}


	@Override
	public Map<String, DataDefine> map(CriteriaMap params) {
		List<DataDefine> dataDefineList = mapper.list(params);
		if(dataDefineList != null && dataDefineList.size() > 0)	{
			HashMap<String, DataDefine> dataDefineMap = new HashMap<String, DataDefine>();
			for(DataDefine dataDefine : dataDefineList){
				dataDefineMap.put(dataDefine.getDataCode(), dataDefine);
			}
			return dataDefineMap;
		}
		return null;
	}

}
