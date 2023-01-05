package com.maicard.money.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsBaseService;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.money.dao.mapper.PayStatMapper;
import com.maicard.money.entity.PayStat;
import com.maicard.money.service.PayStatService;
 

@Service
public class PayStatServiceImpl extends AbsBaseService<PayStat,PayStatMapper> implements PayStatService {
 
  

 
	
	@Override
	public void calculateProfit(){
		mapper.calculateProfit();
		
	}
	@Override
	public void statistic(CriteriaMap payStatCriteria) {
		Assert.notNull(payStatCriteria,"尝试执行统计的条件不能为空");
		Assert.notNull(payStatCriteria.get("queryBeginTime"),"尝试执行统计的开始时间不能为空");
		Assert.notNull(payStatCriteria.get("queryEndTime"),"尝试执行统计的结束时间不能为空");
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.STAT_HOUR_FORMAT);
		payStatCriteria.put("statisticTimeBegin",sdf.format(payStatCriteria.get("queryBeginTime")));
		payStatCriteria.put("statisticTimeEnd",sdf.format(payStatCriteria.get("queryEndTime")));
		logger.info("重新执行以下时间段的统计:" + payStatCriteria.get("statisticTimeBegin") + "=>" + payStatCriteria.get("statisticTimeEnd"));
		mapper.statistic(payStatCriteria);
		
	}


}
