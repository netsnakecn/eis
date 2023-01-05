package com.maicard.base;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.maicard.core.service.ApplicationContextService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.core.constants.Constants;
import com.maicard.core.service.ConfigService;


public abstract class BaseController {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected int ROW_PER_PAGE = Constants.DEFAULT_PARTNER_ROWS_PER_PAGE;

	
	@Resource
	protected ConfigService configService;




	protected void sort(CriteriaMap criteriaMap, Class clazz){
		String order = criteriaMap.get("sort");
		if(StringUtils.isBlank(order)){
			return;
		}
		criteriaMap.remove("sort");
		criteriaMap.setOrder(order,clazz);
	}
	
	@PostConstruct
	public void init() {
		ROW_PER_PAGE = configService.getIntValue("ROW_PER_PAGE", 0);
		if (ROW_PER_PAGE < 1) {
			ROW_PER_PAGE = Constants.DEFAULT_PARTNER_ROWS_PER_PAGE;
		}
	}


}
