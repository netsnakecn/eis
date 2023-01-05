package com.maicard.mb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.Operate;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.GlobalOrderIdService;
import com.maicard.mb.annotation.ProcessMessageObject;
import com.maicard.mb.constants.MessageStatus;
import com.maicard.mb.constants.UserMessageSendMethod;
import com.maicard.mb.dao.mapper.UserMessageMapper;
import com.maicard.mb.entity.UserMessage;
import com.maicard.mb.iface.EisMessageListener;
import com.maicard.mb.iface.MessageGateway;
import com.maicard.mb.service.MessageService;
import com.maicard.mb.service.UserMessageService;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.NumericUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
 
@Service
//@ProcessMessageObject("userMessage")
public class UserMessageServiceImpl extends BaseService implements UserMessageService,EisMessageListener {

	@Resource
	private UserMessageMapper userMessageMapper;

	@Resource
	private ApplicationContextService applicationContextService;
	@Resource
	private ConfigService configService; 
	@Resource
	private MessageService messageService;
	@Resource
	private GlobalOrderIdService globalOrderIdService;


	private boolean handlerUserMessageUpdate = false;
	private String[] defaultSendMode;

	private String beanEmailGateway;

//	private int frontUserStartUuid = 100000000;

	@PostConstruct
	public void init(){
		//handlerUserMessageUpdate = configService.getBooleanValue(DataName.handlerUserMessageUpdate.toString(),0);

		String sendArray = configService.getValue(DataName.userMessageDefaultSendMode.toString(),0);
		if(StringUtils.isBlank(sendArray)){
			defaultSendMode = new String[]{UserMessageSendMethod.site.name()};
		} else {
			defaultSendMode = sendArray.split(",");

		}
		beanEmailGateway = configService.getValue(DataName.beanEmailGateway.toString(),0);
	//	frontUserStartUuid = configService.getIntValue(DataName.frontUserStartUuid.toString(),0);
	}

	@Override
	public int insert(UserMessage userMessage) {
		if(userMessage == null){
			return -1;
		}
		if(StringUtils.isBlank(userMessage.getMessageId())){
			generateMessageId(userMessage);
		}
		return insertLocal(userMessage);

		/*if(handlerUserMessageUpdate){
			return insertLocal(userMessage);

		} else {
			return insertRemote(userMessage);
		}*/

	}

	private void generateMessageId(UserMessage userMessage) {
		if(userMessage.getTopicId() > 0){
			userMessage.setMessageId("1" + globalOrderIdService.generate( NumericUtils.parseInt(userMessage.getMessageType())));
		} else {
			userMessage.setMessageId("0" + globalOrderIdService.generate( NumericUtils.parseInt(userMessage.getMessageType())));
		}
	}

	@Override
	public int insertLocal(UserMessage userMessage){
		if(userMessage == null){
			return -1;
		}
		if(userMessage.getSenderStatus() == MessageStatus.queue.id){
			userMessage.setSenderStatus(MessageStatus.sent.id);
		}
		try{
			if( userMessageMapper.insert(userMessage) == 1 ){
				return 1;
			}
		}catch(DataIntegrityViolationException e){
			logger.error("必须的字段不完整，或关联数据不完整，无法插入数据库:" + e.getMessage());
			e.printStackTrace();
		}catch(Exception e){
			logger.error("无法插入消息[" + userMessage.getContent() + "]数据库:" + ExceptionUtils.getFullStackTrace(e));
		}
		return -1;
	}

	/*private int insertRemote(UserMessage userMessage) {
		if(userMessage == null){
			return 0;
		}
		EisMessage m = new EisMessage();
		m.setOperateCode(Operate.create.getId());
		m.setMessageLevel(MessageLevel.user.getCode());
		m.setObjectType(ObjectType.userMessage.toString());
		m.setAttachment(new HashMap<String,Object>());
		m.getAttachment().put("userMessage", userMessage);	
		m.setNeedReply(false);
		try{
			messageService.send(messageBusName, m);
			m = null;
			return 1;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;		
	}
*/
	@Override
	public int delete(String messageId) {
		
		int rs = deleteLocal(messageId);
		if(rs == 1){
			messageService.sendJmsDataSyncMessage(null, "userMessageService", "delete", messageId);
		}
		return rs;
		/*if(handlerUserMessageUpdate){
			return deleteLocal(messageId);
		} else {
			return deleteRemote(messageId);
		}*/
	}

