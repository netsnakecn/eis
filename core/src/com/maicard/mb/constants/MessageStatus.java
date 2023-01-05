package com.maicard.mb.constants;


//消息状态
public  enum MessageStatus{
	unknown(0),
	/**
	 * ,"新消息"
	 */
	unread(140001),

	/**
	 * ,"草稿"
	 */
	draft(140002),

	/**
	 * ,"已读"
	 */
	readed(140003),

	/**
	 * ,"已删除"
	 */
	deleted(140004),

	/**
	 * ,"已发送"
	 */
	sent(140005),

	/**
	 * ,"等待发送"
	 */
	queue(140006);

	public final int id;
	private MessageStatus(int id){
		this.id = id;
	}
	public MessageStatus findById(int id){
		for(MessageStatus value: MessageStatus.values()){
			if(value.id == id){
				return value;
			}
		}
		return unknown;
	}
}