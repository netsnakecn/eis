package com.maicard.tx.service;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.base.IService;
import com.maicard.core.entity.EisMessage;
import com.maicard.security.entity.User;
import com.maicard.tx.entity.Coupon;

public interface CouponService extends GlobalSyncService<Coupon> {

		
	void couponPublish();
	/**
	 * 根据条件返回一组coupon<br/>
	 * 如果未能成功获取，则返回一个EisMessage告知失败原因<br/>
	 * 注意！不能返回一个null<br/>
	 * 如果成功，则该EisMessage中放入一个couponList对象，为获取到的coupon列表<br/>
	 * 就算只有单个coupon，也是一个couponList列表对象<br/>
	 * @param couponCriteria
	 * @return
	 */
	EisMessage fetch(CriteriaMap couponCriteria);


	EisMessage consume(User frontUser, float couponAmount, String couponCode);
}
