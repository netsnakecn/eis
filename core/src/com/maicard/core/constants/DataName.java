package com.maicard.core.constants;

import cn.afterturn.easypoi.excel.annotation.Excel;

//数据名称定义
public enum DataName{

	//bossUsePageShowException,	//BOSS是否使用页面显示来替代抛出异常		
	//siteDomain,			
	appToken,				//APP令牌
	DEFAULT_PRODUCT_VALIDATOR,

	autoRefund,				//是否自动退款
	autoWithdraw,				//是否自动提现
	accountProcessIdleTime,	//充值处理的间隔时间		
	activityQuota,		//活动的总份额，即中奖率的分母	
	activityCode,			//活动代码
	activityType,				//活动类型
	activityProcessor,			//活动处理器
	activityBeginTime,			//活动开始时间
	activityEndTime,				//活动结束时间
	activityPrice,					//活动特定价格
	advertId,	//文章关联的广告ID		
	alertMailReceiver,	//告警邮箱收件人		
	alertSmsReceiver,	//告警短信收件人		
	alipayAccountId,	//支付宝账户ID
	alipayKey,			//支付宝支付KEY
	alipayUserName, //支付宝账户名			
	allowOldUserLoginFromQuickStart,	//是否允许已存在的用户从Quickstart登录	
	allowSsoLogin,		//是否允许用户直接以SSO形式登录
	allProductValideForAllPartner, //对所有合作伙伴而言，是否所有产品都可用，如果为true，合作伙伴能看到所有产品，否则将根据productPartnerRelation进行筛选		
	askUseCouponMoney,			//用户要求使用多少卡券金额
	autoClosePrepayChannelWhenMoneyNotEnough,	//是否在预付资金不足时，自动关闭那些预付费通道		
	autoCreateRandomNameWhenNullRegister,	//当注册提交的用户名为空时，是否自动创建一个随机数来进行注册		
	autoLinkPrivilegeSuperRoleId,		//如果自动为超级用户设置该权限，超级用户所在的角色组ID	
	autoLinkPrivilegeToSuperUser, //新建系统权限时，是否自动为超级用户设置该权限
	autoTagList,						//哪些数据应当自动作为tag标签写入
	autoPasswordForPhoneUser,					//如果用户没有提供密码，并且是手机验证用户，则自动以手机验证码作为密码
	availableCount,						//产品剩余数量
	bankAccount,	//银行账户ID		
	bankIssuer,		//开户行	
	bankName,		//银行名称	
	bankUserName,	//银行账户名		
	baseDir,				//数据存放的总目录，如/var/xxxx
	baseOnlineUser, //在线用户基数			
	baseTotalUser,	//前台用户基数		
	beanEmailGateway,	//邮件网关的BEAN名称		
	beanSmsGateway,	//短信网关的BEAN名称		
	bindPhoneValidateMessage,			//绑定手机号下发的短信内容
	billingDefaultPeriod,	//结算默认周期		
	bookingActivity,		//预定商品相关联的活动	
	bookingPrice,			
	bossOperateLogLevel,	//BOSS后台的操作记录级别		
	bossTheme,			
	both,			//查询所有表
	
	captchaBackColor,			
	partnerCaptchaForeColor,		
	frontCaptchaForeColor,
	captchaMaxLength,			//验证码最大长度
	captchaMinLength,			//验证码最小长度
	captchaUsingFilter,			//验证码是否使用变形等效果
	captchaWord,			//客户端版本控制文件
	cardChannelAutoDisableCount,	//点卡通道异常多少次将自动关闭		
	cardProcessIdelTime,		//点卡处理每次间隔时间	
	cardProcessMaxQueryCount,	//点卡处理时最多查询次数		
	cardProcessQueryInterval,	//点卡处理时每次查询间隔		
	cartTtl,			
	canChangeUserName,					//是否允许用户修改自己的用户名
	channelSharePercent,			
	city,							//城市
	clientIp,			
	clientVersionFile,	

