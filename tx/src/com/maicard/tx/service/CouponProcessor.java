package com.maicard.tx.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.EisMessage;
import com.maicard.tx.entity.Coupon;
import com.maicard.tx.entity.CouponModel;

public interface CouponProcessor {
		
	public List<CouponModel> list(CriteriaMap couponModelCriteria, Object parameter);

	/**
	 * 根据条件返回一组coupon<br/>
	 * 如果未能成功获取，则返回一个EisMessage告知失败原因<br/>
	 * 注意！不能返回一个null<br/>
	 * 如果成功，则该EisMessage中放入一个couponList对象，为获取到的coupon列表<br/>
	 * 就算只有单个coupon，也是一个couponList列表对象<br/>
	 * 
	 * @param couponCriteria
	 * @return
	 */
	public EisMessage fetch(CriteriaMap couponCriteria);

	/**
	 * 消费、使用一张卡券，该卡券将被设置为已使用，不能再次使用
	 */
	int consume(Coupon coupon);
	
	public String getProcessorDesc();

	
	//校验一个卡券是否有效，并且是否属于对应用户
	int validate(Coupon coupon);

	/**
	 * 把对应卡券设置为某个用户所有
	 * 状态从新卡券改为锁定中
	 */
	Coupon lock(CriteriaMap couponCriteria);

	/**
	 * 把对应卡券设置为某个用户所有
	 * 状态从新卡券100001改为锁定中710007
	 */
	Coupon lock(Coupon coupon);
}
