package com.maicard.core.constants;

/**
 * 标准错误码,从500001到509999
 * @author GHOST
 * @date 2012-12-02
 *
 */
public enum EisError {
	/**
	 * "未知代码"
	 */
	UNKNOWN(0),
	
	/**
	 * "没有找到起码一个关联节点"
	 */
	NO_RELATED_NODE(500001),
	
	/**
	 * ,"没有找到起码一个工作流"
	 */
	NO_WORKFLOW(500002),
	
	/**
	 * ,"对象不允许被编辑"
	 */
	OBJECT_NOT_EDITABLE(500003),
	
	/**
	 * ,"需要的对象为空"
	 */
	OBJECT_IS_NULL(500004),
	
	/**
	 * ,"认证失败"
	 */
	AUTH_FAIL(500005),
	
	/**
	 * ,"无权限"
	 */
	ACCESS_DENY(500006),
	
	/**
	 * ,"不被支持的文件名"
	 */
	UNSUPPORTED_FILENAME(500008),
	
	/**
	 * ,"订单已存在"
	 */
	BILL_ALREADY_EXIST(500009),
	
	/**
	 * ,"订单更新失败"
	 */
	BILL_UPDATE_FAIL(500011),
	
	/**
	 * ,"数据更新失败"
	 */
	DATA_UPDATE_FAIL(500013),
	
	/**
	 * ,"版本不支持"
	 */
	VERSION_UNSUPPORTED(500014),
	
	/**
	 * ,"未知错误"
	 */
	UNKNOWN_ERROR(500015),
	
	/**
	 * ,"校验失败"
	 */
	VERIFY_ERROR(500016),
	
	/**
	 * , "无法解析需要的对象"
	 */
	CAN_NOT_PARSE_OBJECT(500017),
	
	/**
	 * ,"要创建的对象已存在"
	 */
	OBJECT_ALREADY_EXIST(500018),
	
	/**
	 * ,"账户被锁定"
	 */
	ACCOUNT_LOCKED(500019),
	
	/**
	 * ,"父账户被锁定"
	 */
	PARENT_ACCOUNT_LOCKED(500020),
	
	/**
	 * ,"无法创建订单"
	 */
	BILL_CREATE_FAIL(500021),
	
	/**
	 * ,"订单不存在"
	 */
	BILL_NOT_EXIST(500022),
	
	/**
	 * ,"错误的数据格式"
	 */
	DATE_FORMAT_ERROR(500023),
	
	/**
	 * ,"帐号错误"
	 */
	ACCOUNT_ERROR(500024),
	
	/**
	 * ,"帐号不存在"
	 */
	accountNotExist(500025),
	
	/**
	 * ,"金额超范围"
	 */
	moneyRangeError(500026),
	
	/**
	 * ,"没找到对应的激活码"
	 */
	activeSignNotFound(500027),
	
	/**
	 * ,"没找到对应的邮箱绑定签名"
	 */
	mailBindSignNotFound(500028),
	
	/**
	 * ,"没找到对应的手机绑定签名"
	 */
	phoneBindSignNotFound(500029),
	
	/**
	 * ,"没找到对应的找回密码签名"
	 */
	findPasswordSignNotFound(500030),
	
	/**
	 * ,"在Session中没找到用户"
	 */
	userNotFoundInSession(500031),
	
	/**
	 * ,"必须的数据没有提供"
	 */
	REQUIRED_PARAMETER(500032),
	
	/**
	 * ,"系统维护中"
	 */
	systemInMaintenance(500033),
	
	/**
	 * ,"系统例行升级中"
	 */
	systemInUpgrade(500034),
	
	/**
	 * ,"订单重复"
	 */
	BILL_DUPLICATE(500035),
	
	/**
	 * ,"数据错误"
	 */
	DATA_ERROR(500036),
	
	/**
	 * ,"无匹配的权限"
	 */
	noMatchedPrivilege(500037),
	
	/**
	 * ,"业务流工作步骤数量错误"
	 */
	flowWorkRouteCountError(500038),
	
	/**
	 * ,"充值到对方异常"
	 */
	chargeToPeerError(500039),
	
	/**
	 * ,"邮箱已绑定"
	 */
	mailAlreadyBinded(500041),
	
	/**
	 * ,"在请求中没找到用户"
	 */
	userNotFoundInRequest(500042),
	
	/**
	 * ,"请求中没找到对应的绑定码"
	 */
	mailBindSignNotFoundInRequest(500043),
	
	/**
	 * ,"系统中没找到对应的绑定码"
	 */
	mailBindSignNotFoundInSystem(500044),
	