	coinName,		
	coinPaid,						//外部coin已完成支付
	commonFooter,			
	cookiePolicy,			
	cooperationUserId,			//渠道分成比例
	country,						//国家
	couponBindUser,			
	couponLeastCost,			
	couponReduceCost,			
	couponUseCustomCode,			
	currentJoinUserCount,			
	dataWriteIdelForActiveCount,	//当数据库连接池中有多少个活动连接时，暂停写入数据		
	dataWriteIdelIntervalMs,	//在每次查询是否写入数据之间的间隔毫秒		
	dataWriteIdleForWaitingCount,	//当数据库连接池中有多少个等待连接时，暂停写入数据		
	dataWriteMaxWaitingCount,	//数据写入的最长等待次数		
	defaultTransactionExecutor,		//默认交易处理器	
	deliveryFee,				//快递费用
	deliveryOrderId,			//快递单号
	deliveryCompanyId,			//快递公司ID
	deliveryCompanyName,			//快递公司代码
	deliveryOrderBrief,				//配送简述
	defaultFromArea,				//默认发货地
	deliveryFromArea,				//发货地
	deliveryPriceListFile,			//快递报价单文件


	documentBrief,			
	documentCreateBPName,			
	documentDirectPublish,			
	EXTRA_DATA_URL_PREFIX,		//需授权才能下载的文档，其Nginx的虚假路径	
	documentEditPublished,			
	documentPdf2jpgExeLocation,		//文档PDF转JPG程序的执行目标	//是否以分布式发送通知
	documentPdf2swfExeLocation,		//文档PDF转SWF程序的执行目标	//文档后期处理器
	documentPdf2swfOldFileDir,		//文档PDF转SWF后旧PDF保存位置	//文档上传的附件保存目录
	
	/**
	 * 文档新增或更新后需要立即执行的后续处理器，比如对文档内容或扩展数据进行修改
	 */
	documentPostProcessor,
	
	/**
	 * 文档新增或更新后异步后续处理器，比如进行较长时间的静态化或其他操作
	 */
	asyncDocumentPostProcessor,
	documentUpdateBPName,			
	//documentUploadSaveDir,			
	entityShopList,			//文档是否直接发布不需要工作流介入
	eventKey,						//对象关联的eventKey
	favoriteCount,			//
	feedBackTopicId,			
	fetchUrl,	
	freeDeliveryLeastOrderMoney,		//订单总金额多于这个数可以免运费
	frontInviteUrl,					//前端邀请URL模版
	frontLoginUrl,					//前端用户未登录时自动跳转到哪个URL
	forceCloseProcessingItemSec,		//强行关闭超过此秒数仍处于处理中的订单，如果不设置即为0，则不关闭	
	forcePayWithoutAccountMoney,		//不考虑用户账户中的余额，即便余额足够，也对订单进行支付
	forceUseGiftMoneyFirst,			//强制优先使用giftMoney，即代金券冲入的资金
	forgetPasswordSmsValidateMessage,			//找回密码时的短信内容
	frontTheme,			
	frontUserStartUuid,	
	frontRowsPerPage,					//前端分页数量
	galleryStaticOutPutDir,					//画册静态化文件输出路径
	generateLoginKeyForNewUser,		//是否生成一个新的登陆密钥给新注册用户	
	generalTaxPayerCetificateImage,		//一般纳税人凭证2
	getEachCountPerUserLimit,			
	getTotalCountPerUserLimit,			
	goodsSmallImage,		
	goodsWeight,					//商品重量
	heapUsageAlertPercent,		//系统堆内存告警百分比
	imageFileType,					//允许的图片文件类型
	initNotifySendInterval,		//初始化异步通知的发送间隔	
	internalKey,			
	internalServerHost,			
	internalServerPort,			//节点是否负责更新用户数据
	itemNoLocalInsert,				////在新增Item时，是否先在本地插入数据，再传递给远程，如果是true，则直接
	ipChangeProcessor,			
	isSiteReplactionSlaver, 	//本站点/系统是否是一个复制关系的从站点		
	itemFailPolicy,			
	itemRegion,			//充值服务的内部服务器主机
	jmsDefaultTtl,			//充值服务的内部服务器端口

