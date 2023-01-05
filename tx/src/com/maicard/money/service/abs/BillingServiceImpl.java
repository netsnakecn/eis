package com.maicard.money.service.abs;
 
import com.maicard.base.CriteriaMap;
import com.maicard.core.service.abs.AbsBaseService;
import com.maicard.money.constants.SettlementStatus;
import com.maicard.money.constants.TxStatus;
import com.maicard.money.dao.mapper.BillingMapper;
import com.maicard.money.entity.Billing;
import com.maicard.money.service.BillingService;
import com.maicard.money.service.PayService;
import com.maicard.utils.StringTools;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class BillingServiceImpl extends AbsBaseService<Billing, BillingMapper> implements BillingService {


	@Resource
	private PayService payService;



	@Override
	public Billing billing(CriteriaMap billingCriteria) {
		
		
		Date beginTimeBegin = billingCriteria.get("billingBeginTimeBegin");
		Date billingEndTimeEnd = billingCriteria.get("billingEndTimeEnd");
		Assert.notNull(beginTimeBegin,"结算开始日期起始时间beginTimeBegin不能为空");
		Assert.notNull(billingEndTimeEnd,"结算结束日期截至时间endTimeEnd不能为空");
		
		long uuid = billingCriteria.getLongValue("uuid");
		Assert.isTrue(uuid > 0,"结算用户不能为空");
		
		CriteriaMap payCriteria = CriteriaMap.create(billingCriteria.getLongValue("ownerId"));
		payCriteria.put("endTimeBegin",beginTimeBegin);
		payCriteria.put("endTimeEnd", billingEndTimeEnd);
		payCriteria.put("payFromAccount",uuid);
		payCriteria.putArray("currentStatus",TxStatus.success.id);
		payCriteria.put("payMethodId",billingCriteria.getLongValue("objectId"));
		
		String payCardType = billingCriteria.getStringValue("payCardType");
		if(StringUtils.isNotBlank(payCardType)){
			payCriteria.put("payCardType",payCardType);
		}

		int successCount = payService.count(payCriteria);
		if(successCount < 1){
			logger.info("用户:" + uuid + "在时间段[" + StringTools.time2String(beginTimeBegin) + "=>" + StringTools.time2String(beginTimeBegin) + "]期间的成功订单为0，忽略");
			return null;
		}
		Billing billing = new Billing(billingCriteria.getLongValue("ownerId"));
		
		billing.setUuid(uuid);
		billing.setBillingBeginTime(billingCriteria.get("billingBeginTimeBegin"));
		billing.setBillingEndTime(billingCriteria.get("billingEndTimeEnd"));
		billing.setObjectId(billingCriteria.getLongValue("objectId"));
		billing.setShareConfigId(billingCriteria.getLongValue("shareConfigId"));
		billing.setCurrentStatus(SettlementStatus.settled.id);
		int rs = this.insert(billing);
		if(rs != 1){
			logger.error("无法创建结算，因为新增结算数据返回为:" + rs);
			return null;
		}
		
		mapper.billing(billing);
		return select(billing.getId());
		
	}

}
