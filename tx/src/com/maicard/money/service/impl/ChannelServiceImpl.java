package com.maicard.money.service.impl;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.maicard.base.BaseService;
import com.maicard.money.entity.Pay;
import com.maicard.money.entity.PayChannelMechInfo;
import com.maicard.money.entity.PayMethod;
import com.maicard.money.entity.Withdraw;
import com.maicard.money.entity.WithdrawMethod;
import com.maicard.money.service.ChannelService;
import com.maicard.money.service.PayMethodService;
import com.maicard.money.service.WithdrawMethodService;
import com.maicard.utils.JsonUtils;

@Service
public class ChannelServiceImpl extends BaseService implements ChannelService {

	@Resource
	private PayMethodService payMethodService;

	@Resource
	private WithdrawMethodService withdrawMethodService;

	@Override
	public PayChannelMechInfo getChannelInfo(Pay pay) {
		Assert.notNull(pay,"尝试获取支付参数的pay实例不能为空");
		PayMethod payMethod = pay.getPayMethod();
		logger.debug("尝试获取支付参数的pay:{}已存在的payMethod={},对应的payMethodId={}", pay.getTransactionId(),payMethod, pay.getPayMethodId());
		if(payMethod == null && pay.getPayMethodId() > 0){
			payMethod = payMethodService.select(pay.getPayMethodId());
		}
		Assert.notNull(payMethod,"尝试获取支付参数的pay实例中的payMethod不能为空");
		logger.debug("当前的支付方式实例是:" + JsonUtils.toStringApi(payMethod));
		PayChannelMechInfo payChannelMechInfo = new PayChannelMechInfo();
		
		if(payMethod.getData() == null || payMethod.getData().size() < 1) {
			logger.error("支付订单:{}对应的payMethod:{}没有任何扩展配置", pay.getTransactionId(), payMethod.getId());
			return null;
		}
		
		//使用反射来设置属性
		boolean useReflect = true;
		for(Entry<String,Object> entry : payMethod.getData().entrySet()) {
			Field field = null;
			try {
				field = PayChannelMechInfo.class.getDeclaredField(entry.getKey());

				if(field == null) {
					logger.debug("在类:" + PayChannelMechInfo.class.getSimpleName() + "中找不到指定的属性:" + entry.getKey());
					continue;
				}
				field.set(payChannelMechInfo, entry.getValue());
				logger.debug("设置PayChannelMechInfo的属性:" + entry.getKey() + "=>" + entry.getValue());
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
		
		if(useReflect) {
			return payChannelMechInfo;
		}

		String key = payMethod.getExtra("accountId");
		if(key != null){
			payChannelMechInfo.accountId = key.trim();
		} else {
			logger.warn("找不到ownerId=" + payMethod + "的配置参数accountId");
			//return null;
		}

		String accountName = payMethod.getExtra("accountName");
		if(accountName != null){
			payChannelMechInfo.accountName = accountName.trim();
		} else {
			logger.warn("找不到ownerId=" + payMethod + "的配置参数accountName");
			//return null;
		}
		String payKey = payMethod.getExtra("payKey");
		if(payKey != null){
			payChannelMechInfo.payKey = payKey.trim();
		} else {
			logger.error("找不到ownerId=" + payMethod + "的配置参数payKey");
			return null;
		}
		String cryptKey = payMethod.getExtra("cryptKey");
		if(cryptKey != null){
			payChannelMechInfo.cryptKey = cryptKey.trim();
		}

		String submitUrl = payMethod.getExtra("submitUrl");
		if(submitUrl != null){
			payChannelMechInfo.submitUrl = submitUrl.trim();
		} else {
			logger.error("找不到[" + payMethod + "]的配置参数submitUrl");
			return null;
		}
		String queryUrl = payMethod.getExtra("queryUrl");
		if(queryUrl != null){
			payChannelMechInfo.queryUrl = queryUrl.trim();
		} else {
			logger.error("找不到[" + payMethod + "]的配置参数queryUrl，无法进行查询");
		}

		payChannelMechInfo.setInProgressWhenSubmitFail = String.valueOf(payMethod.getBooleanExtra("setInProgressWhenSubmitFail"));
		/*
			synchronized(this){
				mechCache.put(String.valueOf(ownerId), heepayMechInfo);
			}*/
		//logger.debug("把ownerId=" + payMethod + "的汇付宝商户信息:" + heepayMechInfo + "放入缓存");



		return payChannelMechInfo;
	}

	@Override
	public PayChannelMechInfo getChannelInfo(Withdraw withdraw) {
		Assert.notNull(withdraw,"尝试获取支付参数的withdraw实例不能为空");
		WithdrawMethod withdrawMethod = withdraw.getWithdrawMethod();
		if(withdrawMethod == null && withdraw.getWithdrawMethodId() > 0){
			withdrawMethod = withdrawMethodService.select(withdraw.getWithdrawMethodId());
		}
		Assert.notNull(withdrawMethod,"尝试获取支付参数的withdraw实例中的withdrawMethod不能为空,withdrawTypeId=" + withdraw.getWithdrawTypeId() + ",withdrawMethodId=" + withdraw.getWithdrawMethodId());

		if(logger.isDebugEnabled())logger.debug("提现订单:" + withdraw.getTransactionId() + "对应的提现请求的withdrawMethod是:" + JsonUtils.toStringApi(withdrawMethod));
		PayChannelMechInfo payChannelMechInfo = new PayChannelMechInfo();


		if(withdrawMethod.getData() == null || withdrawMethod.getData().size() < 1) {
			logger.error("提现订单:{}对应的withdrawMethod:{}没有任何扩展配置", withdraw.getTransactionId(), withdrawMethod.getId());
			return null;
		}
		
		//使用反射来设置属性
		boolean useReflect = true;
		for(Entry<String,Object> entry : withdrawMethod.getData().entrySet()) {
			Field field = null;
			try {
				field = PayChannelMechInfo.class.getDeclaredField(entry.getKey());

				if(field == null) {
					logger.debug("在类:" + PayChannelMechInfo.class.getSimpleName() + "中找不到指定的属性:" + entry.getKey());
					continue;
				}
				field.set(payChannelMechInfo, entry.getValue());
				logger.debug("设置PayChannelMechInfo的属性:" + entry.getKey() + "=>" + entry.getValue());
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}
		
		if(useReflect) {
			return payChannelMechInfo;
		}

		String key = withdrawMethod.getExtra("accountId");
		if(key != null){
			payChannelMechInfo.accountId = key.trim();
		} else {
			logger.error("找不到ownerId=" + withdrawMethod + "的配置参数accountId");
			return null;
		}

		String accountName = withdrawMethod.getExtra("accountName");
		if(accountName != null){
			payChannelMechInfo.accountName = accountName.trim();
		} else {
			logger.warn("找不到ownerId=" + withdrawMethod + "的配置参数accountName");
			//return null;
		}
		String smallWithdrawUrl = withdrawMethod.getExtra("smallWithdrawUrl");
		if(smallWithdrawUrl != null){
			payChannelMechInfo.smallWithdrawUrl = smallWithdrawUrl.trim();
		} else {
			logger.error("找不到ownerId=" + withdrawMethod + "的配置参数smallWithdrawUrl");
			return null;
		}

		String bigWithdrawUrl = withdrawMethod.getExtra("bigWithdrawUrl");
		if(bigWithdrawUrl != null){
			payChannelMechInfo.bigWithdrawUrl = bigWithdrawUrl.trim();
		} else {
			logger.warn("找不到ownerId=" + withdrawMethod + "的配置参数bigWithdrawUrl");
		}

		String withdrawKey = withdrawMethod.getExtra("withdrawKey");
		if(withdrawKey != null){
			payChannelMechInfo.withdrawKey = withdrawKey.trim();
		} else {
			logger.warn("找不到ownerId=" + withdrawMethod + "的配置参数withdrawKey");
		}

		String withdrawCryptKey = withdrawMethod.getExtra("withdrawCryptKey");
		if(withdrawCryptKey != null){
			payChannelMechInfo.withdrawCryptKey = withdrawCryptKey.trim();
		} else {
			logger.warn("找不到ownerId=" + withdrawMethod + "的配置参数withdrawCryptKey");
		}

		String withdrawQueryUrl = withdrawMethod.getExtra("withdrawQueryUrl");
		if(withdrawQueryUrl != null){
			payChannelMechInfo.withdrawQueryUrl = withdrawQueryUrl.trim();
		} else {
			logger.warn("找不到ownerId=" + withdrawMethod + "的配置参数withdrawQueryUrl");
		}

		String peerPublicKey = withdrawMethod.getExtra("peerPublicKey");
		if(withdrawQueryUrl != null){
			payChannelMechInfo.peerPublicKey = peerPublicKey.trim();
		}
		/*
			synchronized(this){
				mechCache.put(String.valueOf(ownerId), heepayMechInfo);
			}
			logger.debug("把ownerId=" + ownerId + "的汇付宝商户信息:" + heepayMechInfo + "放入缓存");
		 */

		return payChannelMechInfo;
	}

}