	haltWhenAskCouponMoneyExceedReal,			//交易时，如果用户请求使用的代金券金额超过了实际代金券金额，停止该交易

	kuaidi100Key,				//快递100接口KEY
	keepNotifyLogDay,			
	keepOperateLogDay,	
	lastEventKey,			//用户最后一次进入时所带的eventKey
	
	signKey,
	language,				//用户所用的语言
	legacyOutDesc,			
	legacyOutGoodInfo,			
	legacyOutGoods,			
	legacyOutUsername,			
	legacyProduct,			//动态改变IP的规则处理器
	loginRetryCountInDuration,			
	loginRetryDurationSec,			//OperateLog保留的时间天数
	loose,			//NotifyLog保留的时间天数

	lockProductForUpdate,		//管理后台是否锁定正在编辑的产品，以防止同时编辑产生冲突
	lockDocumentForUpdate,	//管理后台是否锁定正在编辑的文章，以防止同时编辑产生冲突
	mainSite,				//网站展示给普通用户的主地址
	mailSenderName, 			
	mailSenderPassword, 		//邮件服务的发送者密码	//检查登录失败次数的时间长短
	mailSmtpHost, 			//登录失败时间内多少次将被限制登录
	mailSmtpPort,   			
	masterSiteDomain,			
	masterSystemName,			//旧系统外部用户名
	maxActiveThread, 			
	maxChannelSharePercent,		//渠道分成最高比例	
	maxContinueSignDay,			//旧系统外部产品信息
	maxGiftMoneyAmount,			//每个关卡消耗的体力值
	maxJoinUserCount,			
	maxMoneySharePercent,		//最高现金分成比例	
	maxNotifyCount,			//邮件服务的发送者帐号
	maxNotifyMinute,			
	maxProductSharePercent,		//产品分成的最高比例	
	maxReadCount,			//最多阅读数量
	memory,			
	memberCard,						//会员卡
	messageBusSystem,			//使用站点复制功能时，主站的系统名称
	messageBusTransaction,		//JMS交易总线名称	//使用站点复制功能时，主站的域名
	messageBusUser,			
	messageQueueSize,			//最大活动线程数
	minJoinUserCount,			
	MONEY_SHARE_MODE,			//资金的分成类型	0:无分成 1:分成给充值用户，用于B端商户系统 2:分成给充值用户的inviter，用于C端系统
	moneyName,			
	moneyType,					//资金类型
	moneyPerRead,			
	moneySharePercent,			//异步通知的最大发送时间
	musicFileType,					//允许的音乐文件类型
	mustValidFieldForQuickRegister,		//自动注册帐号时哪些字段是必须的	

	/**
	 * 产品是否需要配送
	 */
	productNeedDelivery,		
	needCaptchaWhenRegisterByPhone,			//通过手机注册时是否需要验证码
	needPathNodeList,				//内容处理器需要节点路径列表
	noDistributedNotify,			//最长有效连续签到时间

	noFetchRelatedDocument,				//在文章详情页不获取相关文章
	noFetchLastAndNextDocument,		//不要获取上一篇和下一篇的文章


