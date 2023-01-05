package com.maicard.money.service.task;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.core.constants.Constants;
import com.maicard.core.service.CenterDataService;
import com.maicard.money.entity.Money;

/**
 * 对系统中的资金账户，同步到数据库
 *
 *
 * @author GHOST
 * @date 2017-12-19
 */

@Service
public class MoneySyncTask extends BaseService {
	
	@Resource
	private CenterDataService centerDataService;
	
	public void run(){
		this.sync();
	}

	private void sync(){
		Set<String> moneyKeys = centerDataService.listKeys(Money.MONEY_INSTANCE_PREFIX);
		if(moneyKeys == null || moneyKeys.size() < 1){
			logger.info("REDIS中没有任何资金账户");
			return;
		}
		for(String moneyKey : moneyKeys){
			String lockKey = moneyKey.replaceFirst(Money.MONEY_INSTANCE_PREFIX,Money.MONEY_LOCK_PREFIX);
			if(!centerDataService.setIfNotExist(lockKey, lockKey, Constants.DISTRIBUTED_DEFAULT_LOCK_SEC)){
				logger.info("无法对资金账户#{}进行加锁", moneyKey);
				continue;
			}
		}
	}
	
}
