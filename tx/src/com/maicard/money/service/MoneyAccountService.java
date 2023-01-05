package com.maicard.money.service;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.base.IService;
import com.maicard.core.entity.EisMessage;
import com.maicard.money.constants.AccountTypeEnum;
import com.maicard.money.entity.MoneyAccount;
import com.maicard.money.entity.MoneyFlow;
import com.maicard.security.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MoneyAccountService extends GlobalSyncService<MoneyAccount> {

    @Transactional(rollbackFor = Exception.class)
    boolean tx(MoneyFlow moneyFlow, MoneyAccount moneyAccount);

    public int updateAccountStatus(MoneyAccount v);

    public MoneyAccount queryByAccountNo(String accountNo);

	List<MoneyAccount> getTotal(CriteriaMap criteria);


    MoneyAccount getByTypeAndUuid(AccountTypeEnum accountType, long uuid);

    MoneyAccount createIfNotExist(User user, AccountTypeEnum accountTypeEnum);

    @Transactional(rollbackFor = Exception.class)
    EisMessage txs(MoneyFlow... flows);
}