	private int deleteLocal(String messageId){
		CriteriaMap messageCriteria = new CriteriaMap();
		messageCriteria.put("messageId",messageId);
		List<UserMessage> msgList = list(messageCriteria);
		logger.debug("进入deleteLocal,msgList为"+msgList);
		if(msgList == null || msgList.size() != 1){
			return -1;
		}
		return userMessageMapper.delete(messageId);
	}

	/*private int deleteRemote(String messageId) {
		if(messageId == null){
			return 0;
		}
		EisMessage m = new EisMessage();
		m.setOperateCode(Operate.delete.getId());
		m.setObjectType(ObjectType.userMessage.toString());
		m.setAttachment(new HashMap<String,Object>());
		m.getAttachment().put("userMessage", messageId);	
		m.setNeedReply(false);
		try{
			messageService.send(messageBusName, m);
			m = null;
			return 1;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;

	}
*/
	@Override
	public UserMessage select(String messageId) {
		CriteriaMap messageCriteria = new CriteriaMap();
		messageCriteria.put("messageId",messageId);
		List<UserMessage> msgList = list(messageCriteria);
		if(msgList == null || msgList.size() != 1){
			return null;
		}
		UserMessage userMessage = msgList.get(0);
		afterFetch(userMessage);
		return userMessage;

	}

	@Override
	public List<UserMessage> list(CriteriaMap messageCriteria) {
		List<UserMessage> userMessageList= userMessageMapper.list(messageCriteria);
		if(userMessageList == null || userMessageList.size() < 1){
			return Collections.emptyList();
		}	
		for(int i =0; i < userMessageList.size(); i++){
			userMessageList.get(i).setIndex(i+1);
			afterFetch(userMessageList.get(i));
		}
		return userMessageList;
	}

	@Override
	public List<UserMessage> listOnPage(CriteriaMap messageCriteria) {
		List<UserMessage> userMessageList =  userMessageMapper.list(messageCriteria);
		if(userMessageList == null || userMessageList.size() < 1){
			return Collections.emptyList();
		}
		for(int i =0; i < userMessageList.size(); i++){
			userMessageList.get(i).setIndex(i+1);
			afterFetch(userMessageList.get(i));
		}
		return userMessageList;
	}

	@Override
	public int update(UserMessage eisMessage) {
		if(eisMessage == null){
			return -1;
		}
		
		return updateLocal(eisMessage);

		/*if(handlerUserMessageUpdate){
			return updateLocal(eisMessage);
		} else {
			return updateRemote(eisMessage);
		}	*/	
	}

	private int updateLocal(UserMessage eisMessage){
		try{
			return userMessageMapper.update(eisMessage);
		}catch(DataIntegrityViolationException e){
			logger.error("必须的字段不完整，或关联数据不完整，无法更新数据库:" + e.getMessage());
			e.printStackTrace();
		}
		return -1;

	}

	/*private int updateRemote(UserMessage userMessage) {
		if(userMessage == null){
			return 0;
		}
		EisMessage m = new EisMessage();
		m.setOperateCode(Operate.update.getId());
		m.setObjectType(ObjectType.userMessage.toString());
		m.setAttachment(new HashMap<String,Object>());
		m.getAttachment().put("userMessage", userMessage);	
		m.setNeedReply(false);
		try{
			messageService.send(messageBusName, m);
			m = null;
			return 1;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;		
	}
*/

	@Override
	public  int count(CriteriaMap messageCriteria){
		return userMessageMapper.count(messageCriteria);
	}



	@Override
	public int send(UserMessage userMessage){	
		if(StringUtils.isBlank(userMessage.getMessageId())){
			generateMessageId(userMessage);
		}
		if(userMessage.getTopicId() > 0){
			return sendTopic(userMessage);
		} else {
			return sendMsg(userMessage);
		}
		/*if(handlerUserMessageUpdate){
			if( sendLocal(userMessage) == 1){
				messageService.sendJmsDataSyncMessage(messageBusName, "userMessageService", "insertLocal", userMessage);
				return 1;
			}
			return 0;
		} else {
			return sendRemote(userMessage);
		}*/
	}


/*
	private int sendRemote(UserMessage userMessage){
		if(userMessage == null){
			return 0;
		}
		EisMessage m = new EisMessage();
		m.setOperateCode(Operate.sendNotify.getId());
		m.setMessageLevel(MessageLevel.user.getCode());
		m.setObjectType(ObjectType.userMessage.toString());
		m.setAttachment(new HashMap<String,Object>());
		m.getAttachment().put("userMessage", userMessage);	
		m.setNeedReply(false);
		try{
			messageService.send(messageBusName, m);
			return 1;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;		
	}*/