	none,			//活动的最多参与人数
	notifyMessageTypeId,			//活动参与的最少人数
	notifyProcessor,		//自定义通知处理器	
	notifySendRetryInterval,	//通知重发间隔		//JMS队列大小
	oldOperateLogSaveDir,	//操作日志备份文件目录		//JMS系统总线名称
	oneRolePrePlayer,			
	only,			
	operator,			
	orderAddress,			
	orderAddressId,		//订单配送地址对应的ID	
	orderReturn,			
	orderTtl,			
	outChargeUnit,			
	outErrorCode,			//外部错误代码
	outOperateCode,		//外部系统的操作代码	
	outOrderId,			
	outProductCode,			
	outProductId,			
	outProductName,			
	outProductServerCode,	//二级区服代码		
	outProductServerName,	//二级区服名称		
	outRegionCode,			
	outRegionName,			
	outShareUrl,		//外部系统的共享URL	
	outUnit, //外部产品的购买单位			
	outUuid,	//外部系统的UUID		
	partnerBossName,			
	partnerTheme,			
	partnerRowsPerPage,			//partner后台每页行数
	passwordMinLength,			//通知发送对应的消息类型ID
	passwordStrongGrade,			
	passwordTtl,			//每个用户只能创建一个角色
	payChannelmaxErrorCount,	//支付通道最大错误数		
	payNotifyUrl,			
	payOrderTimeoutSec,			
	payReturnUrl,			
	paySuccessShareMoneyToChannel,		//支付成功后给推广渠道的奖励配置
	paySuccessRewardMoney,					//支付成功后给自己的奖励
	permUsageAlertPercent,			//一级区服代码
	perUserJoinCountLimit,			//一级区服名称
	perUserVoteCountLimit,			//单个用户投票次数限制
	pointName,			
	pointPerRead,//阅读一次可以获取的积分数			
	postPay,		//后付费	
	prePay,			
	privateKeyExponent,			
	privatekeyModulus,	
	processOnlyOnce,			//仅处理一次
	processInterval,					//多长时间内能处理一次

