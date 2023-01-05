package com.maicard.site.service.impl;

import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;

import com.maicard.base.BaseService;
import com.maicard.core.service.CenterDataService;
import com.maicard.site.service.TokenService;
 

@Service
public class TokenServiceImpl extends BaseService implements TokenService {
	
	@Resource
	private CenterDataService centerDataService;
	
	private final int pageTimeout = 3600;
	private final String pageTokenName = "PAGE_TOKEN";

	@Override
	public void writeToken(ModelMap map) {
		String tokenKey = UUID.randomUUID().toString();		
		centerDataService.setForce(tokenKey, tokenKey,  pageTimeout * 1000);		
		map.put(pageTokenName, tokenKey);
		logger.debug("向map中写入" + pageTokenName + "=>" + tokenKey);
		return;
	}

	@Override
	public boolean tokenIsValid(HttpServletRequest request) {
		String tokenKey = ServletRequestUtils.getStringParameter(request, pageTokenName, null);
		if(StringUtils.isBlank(tokenKey)){
			logger.error("页面未提交参数:" + pageTokenName);
			return false;
		}
		String tokenValue = centerDataService.getExclusive(tokenKey, true);
		logger.debug("在缓存中查找key=" + pageTokenName + "的数据,结果:" + tokenValue);
		if(StringUtils.isBlank(tokenValue)){
			return false;			
		}		
		return true;
	}


	
	

}
