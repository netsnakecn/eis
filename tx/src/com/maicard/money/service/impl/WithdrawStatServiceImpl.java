package com.maicard.money.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.money.dao.mapper.WithdrawStatMapper;
import com.maicard.money.entity.WithdrawStat;
import com.maicard.money.service.WithdrawStatService;
 

@Service
public class WithdrawStatServiceImpl extends BaseService implements WithdrawStatService {
	@Resource
	private WithdrawStatMapper withdrawStatMapper;
 

	@Override
	public List<WithdrawStat> listOnPage(CriteriaMap withdrawStatCriteria) {

		if(withdrawStatCriteria == null){
			return Collections.emptyList();
		}
		
		return withdrawStatMapper.list(withdrawStatCriteria);
	}
	@Override
	public List<WithdrawStat> list(CriteriaMap withdrawStatCriteria) {
		if(withdrawStatCriteria == null){
			return Collections.emptyList();
		}
		return withdrawStatMapper.list(withdrawStatCriteria);
	}



	@Override
	public int count(CriteriaMap withdrawStatCriteria)
	{
		//查询开始时间
		if(withdrawStatCriteria.get("queryBeginTime") == null){
			//如果没设置起始时间，设置为一周前
			withdrawStatCriteria.put("queryBeginTime",DateUtils.truncate(DateUtils.addDays(new Date(), -7),Calendar.DAY_OF_MONTH));
		}
		return withdrawStatMapper.count(withdrawStatCriteria);
	}
	
	@Override
	public void calculateProfit(){
		withdrawStatMapper.calculateProfit();
		
	}
	@Override
	public void statistic(CriteriaMap withdrawStatCriteria) {
		Assert.notNull(withdrawStatCriteria,"尝试执行统计的条件不能为空");
		Assert.notNull(withdrawStatCriteria.get("queryBeginTime"),"尝试执行统计的开始时间不能为空");
		Assert.notNull(withdrawStatCriteria.get("queryEndTime"),"尝试执行统计的结束时间不能为空");
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.STAT_HOUR_FORMAT);
		withdrawStatCriteria.put("statisticTimeBegin",sdf.format(withdrawStatCriteria.get("queryBeginTime")));
		withdrawStatCriteria.put("statisticTimeEnd",sdf.format(withdrawStatCriteria.get("queryEndTime")));
		logger.info("重新执行以下时间段的统计:" + withdrawStatCriteria.get("statisticTimeBegin") + "=>" + withdrawStatCriteria.get("statisticTimeEnd"));
		withdrawStatMapper.statistic(withdrawStatCriteria);
		
	}

}