	productAccountName,		//产品帐号名称	
	productAgentType,		//产品的代理机构类型	
	productBigImage,		//产品大图	//订单的配送地址
	productBuyMoney,			//退货
	productCode,			
	productConsumer,		//产品消费者	
	//productFileUrl,			
	productIncomingTotal, //总收益			
	productInterestMonthly,	//月利息		//Partner系统的主题
	productInterestRateMonthly,	//每月利率		
	productInterestRateYearly,	//年化利率		
	productInterestTotal,	//总利息		//用户密码的最小长度
	productLabelMoney,		//产品标记价格，面额	//密码的强度要求
	productLoanMaxTime,		//产品最长贷款周期	
	productLoanMinTime,		//产品最短贷款周期	
	productLoanTimeline,	//产品贷款周期		
	productMaxMoney,		//产品允许的最高金额	//支付订单的超时秒数
	productMinMoney,		//产品允许的最低金额	
	productMarketPrice,			//产品的市场价、原价
	productMoneyAcceptDay,	//产品最快放贷时间（天）		//单个用户参与次数的限制
	productName,			
	productOrigin,					//原产地
	productPassword,		//卡密	
	productRegion,			
	productRepaymentMonthly,	//产品月还款金额		
	productRepaymentTotal,	//产品总还款金额		
	productRepaymentType,	//产品还款类型		
	productRequireCarType,	//产品需要汽车类型，若不需要则为空		
	productRequireCredit,	//产品所需信用情况，若不需要则为空		
	productRequireHouseType,	//产品所需房产类型，若不需要则为空		//产品代码
	productRequireJob,			//产品名称
	productRequireJobTime,		//产品对工作年限的要求，若不需要则为空	
	productRequireJobType,		//产品对工作性质的要求，若不需要则为空	//产品文件访问URL
	productRequireLocalCommonFund,	//产品是否需要本地缴纳公积金		
	productRequireMortgageType,	//产品要求的必须抵押类型，若不需要则为空		
	productRequireSalary,		//产品有无每月工资收入要求，若不需要则为0	
	productSerialNumber,	//序列号		//产品区域
	productServer,			//产品服务器
	productServerActivityCacheTtl,	//产品服务器活动状况的缓存时间		
	productIconIndex,			//产品图标的索引
	productServerName,		//产品服务器名称	
	productSharePercent,	//产品分成比例		
	productSmallImage,		//产品小图	
	//productUploadSaveDir,	//产品附加文件如产品图片的保存目录		
	productValidator,		//产品校验器	
	productWord,	//断言、短语		//产品实际购买价格
	productGallery,			//产品的画册，即多个产品大图
	paddingConfig,				//补单补充策略
	passwordLength,				//密码长度
	publicKeyExponent,			
	publicKeyModulus,			
	province,					//省份
	readCount,		//已阅读次数	
	refAwardId,			//对应的奖励对象ID
	refUdid,			//对应的文档ID
	refProductId,			//对应的产品ID
	refUrl,					//对应的URL
	refImage,			//对应文档的图片
	refTitle,			//对应文档或产品标题
	refBrief,			//对应文档或产品的简介
	rememberMe,			
	remoteLockWaitingRetry,		//远程锁定等待结果的最大重试次数	
	replyQueueName,			
	requestValidTtl,			
	requireWorkflowObject,		//哪些对象的哪些操作需要工作流处理	
	requireStaticize,				//该对象是否需要静态化
	registerSmsValidateMessage,			//注册用户时下发验证短信的内容
	saleNotifyUrl,
	soldCount,						//已售出数量
	scoreName,			
	secAuthObjectAndOperate,			
	secAuthTtl,			
	selfCarry,			
	sendWelcomeEmailAfterRegister,		//用户注册后是否发送欢迎邮件	
	sendWelcomeMessageAfterRegister,	//用户注册后是否发送站内短信		//产品是否必须要求工作，若不需要则为空
	sendWelcomeSmsAfterRegister,		//用户注册后是否发送欢迎短信	
	sessionTimeout,			
	serialNumberLength,					//序列号长度
	settlement,	
	smsRegisterSign,					//用户注册时的短信验证
	signObject,			
	siteBbsLoginUrl,			
	siteDefaultNodeProcessor,	//站点默认的节点处理器		
	siteDynamicUrl,					//站点的动态数据访问地址，即没有做过静态化的
	siteDocumentPreviewHost,	//未发布文档的预览主机地址		
	siteLoginAfterRegister,		//是否在注册后自动登录	
	siteRegisterNeedMailActive,	//注册是否需要邮件激活		
	siteRegisterNeedPatchca,	//注册是否需要验证码		
	siteStaticize,			
	siteTheme,			
	siteUseMultiDomain,			
	siteUseMultiSystemName,		//站点是否使用多个系统名称
	sitePartnerId,						//站点是属于哪个合作方的站点，在类似微信第三方平台中，用来区分产品、主题等属性
	siteUseMultiTheme,	
	globalSiteStaticPathPrefix,					//站点静态化后的存储路径，不区分ownerId
	syncToWeixin,					//该对象（如优惠券）是否与微信同步
	strict,			
	ssoTimestampTtl,						//SSO登录的时间戳有效期
	splitOrderBySupplier,					//是否将订单拆分为不同供应商的订单
	submitUrl,			
	supplierAccountExistUrl,	//合作伙伴查询帐号是否存在的URL		//预付费
	supplierAgentId,			
	supplierAgentName,			//是否记住帐号
	supplierCharacterQueryUrl,	//合作伙伴查询角色的URL		
	supplierChargeHost,			
	supplierChargeKey,			//请求的TTL有效期
	subscribeShareMoneyToChannel,			//新关注用户给推广渠道的奖励配置
	supplierChargePort,			
	supplierChargeUrl,			
	supplierDomain,			
	supplierInviteUrl,			
	supplierLastYearIncomingTotal,	//合作伙伴上一年度主营业务总收入		
	supplierLoginKey,			
	supplierLoginUrl,			
	supplierNewbieCardUrl,		//合作伙伴新手卡URL	
	supplierOtherKey,			
	supplierProcessClass,		//合作伙伴的处理器BEAN	
	supplierProductListUrl,		//合作伙伴的产品列表URL	//短信网关帐号
	supplierQueryUrl,			//短信网关密码
	supplierTheme,			
	supplierTimeUrl,			//合作伙伴的渠道ID
	suppliyerLastYearPurchaseTotal,	//合作伙伴上一年度采购总额		//合作伙伴的渠道名称或代码
	systemBossName,			//合作伙伴的充值主机地址
	systemIsBusinessSystem,			//合作伙伴的充值主机端口
	systemIsEcommerceSystem,			//合作伙伴的充值密钥
	systemIsIoServerMapping,	//系统是否使用了内外部产品、区域和服务器映射机制		//合作伙伴的充值URL
	systemName,			
	systemServerId, 			//合作伙伴的查询URL
	systemThrowException,			//合作伙伴的时间查询URL
	tagtagConfig,					//标签的标签配置
	taxRegistrationCertificatecImage,			//发票注册认证图片
	timeScope,		
	timeoutConfig,					//超时配置
	totalFreePlayCount,			
	//tempUploadPath,							//用户可访问的临时上传文件存放路径
	transactionSaveGeneratedOrderId,	//是否把生成的交易ID保存到数据库		
	tuanActivity,			
	tuanCode,			
	updateUrl,		//软件更新URL	//合作伙伴登录密钥
	userBankAccountBound,			//合作伙伴登录URL
	userBindMailBox,			//合作伙伴其他密钥
	userBindPhoneNumber,			//合作伙伴的邀请URL
	userCertified,			
	userCommonKey,			
	userCompany,		//用户单位或公司	
	userDescription,	//用户介绍		
	userFavorites, 			
	avatarUrl,	//用户头像的图片地址		
	userNickName,			//用户昵称
	userInviteByCode,			//系统服务器ID
	userInviteCode,			
	userIsCertified,			
	userIsWaitingLogin,			
	userLastAutoUpdateOutInfoTs,			//用户最后一次自动更新外部信息的时间戳
	userMailActiveSign,			
	userMailBindSign,			
	userMailBound,			
	userMailFindPasswordSign,		//用户通过邮箱找回密码时的校验	
	userMessageDefaultSendMode,			//站点是否使用多个域名
	needOldPasswordWhenChangePassword,			//修改密码时是否需要提供旧密码
	userPhoneBindSign,			
	userPhoneBound,			
	userPhoneFindPasswordSign,		//用户使用手机找回密码时的验证码	
	userRealName,			
	userRealNameBound,			
	userRealNameIdCardFile,			
	userRealNameIdCardNumber,		//用户身份证明号码	
	userRealNameIdCardType,			
	userRegisterIp,			
	userRegisterWelcomeMailTemplate,
	USER_REGISTER_NEED_VERIFY_CODE,				//用户注册时需要提交手机短信验证码
	userRegisterWelcomeMessage,			
	userRegisterWelcomeSms,			
	userRememberName,			//总免费次数
	userShortMessageNotifyViaMail,			
	userSubscribeMessageNotifyViaMail,			//对应的团购活动
	userTimeToOffline,			
	userUploadDir,			
	userVipLevel,			//用户是否已绑定了银行帐号
	useSiteCodeView,		//使用站点代码来获取view资源	
	useSplitLogDbSource,	//系统使用单独的日志数据库		
	useSplitUserDbSource, //系统使用单独的用户数据库			
	validateCacheExistCount, //验证缓存中重复数据允许存在的次数			
	validateUserIp,	//是否需要校验用户访问IP		
	videoFileType,					//允许的视频文件类型
	videoCoverImage,				//视频的封面
	videoUrl,					//视频内容的地址
	videoLength,				//视频内容的长度，秒
	weixinAppId,		//微信公众号ID	
	weixinAppSecret,	//公众号通讯密钥		
	weixinAppToken,	//公众号令牌		
	weixinPayKey,	//公众号支付密钥	
	weixinPayMechId,	//公众号支付商户ID		
	weixinNotAutoRegister,	//微信访问，不自动注册为前端用户
	weixinNotAutoLogin,			//微信访问，不自动为用户跳转到微信登录界面，用于某些不方便将公众号页面地址设置为我们系统的地址
	weixinGroupid,			//微信分组ID
	weixinMenuId,			//微信自定义菜单ID
	weixinPageVersion,		//微信分组对应的pageVersion，用来在我方系统中显示不同的页面
	weixinNoInfoPrivilege,		//该帐号是不是没有调用用户信息的权限，比如订阅号
	withdrawType,					//提现方式
	allowNewWithdrawBankAccount,		//是否允许用户自行提交提现银行帐号
	allowWithdrawType,					//客户允许使用的提现类型列表
	publicDownloadDir,						//公用下载目录
	GUIDE_STEP,								//当前客户执行到了向导第几步
	GUIDE_ENABLED, 		//当前系统是否启用了向导功能
	MONEY_SHARE_UP_LEVEL,			//向上多级分润配置
	NUMERIC_ONLY,			//只允许有数字
	
