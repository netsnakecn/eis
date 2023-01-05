package com.maicard.money.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IService;
import com.maicard.core.entity.EisMessage;
import com.maicard.money.entity.ShareConfig;
 

public interface ShareConfigService extends IService<ShareConfig> {


	List<ShareConfig> listOnPage(CriteriaMap shareConfigCriteria);


	ShareConfig calculateShare(
			CriteriaMap shareConfigCriteria);

	
	ShareConfig calculateShare(Object obj, CriteriaMap shareConfigCriteria);


	EisMessage clone(CriteriaMap shareConfigCriteria);

}
