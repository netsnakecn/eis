package com.maicard.core.constants;

public enum ObjectType {
	
	/**
	 * 未知
	 */
	unknown,
	
	/**
	 * 活动
	 */
	activity,
	
	/**
	 * 地址本
	 */
	addressBook,
	
	/**
	 * 广告
	 */
	advert,
	
	/**
	 * 审计
	 */
	audit,
	
	/**
	 * 银行帐号
	 */
	bankAccount,
	
	/**
	 * 结算
	 */
	billing,
	
	/**
	 * 业务操作日志
	 */
	operateLog,
	
	/**
	 * 系统配置
	 */
	config, 
	
	/**
	 * 云端用户
	 */
	cloudOwner,
	
	/**
	 * 评论
	 */
	comment,
	
	/**
	 * 数据字典定义
	 */
	dataDefine,
	
	/**
	 * 快递单
	 */
	deliveryOrder,
	
	/**
	 * 快递公司
	 */
	deliveryCompany,
	
	/**
	 * 显示类型
	 */
	displayType,
	
	/**
	 * 文档
	 */
	document,
	
	/**
	 * 文档数据
	 */
	documentData,
	
	/**
	 * 文档类型
	 */
	documentType,
	
	/**
	 * 个人用户
	 */
	frontUser,
	
	/**
	 * 全局唯一
	 */
	globalUnique,
	
	/**
	 * 礼品卡
	 */
	giftCard,
	
	/**
	 * 发票
	 */
	invoice,
	
	/**
	 * 交易品
	 */
	item,/*
	itemStatByInviter("按合作方统计交易品"),
	itemStatByProduct("按产品统计交易品"),
	itemStatByTime("按时间统计交易品"),
	ioServerMap("内外产品服务器映射"),
	ip("IP地址"),
	jmsHeartBeat("JMS心跳"),*/
	
	/**
	 * 工作
	 */
	job,
	
	/**
	 * 语言
	 */
	language,
	
	/**
	 * 登录
	 */
	login,
	//match("匹配交易"),
	
	/**
	 * 消息
	 */
	message,
	
	/**
	 * 消息类型
	 */
	messageType,
	
	/**
	 * 资金
	 */
	money,
	//moneyGift("现金赠送"),
	
	/**
	 * "栏目"
	 */
	node,
	
	/**
	 * 异步通知日志
	 */
	notifyLog,
	
	/**
	 * 订单
	 */
	order,

	/**
	 * 合作伙伴
	 */
	partner,
	
	/**
	 * 合作伙伴权限
	 */
	partnerPrivilege,
	
	/**
	 * 合作伙伴角色
	 */
	partnerRole,
	
	
	/**
	 * 支付订单
	 */
	pay,
	
	
	
	/**
	 * 区域
	 */
	region,
	
	/**
	 * 富文本消息
	 */
	richTextMessage,
	
	/**
	 * 系统服务器
	 */
	server,
	
	/**
	 * 安全级别
	 */
	securityLevel,
	
	/**
	 * 分成配置
	 */
	shareConfig,
	//slowQueue("慢充队列"),
	
	/**
	 * 管理系统菜单
	 */
	partnerMenu,
	
	/**
	 * 标签
	 */
	tag,
	
	/**
	 * 模板
	 */
	template,
	
	//transaction("交易"), 
	
	/**
	 * 用户
	 */
	user,
	
	/**
	 * 用户消息
	 */
	userMessage,
	
	/**
	 * 用户类型
	 */
	userType, 
	//validateCache("数据校验缓存"),
	//stockItem("库存商品"), 
	
	/**
	 * 提现
	 */
	withdraw,
	
	
	
	/**
	 * 签到
	 */
	sign, 
	
	/**
	 * 角色
	 */
	role, 
//	couponExchange("优惠券兑换"), 
	
	
	
	
	/**
	 * REDIS中央缓存
	 */
	redis,
	
	
	/**
	 * 商户
	 */
	merchant,
	

	/**
	 * 好友
	 */
	friend,
	
	/**
	 * 支付方法
	 */
	payMethod,
	
	/**
	 * 付款方法
	 */
	withdrawMethod,
	

	
	
	
	
	
	/**
	 * 充值
	 */
	charge, userRelation;
	/////////////////////// 结束up使用 /////////////////////////
	

	
	@Override
	public String toString(){
		return name();
	}
	
	public ObjectType findByCode(String code){
		for(ObjectType value: ObjectType.values()){
			if(value.name().equals(code)){
				return value;
			}
		}
		return unknown;
	}
	


}