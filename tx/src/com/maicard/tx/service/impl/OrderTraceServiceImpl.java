package com.maicard.tx.service.impl;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.tx.dao.mapper.OrderTraceMapper;
import com.maicard.tx.entity.OrderTrace;
import com.maicard.tx.service.OrderTraceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class OrderTraceServiceImpl extends AbsGlobalSyncService<OrderTrace, OrderTraceMapper> implements OrderTraceService{




}
