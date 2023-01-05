package com.maicard.money.service.impl;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.money.dao.mapper.PayTypeMapper;
import com.maicard.money.entity.PayType;
import com.maicard.money.service.PayTypeService;
import org.springframework.stereotype.Service;

@Service
public class PayTypeServiceImpl extends AbsGlobalSyncService<PayType, PayTypeMapper> implements PayTypeService {



}
