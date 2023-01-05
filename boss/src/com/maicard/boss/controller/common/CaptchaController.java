package com.maicard.boss.controller.common;

import java.awt.Color;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.maicard.base.BaseController;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.DataName;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.ConfigService;
import com.maicard.security.annotation.IgnorePrivilegeCheck;
import com.maicard.site.entity.Captcha;
import com.maicard.site.service.CaptchaService;
import com.maicard.utils.NumericUtils;

/*
 * 生成验证码图片
 */
@Controller
@RequestMapping("/captcha")
public class CaptchaController extends BaseController{
	
	@Resource
	private CaptchaService captchaService;
	
	@Resource
	private CenterDataService centerDataService;
	
	@Resource
	private ConfigService configService;
	
	int captchaLength = 4;
	String foreColorConfig = null;
	
	@PostConstruct
	public void init(){
		foreColorConfig = configService.getValue(DataName.partnerCaptchaForeColor.toString(), 0);
		if(foreColorConfig != null){		
			logger.debug("系统配置的验证码颜色定义是:" + foreColorConfig);
		} else {
			logger.debug("系统没有配置验证码颜色定义，使用默认黑色");
			foreColorConfig = "#000000";
		}
		
		captchaLength = configService.getIntValue(DataName.CAPTCHA_LENGTH.name(), 0);
		if(captchaLength < 1) {
			logger.debug("系统没有配置验证码长度，使用默认4个字符");
			captchaLength = 4;
		} else {
			logger.debug("系统配置的验证码长度是:" + captchaLength);

		}
		
	}

	
	@RequestMapping(method=RequestMethod.GET)
	@IgnorePrivilegeCheck
	public void list(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws UnsupportedEncodingException {
		CriteriaMap captchaCriteria = CriteriaMap.create();
		captchaCriteria.put("foreColor",foreColorConfig);
		captchaCriteria.put("maxLength",captchaLength);
		captchaCriteria.put("minLength",captchaLength);
		Captcha captcha = captchaService.get(captchaCriteria);
		if(captcha == null){
			logger.error("无法生成验证码");
			return;
		}		
		try {
			captchaService.setToCookie(request, response, null, captcha.getWord());
			response.getOutputStream().write(captcha.getImage());
			//ImageIO.write(captcha.getImage(), "png", response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@IgnorePrivilegeCheck
	@RequestMapping("/deleteRedis")
	public void deleteRedis(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws UnsupportedEncodingException {
		Set<String> keys = centerDataService.listKeys("GlobalUnique*");
		if(keys == null || keys.size() < 1){
			logger.warn("中央缓存中没有以GlobalUnique开头的缓存");
			return;
		}
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String key = it.next();
			logger.debug("删除键:" + key);
			centerDataService.delete(key);
		}
	}
}
