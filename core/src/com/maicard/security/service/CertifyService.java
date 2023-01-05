package com.maicard.security.service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maicard.security.constants.SignType;
import com.maicard.security.vo.UserVo;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.ui.ModelMap;

import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.security.entity.User;


/**
 * 认证服务，确认与保持用户身份
 * @author NetSnake
 * @date 2012-12-30
 */
public interface CertifyService {

	
	//通过请求指定类型的当前用户
	User getLoginedUser(HttpServletRequest request, HttpServletResponse response,  int userTypeId) throws EisException;
	
	//生成用户令牌
	String generateUserToken(User user);

	//生成SSO Token
	String generateUserToken(UserVo vo);

	User getUserByToken(HttpServletRequest request, String userType);

    //通过令牌获取用户
	User getUserByToken(String cryptedToken);
	
	//用户登录
	User login(HttpServletRequest request, HttpServletResponse response, CriteriaMap params);
	
	//直接登录，不查询数据库
	User login(HttpServletRequest request, HttpServletResponse response, User user);


	String setVerifyCode(User user, SignType signType);

	//退出登录
	void logout(HttpServletRequest request, HttpServletResponse response,
			User frontUser);

	void getRemeberMeStatus(HttpServletRequest request, HttpServletResponse response, ModelMap map);

	void setRememberMe(HttpServletRequest request, HttpServletResponse response, ModelMap map);

	int checkSecAuth(HttpServletRequest request, HttpServletResponse response, User partner, String userPassword);

	boolean isSecAuthed(HttpServletRequest request, HttpServletResponse response, User partner);

	boolean passwordNeedChange(User partner);

	EisMessage passwordIsFine(User partner, String password);

	/**
	 * 通过request中的token参数来确定该用户是否合法<br/>
	 * 主要用于外部合作的SSO登录
	 * @param request
	 * @param userTypeId
	 * @return
	 */
	User getUserByRequestToken(HttpServletRequest request, int userTypeId);

	String generateSsoToken(User user, long timestamp);
	
	/**
	 * 以重定向方式把用户转发到前往的URL
	 * 并把登录后返回URL写入Cookie
	 */

	String redirectByResponse(HttpServletRequest request, HttpServletResponse response, int userTypeId,
			String toUrl, String backUrl, String cookieDomain);

	int strictAuthorize(HttpServletRequest request, HttpServletResponse response, User partner, String userPassword);

	User forceLogin(HttpServletRequest request, HttpServletResponse response, String openId);

	User loginByToken(HttpServletRequest request, HttpServletResponse response);

	User loginByToken(ServerHttpRequest request);

    boolean validateVerifyCode(User user, HttpServletRequest request, SignType login);

    void writeSessionCookie(User user, HttpServletRequest request, HttpServletResponse response);

	void writeSessionCookie(UserVo vo, HttpServletRequest request, HttpServletResponse response);

	String createJwtToken(String username);

//	void redirectByResponse(HttpServletRequest request, HttpServletResponse response, ModelMap map, boolean noJumpWhenIsJson, int userTypeId,			String weixinRegisterUrl, String backUrl, String cookieDomain);



}
