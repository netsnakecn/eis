package com.maicard.tx.service.impl;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.OpResult;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.*;
import com.maicard.core.service.abs.AbsBaseService;
import com.maicard.mb.service.MessageService;
import com.maicard.money.constants.TxConstants;
import com.maicard.money.constants.TxStatus;
import com.maicard.money.constants.TxType;
import com.maicard.security.entity.User;
import com.maicard.security.service.PartnerService;
import com.maicard.tx.dao.mapper.ItemMapper;
import com.maicard.tx.entity.FailedNotify;
import com.maicard.tx.entity.Item;
import com.maicard.tx.iface.NotifyProcessor;
import com.maicard.tx.service.FailedNotifyService;
import com.maicard.tx.service.ItemService;
import com.maicard.utils.HttpUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ItemServiceImpl extends AbsBaseService<Item, ItemMapper> implements ItemService,NotifyProcessor {

 

	@Resource
	protected ConfigService configService;
	@Resource
	protected DataDefineService dataDefineService;
	@Resource
	protected MessageService messageService;
	@Resource
	protected GlobalOrderIdService globalOrderIdService; 

	@Resource
	protected GlobalUniqueService globalUniqueService;

	@Resource
	protected FailedNotifyService failedNotifyService;


	@Resource
	protected ApplicationContextService applicationContextService;

	@Resource
	protected PartnerService partnerService;
	protected static final String  historyTableName = "item_history";





	protected  static boolean handlerNotify = false;
	



	@Override
	public int insert(Item item){
		if(item == null){
			throw new EisException(EisError.OBJECT_IS_NULL.id,"尝试新增的Item是空");
		}
		if(item.getTransactionTypeId() == 0){
			item.setTransactionTypeId(TxType.buy.id);
		}
		if(StringUtils.isBlank(item.getTransactionId())){
			item.setTransactionId(globalOrderIdService.generate(item.getTransactionTypeId()));
		}
		try{
			if(mapper.insert(item) == 1) {
				return 1;
			}
			return  OpResult.failed.id;

		}catch(Exception e){
			logger.debug("交易[" + item + "]无法插入数据库:" + e.getMessage());
			e.printStackTrace();
			return  OpResult.failed.id;
		}
		 
	}
 
 
  




	@Override
	public Item select(String transactionId) {
		List<Item> list = list(CriteriaMap.create().put("transactionId",transactionId));
		if(list.size() > 0){
			Item item = list.get(0);
			afterFetch(item);
			return item;
		}
		return null;
	}






	@Override
	public Item fetchWithLock(CriteriaMap itemCriteria)  {
		Item item = mapper.fetchWithLock(itemCriteria);
		if(item == null){
			return null;
		}
		afterFetch(item);
		return item;
	}

	/**
	 * 将一个Item的currentStatus设置为afterLockStatus，保证在设置前它的currentStatus必须是beforeLockStatus
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public int lock(Item item){
		return mapper.lock(item);
	}








	@Override
	public int changeStatus(Item item) {
		return mapper.changeStatus(item);
	}







	@Override
	public boolean recycle(Item item){
		return true;
	}

	@Override
	@Async
	public void sendNotifyAsync(BaseEntity obj){
		if(obj == null){
			logger.error("尝试发送异步通知的Item为空");
			return;
		}
		if( !(obj instanceof Item)) {
			logger.error("尝试发送异步通知的对象不是Item");
			return;
		}

		Item item = (Item)obj;
		int sleep = 1;
		/*if(item.getTransactionTypeId() == TransactionType.buy.getId()){
			sleep = sleepSecBeforeSend * 6; 
		} else {
			sleep = sleepSecBeforeSend;
		}*/
		logger.info("交易[" + item.getTransactionId() + "]进入异步通知发送新线程，睡眠" + sleep + "秒后发送通知.");
		try {
			Thread.sleep(sleep * 1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if( !handlerNotify){
			logger.info("本节点不负责发送交易通知，也不负责同步Jms数据，忽略异步通知请求");
			return;
		}

		if(item.getItemData() == null || item.getItemData().size() < 1){
			FailedNotify failedNotify = new FailedNotify(item.getTransactionId());
			failedNotifyService.replace(failedNotify);
			logger.warn("尝试发送异步通知的Item[" + item.getTransactionId() + "]扩展数据为空");
			return;
		}
		if(item.getCurrentStatus() == TxStatus.success.getId()){
			item.setRequestMoney(0f);
		}

		if(item.getSyncFlag() == 0){
			boolean noDistributedNotify = false;
			try{
				noDistributedNotify = Boolean.parseBoolean(item.getItemData().get(DataName.noDistributedNotify.toString()).getDataValue());
			}catch(Exception e){}
			if(noDistributedNotify){
				logger.info("异步通知[" + item.getTransactionId() + "]不需要进行分布式异步通知");				
			}	else {
				logger.info("异步通知[" + item.getTransactionId() + "]需要进行分布式异步通知");
				messageService.sendJmsDataSyncMessage(null, "notifyService", "sendNotify", item);
			}
		} else {
			logger.info("交易[" + item.getTransactionId() + "]的同步标志为1，无需通知其他节点发送异步通知");
		}

		String customNotifyProcessor =   item.getExtra(DataName.notifyProcessor.name());

		NotifyProcessor notifyProcessor = null;
		if(StringUtils.isNotBlank(customNotifyProcessor)){
			notifyProcessor = applicationContextService.getBeanGeneric(customNotifyProcessor);

		}
		String notifyUrl = null;
		try{
			notifyUrl = item.getItemData().get(DataName.payNotifyUrl.toString()).getDataValue();
		}catch(Exception e){}
		if(notifyUrl == null && notifyProcessor == null){
			logger.warn("找不到Item[" + item.getTransactionId() + "]的payNotifyUrl数据，同时也没有自定义通知处理器");
			return;
		}
		int customNotifySendRetry = 0;
		try{
			customNotifySendRetry = Integer.parseInt(item.getItemData().get(DataName.maxNotifyCount.toString()).getDataValue());
		}catch(Exception e){}
		if(customNotifySendRetry <= 0){
			customNotifySendRetry = TxConstants.notifySendRetry;
		}
		int customNotifySendRetryInterval = 0;
		try{
			customNotifySendRetryInterval = Integer.parseInt(item.getItemData().get(DataName.notifySendRetryInterval.toString()).getDataValue());
		}catch(Exception e){}
		if(customNotifySendRetryInterval <= 0){
			customNotifySendRetryInterval = TxConstants.notifySendRetryInterval;
		}

		boolean sendSuccess = false;
		int sendCount = 0;
		for(int i = 0; i < customNotifySendRetry; i++){		
			sendCount = i+1;
			if(logger.isDebugEnabled()){
				logger.debug("第" + (i+1) + "次为交易[" + item.getTransactionId() + "]发送异步通知， 共尝试" + TxConstants.notifySendRetry + "次");
			}
			String result = null;
			if(notifyProcessor != null ){
				logger.info("交易[" + item.getTransactionId() + "]定义了自定义异步通知器[" + customNotifyProcessor + "]，使用该服务发送异步通知");
				result = notifyProcessor.sendNotifySync(item);
			} else { 
				result = sendNotifyCurrent(item);

			}
			logger.debug("第" + (i+1) + "次为交易[" + item.getTransactionId() + "]发送异步通知，结果:" + result);
			if(isValidResponse(item, result)){
				sendSuccess = true;
				break;
			}			
			try {
				Thread.sleep(customNotifySendRetryInterval * 1000 * (i+1));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	

		}
		if(!sendSuccess){
			FailedNotify failedNotify = new FailedNotify(item.getTransactionId());
			failedNotifyService.replace(failedNotify);
			logger.info("交易[" + item.getTransactionId() + "]在尝试" + sendCount + "次后仍然失败，记录为失败通知");
		} else {
			failedNotifyService.delete(item.getTransactionId());
			logger.info("交易[" + item.getTransactionId() + "]在尝试" + sendCount + "次后发送成功，从失败通知中去除");
		}
		return;

	}

	//增加交易的成功金额为指定的锁定金额，并扣除锁定金额

	@Override
	public String sendNotifySync(BaseEntity obj){
		if(obj == null){
			logger.error("尝试发送异步通知的Item为空");
			return null;
		}
		if( !(obj instanceof Item)) {
			logger.error("尝试发送异步通知的对象不是Item");
			return null;
		}

		Item item = (Item)obj;
		if(item.getItemData() == null || item.getItemData().size() < 1){
			logger.warn("尝试发送异步通知的Item[" + item.getTransactionId() + "]扩展数据为空");
			return null;
		}
		if(item.getCurrentStatus() == TxStatus.success.getId()){
			item.setRequestMoney(0f);
		}
		if(item.getSyncFlag() == 0){
			boolean noDistributedNotify = false;
			try{
				noDistributedNotify = Boolean.parseBoolean(item.getItemData().get(DataName.noDistributedNotify.toString()).getDataValue());
			}catch(Exception e){}
			if(noDistributedNotify){
				logger.info("异步通知[" + item.getTransactionId() + "]不需要进行分布式异步通知");				
			}	else {
				logger.info("异步通知[" + item.getTransactionId() + "]需要进行分布式异步通知");
				messageService.sendJmsDataSyncMessage(null, "notifyService", "sendNotify", item);
			}
		} else {
			logger.info("交易[" + item.getTransactionId() + "]的同步标志为1，无需进行异步通知");
		}

		String result = null;
		String customNotifyProcessor = null;
		try{
			customNotifyProcessor = item.getItemData().get(DataName.notifyProcessor.toString()).getDataValue();
		}catch(Exception e){

		}
		NotifyProcessor notifyProcessor = null;
		if(StringUtils.isNotBlank(customNotifyProcessor)){
			try{
				notifyProcessor = applicationContextService.getBeanGeneric(customNotifyProcessor);
			}catch(Exception e){
				logger.error("交易[" + item.getTransactionId() + "]定义了自定义异步通知器[" + customNotifyProcessor + "]，但无法找到该服务");
			}
		}
		if(notifyProcessor != null ){
			logger.info("交易[" + item.getTransactionId() + "]定义了自定义异步通知器[" + customNotifyProcessor + "]，使用该服务发送异步通知");
			result = notifyProcessor.sendNotifySync(item);
		} else {
			String notifyUrl = item.getInNotifyUrl();
			 
			if(notifyUrl == null){
				logger.warn("找不到Item[" + item.getTransactionId() + "]的inNotifyUrl数据");
			}

			result = sendNotifyCurrent(item);

		}
		if(isValidResponse(item, result)){
			failedNotifyService.delete(item.getTransactionId());
			logger.info("交易[" + item.getTransactionId() + "]直接发送成功，从失败通知中去除");
		} else {
			FailedNotify failedNotify = new FailedNotify(item.getTransactionId());
			failedNotifyService.replace(failedNotify);
			logger.info("交易[" + item.getTransactionId() + "]直接发送失败，记录为失败通知");
		}
		return result;
	}

	public static boolean isValidResponse(Item item, String response){
		if(StringUtils.isBlank(response)){
			return false;
		}
		if(response.trim().startsWith(item.getInOrderId()) 
				|| response.trim().equalsIgnoreCase("true") 
				|| response.trim().equalsIgnoreCase("success") 
				|| response.trim().equalsIgnoreCase("ok")){
			return true;
		}
		return false;
	}


	protected String sendNotifyCurrent(Item item){
		if(item == null){
			logger.warn("尝试发送异步通知的Item为空");
			return "尝试发送异步通知的Item为空";
		}
		if(item.getItemData() == null || item.getItemData().size() < 1){
			logger.warn("尝试发送异步通知的Item[" + item.getTransactionId() + "]扩展数据为空");
			return "尝试发送异步通知的Item[" + item.getTransactionId() + "]扩展数据为空";
		}
		String notifyUrl = item.getExtra(DataName.payNotifyUrl.toString());
		if(notifyUrl == null){
			logger.warn("找不到交易[" + item.getTransactionId() + "]的payNotifyUrl数据");
			return "找不到交易[" + item.getTransactionId() + "]的payNotifyUrl数据";
		}
		String timestamp = String.valueOf(new Date().getTime() / 1000);

		NameValuePair[] requestData = { 
				new NameValuePair("transactionId", item.getTransactionId()),
				new NameValuePair("orderId", item.getInOrderId()),
				new NameValuePair("requestMoney ", String.valueOf(item.getLabelMoney() * item.getCount())),
				new NameValuePair("successMoney", String.valueOf(item.getSuccessMoney())),
				new NameValuePair("operateCode", String.valueOf(item.getOutStatus())),
				new NameValuePair("timestamp", timestamp),
				new NameValuePair("sign", generateNotifySign(item, timestamp))
		};
		StringBuffer sb = new StringBuffer();
		for(NameValuePair pair : requestData){
			sb.append(pair.getName() + "=" + pair.getValue() + "&");
		}
		logger.info("尝试为交易[" + item.getTransactionId() + "]发送异步通知到:" + notifyUrl + "?" + sb.toString());


		String result = null;

		try{
			result = HttpUtils.postData(notifyUrl, requestData);
		}catch(Exception e){
			logger.warn("在发送交易[" + item.getTransactionId() + "]异步通知时发生异常:" + e.getMessage());
			return e.getMessage();
		}
		logger.info("交易[" + item.getTransactionId() + "]通知发送结果:" + result);

		return result;
	}



	protected String generateNotifySign(Item item, String timestamp) {
		if(item == null){
			return null;
		}
		User partner = partnerService.select(item.getChargeFromAccount());
		if(partner == null){
			return null;
		}
		String key =   partner.getExtra(DataName.supplierLoginKey.name());
		if(key == null){
			logger.error("合作伙伴[" + partner.getUuid() + "]未配置supplierLoginKey");
			return null;
		}
		try{
			String src = partner.getUuid() + "|" 
					+ item.getInOrderId() + "|"
					+ timestamp;
			src = src + "|" + key;
			String md5 = DigestUtils.md5Hex(src).toLowerCase();
			if(logger.isDebugEnabled()){
				logger.debug("生成异步通知校验，源字符串[" + src + "]的MD5结果" + md5);
			}
			return md5;

		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}





}