	/**
	 * ,"绑定签名不一致"
	 */
	mailBindSignNotMatch(500045),
	
	/**
	 * ,"数据唯一性错误"
	 */
	dataUniqueConflict(500046),
	
	/**
	 * ,"数据全局唯一性错误"
	 */
	dataGlobalUniqueConflict(500047),
	
	/**
	 * ,"没找到对应的找回密码签名"
	 */
	findPasswordSignNotFoundInSystem(500048),
	
	/**
	 * ,"交易对象不匹配"
	 */
	transactionObjectNotMatch(500049),
	
	/**
	 * ,"资金帐号不存在"
	 */
	moneyAccountNotExist(500050),
	
	/**
	 * ,"资金余额不足"
	 */
	moneyNotEnough(500051),
	
	/**
	 * ,"购物车为空;"
	 */
	cartIsNull(500052),
	
	/**
	 * ,"购物车没有物品"
	 */
	cartIsEmpty(500053),
	
	/**
	 * ,"找不到对应的交易处理器"
	 */
	transactionProcessorNotFound(500053),
	
	/**
	 * ,"向购物车添加物品失败"
	 */
	addToCartFail(500054),
	
	/**
	 * ,"关联的支付方式为空"
	 */
	payMethodIsNull(500055),
	
	/**
	 * ,"指定的支付处理器不存在"
	 */
	payProcessorIsNull(500056),
	
	/**
	 * ,"找不到供应商"
	 */
	supplierNotFound(500057),
	
	/**
	 * 系统中找不到对应的用户
	 */
	USER_NOT_EXIST_IN_SYSTEM(500058),
	
	/**
	 * ,"数据校验失败"
	 */
	dataVerifyFail(500059),
	
	/**
	 * ,"找不到匹配的工作流实例"
	 */
	workflowInstanceNotFound(500060),
	
	/**
	 * ,"找不到对应的工作步骤"
	 */
	workflowRouteNotFound(500061),
	
	/**
	 * ,"服务不可用"
	 */
	serviceUnavaiable(500062),
	
	/**
	 * ,"支付类型为空"
	 */
	payTypeIsNull(500063),
	
	/**
	 * ,"不支持的资金类型"
	 */
	unsupportedMoneyType(500064), 
	
	/**
	 * ,"卡号错误"
	 */
	serialNumberError(500065),
	
	/**
	 * ,"充值失败"
	 */
	chargeError(500066),
	
	/**
	 * ,"图片验证码识别错误"
	 */
	captchaError(500067),
	
	/**
	 * "卡密错误"
	 */
	cardPasswordError(500068),
	
	/**
	 * ,"IP地址非法或已被拉黑"
	 */
	invalidIpAddress(500069),
	
	/**
	 * ,"不支持的产品"
	 */
	unSupportedProduct(500070), 
	
	/**
	 * ,"时间区间错误"
	 */
	timePeriodError(500071),
	
	/**
	 * ,"活动已结束"
	 */
	activityClosed(500072), 
	
	/**
	 * ,"活动未命中"
	 */
	activityMissedShot(500073),
	
	/**
	 * ,"活动受限"
	 */
	activityLimited(500074),
	
	/**
	 * ,"IP地址使用次数已达上限"
	 */
	ipLimitCountReached(500075),
	
	/**
	 * ,"卡号或密码无效"
	 */
	invalidCard(500076),
	
	/**
	 * ,"受限卡"
	 */
	limitedCard(500077),
	
	/**
	 * ,"已被使用"
	 */
	cardUsedBefore(500078),
	
	/**
	 * ,"资金被冻结"
	 */
	moneyIsFrozen(500079),
	
	/**
	 * ,"尚未激活"
	 */
	notActive(500080), 
	
	/**
	 * ,"卡尚未使用"
	 */
	cardNotUsed(500081),
	
	/**
	 * ,"卡被没收"
	 */
	cardBeTaked(500082),
	
	/**
	 * ,"库存不足"
	 */
	stockEmpty(500083),
	
	/**
	 * ,"卡密金额不符"
	 */
	cardMoneyNotMatch(500084),		
	
	/**
	 * ,"IP地址段使用次数已达上限"
	 */
	ipRangeLimitCountReached(500085), 
	
	/**
	 * ,"请求已超时"
	 */
	requestTimeout(500086), 
	
	/**
	 * ,"循环交易"
	 */
	cycleTransaction(500087),
	
	/**
	 * ,"产品已关闭"
	 */
	productClosed(500088), 
	
