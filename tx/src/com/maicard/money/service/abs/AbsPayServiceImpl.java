package com.maicard.money.service.abs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.*;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.GlobalOrderIdService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.mb.annotation.ProcessMessageObject;
import com.maicard.mb.constants.MessageBusEnum;
import com.maicard.mb.iface.EisMessageListener;
import com.maicard.mb.service.MessageService;
import com.maicard.misc.ThreadHolder;
import com.maicard.money.constants.*;
import com.maicard.money.dao.mapper.PayMapper;
import com.maicard.money.entity.*;
import com.maicard.money.iface.PayProcessor;
import com.maicard.money.iface.TxExecutor;
import com.maicard.money.service.*;
import com.maicard.security.constants.UserStatus;
import com.maicard.security.entity.User;
import com.maicard.security.service.FrontUserService;
import com.maicard.security.service.PartnerService;
import com.maicard.tx.entity.FailedNotify;
import com.maicard.tx.entity.Item;
import com.maicard.tx.iface.NotifyProcessor;
import com.maicard.tx.service.FailedNotifyService;
import com.maicard.utils.HttpUtils;
import com.maicard.utils.IpUtils;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.NumericUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@ProcessMessageObject("pay")
public class AbsPayServiceImpl extends AbsGlobalSyncService<Pay, PayMapper> implements PayService, EisMessageListener, NotifyProcessor {


    @Resource
    protected ApplicationContextService applicationContextService;


    @Resource
    protected ChannelRobinService channelRobinService;

    @Resource
    protected TransPlanService transPlanService;
    @Resource
    protected ConfigService configService;
    @Resource
    protected CenterDataService centerDataService;
    @Resource
    protected FrontUserService frontUserService;
    @Resource
    protected GlobalOrderIdService globalOrderIdService;
    @Resource
    protected MessageService messageService;
    @Resource
    protected PartnerService partnerService;
    @Resource
    protected PayMethodService payMethodService;
    @Resource
    protected PayTypeService payTypeService;

    @Autowired(required = false)
    protected ShareConfigService shareConfigService;

    @Resource
    protected FailedNotifyService failedNotifyService;

    @Resource
    protected WithdrawTypeService withdrawTypeService;

    protected boolean handlerPay = false;
    protected int MONEY_SHARE_MODE = 0;
    protected String messageBusName = MessageBusEnum.TX.name();

    protected boolean mqEnabled = true;


    /**
     * 未指定任何支付方式时的默认支付方式
     */
    public static final int DEFULAT_PAY_TYPE = 1;

    @PostConstruct
    public void init() {
        handlerPay = configService.getBoolProperty(HandlerEnum.HANDLE_PAY.toString());
        MONEY_SHARE_MODE = configService.getIntValue(DataName.MONEY_SHARE_MODE.toString(), 0);
        mqEnabled = configService.getBoolProperty(DataName.MQ_ENABLED.name());
        logger.info("当前系统是否负责处理支付业务:" + handlerPay + ",消息总线是否开启:" + mqEnabled);
        //transactionCachePolicy = configService.getIntValue(DataName.transactionCachePolicy.toString(), 0);
    }

    @Override
    public void createRefundNotifyUrl(Pay pay, HttpServletRequest request) {
        String payNotifyTemplate = configService.getValue(DataName.REFUND_NOTIFY_URL.toString(), pay.getOwnerId());
        if (payNotifyTemplate == null) {
            payNotifyTemplate = TxConstants.DEFAULT_REFUND_NOTIFY_TEMPLATE;
        }
        payNotifyTemplate = payNotifyTemplate.replaceAll("\\$\\{hostUrl\\}", HttpUtils.generateUrlPrefix(request));
        pay.setNotifyUrl(payNotifyTemplate);
    }


    @Override
    public void createNotifyUrl(Pay pay, HttpServletRequest request) {
        String payNotifyTemplate = configService.getValue(DataName.payNotifyUrl.toString(), pay.getOwnerId());
        if (payNotifyTemplate == null) {
            payNotifyTemplate = TxConstants.DEFAULT_PAY_NOTIFY_TEMPLATE;
        }
        payNotifyTemplate = payNotifyTemplate.replaceAll("\\$\\{hostUrl\\}", HttpUtils.generateUrlPrefix(request));
        pay.setNotifyUrl(payNotifyTemplate);
    }

    @Override
    public EisMessage beginPay(Object order, User frontUser, HttpServletRequest request) {
        return null;
    }

    @Override
    public int insert(Pay pay) {
        if (pay.getCurrentStatus() == 0) {
            pay.setCurrentStatus(TxStatus.newOrder.getId());
        }
        if (StringUtils.isBlank(pay.getTransactionId())) {
            pay.setTransactionId(globalOrderIdService.generate(TxType.pay.getId()));
            logger.info("尝试插入的支付订单没有订单号，产生一个新的订单号:{}", pay.getTransactionId());
        }
        Pay _oldPay = mapper.select(pay.getTransactionId());
        if (_oldPay != null) {
            logger.info("支付订单[" + pay.getTransactionId() + "]已存在，停止插入");
            return 0;
        }
        // 默认值
        if (StringUtils.isEmpty(pay.getPayCardType())) {
            pay.setPayCardType(PayCardTypeEnum.UNKNOWN.getCode());
        }
        int rs = mapper.insert(pay);
        logger.debug("向系统中插入新的支付订单[" + pay + "]，结果:" + rs);
        if (rs != 1) {
            return rs;
        }
        //messageService.sendJmsDataSyncMessage(messageBusName, "payService", "insert", pay);
        return rs;
    }


    public int update(Pay pay) {
        int rs = mapper.update(pay);
        logger.debug("更新系统中的支付订单[" + pay + "]，结果:" + rs);
        if (rs != 1) {
            return rs;
        }
        //messageService.sendJmsDataSyncMessage(messageBusName, "payService", "update", pay);
        return rs;
    }


    public int delete(String transactionId) {
        int actualRowsAffected = 0;

        Pay _oldPay = mapper.select(transactionId);

        if (_oldPay != null) {
            actualRowsAffected = mapper.delete(transactionId);
        }

        return actualRowsAffected;
    }

