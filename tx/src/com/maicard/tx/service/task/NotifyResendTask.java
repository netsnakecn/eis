package com.maicard.tx.service.task;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.DataName;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.misc.ThreadHolder;
import com.maicard.money.entity.Pay;
import com.maicard.money.entity.Withdraw;
import com.maicard.money.service.PayService;
import com.maicard.money.service.WithdrawService;
import com.maicard.tx.entity.FailedNotify;
import com.maicard.tx.entity.Item;
import com.maicard.tx.iface.NotifyProcessor;
import com.maicard.tx.service.FailedNotifyService;
import com.maicard.tx.service.ItemService;
import com.maicard.utils.StringTools; 

public class NotifyResendTask extends BaseService{
	
	@Resource
	private FailedNotifyService failedNotifyService;
	
	@Resource
	private PayService payService;
	
	@Resource
	private WithdrawService withdrawService;
	
	@Resource
	private ItemService itemService;
	
	@Resource
	private ConfigService configService;
	
	@Resource
	private ApplicationContextService applicationContextService;
	 
	
	private int maxNotifyCount; //发送异步通知的最大次数

	private int initSendInterval; //初始化发送间隔

	private int maxNotifyMinute;	//发送异步通知的最长时间，早于这个时间的通知，不管是否送达，都不会重新发送  