	/**
	 * ,"找不到指定的BEAN"
	 */
	beanNotFound(500089), 
	
	/**
	 * ,"帐号UUID与名称不一致"
	 */
	accountUuidNameNotMatch(500090),
	
	/**
	 * ,"网络异常"
	 */
	networkError(500091),
	
	/**
	 * ,"用户密钥不存在"
	 */
	KEY_NOT_FOUND(500092),
	
	/**
	 * ,"操作过于频繁"
	 */
	operateTooSoon(500093), 
	
	/**
	 * ,"产品不存在"
	 */
	productNotExist(500094),
	
	/**
	 * ,"受唯一性限制数据重复"
	 */
	dataDuplicate(500095), 
	
	/**
	 * ,"系统繁忙"
	 */
	systemBusy(500096),
	
	/**
	 * ,"数据不合法"
	 */
	DATA_ILLEGAL(500097),
	
	/**
	 * ,"锁定资金失败"
	 */
	moneyLockFail(500098), 
	
	/**
	 * ,"状态异常"
	 */
	statusAbnormal(500099), 
	
	/**
	 * ,"数量不够"
	 */
	COUNT_NOT_ENOUGH(500100),
	
	/**
	 * ,"需要提供配送地址"
	 */
	requireDeliveryAddress(500101),
	
	/**
	 * ,"需要指定自提点"
	 */
	requireSelfCarryAddress(500102), 
	
	/**
	 * ,"兑换规则不存在"
	 */
	exchangeRuleNotExist(500103), 
	
	/**
	 * ,"兑换规则资金错误"
	 */
	exchangeRuleMoneyError(500104), 
	
	/**
	 * ,"数量错误"
	 */
	amountError(500105),
	
	/**
	 * ,"条件不满足"
	 */
	conditionNotEnough(500106), 
	
	/**
	 * ,"解密错误"
	 */
	DECRYPT_ERROR(500108),
	
	/**
	 * ,"加密错误"
	 */
	ENCRYPT_ERROR(500109),
	
	/**
	 * ,"系统中找不到指定的角色"
	 */
	roleNotFoundInSystem(500110),
	
	/**
	 * ,"系统数据异常"
	 */
	SYSTEM_DATA_ERROR(500111), 
	
	/**
	 * ,"平台属主不匹配")
	 */
	ownerNotMatch(500115), 
	
	/**
	 * ,"时间还没到"
	 */
	timeNotUp(500116),
	
	/**
	 * ,"已过期"
	 */
	outOfDate(500117),
	
	/**
	 * ,"节点不存在")
	 */
	nodeNotExist(500118),
	
	/**
	 * ,"指定的节点处理器不存在"
	 */
	nodeProcessorIsNull(500119),
	
	/**
	 * ,"对应的处理器不存在"
	 */
	processorIsNull(500120), 
	
	/**
	 * ,"类型错误"
	 */
	typeError(500121),
	
	/**
	 * ,"活动不存在"
	 */
	activityNotExist(500122),
	
	/**
	 * ,"参与人数受限"
	 */
	joinUserCountLimited(500123),
	
	/**
	 * ,"获取总数受限"
	 */
	getTotalCountPerUserLimited(500124),
	
	/**
	 * ,"获取单个产品的数量受限"
	 */
	getEachCountPerUserLimited(500125), 
	
	/**
	 * ,"在请求中没找到对应的证书"
	 */
	certifyNotFoundInReuest(500126), 
	
	/**
	 * ,"用户名或密码为空"
	 */
	usernameOrPasswordIsNull(500127),
	
	/**
	 * ,"需要二次验证"
	 */
	needSecAuth(500128), 
	
	/**
	 * ,"修改密码时老密码不匹配"
	 */
	oldPasswordNotMatch(500129),
	
	/**
	 * "修改密码时两次输入密码不一致"
	 */
	twicePasswordNotMatch(500130), 
	
	/*8
	 * ,"密码太短"
	 */
	PASSWORD_TOO_SHORT(500131), 
	
	/**
	 * ,"密码不够强壮"
	 */
	passwordNotStrong(500132), 
	
	/**
	 * ,"价格不存在"
	 */
	priceNotExist(500133), 
	
	/**
	 * ,"价格金额错误"
	 */
	priceMoneyError(500134), 
	
	/**
	 * ,"数量为0"
	 */
	COUNT_IS_ZERO(500135), 
	
	/**
	 * ,"数据不完整"
	 */
	dataImperfect(500136),
	
	/**
	 * ,"次数超限"
	 */
	COUNT_LIMIT_EXCEED(500137), 
	
