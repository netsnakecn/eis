package com.maicard.tx.service;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.tx.entity.CouponModel;

import java.util.List;


public interface CouponModelService extends GlobalSyncService<CouponModel> {

	CouponModel select(String couponCode, long ownerId);

}
