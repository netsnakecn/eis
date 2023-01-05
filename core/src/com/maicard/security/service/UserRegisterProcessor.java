package com.maicard.security.service;

import com.maicard.core.entity.EisMessage;
import com.maicard.security.entity.User;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserRegisterProcessor {
    EisMessage register(HttpServletRequest request, HttpServletResponse response, ModelMap map, User frontUser);
}
