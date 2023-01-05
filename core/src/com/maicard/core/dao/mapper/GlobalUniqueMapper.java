package com.maicard.core.dao.mapper;


import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.core.entity.GlobalUnique;


public interface GlobalUniqueMapper extends IDao<GlobalUnique> {

	
	boolean exist(GlobalUnique gu) throws Exception;

	boolean create(GlobalUnique globalUnique) throws Exception;
	
	int insertIgnore(GlobalUnique globalUnique);

	int delete(GlobalUnique globalUnique);

}
