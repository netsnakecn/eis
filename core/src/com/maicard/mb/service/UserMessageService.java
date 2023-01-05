package com.maicard.mb.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.mb.entity.UserMessage;
 

public interface UserMessageService {

	int insert(UserMessage userMessage);

	int delete(String messageId);
		
	UserMessage select(String messageId);

	List<UserMessage> list(CriteriaMap messageCriteria);

	List<UserMessage> listOnPage(CriteriaMap messageCriteria);
		
	int count(CriteriaMap messageCriteria);
	
	int update(UserMessage userMessage);

	int send(UserMessage userMessage);
	
	int insertLocal(UserMessage userMessage);

	List<String> getUniqueIdentify(long ownerId);

	/**
	 * 读取广播消息，并将获取到的消息设置为已读
	 * 
	 *
	 * @author GHOST
	 * @date 2018-01-10
	 */
	//List<UserMessage> popBroadcastMessage(CriteriaMap userCriteriaMap);

	/**
	 * 根据条件删除某个用户所有订阅消息的关联
	 * 
	 *
	 * @author GHOST
	 * @date 2018-01-12
	 */
	int deleteSubscribe(CriteriaMap userCriteriaMap);

 	
		


}
