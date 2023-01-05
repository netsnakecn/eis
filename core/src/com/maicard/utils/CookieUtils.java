package com.maicard.utils;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public  class CookieUtils   {

	protected static final Logger logger = LoggerFactory.getLogger(CookieUtils.class);



	public static void addCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
			String cookieValue, int ttl, String domain, boolean httpOnly) {
		if(StringUtils.isBlank(domain)){
			domain = request.getServerName();
		}
		boolean secure = request.getScheme().equalsIgnoreCase("https");

		domain =  domain.replaceFirst("^\\.", "");
		Cookie[] cookies = request.getCookies();
		if(cookies == null){
			logger.debug("request中的cookies为空，当前请求:" + request.getServerName() + "没有任何Cookie");
		} else {
			for(Cookie c : cookies){
				if(c.getName().equalsIgnoreCase(cookieName)){
					c.setValue(cookieValue);
					c.setMaxAge(ttl);
					c.setHttpOnly(httpOnly);
					c.setDomain(domain);
					c.setPath("/");
					c.setSecure(secure);
					response.addCookie(c);
					logger.debug("在请求中找到了同名cookie[" + cookieName +"]，直接设置该Cookie[name=" + c.getName() + ",value=" + c.getValue() + ",domain=" + c.getDomain() + ",path=" + c.getPath() + ",ttl=" + c.getMaxAge() + ",httpOnly=" + c.isHttpOnly() + ", secure=" + c.getSecure() + ",version=" + c.getVersion());			
					return;
				}
			}
		}
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(ttl);
		cookie.setHttpOnly(httpOnly);
		cookie.setPath("/");
		cookie.setSecure(secure);

		boolean isDomainCookie = false;

		cookie.setDomain(domain);
		if(logger.isDebugEnabled()){
			logger.debug("在请求中未找到同名Cookie，添加" + (isDomainCookie ? "域名" : "主机") + "Cookie:" + ",name=" + cookie.getName() + ",value=" + cookie.getValue() + ",设置domain=" + domain + ", cookie domain:" + cookie.getDomain() + ",ttl:" + cookie.getMaxAge() + ",httpOnly:" + httpOnly + ",secure=" + cookie.getSecure());
		}

		response.addCookie(cookie);

	} 

	public static void addCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
			String cookieValue, int ttl, String domain) {
		addCookie(request, response, cookieName, cookieValue, ttl, domain, true);
	}


	public static String getCookie(HttpServletRequest request, String cookieName) {
		try{
			Cookie[] cookies = request.getCookies();
			if(cookies == null){
				logger.debug("request中的cookies为空，当前请求:" + request.getServerName() + "没有任何Cookie");
				return null;
			}
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(cookieName)){
					logger.debug("找到名称为[" + cookieName + "]的cookie[value=" + cookie.getValue() + ",domain=" + cookie.getDomain() + ",path=" + cookie.getPath() + ",ttl=" + cookie.getMaxAge() + ",httpOnly=" + cookie.isHttpOnly());
					return cookie.getValue();
				}				
			}
			logger.debug("找不到名字为[" + cookieName + "]的cookie");

		}catch(Exception e){
			e.printStackTrace();
		}	
		return null;
	}

	public static void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String domain) {

		Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);	
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		boolean isDomainCookie = false;
		if(StringUtils.isBlank(domain)){
			domain = request.getServerName();
		} 
		domain = domain.replaceFirst("^\\.", "");

		cookie.setDomain(domain);
		if(logger.isDebugEnabled()){
			logger.debug("1删除" + (isDomainCookie ? "域" : "主机") + "Cookie:" + ",name=" + cookie.getName() + ",value=" + cookie.getValue() + ",设置domain=" + domain + ", cookie domain:" + cookie.getDomain() + ",ttl:" + cookie.getMaxAge() + ",httpOnly:" + cookie.isHttpOnly());
		}

		response.addCookie(cookie);


		/*Cookie[] cookies = request.getCookies();
		if(cookies == null){
			logger.debug("request中的cookies为空，当前请求:" + request.getServerName() + "没有任何Cookie");
		} else {
			for(Cookie c : cookies){
				logger.debug("删除cookie[" + cookieName +"]后，当前还存在的cookie[name=" + c.getName() + ",value=" + c.getValue() + ",domain=" + c.getDomain() + ",path=" + c.getPath() + ",ttl=" + c.getMaxAge() + ",httpOnly=" + c.isHttpOnly());			
			}
		}
*/

		cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);	
		cookie.setPath("/");		
		cookie.setDomain(domain);
		cookie.setHttpOnly(false);
		/*if(logger.isDebugEnabled()){
			logger.debug("2删除" + (isDomainCookie ? "域" : "主机") + "Cookie:" + ",name=" + cookie.getName() + ",value=" + cookie.getValue() + ",设置domain=" + domain + ", cookie domain:" + cookie.getDomain() + ",ttl:" + cookie.getMaxAge() + ",httpOnly:" + cookie.isHttpOnly());
		}*/

		response.addCookie(cookie);
		/*try{			
			Cookie cookie = new Cookie(cookieName, null);
			cookie.setMaxAge(0);	
			//response.addCookie(cookie);	
			//logger.debug("删除默认Cookie:" + ",Cookie:" + cookie.getName() + ",value=" + cookie.getValue() + ", domain:" + cookie.getDomain() + ",ttl:" + cookie.getMaxAge());

			cookie.setPath("/");
			a
			if(domain != null && !domain.equalsIgnoreCase(request.getRemoteHost())){
				cookie.setDomain("." + domain);				
				if(logger.isDebugEnabled()){
					logger.debug("删除域Cookie:" + ",Cookie:" + cookie.getName() + ",value=" + cookie.getValue() + ", 设置domain:" + domain + ", cookie domain:" + cookie.getDomain() + ",ttl:" + cookie.getMaxAge());
				}
			} else {
				cookie.setDomain(request.getServerName());
				if(logger.isDebugEnabled()){
					logger.debug("删除主机Cookie:" + ",Cookie:" + cookie.getName() + ",value=" + cookie.getValue() + ", 设置domain:" + domain + ", cookie domain:" + cookie.getDomain() + ",ttl:" + cookie.getMaxAge());
				}
				//response.addCookie(cookie);	
				//cookie.setDomain("." + request.getServerName());
			}
			response.addCookie(cookie);	


		}catch(Exception e){
			e.printStackTrace();
		}*/
		/*try{
			Cookie[] cookies = request.getCookies();
			if(cookies == null){
				logger.debug("request中的cookies为空，当前请求:" + request.getServerName() + "没有任何Cookie");
			}
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(cookieName)){
					logger.debug("删除存在的cookie[name=" + cookie.getName() + ",value=" + cookie.getValue() + ",domain=" + cookie.getDomain() + ",path=" + cookie.getPath() + ",ttl=" + cookie.getMaxAge() + ",httpOnly=" + cookie.isHttpOnly());			
					cookie.setMaxAge(0);
					response.addCookie(cookie);	
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}	
		 */
	/*	cookies = request.getCookies();
		if(cookies == null){
			logger.debug("request中的cookies为空，当前请求:" + request.getServerName() + "没有任何Cookie");
			return;
		}
		for(Cookie c : cookies){

			logger.debug("2删除cookie[" + cookieName +"]后，当前还存在的cookie[name=" + c.getName() + ",value=" + c.getValue() + ",domain=" + c.getDomain() + ",path=" + c.getPath() + ",ttl=" + c.getMaxAge() + ",httpOnly=" + c.isHttpOnly());			
		}
*/

		return;				
	}



}
