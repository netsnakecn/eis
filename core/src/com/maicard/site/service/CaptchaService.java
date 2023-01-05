package com.maicard.site.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maicard.base.CriteriaMap;
import com.maicard.site.entity.Captcha;


public interface CaptchaService {
	
	public int insert(Captcha captcha);
	
	public int update(Captcha captcha);
	
	public int delete(Captcha captcha);
	
	public List<Captcha> list(CriteriaMap captchaCriteria);
	
	
	public Captcha get(CriteriaMap captchaCriteria);

	void setToCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String word);

	String getFromCookie(HttpServletRequest request, HttpServletResponse response, String cookieName);



	boolean verify(HttpServletRequest request, HttpServletResponse response, String cookieName, String word);


}
