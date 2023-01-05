package com.maicard.money.service.impl;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.money.dao.mapper.WithdrawTypeMapper;
import com.maicard.money.entity.WithdrawType;
import com.maicard.money.service.WithdrawTypeService;
import org.springframework.stereotype.Service;

@Service
public class WithdrawTypeServiceImpl extends AbsGlobalSyncService<WithdrawType,WithdrawTypeMapper> implements WithdrawTypeService {


}
