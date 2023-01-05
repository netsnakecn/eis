package com.maicard.security.service;

import java.util.List;

import com.maicard.base.GlobalSyncService;
import com.maicard.base.IService;
import com.maicard.security.constants.SignType;
import com.maicard.security.dto.UserDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;

import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.EisMessage;
import com.maicard.security.entity.User;

import javax.servlet.http.HttpServletRequest;


public interface FrontUserService extends GlobalSyncService<User> {
	User login(CriteriaMap frontCriteriaMap);

}
