package com.maicard.money.service;

import com.maicard.money.entity.Pay;
import com.maicard.money.entity.PayMethod;
import com.maicard.security.entity.User;

public interface ChannelRobinService {

	PayMethod getPayMethod(Pay pay, User partner);

}