	/**
	 * 渠道推广二维码
	 */
	INVITE_QR_URL,
	
	/**
	 * 是否在所有节点上都记录资金变化日志
	 */
	LOG_MONEY_CHANGE_ON_BOTH_NODE,
	
	/**
	 * 在支付时，是否强制使用产品名字替换商户传递来的支付产品名称
	 */
	forceReplacePayNameByProductName,
	
	/**
	 * 在支付时，是否在付款产品名称前加上商户订单号的尾部4位
	 */
	useOrderIdSuffixToPayName, 
	
	/**
	 * 是否允许各个商户使用同一张银行卡提现
	 */
	BANK_ACCOUNT_SHARED, 
	
	/**
	 * 缩略图
	 */
	thumbnail,
	
	/**
	 * 价格
	 */
	price, banner, INTER_DATA_DIR, CHANNEL_ROBIN_TYPE,
	
	/**
	 * 当前系统可以使用的价格类型
	 */
	availablePriceType,
	
	/**
	 * 分单总数
	 */
	SPLIT_TOTAL,
	
	/**
	 * 已完成分单数
	 */
	SUCCESS_SPLIT_COUNT,
	
	
	/**
	 * 剩余分单数
	 */
	REMAIN_SPLIT_COUNT,
	
	/**
	 * 分单模式-按份
	 */
	QUOTA_TYPE_PART,
	
