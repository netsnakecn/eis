package com.maicard.tx.service.impl;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.EisError;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.GlobalOrderIdService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.mb.service.MessageService;
import com.maicard.money.constants.AccountTypeEnum;
import com.maicard.money.constants.FundTypeEnum;
import com.maicard.money.constants.TxStatus;
import com.maicard.money.constants.TxType;
import com.maicard.money.entity.MoneyFlow;
import com.maicard.money.service.MoneyFlowService;
import com.maicard.money.util.MoneyAccountUtils;
import com.maicard.money.util.MoneyUtils;
import com.maicard.security.entity.User;
import com.maicard.tx.dao.mapper.CouponMapper;
import com.maicard.tx.entity.Coupon;
import com.maicard.tx.entity.CouponModel;
import com.maicard.tx.service.CouponModelService;
import com.maicard.tx.service.CouponProcessor;
import com.maicard.tx.service.CouponService;
import com.maicard.utils.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class CouponServiceImpl extends AbsGlobalSyncService<Coupon,CouponMapper> implements CouponService {




    @Resource
    private ApplicationContextService applicationContextService;

    @Resource
    private MoneyFlowService moneyFlowService;

    @Resource
    private CouponModelService couponModelService;

    @Resource
    private GlobalOrderIdService globalOrderIdService;


    //private final boolean usingActivityForCoupon = true;

    @Override
    public int insert(Coupon coupon) {
        if (coupon.getFetchTime() == null) {
            coupon.setFetchTime(new Date());
        }
        if (coupon.getTransactionId() == null) {
            coupon.setTransactionId(globalOrderIdService.generate(TxType.coupon.getId()));
        }
        int rs = 0;
        try {
            rs = mapper.insert(coupon);
        } catch (Exception e) {
            logger.error("插入数据失败:" + e.getMessage());
        }
        return rs;
    }









    /**
     * 把点券发布到中央缓存以便于其他节点获取
     */
    @Override
    public void couponPublish() {


    }

    @Override
    public EisMessage fetch(CriteriaMap couponCriteria) {

        CouponModel couponModel = null;
        CouponProcessor bean = null;
        String couponCode = couponCriteria.get("couponCode");
        long ownerId = couponCriteria.getLongValue("ownerId");

        long couponModelId = couponCriteria.getLongValue("couponModelId");
        if (couponModelId > 0) {
            couponModel = couponModelService.select(couponModelId);
        } else {
            CriteriaMap couponModelCriteria = CriteriaMap.create(ownerId);
            couponModelCriteria.put("couponCode", couponCode);
            couponModelCriteria.putArray("currentStatus", BasicStatus.normal.getId());
            List<CouponModel> couponModelList = couponModelService.list(couponModelCriteria);
            if (couponModelList == null || couponModelList.size() < 1) {
                logger.warn("找不到点券编码为[" + couponCode + "]的点券产品");
                return EisMessage.error(EisError.productNotExist.id, "找不到指定的卡券产品");

            } else if (couponModelList.size() != 1) {
                logger.warn("点券编码为[" + couponCode + "]的点券产品数量异常，为" + couponModelList.size());
                return EisMessage.error(EisError.productNotExist.id, "找不到指定的卡券产品");
            } else {
                couponModel = couponModelList.get(0);
            }
        }
        if (couponModel == null) {
            logger.warn("根据条件[couponModelId=" + couponModelId + ",couponCode=" + couponCode + "]找不到任何点券产品");
            return EisMessage.error(EisError.productNotExist.id, "找不到指定的卡券产品");
        }
        if (StringUtils.isBlank(couponModel.getProcessor())) {
            logger.error("点券产品[" + couponModelId + "]的处理器为空");
            return EisMessage.error(EisError.processorIsNull.id, "找不到指定的处理器");
        }
        logger.debug("通过点券编码为[" + couponModelId + "]找到对应产品处理器是:" + couponModel.getProcessor());
        bean = applicationContextService.getBeanGeneric(couponModel.getProcessor());

        if (bean == null) {
            logger.error("找不到点券:" + couponModelId + "的处理器:" + couponModel.getProcessor());
            return EisMessage.error(EisError.processorIsNull.id, "找不到指定的处理器");
        }

        return bean.fetch(couponCriteria);

    }

    @Transactional(rollbackFor = Exception.class)
    public EisMessage consume(User frontUser, float couponAmount, String couponPassword) {

        String accountNo = MoneyAccountUtils.getAccountNo(AccountTypeEnum.CONSUMER_COUPON_ACCOUNT,frontUser.getUuid());
        CriteriaMap criteria = CriteriaMap.create(frontUser.getOwnerId()).put("couponPassword", couponPassword).putArray("currentStatus", BasicStatus.normal.id);
        Coupon coupon = selectOne(criteria);
        if (coupon == null) {
            logger.error("根据条件:" + JsonUtils.toStringFull(criteria) + "找不到任何点券");
            throw new EisException(EisError.CONSUME_FAIL.id);
        }
        Coupon lock = coupon.clone();
        lock.setLockStatus(coupon.getCurrentStatus());
        lock.setCurrentStatus(Coupon.STATUS_USED);
        lock.setUseTime(new Date());
        int rs = update(lock);
        if (rs <= 0) {
            logger.error("无法消费点券:" + couponPassword + "更新失败");
            throw new EisException(EisError.CONSUME_FAIL.id);
        }
        MoneyFlow moneyFlow1 = new MoneyFlow(frontUser.getOwnerId());
        moneyFlow1.setUuid(frontUser.getUuid());
        moneyFlow1.setAccountType(AccountTypeEnum.CONSUMER_COUPON_ACCOUNT.code);
        moneyFlow1.setTradeAmount(MoneyUtils.roundToCent(lock.getGiftMoney()));
        moneyFlow1.setTradeType(Coupon.TRADE_COUPON_INCOME);
        moneyFlow1.setClearStatus(TxStatus.success.name);
        moneyFlow1.setTransactionId(couponPassword);
        moneyFlow1.setFundType(FundTypeEnum.AVAL.name());
        moneyFlow1.setAccountNo(accountNo);
        moneyFlowService.insert(moneyFlow1);

        MoneyFlow moneyFlow2 = new MoneyFlow(frontUser.getOwnerId());
        moneyFlow2.setUuid(frontUser.getUuid());
        moneyFlow2.setAccountType(AccountTypeEnum.CONSUMER_COUPON_ACCOUNT.code);
        moneyFlow2.setTradeAmount(-MoneyUtils.roundToCent(lock.getGiftMoney()));
        moneyFlow2.setTradeType(Coupon.TRADE_COUPON_CONSUME);
        moneyFlow2.setClearStatus(TxStatus.success.name);
        moneyFlow2.setTransactionId(couponPassword);
        moneyFlow2.setFundType(FundTypeEnum.AVAL.name());
        moneyFlow2.setAccountNo(accountNo);
        moneyFlowService.insert(moneyFlow2);

        EisMessage msg = EisMessage.success();
        msg.setMessage( String.valueOf(lock.getGiftMoney()));
        return  msg;
    }

}