	/**
	 * ,"全局加锁失败"
	 */
	distributedLockFail(500138),
	
	/**
	 * ,"创建唯一ID失败"
	 */
	uuidCreateFail(500139), 
	
	/**
	 * ,"包含了敏感词"
	 */
	haveDirtyWord(500140), 
	
	/**
	 * ,"订阅次数错误"
	 */
	subscribeCountError(500141),
	
	/**
	 * ,"手机号之前已绑定"
	 */
	phoneAlreadyBound(500142),
	
	/**
	 * ,"处于编辑锁定状态"
	 */
	updateLocked(500143), 
	
	/**
	 * ,"找不到指定的步骤"
	 */
	stepNotFound(500144), 
	
	/**
	 * ,"不允许重复操作"
	 */
	duplicateOperate(500145),
	
	/**
	 * ,"找不到指定的实例"
	 */
	instanceNotFound(500146),
	
	/**
	 * ,"无法识别的操作"
	 */
	unknownOperate(500147),
	
	/**
	 * ,"版本冲突"
	 */
	versionConflect(500148), 
	
	/**
	 * ,"不是好友"
	 */
	notFriend(500149), 
	
	/**
	 * ,"找不到指定的卡券产品"
	 */
	COUPON_PRODUCT_NOT_EXIST(500150),
	
	
	/**
	 * ,"指定的卡券处理器为空"
	 */

	/**
	 * ,"银行账号错误"
	 */
	bankAccountNumberError(500152),
	
	/**
	 * ,"银行帐号已被其他客户绑定"
	 */
	bankAccountBindByOtherClient(500153), 
	
	/**
	 * ,"银行帐号额度超限"
	 */
	bankAccountQuotaExceed(500154),
	
	/**
	 * ,"时间错误"
	 */
	timeError(500155),
	
	/**
	 * ,"在系统中找不到指定的数据"
	 */
	dataNotFoundInSystem(500156), 
	
	/**
	 * ,"没有可用的付款方式"
	 */
	noWithdrawMethod(500157),
	
	/**
	 * ,"系统出现异常"
	 */
	systemException(500158),
	
	/**
	 * ,"通道资金不足"
	 */
	systemMoneyNotEnough(500159),
	
	/**
	 * ,"通道帐号额度超限"
	 */
	channelAccountQuotaExceed(500160), 
	
	/**
	 * ,"银行卡已使用的批付通道不一致"
	 */
	withdrawMethodNotMatch(500161),
	
	/**
	 * ,"参数错误"
	 */
	PARAMETER_ERROR(500162), 
	
	/**
	 * 找不到指定的批付方式
	 */
	WITHDRAW_TYPE_NOT_FOUND(500163),
	
	/**
	 * 对象创建失败
	 */
	OBJECT_CREATE_ERROR(500164),
	
	/**
	 * 订单数据错误
	 */
	ORDER_DATA_ERROR(500165), 
	
	/**
	 * 需要更高级别
	 */
	REQUIRE_HIGH_LEVEL(500166),
	
	/**
	 * 份额已完成
	 */
	QUOTA_FULL(500167),
	
	/**
	 * 份额不足
	 */
	QUOTA_INSUFFICIENT(500168), 
	/**
	 * 错误的地址本
	 */
	INVALID_ADDRESS_BOOK(500169),
	
	/**
	 * 订单已不允许修改
	 */
	CART_NOT_ALLOW_UPDATE(500170),
	
	/**
	 * 不支持的银行
	 */
	UNSUPPORTED_BANK(500171),
	
	/**
	 * 签发证书失败
	 */
	ISSUE_CERTIFICATE_FAIL(500172), 
	
	
	/**
	 * 解码错误
	 */
	DECODING_ERROR(500173), 
	
	/**
	 * 与其他数据有关联
	 */
	RELATION_DATA_CONFILECT(500174),

	//点券消费失败
	CONSUME_FAIL(500175),

	PAY_CREATE_ERROR(500176),

	/**
	 * 数据未改变
	 */
	DATA_NOT_CHANGE(500177),

	/**
	 * 当前节点不负责处理该事务
	 */
  NOT_HANDLE_NODE(500178);
    public final int id;
	private EisError(int id){
		this.id = id;
	}
	public int getId() {
		return id;
	}



	@Override
	public String toString(){
		return name();
	}	

	public EisError findById(int id){
		for(EisError value: EisError.values()){
			if(value.getId() == id){
				return value;
			}
		}
		return UNKNOWN;
	}
}