	/**
	 * 分单模式-自由按金额
	 */
	QUOTA_TYPE_MONEY, 
	
	/**
	 * 分单模式-不分单
	 */
	QUOTA_TYPE_NONE,
	/**
	 * 产品分组ID
	 */
	productGroupId,
	
	/**
	 * 文档本身没有内容，获取该参考文档的内容
	 */
	refContentId, 
	
	
	
	/**
	 * 内部系统间支付调用时使用的parnter uuid
	 */
	INTERNAL_PAY_PARTNER_ID,
	
	/**
	 * 内部系统间支付调用时使用的支付名字
	 */
	INTERNAL_PAY_TYPE_ID,
	
	/**
	 * 产品寄售的价格
	 */
	productSaleMoney, cryptKey,
	/**
	 * 证书签发机构的名称DN格式
	 */
	issueName, 
	
	/**
	 * 签发证书的主题
	 */
	subject, 
	
	
	/**
	 * 消息总线是否启用
	 */
	MQ_ENABLED, 
	
	/**
	 * 验证码字数
	 */
	CAPTCHA_LENGTH, OUT_NOTIFY_IP, 

	VERIFY_CODE_TTL_SEC,	//验证码有效期秒
	VERIFY_CODE_RESEND_INTERVAL_SEC,	//验证码重新发送间隔
	
	publicKey,

	SEARCH_PROCESSOR,
	GLOBAL_LOCALE,
 	priceId,
    addressBookId,
    PRODUCT_DOCUMENT_TYPE,
     BUYER,
	IGNORE_STOCK,
	STOCK_MODE,
	REGISTER_TEMP_AES_KEY ,
	SERVER_PRIVATE_KEY,
    REFUND_NOTIFY_URL,
	systemCode,
	pic;
}

