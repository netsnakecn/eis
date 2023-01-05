package com.maicard.tx.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.tx.dao.mapper.ItemStatMapper;
import com.maicard.tx.entity.ItemStat;
import com.maicard.tx.service.ItemStatService;
 

@Service
public class ItemStatServiceImpl extends AbsGlobalSyncService<ItemStat,ItemStatMapper> implements ItemStatService {
 
   
 
	@Override
	public void calculateProfit(){
		mapper.calculateProfit();
		
	}
	@Override
	public void statistic(CriteriaMap itemStatCriteria) {
		Assert.notNull(itemStatCriteria,"尝试执行统计的条件不能为空");
		Assert.notNull(itemStatCriteria.get("queryBeginTime"),"尝试执行统计的开始时间不能为空");
		Assert.notNull(itemStatCriteria.get("queryEndTime"),"尝试执行统计的结束时间不能为空");
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.STAT_HOUR_FORMAT);
		itemStatCriteria.put("statisticTimeBegin",sdf.format(itemStatCriteria.get("queryBeginTime")));
		itemStatCriteria.put("statisticTimeEnd",sdf.format(itemStatCriteria.get("queryEndTime")));
		logger.info("重新执行以下时间段的统计:" + itemStatCriteria.get("statisticTimeBegin") + "=>" + itemStatCriteria.get("statisticTimeEnd"));
		mapper.statistic(itemStatCriteria);
		
	}

}
