package com.maicard.money.service.impl;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.Constants;
import com.maicard.core.ds.DataSourceMapper;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.money.constants.AccountTypeEnum;
import com.maicard.money.constants.FundTypeEnum;
import com.maicard.money.dao.mapper.MoneyAccountMapper;
import com.maicard.money.entity.MoneyAccount;
import com.maicard.money.entity.MoneyFlow;
import com.maicard.money.service.MoneyAccountService;
import com.maicard.money.service.MoneyFlowService;
import com.maicard.money.util.MoneyAccountUtils;
import com.maicard.security.entity.User;
import com.maicard.utils.JsonUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Service
@DataSourceMapper(Constants.DB_MONEY)
public class MoneyAccountServiceImpl extends AbsGlobalSyncService<MoneyAccount,MoneyAccountMapper> implements MoneyAccountService {
 

    @Resource
    private MoneyFlowService moneyFlowService;
 


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean tx(MoneyFlow moneyFlow, MoneyAccount moneyAccount) {


        String text = JsonUtils.toStringFull(moneyFlow);
        logger.info("increase moneyFlow=" + text );
        if(StringUtil.isBlank(moneyFlow.getFundType())){
            moneyFlow.setFundType(FundTypeEnum.AVAL.name());
        }
        Assert.notNull(moneyFlow.getAccountNo(),"Tx account no could not be null");
        Assert.isTrue(moneyFlow.getTradeAmount() != 0,"Tx trade amount could not be 0");

        long tradeAmount = moneyFlow.getTradeAmount();
        int flowCount =  moneyFlowService.insert(moneyFlow);
        Assert.state(1 == flowCount, "账户流水插入失败:" + text);
        Map<String,Object> map = new HashMap<String,Object>(3);
        map.put("accountNo", moneyFlow.getAccountNo());
       // map.put("fundType", moneyAccount.getFfundType.getCode());
        map.put("tradeAmount", Math.abs(tradeAmount));
        map.put("version",moneyAccount.getVersion());
        map.put("fundType",moneyFlow.getFundType());

        String mapText = JsonUtils.toStringFull(map);
        int destCount = 0;
        if(tradeAmount < 0){
            destCount = mapper.decrease4OptLock(map);
        } else  {
            destCount = mapper.increase(map);
        }
        Assert.state(1 == destCount, "调账失败:" + mapText);
        logger.info("increase moneyFlow=" + text + ",map=" + mapText + ",success" );

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAccountStatus(MoneyAccount entity) {
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("accountNo", entity.getAccountNo());
        map.put("accountStatus", entity.getAccountStatus());
        return mapper.updateStatus(map);
    }

    @Override
    public List<MoneyAccount> getTotal(CriteriaMap map) {
        List<MoneyAccount> list = mapper.getTotal(map);
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list;
        }
    }



    @Override
    public MoneyAccount queryByAccountNo(String accountNo) {
        return mapper.queryByAccountNo(accountNo);
    }



    @Override
    public MoneyAccount getByTypeAndUuid(AccountTypeEnum accountType, long uuid) {
        String accountNumber = String.valueOf(uuid) + accountType.code;
        return selectOne(CriteriaMap.create().put("accountNo",accountNumber));
    }


    @Override
    public MoneyAccount createIfNotExist(User user, AccountTypeEnum accountTypeEnum) {
        CriteriaMap criteria = CriteriaMap.create(user.getOwnerId());
        criteria.put("uuid", user.getUuid());
        criteria.put("accountType", accountTypeEnum);
        List<MoneyAccount> moneyAccountList = list(criteria);
        MoneyAccount moneyAccount = null;
        if (moneyAccountList.size() <= 0) {
            String accountName = user.getUuid() + accountTypeEnum.code;
            String accountNo = "";
            logger.info("为用户:{}自动创建类型是:{}的基本资金账户", user.getUuid(), accountTypeEnum);
            accountNo = MoneyAccountUtils.getAccountNo(accountTypeEnum, user.getUuid());


            logger.info("为用户:{}自动创建类型是:{}的资金账户:{}", user.getUuid(), accountTypeEnum, accountNo);
            moneyAccount = new MoneyAccount(accountNo, accountTypeEnum, user.getUuid(), BasicStatus.normal.name(), accountName);
            moneyAccount.setAccountCategory(String.valueOf(user.getUserExtraTypeId()));
            insert(moneyAccount);
        } else {
            moneyAccount = moneyAccountList.get(0);
        }
        return moneyAccount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EisMessage txs(MoneyFlow... flows) {

        for (MoneyFlow moneyFlow : flows){
            tx(moneyFlow,null);

        }

       return EisMessage.success();



    }

}
