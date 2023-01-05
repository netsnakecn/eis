package com.maicard.core.constants;

/**
 * 用于提供给客户端系统作为路由判断
 *
 *
 * @author NetSnake
 * @date 2016年10月19日
 *
 */
public enum OpCode {
	ADD_TO_CART,				//把商品加入购物车
	CHECK_UPDATE,				//检查更新
	CHANGE_PASSWORD_FROM_SMS,		//通过找回短信修改密码
	LIST_PRODUCT,			//列出产品
	ORDER_SETTLEUP,			//结算指定的订单	
	GET_ORDER,				//查看订单
	RECEIVE_CHAT_MESSAGE,			//收到聊天信息
	SEND_CHAT_MESSAGE,			//发送聊天信息
	SEND_REGISTER_SMS, 	//发送注册短信
	SEND_FIND_PASSWORD_SMS,		//发送找回密码的短信
	USER_REGISTER,			//提交用户注册
	USER_LOGIN,				//用户登录
	USER_LOGOUT,				//用户退出登录
	USER_KICKOUT,				//用户被踢
	GET_USER,				//获取用户数据
	CONFIRM_DELIVERY,			//确认订单已收货
	NOTIFY,					//信息提示
	VOTE,					//投票
	
	UPDATE_ROLENAME,		//修改角色名
	
	/**
	 * 列出可用的扑克模型
	 */
	LIST_MODEL,		
	
	/**
	 * 列出好友模式下的模型
	 */
	LIST_MODEL_PRIVACY,
	
	LIST_SIGN,				//列出签到记录
	LIST_ACTIVITY,			//列出活动配置
	SIGN, 					//签到
	CREATE_ORDER,			//创建一个订单，但不进行后续操作
	
	/**
	 * 列出自己的好友
	 */
	LIST_FRIEND,
	/**
	 * 列出在线用户
	 */
	LIST_ONLINE_USER,
	/**
	 * 添加好友
	 */
	ADD_FRIEND,
	/**
	 * 确认添加好友
	 */
	CONFIRM_FRIEND,
	/**
	 * 列出请求加我为好友的列表
	 */
	LIST_FRIEND_WAIT_CONFIRM,
	/**
	 * 删除好友
	 */
	DELETE_FRIEND,
	/**
	 * 微信注册或更新一个用户
	 */
	WEIXIN_USER_SYNC, 
	DISCONNECT, JOIN_GAME, SEND_BIND_PHONE_SMS, BIND_PHONE,				//断线
	
	

}