	private int sendMsg(UserMessage eisMessage){

		boolean sendSmsUseAllChannel = true;

		String[] sendMode;
		if(eisMessage.getPerferMethod() == null || eisMessage.getPerferMethod().length < 1){
			sendMode = defaultSendMode;
		} else {
			sendMode= eisMessage.getPerferMethod();
		}
		int sendCount = 0;
		for(String sendMethod : sendMode){
			if(logger.isDebugEnabled()){
				logger.debug("检查发送方式:" + sendMethod);
			}
			if(sendMethod.equals(UserMessageSendMethod.site.toString())){
				if(insert(eisMessage) == 1){
					sendCount++;
				}
			}
			if(sendMethod.equals(UserMessageSendMethod.email.toString())){
				if(StringUtils.isBlank(beanEmailGateway)){
					logger.error("系统未配置短信网关服务");
					continue;
				}
				MessageGateway emailGatewayService = null;
				try{
					emailGatewayService = (MessageGateway)applicationContextService.getBean(beanEmailGateway);
				}catch(Exception e){
				}	
				if(emailGatewayService == null){
					logger.error("系统未部署邮件网关[" + beanEmailGateway + "]");
					continue;
				}
				try{
					emailGatewayService.send(eisMessage);
					sendCount++;
				}catch(Exception e){
					e.printStackTrace();
					logger.error("在使用邮件网关[" + beanEmailGateway + "]发送消息时出现异常:" + e.getMessage());
				}
			}
			if(sendMethod.equals(UserMessageSendMethod.sms.toString())){
				String[] beanNames = applicationContextService.getBeanNamesForType(MessageGateway.class);
				if(beanNames == null || beanNames.length < 1){
					logger.error("系统未配置任何一个短信网关服务");
					continue;
				}
				logger.debug("系统有" + beanNames.length + "个短信网关,是否使用多个网关发送:" + sendSmsUseAllChannel);
				int i = 1;
				for(String smsGatewayName : beanNames){
					MessageGateway smsGatewayService = null;
					try{
						smsGatewayService = applicationContextService.getBeanGeneric(smsGatewayName);
					}catch(Exception e){
					}	
					if(smsGatewayService == null){
						logger.error("系统未部署短信网关[" + smsGatewayName + "]");
						continue;
					}
					logger.debug("准备使用第" + i + "个短信网关" + smsGatewayName + "发送短信");
					try{
						boolean sendSuccess = smsGatewayService.send(eisMessage);
						logger.debug("第" + i + "个短信网关" + smsGatewayName + "发送短信结果:" + sendSuccess);
				
						if(sendSuccess){
							sendCount++;
						}
						if(!sendSmsUseAllChannel && sendSuccess){
							logger.debug("第" + i + "个短信网关" + smsGatewayName + "短信发送完成，并且未设置多重发送，返回");
							break;
						}
					}catch(Exception e){
						logger.error("在使用短信网关[" + smsGatewayName + "]发送消息时出现异常:" + e.getMessage());
					}
					i++;
				}
			}

		}
		return sendCount;
	}



	private int sendTopic(UserMessage userMessage){
		
		 
		return 1;
	}


/*
	private User findReciver(long uuid) {
		User user = null;
		CriteriaMap userDataCriteria = new CriteriaMap();
		userDataCriteria.put("uuid",uuid);
		if(uuid > frontUserStartUuid){
			user =  frontUserDao.select(uuid);
			if(user != null){
 				userDataCriteria.put("userTypeId",UserTypes.frontUser.id);
				user.setUserData(userDataService.map(userDataCriteria));
				return user;
			}
		}
		CriteriaMap userCriteria = new CriteriaMap();
		userCriteria.put("uuids", new long[] {uuid}); 
		if(partnerDao.count(userCriteria) > 0){
			user =  partnerDao.select(uuid);
			if(user != null){
 				userDataCriteria.put("userTypeId",UserTypes.partner.id);
				user.setUserData(userDataService.map(userDataCriteria));
				return user;
			}
		}
		logger.warn("找不到收件人:" + uuid);
		return null;
	}*/

