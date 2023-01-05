package com.maicard.security.dao.mapper;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.security.entity.OperateLog;


public interface OperateLogMapper extends IDao<OperateLog> {


	List<OperateLog> listOnPage(CriteriaMap operateLogCriteria) throws Exception;
	

	int clearOldLog(CriteriaMap operateLogCriteria);


}
