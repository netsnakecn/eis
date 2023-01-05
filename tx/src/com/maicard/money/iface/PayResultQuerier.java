package com.maicard.money.iface;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;

import com.maicard.core.entity.EisMessage;
import com.maicard.money.entity.Pay;

public interface PayResultQuerier {

	@Async
	void queryAsync(Pay pay, Map<String, String> params);

	EisMessage onQuery(Pay pay, Map<String, String> params);

}
