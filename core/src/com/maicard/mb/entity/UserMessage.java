/**
 * 
 */
package com.maicard.mb.entity;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;

/**
 * 用户消息
 *
 * @author NetSnake
 * @date 2013-9-12 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserMessage extends BaseEntity{
	private static final long serialVersionUID = 6440459415544919357L;
	
	private String messageId;
	private String replyMessageId;
	private int topicId = 0;
	private long senderId = 0;
	private long receiverId = 0;
	private String title;
	private String content;
	private Date sendTime;
	private Date receiveTime;
	private Date validTime;
	private boolean needReply = false;
	private String[] perferMethod; //首选优先发送方式
	private String sign;	//
	private String senderName;
	private String receiverName;
	private int senderStatus;
	private String senderStatusName;
	private int receiverStatus;
	private String receiverStatusName;

	private String originalType;
	private String messageType;
	private String messageExtraType;

	private String identify;	//该消息可能对应的事件或对象的识别码

	private long inviter;
	 
	private Map<String,Object> attachment;

	//在显示时，预先显示的附加数据
	private String attachmentText;

	public UserMessage() {
 	}	

	public UserMessage(long ownerId) {
		this.ownerId = ownerId;
	}

	public String getReplyMessageId() {
		return replyMessageId;
	}

	public void setReplyMessageId(String replyMessageId) {
		this.replyMessageId = replyMessageId;
	}

	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	public long getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(long receiverId) {
		this.receiverId = receiverId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	public Date getValidTime() {
		return validTime;
	}

	public void setValidTime(Date validTime) {
		this.validTime = validTime;
	}

	public boolean isNeedReply() {
		return needReply;
	}

	public void setNeedReply(boolean needReply) {
		this.needReply = needReply;
	}

	public String[] getPerferMethod() {
		return perferMethod;
	}

	public void setPerferMethod(String... perferMethod) {
		this.perferMethod = perferMethod;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public int getSenderStatus() {
		return senderStatus;
	}

	public void setSenderStatus(int senderStatus) {
		this.senderStatus = senderStatus;
	}

	public int getReceiverStatus() {
		return receiverStatus;
	}

	public void setReceiverStatus(int receiverStatus) {
		this.receiverStatus = receiverStatus;
	}

	public String getSenderStatusName() {
		return senderStatusName;
	}

	public void setSenderStatusName(String senderStatusName) {
		this.senderStatusName = senderStatusName;
	}

	public String getReceiverStatusName() {
		return receiverStatusName;
	}

	public void setReceiverStatusName(String receiverStatusName) {
		this.receiverStatusName = receiverStatusName;
	}

 
	public String getOriginalType() {
		return originalType;
	}

	public void setOriginalType(String originalType) {
		this.originalType = originalType;
	}


	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessageExtraType() {
		return messageExtraType;
	}

	public void setMessageExtraType(String messageExtraType) {
		this.messageExtraType = messageExtraType;
	}

	public String getIdentify() {
		return identify;
	}

	public void setIdentify(String identify) {
		this.identify = identify;
	}

	public long getInviter() {
		return inviter;
	}

	public void setInviter(long inviter) {
		this.inviter = inviter;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}


	public String getAttachmentText() {
		return attachmentText;
	}

	public void setAttachmentText(String attachmentText) {
		this.attachmentText = attachmentText;
	}

	public Map<String, Object> getAttachment() {
		return attachment;
	}

	public void setAttachment(Map<String, Object> attachment) {
		this.attachment = attachment;
	}
}
