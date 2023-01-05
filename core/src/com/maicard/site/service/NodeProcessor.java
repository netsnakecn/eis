package com.maicard.site.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.ui.ModelMap;

import com.maicard.security.entity.User;


public interface NodeProcessor {	
	String index(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception;
	
	String detail(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception;

	void writeExtraData(ModelMap map, User frontUser, Map<String, Object> parameter);

}