	/* (non-Javadoc)
	 * @see com.maicard.mb.service.EisMessageListener#onMessage(com.maicard.mb.domain.EisMessage)
	 */
	@Override
	@Async
	public void onMessage(EisMessage eisMessage) {
		if(!handlerUserMessageUpdate){
			if(logger.isDebugEnabled()){
				logger.debug("本节点不负责处理用户数据更新，也不负责从数据更新忽略消息[" + eisMessage.getMessageId() + "].");
			}
			eisMessage = null;
			return;
		}
		if(eisMessage.getCode() == 0){
			if(logger.isDebugEnabled()){
				logger.debug("消息操作码为空，忽略消息[" + eisMessage.getMessageId() + "].");
			}
			eisMessage = null;
			return;
		}
		if(eisMessage.getCode() == Operate.create.getId()
				|| eisMessage.getCode() == Operate.delete.getId()
				|| eisMessage.getCode() == Operate.update.getId()
				||  eisMessage.getCode() == Operate.notify.getId()
				){
			try{
				operate(eisMessage);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		} else {			 
			if(logger.isDebugEnabled()){
				logger.debug("消息操作码非法[" + eisMessage.getCode() + "]，忽略消息[" + eisMessage.getMessageId() + "].");
			}
			eisMessage = null;
			return;
		}
		eisMessage = null;		
	}

	private void operate(EisMessage eisMessage) throws Exception{

		UserMessage userMessage = null;

		if(eisMessage.getCode() == Operate.create.getId()){
			try{
				Object object = eisMessage.getExtra("userMessage");
				if(object == null){
					return;
				}
				if(object instanceof UserMessage){
					userMessage = (UserMessage)object;
				}
				if(object instanceof LinkedHashMap){
					ObjectMapper om = JsonUtils.getInstance();
					String textData = null;
					textData = om.writeValueAsString(object);
					userMessage = om.readValue(textData, UserMessage.class);
					if(userMessage == null){
						logger.warn("无法将请求执行的对象转换为UserMessage对象");
						return;
					}

				}

				if(userMessage == null){
					logger.error("得不到消息中的对象[userMessage]");
					return;
				}
				insertLocal(userMessage);
			}catch(Exception e){
				e.printStackTrace();
			}
			eisMessage = null;
			return;
		}
		if(eisMessage.getCode() == Operate.update.getId()){
			try{
				Object object = eisMessage.getExtra("userMessage");
				if(object == null){
					return;
				}
				if(object instanceof UserMessage){
					userMessage = (UserMessage)object;
				}
				if(object instanceof LinkedHashMap){
					ObjectMapper om = JsonUtils.getInstance();
					String textData = null;
					textData = om.writeValueAsString(object);
					userMessage = om.readValue(textData, UserMessage.class);
					if(userMessage == null){
						logger.warn("无法将请求执行的对象转换为UserMessage对象");
						return;
					}

				}

				if(userMessage == null){
					logger.error("得不到消息中的对象[userMessage]");
					return;
				}
				updateLocal(userMessage);
			}catch(Exception e){
				e.printStackTrace();
			}
			eisMessage = null;
			return;
		}
		if(eisMessage.getCode() == Operate.delete.getId()){
			try{
				String messageId = eisMessage.getExtra("userMessage");
				deleteLocal(messageId);
			}catch(Exception e){
				e.printStackTrace();
			}
			return;
		}
		if(eisMessage.getCode() == Operate.notify.getId()){
			try{
				Object object = eisMessage.getExtra("userMessage");
				if(object == null){
					return;
				}
				if(object instanceof UserMessage){
					userMessage = (UserMessage)object;
				}
				if(object instanceof LinkedHashMap){
					ObjectMapper om = JsonUtils.getInstance();
					String textData = null;
					textData = om.writeValueAsString(object);
					userMessage = om.readValue(textData, UserMessage.class);
					if(userMessage == null){
						logger.warn("无法将请求执行的对象转换为UserMessage对象");
						return;
					}

				}

				if(userMessage == null){
					logger.error("得不到消息中的对象[userMessage]");
					return;
				}
				send(userMessage);
			}catch(Exception e){
				e.printStackTrace();
			}
			eisMessage = null;
			return;
		}
		eisMessage = null;
	}

	private void afterFetch(UserMessage userMessage){
		/*try{
			userMessage.setSenderStatusName(MessageStatus.deleted.findById(userMessage.getSenderStatus()).getName());
		}catch(Exception e){}
		try{
			userMessage.setReceiverStatusName(MessageStatus.deleted.findById(userMessage.getReceiverStatus()).getName());
		}catch(Exception e){}*/
	}

	 
	@Override
	public List<String> getUniqueIdentify(long ownerId) {
		CriteriaMap messageCriteria = CriteriaMap.create(ownerId);
		return userMessageMapper.getUniqueIdentify(messageCriteria);
	}
	
	 
	@Override
	public int deleteSubscribe(CriteriaMap userCriteriaMap) {
		return userMessageMapper.deleteSubscribe(userCriteriaMap);
	}
}