	@PostConstruct
	public void init(){
		maxNotifyCount = configService.getIntValue(DataName.maxNotifyCount.toString(),0);
		if(maxNotifyCount == 0){
			maxNotifyCount = 30; //默认最多发送通知次数30
		}
		maxNotifyMinute = configService.getIntValue(DataName.maxNotifyMinute.toString(),0);
		if(maxNotifyMinute == 0){
			maxNotifyMinute = 60; //默认发送时间1小时
		}
		initSendInterval = configService.getIntValue(DataName.initNotifySendInterval.toString(),0);
		if(initSendInterval == 0){
			initSendInterval = 60; //默认发送间隔60秒
		} 
	}
	public void run() {
		this.resendFailedNotify();
	}
	/*
	 * 检查未成功发送的通知，并重新发送
	 * 根据最大重发次数和最大重发时间段来判断是否需要重发
	 * 延时78秒后启动，每分钟检查一次
	 */
	
	
	//	@Scheduled(initialDelay= 28 * 1000, fixedRate = 30 * 1000) 
	public void resendFailedNotify(){
		
		CriteriaMap criteria = new CriteriaMap();
		
		List<FailedNotify> failedNotifyList = failedNotifyService.list(criteria);
		if(failedNotifyList == null || failedNotifyList.size() < 1){
			logger.info("系统中没有发送失败的通知");
			return;
		}
		for(FailedNotify failedNotify: failedNotifyList){
			if(failedNotify.getTotalSendCount() > maxNotifyCount){
				logger.info("订单[" + failedNotify.getTransactionId() + "]的通知发送次数[" + failedNotify.getTotalSendCount() + "]已超过系统上限" + maxNotifyCount + ",不再发送，并从失败通知中删除");
				failedNotifyService.delete(failedNotify.getTransactionId());
				continue;
			}
			if((new Date().getTime() - failedNotify.getFirstSendTime().getTime()) / 1000 > maxNotifyMinute * 60){
				logger.info("订单[" + failedNotify.getTransactionId() + "]的初次发送时间[" + StringTools.time2String(failedNotify.getFirstSendTime()) + "]已超过系统规定的最长发送时间" + maxNotifyMinute + "分钟，不再发送，并从失败通知中删除");
				failedNotifyService.delete(failedNotify.getTransactionId());
				continue;
			}

			int notifySendRetryInterval = 0;
			int customNotifyCount = 0;

			if(failedNotify.getObjectType() == null || failedNotify.getObjectType().equalsIgnoreCase("item")){
				//分析订单对应用户的通知规则
				Item item = itemService.select(failedNotify.getTransactionId().trim());
				if(item == null){
					logger.error("找不到失败通知所对应的交易[" + failedNotify.getTransactionId() + "]" );
				} 
				//查找该交易对应的异步通知策略
				customNotifyCount = (int)item.getLongExtra(DataName.maxNotifyCount.name());
				
				notifySendRetryInterval = (int)item.getLongExtra(DataName.notifySendRetryInterval.name());
				

				if(customNotifyCount > 0 && failedNotify.getTotalSendCount() > customNotifyCount){
					logger.info("订单[" + failedNotify.getTransactionId() + "]的通知发送次数[" + failedNotify.getTotalSendCount() + "]已超过自定义发送上限" + customNotifyCount + ",不再发送，并从失败通知中删除");
					failedNotifyService.delete(failedNotify.getTransactionId());
					continue;
				}


				if(notifySendRetryInterval > 0 && failedNotify.getTotalSendCount() > notifySendRetryInterval){
					logger.info("订单[" + failedNotify.getTransactionId() + "]的通知发送次数[" + failedNotify.getTotalSendCount() + "]已超过用户策略上限" + notifySendRetryInterval + ",不再发送，并从失败通知中删除");
					failedNotifyService.delete(failedNotify.getTransactionId());
					continue;
				}

				/*
				 * 发送间隔为初始发送间隔乘以发送总次数
				 * 如果已经发送10次还没成功，那么下一次发送时间间隔应当是10*60=600秒即10分钟
				 */
				int sendInterval = initSendInterval * failedNotify.getTotalSendCount();
				if(failedNotify.getLastSendTime() != null &&  (new Date().getTime() - failedNotify.getLastSendTime().getTime()) / 1000 < sendInterval){
					logger.info("订单[" + failedNotify.getTransactionId() + "]的上次发送时间[" + ThreadHolder.defaultTimeFormatterHolder.get().format(failedNotify.getLastSendTime()) + "]还没达到通知发送间隔秒数[" + sendInterval + "],本次不发送");
					continue;
				}




				NotifyProcessor np = applicationContextService.getBeanGeneric("itemService");
				np.sendNotifySync(item);	
			} else if(failedNotify.getObjectType().equalsIgnoreCase("pay")){
				Pay pay = payService.select(failedNotify.getTransactionId().trim());
				if(pay == null){
					logger.error("找不到失败通知所对应的交易[" + failedNotify.getTransactionId() + "]" );
				}
				


				int sendInterval = initSendInterval * failedNotify.getTotalSendCount();
				if(failedNotify.getLastSendTime() != null &&  (new Date().getTime() - failedNotify.getLastSendTime().getTime()) / 1000 < sendInterval){
					logger.info("订单[" + failedNotify.getTransactionId() + "]的上次发送时间[" + ThreadHolder.defaultTimeFormatterHolder.get().format(failedNotify.getLastSendTime()) + "]还没达到通知发送间隔秒数[" + sendInterval + "],本次不发送");
					continue;
				}
				NotifyProcessor np = applicationContextService.getBeanGeneric("payService");
				np.sendNotifySync(pay);	
			} else {
				//withdraw
				Withdraw withdraw = withdrawService.select(failedNotify.getTransactionId().trim());
				if(withdraw == null){
					logger.error("找不到失败通知所对应的出款交易[" + failedNotify.getTransactionId() + "]" );
					continue;
				}
				


				int sendInterval = initSendInterval * failedNotify.getTotalSendCount();
				if(failedNotify.getLastSendTime() != null &&  (new Date().getTime() - failedNotify.getLastSendTime().getTime()) / 1000 < sendInterval){
					logger.info("订单[" + failedNotify.getTransactionId() + "]的上次发送时间[" + ThreadHolder.defaultTimeFormatterHolder.get().format(failedNotify.getLastSendTime()) + "]还没达到通知发送间隔秒数[" + sendInterval + "],本次不发送");
					continue;
				}
				NotifyProcessor np = applicationContextService.getBeanGeneric("withdrawService");
				np.sendNotifySync(withdraw);	
			}

		}


	}

}