    public Pay select(String transactionId) {
        return mapper.select(transactionId);
    }


    public List<Pay> list(CriteriaMap payCriteria) {
        List<Pay> payList = mapper.list(payCriteria);
        if (payList == null) {
            return Collections.emptyList();
        } else {
            return payList;
        }
    }

    public List<Pay> listOnPage(CriteriaMap payCriteria) {
        if (payCriteria.getStringValue("startTimeBegin") == null && payCriteria.getStringValue("endTimeBegin") == null) {
            //	if(payCriteria.getStartTimeBegin() == null && payCriteria.getStartTime() == null){
            //设置为本月开始
            payCriteria.put("startTimeBegin", DateUtils.truncate(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), Calendar.MONTH));
            //payCriteria.put("endTimeBegin",new Date());


        }

        List<Pay> payList = mapper.list(payCriteria);
        if (payList == null) {
            return Collections.emptyList();
        }
        return payList;
    }


    @Override
    public List<Pay> lock(CriteriaMap payCriteria) {
        int limits = payCriteria.getIntValue("limits");
        if (limits <= 0) {
            payCriteria.put("limits", 10);
            payCriteria.put("starts", 0);
        }

        List<Pay> payList = mapper.lock(payCriteria);
        if (payList == null) {
            return Collections.emptyList();
        }
        return payList;
    }


    public int count(CriteriaMap payCriteria) {
        return mapper.count(payCriteria);
    }

    @Override
    public int countByPartner(CriteriaMap payCriteria) {
        return mapper.countByPartner(payCriteria);
    }

    @Override
    public EisMessage end(String id, String resultString, Object params) throws Exception {
        Pay _oldPay = null;
        PayMethod payMethod = null;
        //id兼容为payMethodId或tid
        if (id.length() >= GlobalOrderIdService.idLength()) {
            //是支付订单号
            _oldPay = select(id);
            payMethod = payMethodService.select(_oldPay.getPayMethodId());
        } else {
            payMethod = payMethodService.select(Long.parseLong(id.trim()));
        }


        if (payMethod == null) {
            logger.error("找不到指定的支付方式,id=" + id);
            return EisMessage.error(EisError.OBJECT_IS_NULL.id);
        }

        HttpServletRequest request = null;
        if (params instanceof HttpServletRequest) {
            request = (HttpServletRequest) params;
        }
        String ipList = payMethod.getExtra(DataName.OUT_NOTIFY_IP.name());
        if (StringUtils.isBlank(ipList)) {
            logger.debug("通道:{}未配置IP白名单限制", payMethod.getId());
        } else if (ipList.trim().equalsIgnoreCase("0.0.0.0")) {
            logger.warn("通道设置了通配白名单0.0.0.0", payMethod.getId());
        } else {
            if (request == null) {
                logger.error("尝试结束的出金订单:{}参数不是request", resultString);
                return EisMessage.error(EisError.PARAMETER_ERROR.id, "参数错误");
            }
            String clientIp = IpUtils.getClientIp(request);
            List<String> whiteIps = Arrays.asList(ipList.split(",|;| "));
            boolean ipIsValid = whiteIps.contains(clientIp);
            if (!ipIsValid) {
                logger.error("当前IP:" + clientIp + "]不在通道:" + payMethod.getId() + "]的IP白名单中:" + ipList + "，忽略通知");
                return EisMessage.error(EisError.invalidIpAddress.id, "IP地址错误");
            }
            logger.debug("当前IP:{}符合通道:{}的IP白名单:{}", clientIp, payMethod.getId(), ipList);
        }


        String[] payProcessConfig = null;
        if (payMethod.getProcessClass() != null) {
            payProcessConfig = payMethod.getProcessClass().split(",");
        } else {
            logger.error("支付方式[" + payMethod.getProcessClass() + "]没有配置处理器");
        }
        String payProcessBeanName = null;
        if (payProcessConfig != null && payProcessConfig.length > 1) {
            payProcessBeanName = payProcessConfig[1];
        } else {
            payProcessBeanName = payMethod.getProcessClass();
        }
        PayProcessor payProcessor = applicationContextService.getBeanGeneric(payProcessBeanName);
        if (payProcessor == null) {
            throw new EisException(EisError.beanNotFound.id, "找不到对应的支付处理器:" + payProcessBeanName);
        }
        Pay pay2 = payProcessor.onResult(id, resultString, request);

        if (pay2 == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("支付处理器[" + payProcessBeanName + "]返回的支付对象是空");
            }
            return EisMessage.error(EisError.UNKNOWN_ERROR.getId(), "success");
        }
        if (pay2.getTransactionId() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("支付处理器[" + payMethod.getProcessClass() + "]返回的支付订单是空");
            }
            return EisMessage.error(EisError.BILL_NOT_EXIST.getId());
        }

        _oldPay = select(pay2.getTransactionId());
        if (_oldPay == null) {
            logger.error("找不到指定的订单:" + pay2.getTransactionId());
        }
        if (_oldPay.getCurrentStatus() == TxStatus.success.getId()) {
            if (logger.isDebugEnabled()) {
                logger.debug("根据支付订单[" + _oldPay.getTransactionId() + "]找到的支付记录已成功，返回字符串为:" + pay2.getPayResultMessage());
            }
            EisMessage result = new EisMessage();
            result.setCode(OpResult.success.getId());
            result.setExtra("pay", _oldPay);
            result.setObjectType(ObjectType.pay.toString());
            return result;
        }
        if (pay2.getCurrentStatus() == TxStatus.inProcess.id) {
            logger.debug("支付订单[" + _oldPay.getTransactionId() + "]由支付处理器[" + payProcessor.getDesc() + "]返回了处理中，返回字符串为:" + pay2.getPayResultMessage());
            //不做任何处理
        } else {

            if (mqEnabled) {
                //消息总线方式，把消息发送到总线，由其他节点完成支付处理
                //先将本地pay订单改为对应状态
                _oldPay.setCurrentStatus(pay2.getCurrentStatus());
                if (pay2.getCurrentStatus() == TxStatus.success.getId()) {
                    _oldPay.setRealMoney(pay2.getRealMoney());
                }
                //	_oldPay.setSyncFlag(1);
                //	update(_oldPay);

                _oldPay.setSyncFlag(0);
                //将pay发送到消息总线
                if (logger.isDebugEnabled()) {
                    logger.debug("将支付订单[" + pay2.getTransactionId() + "]发送到消息总线，支付状态:" + pay2.getCurrentStatus());
                }
                EisMessage m = new EisMessage();
                m.setCode(Operate.close.getId());
                m.setExtra("pay", pay2);
                m.setObjectType(ObjectType.pay.toString());
                messageService.send(messageBusName, m);
                m = null;
            } else {
                //非总线模式，本节点直接完成支付处理
                this.end(pay2);
            }
        }

        EisMessage result = new EisMessage();
        result.setCode(OpResult.success.getId());
        result.setExtra("pay", pay2);
        result.setObjectType(ObjectType.pay.toString());
        return result;
    }


    @Override
    public EisMessage begin(Pay pay) {
        return _startPayRemote(pay, null);

    }


    /*
     * 1、负责检查各项参数
     * 2、调用相应的支付处理器为用户完成下一步操作
     * 3、发送支付对象至消息总线，由对应的节点进行处理
     */
    protected EisMessage _startPayRemote(Pay pay, User payUser) {
        if (pay.getTransactionId() == null) {
            pay.setTransactionId(globalOrderIdService.generate(TxType.pay.getId()));
        }

        if (payUser == null) {
            if (pay.getPayFromAccountType() == UserTypes.partner.id) {
                payUser = partnerService.select(pay.getPayFromAccount());
                if (payUser != null) {
                    //商户模式，放入商户的一些配置参数
                    if (payUser.getBooleanExtra(DataName.forceReplacePayNameByProductName.toString())) {
                        pay.setExtra(DataName.forceReplacePayNameByProductName.toString(), "true");
                    }
                    if (payUser.getBooleanExtra(DataName.useOrderIdSuffixToPayName.toString())) {
                        pay.setExtra(DataName.useOrderIdSuffixToPayName.toString(), "true");
                    }
                }
            } else {
                payUser = frontUserService.select(pay.getPayFromAccount());
            }
        }
        if (pay.getPayTypeId() < 1) {
            pay.setPayTypeId(DEFULAT_PAY_TYPE);
        }
        PayType payType = payTypeService.select(pay.getPayTypeId());
        if (payType == null) {
            logger.error("找不到指定的支付方式:" + pay.getPayTypeId());
            return EisMessage.error(EisError.payTypeIsNull.getId(), "找不到指定的支付类型");
        }
        PayMethod payMethod = pay.getPayMethod();
        //如果没有支付方法，把支付方式的实例放入供后续使用
        if (payMethod == null) {
            payMethod = channelRobinService.getPayMethod(pay, payUser);
            if (payMethod == null) {
                logger.error("处理支付订单[" + pay.getTransactionId() + "]时找不到指定的支付方式payMethod");
                return EisMessage.error(EisError.payMethodIsNull.getId(), "找不到指定的支付方式");
            }
            pay.setPayMethod(payMethod.clone());
        }
        pay.setExtra("payTypeName", payType.getName());
        if (StringUtils.isBlank(pay.getExtra("openId"))) {
            if (payUser != null && payUser.getAuthKey() != null) {
                pay.setExtra("openId", payUser.getAuthKey());
            }
        }
        logger.debug("支付订单[" + pay.getTransactionId() + "]当前支付方式的费率是:" + payType.getPublicRate());
        //logger.info("当前支付方式的费率是:" + currentPayTypeMethodRelation.getRate());
        pay.setRate(payType.getPublicRate());
        pay.setPayMethodId(payMethod.getId());
        pay.setCurrentStatus(TxStatus.newOrder.getId());
        if (pay.getNotifyUrl() != null) {
            pay.setNotifyUrl(pay.getNotifyUrl().replaceAll("\\$\\{payMethodId\\}", String.valueOf(pay.getPayMethodId())).replaceAll("\\$\\{transactionId\\}", pay.getTransactionId()));
        }
        logger.info("处理支付订单[" + pay.getTransactionId() + "]，为用户[uuid=" + pay.getPayFromAccount() + ",用户类型=" + pay.getPayFromAccountType() + "]选择的支付类型[" + pay.getPayTypeId() + "]自动选择支付方法[" + payMethod.getId() + "],回调URL:" + pay.getNotifyUrl());

        String payProcessBeanName = payMethod.getProcessClass();

        PayProcessor payProcessor = applicationContextService.getBeanGeneric(payProcessBeanName.trim());
        if (payProcessor == null) {
            logger.error("处理支付订单[" + pay.getTransactionId() + "]时，找不到指定的支付处理器[" + payProcessBeanName + "]");
            return EisMessage.error(EisError.payProcessorIsNull.getId(), "系统异常");
        }

        long ts = System.currentTimeMillis();
        EisMessage msg = payProcessor.onPay(pay);
        long time = System.currentTimeMillis() - ts;
        logger.info("支付订单:{}提交耗时是{}ms", pay.getTransactionId(), time);
        if (mqEnabled) {
            //将pay发送到消息总线
            EisMessage m = new EisMessage();
            m.setCode(Operate.create.getId());
            m.setExtra("pay", pay);
            m.setObjectType(ObjectType.pay.name());
            logger.debug("将支付订单[" + pay + "]发送到消息总线");
            try {
                messageService.send(messageBusName, m);
                m = null;
            } catch (Exception e) {
                logger.error("消息总线异常:" + e.getMessage());
            }
        } else {
            this._createPay(pay);

        }
        //	pay.setCurrentStatus(oldStatus);


        return msg;
    }

    @Override
    public PayProcessor getProcessor(Pay pay) {
        Assert.notNull(pay, "尝试获取处理器的Pay对象不能为空");

        long payMethodId = 0;
        PayMethod payMethod = null;
        if (pay.getPayMethodId() > 0) {
            payMethodId = pay.getPayMethodId();
            payMethod = payMethodService.select(pay.getPayMethodId());
        } else {
            payMethod = channelRobinService.getPayMethod(pay, partnerService.select(pay.getPayFromAccount()));
        }
        if (payMethod == null) {
            logger.error("找不到指定的支付类型:" + payMethodId);
            return null;
        }


        String payProcessBeanName = payMethod.getProcessClass();

        PayProcessor payProcessor = applicationContextService.getBeanGeneric(payProcessBeanName.trim());

        if (payProcessor == null) {
            logger.error("找不到指定的支付处理器[" + payProcessBeanName + "]");
        }
        return payProcessor;
    }

    @Override
    public void onMessage(EisMessage eisMessage) {
        logger.debug("后台支付服务收到消息" + eisMessage);

        if (!handlerPay) {
            return;
        }
        if (eisMessage == null) {
            logger.error("得到的消息是空");
            return;
        }
        if (eisMessage.getObjectType() == null || !eisMessage.getObjectType().equals(ObjectType.pay.toString())) {
            eisMessage = null;
            return;
        }
        Pay pay = null;
        Object object = eisMessage.getExtra("pay");
        if (object instanceof Pay) {
            pay = (Pay) object;
        } else if (object instanceof LinkedHashMap) {
            ObjectMapper om = JsonUtils.getInstance();
            String textData = null;
            try {
                textData = om.writeValueAsString(object);
                pay = om.readValue(textData, Pay.class);
            } catch (Exception e) {
            }
        }
        if (pay == null) {
            logger.debug("消息中没有找到需要的对象pay");
            eisMessage = null;
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("消息指定的操作是[" + eisMessage.getCode() + "/" + Operate.findById(eisMessage.getCode()).getName() + ",syncFlag=" + pay.getSyncFlag() + "]");
        }
        if (eisMessage.getCode() == Operate.create.getId()) {
            this._createPay(pay);

        }
        if (eisMessage.getCode() == Operate.close.getId()) {
            try {
                this.end(pay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pay = null;

}

    protected void _createPay(Pay pay) {
        if (pay.getCurrentStatus() == Operate.jump.getId()) {
            pay.setCurrentStatus(TxStatus.inProcess.getId());
        }

        int rs = insert(pay);
        if (rs == 1 && mqEnabled) {
            if (logger.isDebugEnabled()) {
                logger.debug("支付订单[" + pay.getTransactionId() + "]创建成功，发送支付订单同步请求[payService.insert(" + pay + ")],syncFlag=" + pay.getSyncFlag());
            }
            pay.setSyncFlag(0);
            messageService.sendJmsDataSyncMessage(messageBusName, "payService", "insert", pay);
        } else {
            logger.error("支付订单[" + pay.getTransactionId() + "]创建失败，返回值:" + rs);
        }
        if (pay.getCurrentStatus() == TxStatus.success.id) {
            //订单直接成功，调用成功后处理逻辑
            try {
                this._postPay(pay, pay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    //本地更新数据
    public EisMessage end(Pay pay) throws Exception {
        if (!handlerPay) {
            if (mqEnabled) {
                logger.info("1当前节点不负责处理支付订单，消息服务器启用:{}也没有禁用，不处理订单:{}", mqEnabled, pay.getTransactionId());
                return null;
            }
        }
        Pay _oldPay = select(pay.getTransactionId());
        boolean directInsert = false;
        if (_oldPay == null) {
            logger.error("数据库中找不到支付订单[" + pay.getTransactionId() + ",仍然按照结束订单处理,先插入该订单");
            insert(pay);
            directInsert = true;
            _oldPay = pay;
            //return EisMessage.error(EisError.billNotExist.getId(), pay.getPayResultMessage());
        }
        if (_oldPay.getBalance() > 0) {
            logger.error("支付订单[" + pay.getTransactionId() + "已经进行了支付成功处理(balance > 0)，不再处理");
            return null;
        }

        if (_oldPay.getTtl() > 0) {
            //如果该订单已超过有效期，则不再进行处理
            if (DateUtils.addSeconds(_oldPay.getStartTime(), _oldPay.getTtl()).before(new Date())) {
                logger.error("支付订单:{}的创建时间是:{}，有效期是:{}，已超过有效期,不再处理", _oldPay.getTransactionId(), _oldPay.getStartTime(), _oldPay.getTtl());
                _oldPay.setExtraStatus(EisError.requestTimeout.id);
                pay.setCurrentStatus(_oldPay.getCurrentStatus());
                if (update(_oldPay) <= 0) {
                    logger.error("无法更新超时支付订单[" + _oldPay.getTransactionId() + "].");
                    return EisMessage.error(EisError.BILL_UPDATE_FAIL.getId(), pay.getPayResultMessage());
                }
                if (mqEnabled) {
                    messageService.sendJmsDataSyncMessage(messageBusName, "payService", "update", _oldPay);
                    logger.debug("超时支付订单[" + _oldPay.getTransactionId() + "]已更新，并发送同步请求[payService.update(item[" + pay.getTransactionId() + "]");
                }
                return EisMessage.error(OpResult.failed.getId(), pay.getPayResultMessage());
            }

        }

        int payUserTypeId = pay.getPayFromAccountType();
        //logger.debug("支付订单[" + pay.getTransactionId() + "]的付款人[" + pay.getPayFromAccount() + "]类型是:" + payUserTypeId);

        if (pay.getData() != null && pay.getData().size() > 0) {
            if (_oldPay.getData() == null) {
                _oldPay.initExtra();
            }
            _oldPay.getData().putAll(pay.getData());
        }
        if (!directInsert || (payUserTypeId != UserTypes.partner.id)) {
            if (_oldPay.getCurrentStatus() == pay.getCurrentStatus()) {
                logger.info("支付订单:{}状态未改变:{}", pay.getTransactionId(), pay.getCurrentStatus());
                return null;//FIXME
            }
        }
        _oldPay.setRealMoney(pay.getRealMoney());
        _oldPay.setOutOrderId(pay.getOutOrderId());
        _oldPay.setEndTime(new Date());
        logger.debug("尝试结束的支付订单[" + pay.getTransactionId() + "]请求结束状态是:" + pay.getCurrentStatus() + ",系统中旧的状态是:" + _oldPay.getCurrentStatus() + ",是否刚即时新增:" + directInsert);
        //只有已存在的支付的状态是处理中时，或者是支付订单的付款人是商户时，才判断新传入的pay对象状态是否是成功
        //if( directInsert || (payUserTypeId == UserTypes.partner.id && pay.getCurrentStatus() == TxStatus.success.id) || (_oldPay.getCurrentStatus() == TxStatus.inProcess.getId() && pay.getCurrentStatus() == TxStatus.success.getId())){// && changeMoney(pay) == Constants.OpResult.success.getId()){
        if (_oldPay.getCurrentStatus() == TxStatus.inProcess.getId() && pay.getCurrentStatus() == TxStatus.success.getId()) {// && changeMoney(pay) == Constants.OpResult.success.getId()){
            //只能成功旧状态是处理中的订单
            return _postPay(pay, _oldPay);

        } else {
            _oldPay.setCurrentStatus(pay.getCurrentStatus());

            if (update(_oldPay) <= 0) {
                logger.error("无法更新支付订单[" + _oldPay.getTransactionId() + "].");
                return EisMessage.error(EisError.BILL_UPDATE_FAIL.getId(), pay.getPayResultMessage());
            }
            if (mqEnabled) {
                messageService.sendJmsDataSyncMessage(messageBusName, "payService", "update", _oldPay);
                logger.debug("支付订单[" + _oldPay.getTransactionId() + "]已更新，并发送同步请求[payService.update(item[" + pay.getTransactionId() + "]");
            }
            return EisMessage.error(OpResult.failed.getId(), pay.getPayResultMessage());
        }


    }

    /**
     * 确认支付成功后的业务逻辑
     * 包括由异步通知返回的支付成功close消息，和内部卡券直接成功create消息
     * 当一次性就成功时，即create消息，那么pay和_oldPay应当是同一个对象
     *
     * @param pay
     * @param _oldPay
     * @return
     * @throws Exception
     */
    protected EisMessage _postPay(Pay pay, Pay _oldPay) throws Exception {
        if (_oldPay.getBalance() > 0) {
            logger.error("尝试执行成功后处理操作的支付订单:" + _oldPay.getTransactionId() + "已经有成功后账户余额balance=" + _oldPay.getBalance() + "，不再进行成功后处理");
            return EisMessage.error(EisError.BILL_ALREADY_EXIST.getId(), "已有balance的支付订单不能再次进行成功后处理");
        }
        Money money = new Money(_oldPay.getOwnerId());
        if (_oldPay.getPayToAccount() > 0) {
            money.setUuid(_oldPay.getPayToAccount());
        } else {
            money.setUuid(_oldPay.getPayFromAccount());
        }
        float plusMoney = 0f;
        float rate = 0f;




        /*
         * 检查系统是否配置了资金分成
         * 如果没有分成配置，那么将付款金额作为chargeMoney
         * 如果是分成配置，那么付款金额将计算分成比例后放入incomingMoney
         */

        boolean moneyShared = false;
        Money partnerMoney = null;

        long shareConfigId = 0;

        if (MONEY_SHARE_MODE > 0) {

            //如何计算分成由各自的shareConfigService实现
            ShareConfig shareConfig = shareConfigService.calculateShare(pay, null);


            if (shareConfig != null) {
                shareConfigId = shareConfig.getId();
                pay.setExtra("shareConfigId", String.valueOf(shareConfig.getId()));
                rate = shareConfig.getSharePercent();

                float commission = 0f;
                if (rate > 1) {
                    logger.info("分成配置[" + shareConfig + "]的分成比例大于，异常的分成比例");
                    return EisMessage.error(EisError.moneyRangeError.id, "分成比例异常");
                } else {
                    //先使用四舍五入计算手续费，否则如果先计算了真实收入，可能会因为真实收入四舍五入来减少了手续费
                    commission = (float) NumericUtils.round(pay.getRealMoney() * (1 - rate));
                    plusMoney = pay.getRealMoney() - commission;
                    //plusMoney = (float)NumericUtils.round(pay.getRealMoney() * rate);
                }

                //计算扣除的手续费
                //float commission = pay.getRealMoney() - plusMoney;
                pay.setCommission(commission);

                moneyShared = true;
                partnerMoney = new Money(shareConfig.getShareUuid(), pay.getOwnerId());
                logger.info("为支付交易[" + pay.getTransactionId() + "]根据条件[objectType=pay,shareUuid=" + shareConfig.getShareUuid() + ",currentStatus=" + BasicStatus.normal.getId() + "]得到的特定配置shareConfig是" + shareConfig.getId() + ",默认配置:" + shareConfig.isDefaultConfig() + ",分成比例是:" + rate + ",实际分成金额是:" + plusMoney);
                if (MONEY_SHARE_MODE == ShareConfig.MONEY_SHARE_MODE_TO_CHANNEL) {
                    //C端分成给商户
                    //给上级分润到多少级
                    int moneyShareUpLevel = (int) shareConfig.getLongExtra(DataName.MONEY_SHARE_UP_LEVEL.toString());
                    long upLevelUuid = shareConfig.getShareUuid();
                    float upLevelTotalSharedMoney = 0;
                    if (moneyShareUpLevel > 0) {
                        float totalSharePercent = 0;
                        for (int i = 1; i <= moneyShareUpLevel; i++) {
                            float shareForUpLevelRate = shareConfig.getFloatExtra(DataName.MONEY_SHARE_UP_LEVEL.toString() + "_" + i);
                            if (totalSharePercent + shareForUpLevelRate > rate) {
                                logger.error("多级分润级别[" + i + "]的分成比例:" + shareForUpLevelRate + "加上已分配比例:" + totalSharePercent + ",已超过可分配最大比例:" + rate);
                                break;
                            }

                            User partner = partnerService.select(upLevelUuid);
                            if (partner == null) {
                                logger.error("在进行第" + i + "级分润时找不到指定的用户:" + upLevelUuid);
                                break;
                            }
                            if (partner.getParentUuid() == 0) {
                                logger.info("在进行第" + i + "级分润时，该用户:" + upLevelUuid + "已经没有上级商户");
                                break;
                            }
                            upLevelUuid = partner.getParentUuid();
                            Money upMoney = new Money(upLevelUuid, pay.getOwnerId());
                            if (shareForUpLevelRate > 1) {
                                upMoney.setIncomingMoney(shareForUpLevelRate);
                            } else {
                                upMoney.setIncomingMoney(pay.getRealMoney() * shareForUpLevelRate);
                            }
                            upLevelTotalSharedMoney += upMoney.getIncomingMoney();
                            upMoney.setMemo(MoneyMemory.支付分成收入.toString());
                            logger.info("进行第" + i + "级分润,为分润用户[" + upLevelUuid + "]分润:" + upMoney.getIncomingMoney() + ",上级总分润:" + upLevelTotalSharedMoney);
                            //moneyService.plus(upMoney);
                        }
                    }

                    partnerMoney.setIncomingMoney(plusMoney - upLevelTotalSharedMoney);
                    partnerMoney.setMemo(MoneyMemory.支付分成收入.toString());

                    logger.info("交易订单[" + pay.getTransactionId() + "]共进行" + moneyShareUpLevel + "级分润,上级总分润:" + upLevelTotalSharedMoney + ",当前经销商分成:" + partnerMoney.getIncomingMoney());


                } else {
                    //	pay.setShareConfigId(shareConfig.getShareConfigId());
                    money.setIncomingMoney(plusMoney);
                    //使用money中的transitMoney作为当前可提现金额
                }

                int withdrawTypeId = 0;
                User partner = null;
                if (pay.getPayFromAccountType() == UserTypes.partner.id) {
                    partner = partnerService.select(pay.getPayFromAccount());
                    if (partner == null) {
                        logger.warn("找不到支付交易:" + pay.getTransactionId() + "对应的商户:" + pay.getPayFromAccount());
                    } else {
                        withdrawTypeId = (int) partner.getLongExtra(DataName.withdrawType.toString());
                    }
                }
                if (withdrawTypeId > 0) {
                    WithdrawType withdrawType = withdrawTypeService.select(withdrawTypeId);
                    if (withdrawType == null) {
                        logger.info("支付订单:" + pay.getTransactionId() + "找不到对应商户:" + partner.getUuid() + "的提现类型配置withdrawType");
                    } else {
                        //提现周期

                        String arraivePeriod = withdrawType.getArrivePeriod();
                        if (StringUtils.isBlank(arraivePeriod)) {
                            arraivePeriod = WithdrawType.DEFAULT_WITHDRAW_ARRIVE_PERIOD;
                            logger.info("分成配置[" + shareConfig.getId() + "]提现的提现到账期是空，使用默认到账周期:" + arraivePeriod);
                        }
                        logger.info("分成配置[" + shareConfig.getId() + "]提现的提现到账期:" + arraivePeriod);
                        if (arraivePeriod.equalsIgnoreCase("d0")) {
                            //T+0，可以提现当天的资金
                            if (MONEY_SHARE_MODE == ShareConfig.MONEY_SHARE_MODE_TO_CHANNEL) {
                                partnerMoney.setIncomingMoney(plusMoney);
                            } else {
                                money.setTransitMoney(plusMoney);
                            }
                        }

                    }
                }


            }
        }

        money.setMemo(MoneyMemory.支付收入.toString());

        if (!moneyShared || MONEY_SHARE_MODE != ShareConfig.MONEY_SHARE_MODE_TO_USER) {
            rate = 1;
            plusMoney = pay.getRealMoney();
            if (pay.getMoneyTypeId() == MoneyType.coin.getId()) {
                money.setCoin(plusMoney);
                logger.info("支付交易[" + pay.getTransactionId() + "]不需要分成，或系统为经销商分成模式，将资金[" + pay.getRealMoney() + "]放入coin资金");
            } else if (pay.getMoneyTypeId() == MoneyType.point.getId()) {
                money.setPoint(plusMoney);
                logger.info("支付交易[" + pay.getTransactionId() + "]不需要分成，或系统为经销商分成模式，将资金[" + pay.getRealMoney() + "]放入point资金");
            } else {
                money.setChargeMoney(plusMoney);
                logger.info("支付交易[" + pay.getTransactionId() + "]不需要分成，或系统为经销商分成模式，将资金[" + pay.getRealMoney() + "]放入chargeMoney资金");
            }
        }

        if (pay.getEndTime() == null) {
            pay.setEndTime(new Date());
        }


        if (pay.getCurrentStatus() != _oldPay.getCurrentStatus()) {
            //在异步模式下更新订单信息，如果直接返回了成功，则不需要
            //XXX 必须以锁定形式更新，才能确保订单不会被重复处理
            _oldPay.setCurrentStatus(pay.getCurrentStatus());
            _oldPay.setLockStatus(TxStatus.inProcess.getId());
            _oldPay.setRealMoney(pay.getRealMoney());
            _oldPay.setRate(rate);
            _oldPay.setCommission(pay.getCommission());
            _oldPay.setBalance(pay.getBalance());
            _oldPay.setExtra("shareConfigId", String.valueOf(shareConfigId));
            if (update(_oldPay) != 1) {
                logger.error("无法更新支付订单[" + _oldPay.getTransactionId() + "].");
                return EisMessage.error(EisError.BILL_UPDATE_FAIL.getId(), pay.getPayResultMessage());
            }

            //更新成功才能加钱
            //writeStat(pay);

			/*if(MONEY_SHARE_MODE == ShareConfig.MONEY_SHARE_MODE_TO_CHANNEL){
				if(moneyShared){
					if(partnerMoney == null){
						logger.error("当前分成模式是2，但是parnterMoney为空");
					} else {
						moneyService.plus(partnerMoney);
						logger.info("为账户[" + money.getUuid() + "]的渠道[" + partnerMoney.getUuid() + "]增加收入资金,付款面值：" + pay.getRealMoney() + "，增加后渠道资金账户余额:" + partnerMoney.getIncomingMoney());
					}
				}
			} 
			moneyService.plus(money);*/
            if (moneyShared && MONEY_SHARE_MODE == ShareConfig.MONEY_SHARE_MODE_TO_USER) {
                logger.info("为账户[" + money.getUuid() + "]增加收入资金,付款面值：" + pay.getRealMoney() + "，实际增加" + plusMoney + "，增加后账户收入资金inComingMoney余额:" + money.getIncomingMoney());
                _oldPay.setBalance(money.getIncomingMoney());
            } else {
                logger.info("为账户[" + money.getUuid() + "]增加充值资金,付款面值：" + pay.getRealMoney() + "，实际增加" + plusMoney + "，增加后账户充值资金chargeMoney余额:" + money.getChargeMoney());
                _oldPay.setBalance(money.getChargeMoney());
            }
            //无条件更新一遍
            _oldPay.setLockStatus(0);
            _oldPay.setSyncFlag(0);
            update(_oldPay);

            if (_oldPay.getRefBuyTransactionId() != null) {
                //有对应的购买订单，发送请求完成此交易
                logger.debug("支付订单[" + _oldPay.getTransactionId() + "]有对应的购买订单[" + _oldPay.getRefBuyTransactionId() + "]，尝试继续购买交易");
                TransPlan tp = transPlanService.select(0);

                TxExecutor transactionExecutor = applicationContextService.getBeanGeneric(tp.getProcessClass());

                if (transactionExecutor == null) {
                    logger.error("找不到指定的交易处理器:" + tp.getProcessClass());
                    return EisMessage.error(EisError.transactionProcessorNotFound.getId(), "找不到指定的交易处理器");
                }
                if (logger.isDebugEnabled())
                    logger.debug("尝试由交易处理器[" + transactionExecutor.toString() + "]处理支付交易[orderId=" + pay.getTransactionId() + "].");

                try {
                    transactionExecutor.begin(_oldPay);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                //如果没有对应的购买订单，那么才发送资金同步请求
                //	messageService.sendJmsDataSyncMessage(messageBusName, "moneyService", "plusLocal", money);
            }
            //2022.11.28 关闭了update的自动sync
            //update本身已经有这个步骤
            messageService.sendJmsDataSyncMessage(messageBusName, "payService", "update", _oldPay);
            //logger.debug("支付订单[" + _oldPay.getTransactionId() + "]已更新，并发送同步请求[payService.update(" + pay.getTransactionId() + ")");

        }


        return EisMessage.success(OpResult.success.getId(), pay.getPayResultMessage());
    }


    protected void writeStat(Pay pay) {
        //放入当前支付渠道这一小时的成功金额
        String hour = new SimpleDateFormat("HH").format(new Date());
        String key = "PayMethod#SuccessMoney#" + hour + "#" + pay.getPayMethodId();
        int setMoney = Float.valueOf(pay.getRealMoney()).intValue();
        centerDataService.increaseBy(key, setMoney, setMoney, 3600);

        //放入当前支付渠道当天的成功金额
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        key = "PayMethod#SuccessMoney#" + day + "#" + pay.getPayMethodId();
        centerDataService.increaseBy(key, setMoney, setMoney, 3600 * 24);
    }


    @Override
    public int refund(Pay orig, Pay refund) {
        orig.setCurrentStatus(TxStatus.refunding.id);
        int rs = 0;
        try {
            rs = update(orig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rs != 1) {
            logger.error("无法在退款前修改支付订单[" + orig.getTransactionId() + "]状态为退款中:" + rs);
            return rs;
        }
        PayProcessor payProcessor = getProcessor(orig);
        if (payProcessor == null) {
            logger.error("无法退款[" + orig.getTransactionId() + "]，根据支付方式[" + orig.getPayTypeId() + "]找不到指定的退款处理器");
            return EisError.payProcessorIsNull.id;
        }

        EisMessage e = payProcessor.onRefund(refund);
        if (e == null) {
            logger.error("无法退款[" + orig.getTransactionId() + ",，退款处理器[" + payProcessor + "]返回空");
            return -EisError.OBJECT_IS_NULL.id;
        }
        if (e.getCode() != OpResult.success.id) {
            logger.error("无法退款[" + orig.getTransactionId() + ",，退款处理器[" + payProcessor + "]返回不是退款成功，而是:" + e.getCode());
        } else {
            orig.setCurrentStatus(TxStatus.refunded.id);
        }
        try {
            update(orig);
            insert(refund);

        } catch (Exception e1) {
            e1.printStackTrace();
            return EisError.DATA_UPDATE_FAIL.id;
        }
        return e.code;

    }


    @Override
    public Map<String, String> generateClientResponseMap(Pay pay) {
        final DecimalFormat df = new DecimalFormat("0.00");

        Map<String, String> param = new HashMap<String, String>();
        param.put("transactionId", pay.getTransactionId());
        param.put("orderId", pay.getInOrderId());
        param.put("requestMoney", df.format(pay.getFaceMoney()));
        param.put("successMoney", df.format(pay.getRealMoney()));
        param.put("result", String.valueOf(pay.getCurrentStatus()));
        param.put("timestamp", String.valueOf(new Date().getTime()));


        User partner = null;
        long forceUseSystemAccount = pay.getLongExtra("FORCE_USE_INTERNAL_PAY_FROM_ACCOUNT");
        if (forceUseSystemAccount > 0) {
            partner = partnerService.select(forceUseSystemAccount);
        } else {
            partner = partnerService.select(pay.getPayFromAccount());
        }
        if (partner == null) {
            logger.error("根据UUID[" + pay.getPayFromAccount() + "]找不到合作伙伴");
            return null;
        }

        if (partner.getCurrentStatus() != UserStatus.normal.getId()) {
            logger.error("用户[" + pay.getPayFromAccount() + "]状态异常[" + partner.getCurrentStatus() + "]");
            return null;
        }

        String loginKey = partner.getExtra(DataName.supplierLoginKey.toString());
        if (StringUtils.isBlank(loginKey)) {
            logger.error("用户[" + partner.getUuid() + "]配置中没有supplierLoginKey");
            return null;
        }

        List<String> keys = new ArrayList<String>(param.keySet());
        Collections.sort(keys);
        StringBuffer sb = new StringBuffer();
        for (String key : keys) {
            String value = param.get(key);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            sb.append(key);
            sb.append('=');
            sb.append(value);
            sb.append('&');
        }

        String signString = sb.toString().replaceAll("&$", "");

        signString += "&key=" + loginKey;
        String sign = DigestUtils.md5Hex(signString);
        logger.debug("用源[" + signString + "]生成签名:" + sign);
        param.put("sign", sign);

        //以下为不参与签名的参数,NetSnake,2017-06-20
        param.put("beginTime", ThreadHolder.defaultTimeFormatterHolder.get().format(pay.getStartTime()));
        if (pay.getEndTime() != null) {
            param.put("endTime", ThreadHolder.defaultTimeFormatterHolder.get().format(pay.getEndTime()));
        } else {
            param.put("endTime", "");
        }
        if (StringUtils.isNotBlank(pay.getPayCardType())) {
            param.put("payCardType", pay.getPayCardType());
        }
        return param;

    }

    @Override
    @Async
    public void sendNotifyAsync(BaseEntity obj) {

        if (obj == null) {
            logger.error("尝试发送异步通知的Item为空");
            return;
        }
        if (!(obj instanceof Item)) {
            logger.error("尝试发送异步通知的对象不是Item");
            return;
        }

        Pay pay = (Pay) obj;
        String notifyUrl = pay.getInNotifyUrl();
        if (StringUtils.isBlank(notifyUrl)) {
            logger.error("尝试发送的pay对象没有inNotifyUrl");
            return;
        }

        Map<String, String> param = generateClientResponseMap(pay);
        if (param == null || param.size() < 1) {
            logger.error("尝试跳转的pay对象生成参数为空");
            return;
        }

        long customNotifySendRetry = pay.getLongExtra(DataName.maxNotifyCount.toString());
        if (customNotifySendRetry <= 0) {
            customNotifySendRetry = TxConstants.notifySendRetry;
        }
        long customNotifySendRetryInterval = pay.getLongExtra(DataName.notifySendRetryInterval.toString());
        if (customNotifySendRetryInterval <= 0) {
            customNotifySendRetryInterval = TxConstants.notifySendRetryInterval;
        }

        boolean sendSuccess = false;
        int sendCount = 0;

        for (int i = 0; i < customNotifySendRetry; i++) {
            sendCount = i + 1;
            if (logger.isDebugEnabled()) {
                logger.debug("第" + (i + 1) + "次为交易[" + pay.getTransactionId() + "]发送异步通知，共尝试" + TxConstants.notifySendRetry + "次");
            }
            String result = null;
            result = sendNotifySync(pay);
            logger.debug("第" + (i + 1) + "次为交易[" + pay.getTransactionId() + "]发送异步通知，结果:" + result);
            if (isValidResponse(pay, result)) {
                sendSuccess = true;
                break;
            }
            try {
                Thread.sleep(customNotifySendRetryInterval * 1000 * (i + 1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        logger.info("交易[" + pay.getTransactionId() + "]通知发送是否成功:" + sendSuccess + ",发送次数:" + sendCount);
		/*if(!sendSuccess){
			FailedNotify failedNotify = new FailedNotify(item.getTransactionId());
			failedNotifyService.replace(failedNotify);
			logger.info("交易[" + item.getTransactionId() + "]在尝试" + sendCount + "次后仍然失败，记录为失败通知");
		} else {
			failedNotifyService.delete(item.getTransactionId());
			logger.info("交易[" + item.getTransactionId() + "]在尝试" + sendCount + "次后发送成功，从失败通知中去除");
		}
		return;

		logger.info("交易[" + pay.getTransactionId() + "]通知发送结果:" + result);	*/
    }


    @Override
    public String sendNotifySync(BaseEntity obj) {

        if (obj == null) {
            logger.error("尝试发送支付订单异步通知的对象为空");
            return null;
        }
        if (!(obj instanceof Pay)) {
            logger.error("尝试发送支付订单异步通知的对象不是Pay，是:{}", obj.getClass().getSimpleName());
            return null;
        }

        Pay pay = (Pay) obj;


        String notifyUrl = pay.getInNotifyUrl();
        if (StringUtils.isBlank(notifyUrl)) {
            logger.error("尝试发送的pay[" + pay.getTransactionId() + "]没有inNotifyUrl");
            return null;
        }
        String localData = notifyUrl.toLowerCase().replaceAll("https", "").replaceAll("http", "");
        if (localData.startsWith("localhost") || localData.startsWith("127.0.0.1")) {
            logger.warn("尝试发送的pay[" + pay.getTransactionId() + "]其inNotifyUrl为本地地址");
        }

        Map<String, String> param = generateClientResponseMap(pay);
        if (param == null || param.size() < 1) {
            logger.error("尝试跳转的pay对象生成参数为空");
            return null;
        }


        NameValuePair[] requestData = new NameValuePair[param.size()];
        int i = 0;
        for (String key : param.keySet()) {
            requestData[i] = new NameValuePair(key, param.get(key));
            i++;
        }

        StringBuffer sb = new StringBuffer();
        for (NameValuePair pair : requestData) {
            sb.append(pair.getName() + "=" + pair.getValue() + "&");
        }
        logger.info("尝试为交易[" + pay.getTransactionId() + "]发送异步通知到:" + notifyUrl + "?" + sb.toString());


        String result = null;

        try {
            result = HttpUtils.postData(notifyUrl, requestData);
        } catch (Exception e) {
            logger.warn("在发送交易[" + pay.getTransactionId() + "]异步通知时发生异常:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        logger.info("交易[" + pay.getTransactionId() + "]通知发送结果:" + result);

        if (isValidResponse(pay, result)) {
            failedNotifyService.delete(pay.getTransactionId());
            logger.info("支付订单[" + pay.getTransactionId() + "]直接发送成功，从失败通知中去除");
        } else {
            FailedNotify failedNotify = new FailedNotify(pay.getTransactionId());
            failedNotifyService.replace(failedNotify);
            logger.info("交易[" + pay.getTransactionId() + "]直接发送失败，记录为失败通知");
        }
        return result;
    }


    public static boolean isValidResponse(Pay pay, String response) {
        if (StringUtils.isBlank(response)) {
            return false;
        }
        if (response.trim().startsWith(pay.getInOrderId())
                || response.trim().equalsIgnoreCase("true")
                || response.trim().equalsIgnoreCase("ok")
                || response.trim().equalsIgnoreCase("success")
                || response.trim().equals("1")) {
            return true;
        }
        return false;
    }


    @Override
    public Pay lock(Pay pay) {
        return mapper.lock(pay);
    }

}
