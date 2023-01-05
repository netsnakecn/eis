package com.maicard.tx.service.impl;

import com.maicard.base.CriteriaMap;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.tx.dao.mapper.CouponModelMapper;
import com.maicard.tx.entity.CouponModel;
import com.maicard.tx.service.CouponModelService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CouponModelServiceImpl extends AbsGlobalSyncService<CouponModel, CouponModelMapper> implements CouponModelService {



	@Override
	public CouponModel select(String couponCode, long ownerId) {
		CriteriaMap couponModelCriteria =  CriteriaMap.create(ownerId);
		couponModelCriteria.put("couponCode",couponCode);
		List<CouponModel> couponModelList = list(couponModelCriteria);
		if(couponModelList == null || couponModelList.size() < 1){
			return null;
		}
		return couponModelList.get(0);
	}


}
