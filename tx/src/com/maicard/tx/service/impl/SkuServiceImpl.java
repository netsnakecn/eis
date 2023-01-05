package com.maicard.tx.service.impl;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.tx.dao.mapper.SkuMapper;
import com.maicard.tx.entity.Sku;
import com.maicard.tx.service.SkuService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuServiceImpl extends AbsGlobalSyncService<Sku,SkuMapper> implements SkuService{



}
