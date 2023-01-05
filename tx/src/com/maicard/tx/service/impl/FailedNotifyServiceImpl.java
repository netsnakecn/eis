package com.maicard.tx.service.impl;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsBaseService;
import com.maicard.tx.dao.mapper.FailedNotifyMapper;
import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.service.CenterDataService;
import com.maicard.tx.entity.FailedNotify;
import com.maicard.tx.service.FailedNotifyService;

@Service
public class FailedNotifyServiceImpl extends AbsBaseService<FailedNotify, FailedNotifyMapper> implements FailedNotifyService{

	@Resource
	private CenterDataService centerDataService;


	@Override
	public int delete(String transactionId) {
		return 0;
	}

	@Override
	public int replace(FailedNotify failedNotify) {
		return 0;
	}
}
