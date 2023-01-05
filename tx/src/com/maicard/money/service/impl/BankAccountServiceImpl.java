package com.maicard.money.service.impl;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.service.DataDefineService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.mb.service.MessageService;
import com.maicard.money.dao.mapper.BankAccountMapper;
import com.maicard.money.entity.BankAccount;
import com.maicard.money.service.BankAccountService;
import com.maicard.utils.JsonUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class BankAccountServiceImpl extends AbsGlobalSyncService<BankAccount,BankAccountMapper> implements BankAccountService {

	@Override
	public int setDefaultAdd(BankAccount bankAccount)
	{
		return mapper.setDefaultAdd(bankAccount);
	}

	@Override
	public int createIfNotExist(BankAccount bankAccount) {
		Assert.notNull(bankAccount,"尝试不重复创建的银行帐号为空");
		Assert.isTrue(bankAccount.getUuid() != 0,"尝试不重复创建的银行帐号，所属用户不能为空");
		Assert.notNull(bankAccount.getBankName(),"尝试不重复创建的银行帐号，银行名称为空");
		Assert.notNull(bankAccount.getBankName(),"尝试不重复创建的银行帐号，银行名称为空");
		Assert.notNull(bankAccount.getBankAccountName(),"尝试不重复创建的银行帐号，开户名字或帐号为空");
		if(bankAccount.getId() > 0){
			logger.error("不重复创建的银行帐号ID大于0,不执行创建尝试");
			return 0;
		}
		int rs = 0;
		try{
			rs = this.insert(bankAccount);
		}catch(Exception e) {
			logger.error("无法创建账号:{}，错误原因:{}", JsonUtils.toStringFull(bankAccount), e.getMessage());
			e.printStackTrace();
			return 0;
		}
		if(rs == 1){
			messageService.sendJmsDataSyncMessage(null, "bankAccountService", "insert", bankAccount);
		}
		return rs;
		
		
	}


}
