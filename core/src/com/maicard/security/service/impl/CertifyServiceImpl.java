package com.maicard.security.service.impl;

import static com.maicard.core.constants.Constants.COOKIE_REDIRECT_COOKIE_NAME;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.lang.Assert;
import cn.hutool.crypto.symmetric.AES;
import com.maicard.core.constants.*;
import com.maicard.core.service.CenterDataService;
import com.maicard.misc.ThreadHolder;
import com.maicard.security.constants.SignType;
import com.maicard.security.vo.UserVo;
import com.maicard.utils.*;
import io.jsonwebtoken.JwtBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
 
import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.entity.SiteDomainRelation;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.ConfigService;
import com.maicard.security.constants.UserStatus;
import com.maicard.security.entity.PasswordLog;
import com.maicard.security.entity.SecurityLevel;
import com.maicard.security.entity.User;
import com.maicard.security.service.CertifyService;
import com.maicard.security.service.FrontUserService;
import com.maicard.security.service.PartnerService;
import com.maicard.security.service.PasswordLogService;
import com.maicard.security.service.SecurityLevelService;
import com.maicard.site.service.SiteDomainRelationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
@Service
public class CertifyServiceImpl extends BaseService implements CertifyService {

	@Resource
	private PartnerService partnerService;
	
	@Resource
	private FrontUserService frontUserService;
	
	@Resource
	private PasswordLogService passwordLogService;
	@Resource
	private SecurityLevelService securityLevelService;
	@Resource
	private SiteDomainRelationService siteDomainRelationService;
	
	@Resource
	protected ConfigService configService;

	@Resource
	private CenterDataService centerDataService;


	private int securityLevelId = SecurityLevelUtils.getSecurityLevel();
	private SecurityLevel securityLevel;
	private int sessionTimeout = 0;
	private String aesKey = null;
	private final SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT);

	private final Pattern capitalCharPattern = Pattern.compile("[A-Z]");
	private final Pattern smallCharPattern = Pattern.compile("[a-z]");
	private final Pattern digitalPattern = Pattern.compile("\\d");
	//Unicode标点符号集合
	private final Pattern symbolCharPattern = Pattern.compile("\\pP");



	@PostConstruct
	public void init(){
		securityLevel = securityLevelService.select(securityLevelId);
		if(securityLevel != null && securityLevel.getData() != null && NumericUtils.isNumeric(securityLevel.getData().get("SESSION_TIMEOUT"))){
			sessionTimeout = NumericUtils.parseInt(securityLevel.getData().get("SESSION_TIMEOUT"));	
			logger.debug("当前安全级别为[" + securityLevelId + "]配置的sessionTimeout数据:" + sessionTimeout);
		} 
		if(sessionTimeout < 1){
			sessionTimeout = Constants.SESSION_DEFAULT_TTL;
			logger.debug("当前安全级别为空或者没有配置sessionTimeout数据，配置为系统默认TTL:" + sessionTimeout);
		}
		try {
			aesKey = SecurityUtils.readAesKey();
		} catch (Exception e) {
			logger.error("无法读取系统AES密钥");
		}
	}



	/*
	 * 根据Cookie检查用户是否设置了记住我的用户名
	 * 如果记住，则从cookie中取出用户名并放入map供前端调用
	 */
	@Override
	public void getRemeberMeStatus(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
		if(response == null){
			logger.error("传入的response为空，放弃处理");
			return;
		}
		String remember =	CookieUtils.getCookie(request, "REMEMBER_ME");
		String rememberName = CookieUtils.getCookie(request, "USER_REMEMBER_NAME");
		boolean rememberMe = false;
		if(remember != null && remember.equalsIgnoreCase("true")){
			rememberMe = true;
		}

		map.put("REMEMBER_ME", rememberMe);
		if(rememberMe){
			logger.debug("用户要求记住用户名，把Cookie中的用户名[" + rememberName + "]放入map");
			map.put("USER_REMEMBER_NAME", rememberName);
		} else {
			logger.debug("用户Cookie中没有要求记住用户名的配置");
		}

		/*if( StringUtils.isBlank(rememberName)){
			CookieUtils.removeCookie(request, response, "USER_REMEMBER_NAME");
			return;
		}*/
		//Crypt crypt = new Crypt();
		//crypt.setAesKey(Constants.cookieAesKey);
		//String cryptUser = crypt.aesEncrypt(rememberName + "|" + System.currentTimeMillis());
		//String cryptUser = rememberName;
		//CookieUtils.addCookie(request, response, "USER_REMEMBER_NAME", cryptUser, Constants.COOKIE_MAX_TTL);
	}

	/*
	 * 把用户是否要求记住的用户名的要求放到Cookie中
	 * 如果不要求记住用户名，则删除Cookie中已经记忆的用户名
	 */
	@Override
	public void setRememberMe(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
		if(response == null){
			logger.error("传入的response为空，放弃处理");
			return;
		}
		String remember =	request.getParameter("REMEMBER_ME");
		String rememberName = request.getParameter("username");
		boolean rememberMe = false;
		if(remember != null && remember.equalsIgnoreCase("true")){
			rememberMe = true;
		}
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		CookieUtils.addCookie(request, response, "REMEMBER_ME", remember,Constants.COOKIE_MAX_TTL, siteDomainRelation.getCookieDomain());		
		if(rememberMe){
			logger.debug("用户要求记住用户名，把用户名[" + rememberName + "]写入Cookie");
			CookieUtils.addCookie(request, response, "USER_REMEMBER_NAME", rememberName,Constants.COOKIE_MAX_TTL,siteDomainRelation.getCookieDomain());
			//map.put("USER_REMEMBER_NAME", rememberName);
		} else {
			logger.debug("用户要求不记住用户名，把用户名[" + rememberName + "]从Cookie中删除");
			CookieUtils.removeCookie(request, response, "USER_REMEMBER_NAME",siteDomainRelation.getCookieDomain());			
		}
	}


	@Override
	public User getLoginedUser(HttpServletRequest request,  HttpServletResponse response, int userTypeId) throws EisException{

		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		if(siteDomainRelation == null){
			logger.error("根据主机名[" + request.getServerName() + "]找不到指定的站点关系");
			return null;
		}

		User user = null;
		if(userTypeId == UserTypes.partner.id){
			Object o = request.getSession().getAttribute("partner");
			if(o != null && o instanceof User){
				user = (User)o;
				logger.debug("从Session中获取到了partner实例");
			} else {
				user = getUserByToken(request, "p");
			}
			if(user == null){
				user = getUserByRequestToken(request, UserTypes.partner.id);
			}
			if(user != null){
				if(user.getOwnerId() != siteDomainRelation.getOwnerId()){
					logger.error("合作伙伴用户[" + user.getUuid() + "]的ownerId[" + user.getOwnerId() + "]与主机名[" + request.getServerName() + "]对应的ownerId[" + siteDomainRelation.getOwnerId() + "]不一致");
					return null;
				}
				Date lastLoginTimeStamp = user.getLastLoginTimestamp();
				if(lastLoginTimeStamp == null || new Date().getTime() - lastLoginTimeStamp.getTime() > Constants.TOKEN_RENEW_INTERVAL * 1000){
					logger.debug("当前用户最后一次更新时间是:" + (lastLoginTimeStamp == null ? "空" : sdf.format(lastLoginTimeStamp)) + ",已超过" + Constants.TOKEN_RENEW_INTERVAL + "秒，应当更新");

					generateUserToken(user);
					user.setLastLoginIp(IpUtils.getClientIp(request));
					if(response != null){
						CookieUtils.addCookie(request, response, Constants.SESSION_TOKEN_NAME + "_p", user.getSsoToken(),sessionTimeout, siteDomainRelation.getCookieDomain());
					}
				}
			}
		} else {
			Object o = request.getSession().getAttribute("frontUser");			
			if(o != null && o instanceof User){
				user = (User)o;
				logger.debug("从Session中获取到了frontUser实例");
			} else {
				user = getUserByToken(request, "f");
			}
			if(user != null){
				if(user.getOwnerId() != siteDomainRelation.getOwnerId()){
					logger.error("前端用户[" + user.getUuid() + "]的ownerId[" + user.getOwnerId() + "]与主机名[" + request.getServerName() + "]对应的ownerId[" + siteDomainRelation.getOwnerId() + "]不一致");
					return null;
				}
				user.setUserTypeId(UserTypes.frontUser.id);

				Date lastLoginTimeStamp = user.getLastLoginTimestamp();
				if(lastLoginTimeStamp == null || new Date().getTime() - lastLoginTimeStamp.getTime() > Constants.TOKEN_RENEW_INTERVAL * 1000){
					logger.debug("当前用户最后一次更新时间是:" + (lastLoginTimeStamp == null ? "空" : sdf.format(lastLoginTimeStamp)) + ",已超过" + Constants.TOKEN_RENEW_INTERVAL + "秒，应当更新");

					generateUserToken(user);
					user.setLastLoginIp(IpUtils.getClientIp(request));
					if(response != null){
						logger.debug("从cookie中得到前端用户后，再次向cookie中写入passport");
						CookieUtils.addCookie(request, response, Constants.SESSION_TOKEN_NAME + "_f", user.getSsoToken(),sessionTimeout,siteDomainRelation.getCookieDomain());
						String uesrCookieName = (StringUtils.isBlank(user.getNickName()) ? user.getUsername() : user.getNickName());
						try{
							uesrCookieName = java.net.URLEncoder.encode(uesrCookieName,"UTF-8");
						}catch(Exception e){
							e.printStackTrace();
						}
						CookieUtils.addCookie(request, response, Constants.SESSION_USER_NAME, uesrCookieName, sessionTimeout,siteDomainRelation.getCookieDomain(), false);
					}
				}
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("从Cookie令牌中未找到前台用户");
				}
				//微信Cookie经常出现异常，如果没有找到登录用户，以第二种方式检查
				if(request.getAttribute("openId") != null){
					String openId = request.getAttribute("openId").toString();
					//user = forceLogin(request,response,openId);
					if(logger.isDebugEnabled()){
						logger.debug("从请求Attributes中找到了openId:" + openId + "，以该openid登录，登录结果:" + user);
					}
				} else {
					if(logger.isDebugEnabled()){
						logger.debug("从请求Attributes也未找到openId");
					}
				}
			}
		}	

		if(user == null || user.getCurrentStatus() != UserStatus.normal.getId()){
			return null;

		}
		if(user.getOwnerId() != siteDomainRelation.getOwnerId()){
			logger.error("用户[" + user.getUuid() + "]的ownerId[" + user.getOwnerId() + "]与系统的[" + siteDomainRelation.getOwnerId() + "]不匹配");
			throw new EisException(EisError.ownerNotMatch.getId(), "系统异常");		
		}
		return user;
	}




	//生成SSO Token
	@Override
	public String generateUserToken(User user){
		if(user == null){
			logger.error("尝试生成加密令牌但用户是空");
			return null;
		}
		user.setLastLoginTimestamp(new Date());

		//生成由 用户类型ID|UUID|最后登录IP|用户名|登录时间戳 组成的Base64编码后的源
		String src = user.getUserTypeId() + "|" + user.getUuid() + "|" +  user.getLastLoginIp() + "|" + user.getUsername() + "|" + user.getLastLoginTimestamp().getTime();
		logger.debug("用户令牌加密源:" + src);
		src = Crypt.base64Encode(src);
		logger.debug("用户令牌编码后:" + src);
		//强制使用加密后的密码进行校验, NetSnake, 2013-11-12
		String sign = SecurityUtils.passwordEncode(src + SecurityUtils.correctPassword(user.getUserPassword()) + aesKey);
		Crypt crypt = new Crypt();
		crypt.setAesKey(aesKey);
		String token = null;
		try {
			token = crypt.aesEncryptBase64(src + "|" + sign);
			user.setSsoToken(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}

	//生成SSO Token
	@Override
	public String generateUserToken(UserVo vo){
		if(vo == null){
			logger.error("尝试生成加密令牌但用户是空");
			return null;
		}

		//if(vo.getLastLoginTimestamp() == null){
			vo.setLastLoginTimestamp(new Date());
		//}
		//生成由 用户类型ID|UUID|最后登录IP|用户名|登录时间戳 组成的Base64编码后的源
		String src = vo.getUserTypeId() + "|" + vo.getUuid() + "|" +  vo.getLastLoginIp() + "|" + vo.getUsername() + "|" + vo.getLastLoginTimestamp().getTime();
		logger.debug("用户令牌加密源:" + src);
		src = Crypt.base64Encode(src);
		logger.debug("用户令牌编码后:" + src);
		//强制使用加密后的密码进行校验, NetSnake, 2013-11-12
		String sign = SecurityUtils.passwordEncode(src + SecurityUtils.correctPassword(vo.getUserPassword()) + aesKey);
		Crypt crypt = new Crypt();
		crypt.setAesKey(aesKey);
		String token = null;
		try {
			token = crypt.aesEncryptBase64(src + "|" + sign);
			vo.setSsoToken(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}
	@Override
	public User getUserByToken(HttpServletRequest request, String userType){
		final String tokenName = Constants.SESSION_TOKEN_NAME + "_" + userType.trim();
		String token = 	  request.getHeader(tokenName);
		if(token == null){
			token = 	CookieUtils.getCookie(request, tokenName);

		}
		if(token == null){
			logger.error("在请求中找不到名为:" + tokenName + "的Cookie或Header,header=" + JsonUtils.toStringFull(request.getHeaderNames()));
			return null;
		}
		return getUserByToken(token);
	}
	/*
	 * 检查用户的Token是否有效
	 * 若有效则更新登录时间
	 */
	@Override
	public User getUserByToken(String cryptedToken){
		if(cryptedToken == null){
			logger.debug("加密令牌为空");
			return null;
		}		
		Crypt crypt = new Crypt();
		crypt.setAesKey(aesKey);
		//	logger.info("尝试解密TOKEN:" + cryptedToken );
		try{
			String token = new String(crypt.aesDecryptBase64(cryptedToken), Constants.DEFAULT_CHARSET);
			
			logger.debug("解密后的Token:" + token);
			String[] data = token.split("\\|");
			logger.debug("token data:" + data.length);
			if(data == null || data.length != 2){
				logger.error("Token数据异常");
				return null;
			}

			String[] data2 = new String(Base64.decodeBase64(data[0]), Constants.DEFAULT_CHARSET).split("\\|");
			if(data2 == null || data2.length != 5){
				logger.error("SSO 数据异常");
				return null;
			}
			long tokenUserTypeId = Long.parseLong(data2[0]);
			long tokenUuid = Long.parseLong(data2[1]);
			//String lastLoginIp = data2[2];
			long ssoTs = Long.parseLong(data2[4]);
			if(tokenUserTypeId == 0){
				logger.error("SSO 用户类型为0");
				return null;					
			}
			if(tokenUuid == 0){
				logger.error("SSO UUID为0");
				return null;			
			}
			User user = null;
			User model = new User();
			model.setId(tokenUuid);
			if(tokenUserTypeId == UserTypes.frontUser.id){
				logger.debug("尝试查找前台用户[" + tokenUuid + "]");
				user = frontUserService.select(model);
			} else if(tokenUserTypeId == UserTypes.partner.id){

				user = partnerService.select(model);
			} else {
				logger.error("错误的用户类型:" + tokenUserTypeId);
				return null;
			}
			if(user == null){
				logger.error("根据UUID[" + tokenUuid + "]找不到类型为[" + tokenUserTypeId + "]的用户");
				return null;
			}
			logger.debug("得到用户[" + user.getUuid() + "]密码是:"+user.getUserPassword());
			String clientSign = data[1];
			String serverSign = SecurityUtils.passwordEncode(data[0] + SecurityUtils.correctPassword(user.getUserPassword())  + aesKey);
			if(!clientSign.equals(serverSign)){
				logger.warn("SSO Token校验失败，用户提交的SIGN：" + clientSign + ",服务器生成的SIGN：" + data[0] + user.getUserPassword()  + aesKey + "======>" + serverSign);
				return null;
			}		
			if(ssoTs == 0){
				logger.debug("SSO 时间戳为0");
				return null;	
			}
			user.setLastLoginTimestamp(new Date(ssoTs));
			long timeout = (new Date().getTime() - ssoTs)/1000;
			logger.debug("Token中的时间戳[" + ssoTs + "]与当前时间[" + new Date().getTime() + "]相差: " + timeout + ", Token超时时间:" + sessionTimeout);
			if(timeout <= sessionTimeout){
				//尚未超时
				return user;
				//				return user.clone();
			}	

		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public User getUserByRequestToken(HttpServletRequest request, int userTypeId) {
		String token = request.getParameter("ssoToken");
		long uuid =  ServletRequestUtils.getLongParameter(request, "uuid", 0);
		long timestamp = ServletRequestUtils.getLongParameter(request, "timestamp",0);
		if(uuid < 1){
			logger.error("请求中未提交用户UUID");
			return null;
		}
		if(timestamp < 1){
			logger.error("请求中未提交timestamp");
			return null;
		}
		String ttl = SecurityLevelUtils.getConfig(securityLevel, "SSO_TIMESTAMP_TTL");
		if(StringUtils.isBlank(ttl)){
			ttl = String.valueOf(Constants.SSO_TIMESTAMP_DEFAULT_TTL);
		}
		if((new Date().getTime() - timestamp) / 1000 > Long.parseLong(ttl)){
			logger.error("时间戳已过期");
			return null;
		}
		if(token == null){
			logger.error("request加密令牌为空");
			return null;
		}
		User partner = partnerService.select(uuid);
		if(partner == null){
			logger.error("找不到UUID=" + uuid + "的partner");
			return null;
		}
		if(partner.getCurrentStatus() != UserStatus.normal.getId()){
			logger.error("UUID=" + uuid + "的partner状态异常:" + partner.getCurrentStatus());
			return null;
		}
		boolean allowSsoLogin = partner.getBooleanExtra("ALLOW_SSO_LOGIN");
		if(!allowSsoLogin){
			logger.error("UUID=" + uuid + "的partner的配置allowSsoLogin不是true，不允许SSO登录");
			return null;
		}
		String supplierLoginKey = partner.getExtra("supplierLoginKey");
		if(StringUtils.isBlank(supplierLoginKey)){
			logger.error("UUID=" + uuid + "的partner的没有配置supplierLoginKey，无法使用SSO登录");
			return null;
		}
		String sign = generateSsoToken(partner, timestamp);
		if(sign.equalsIgnoreCase(token)){
			logger.debug("用户[" + uuid + "]SSO登录token[" + token + "]与本地计算["  + sign + "]一致，SSO登陆成功");
			return partner;
		} 
		logger.debug("用户[" + uuid + "]SSO登录token[" + token + "]与本地计算[" + sign + "]不一致，SSO登陆失败");	
		return null;
	}

	@Override
	public String generateSsoToken(User user, long timestamp){
		String key = user.getExtra("supplierLoginKey");
		StringBuffer  signSource = new StringBuffer().append(user.getUsername()).append('|').append(key).append('|').append(timestamp);
		return DigestUtils.sha256Hex(signSource.toString());
	}


	@Override
	public User login(HttpServletRequest request, HttpServletResponse response, CriteriaMap userCriteria){
		String authKeyQuery = userCriteria.getStringValue("authKey");
		String username = userCriteria.getStringValue("username");
		String userPassword = userCriteria.getStringValue("userPassword");
		int userTypeId = userCriteria.getIntValue( "userTypeId");
		if (StringUtils.isBlank(authKeyQuery)){
			if(StringUtils.isBlank(username)){
				logger.warn("尝试登录的用户名为空");
				return null;
			}
			if(StringUtils.isBlank(userPassword)){
				logger.warn("尝试登录的用户密码为空");
				return null;
			}
		}
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		if(siteDomainRelation == null){
			logger.error("根据主机名[" + request.getServerName() + "]找不到指定的站点关系");
			return null;
		}
		userCriteria.put("ownerId",siteDomainRelation.getOwnerId());

		String plainPassword = userCriteria.getStringValue("userPassword");
		userCriteria.remove("userPassword");
		//userCriteria.setUserPassword(Crypt.passwordEncode(userCriteria.getUserPassword()));
		//登录必须是正常状态的用户
		userCriteria.put("currentStatus",UserStatus.normal.id);
		userCriteria.setCacheType(User.class);
		List<User> loginUserList = null;
		if(userTypeId == UserTypes.frontUser.id){
			loginUserList = frontUserService.list(userCriteria);
		} else 	if(userTypeId == UserTypes.partner.id){
			loginUserList = partnerService.list(userCriteria);
		} else {
			logger.error("错误的用户类型:" + userTypeId);
			return null;					
		}
		if(loginUserList == null){
			logger.info("未能查找到[" + username + "/" + userPassword + "]、类型为[" + userTypeId + "]的正常状态用户");
			return null;
		}
		if(loginUserList.size() != 1){
			logger.info("查找到[" + username + "/" + userPassword + "]、类型为[" + userTypeId+ "]的正常状态用户数量不唯一:" + loginUserList.size());
			return null;
		}
		User user = loginUserList.get(0);
		String encryptPassword = null;

		encryptPassword = SecurityUtils.correctPassword(plainPassword);

		if(!encryptPassword.equals(user.getUserPassword())){
			//密码错误
			logger.warn("用户[" + username + "]提供的登录密码是:" + encryptPassword + ",与数据库中保存的:" + user.getUserPassword() + "不一致");
			return null;

		}



	//	user.setLastLoginTimestamp(new java.util.Date());
		user.setLastLoginIp(IpUtils.getClientIp(request));
		//生成客户端Token
		generateUserToken(user);
		writeSessionCookie(user,request,response);
		return user;

	}

	@Override
	public User loginByToken(HttpServletRequest request, HttpServletResponse response){
		
		String systemCode = configService.getSystemCode();
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		if(siteDomainRelation == null){
			logger.error("根据主机名[" + request.getServerName() + "]找不到指定的站点关系");
			return null;
		}
		String ssoName = SecurityUtils.getSsoName(systemCode);
		String token = request.getHeader(ssoName);
		String uuidName = SecurityUtils.getSsoUuidName(systemCode);
		long uuid = NumericUtils.parseLong(request.getHeader(uuidName));
		if (StringUtils.isBlank(token)){
			logger.warn("尝试登录的请求中没有_sso令牌:{}", ssoName);
			return null;
		}
		
		if(uuid < 1) {
			logger.warn("尝试登录的请求中没有_suid令牌:{}", uuidName);
			return null;
		}
		
		return _loginByToken(uuid, token, siteDomainRelation.getOwnerId());
	}
	
	@Override
	public User loginByToken(ServerHttpRequest request){
		
		
		String systemCode = configService.getSystemCode();
		String serverName = request.getHeaders().getHost().getHostName();
		String token = null;
		long uuid = 0;
		
		String ssoName = SecurityUtils.getSsoName(systemCode);
		if(request.getHeaders().containsKey(ssoName)){
			token = request.getHeaders().get(ssoName).get(0);
		}
		String uuidName = SecurityUtils.getSsoUuidName(systemCode);
		if(request.getHeaders().containsKey(ssoName)){
			uuid = NumericUtils.parseLong(request.getHeaders().get(uuidName).get(0));
		}
		
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(serverName);
		if(siteDomainRelation == null){
			logger.error("根据主机名[" + serverName + "]找不到指定的站点关系");
			return null;
		}
		if (StringUtils.isBlank(token)){
			logger.warn("尝试登录的请求中没有_sso令牌");
			return null;
		}
		
		if(uuid < 1) {
			logger.warn("尝试登录的请求中没有_suid令牌");
			return null;
		}
		
		return _loginByToken(uuid, token, siteDomainRelation.getOwnerId());
		
		
		
		
	}

	@Override
	public boolean validateVerifyCode(User user, HttpServletRequest request, SignType signType) {
		String remoteSign = ServletRequestUtils.getStringParameter(request,"code",null);

		if (StringUtils.isBlank(remoteSign)) {
			return false;
		}
		Assert.notBlank(user.getUsername());
		String username = user.getUsername().trim();
		long ownerId = user.getOwnerId();
		String key = KeyConstants.VERIFY_CODE_INSTANCE_PREFIX + "#" + signType + "#" + ownerId + "#" + username;
		String serverSign = centerDataService.get(key);
		if(StringUtils.isBlank(serverSign)){
			logger.debug("系统未找到验证码:" + key);
			return false;
		}
		if(!serverSign.equalsIgnoreCase(remoteSign)){
			logger.info("用户:" + username + "提交的验证码:" + remoteSign + "与系统中的:" + key + "=>" + serverSign + "不一致");
			return false;
		}
		return true;
	}

	@Override
	public String setVerifyCode(User user, SignType signType){

		Assert.notBlank(user.getUsername());
		String username = user.getUsername().trim();
		long ownerId = user.getOwnerId();
		Random random = new Random();
		String randomCode = String.valueOf(random.nextInt(999999) % (999999 - 100000 + 1) + 100000);
		//放入一个键值到REDIS，以防止用户重复提交
		int verifyCodeResendInterval = configService.getIntValue(DataName.VERIFY_CODE_RESEND_INTERVAL_SEC.toString(), ownerId);
		if(verifyCodeResendInterval < 1){
			verifyCodeResendInterval = 60;
		}

		String ttlKey = KeyConstants.VERIFY_CODE_LOCK_PREFIX + "#" + signType + "#" + ownerId + "#" + username;
		String valueKey = KeyConstants.VERIFY_CODE_INSTANCE_PREFIX + "#" + signType + "#" + ownerId + "#" + username;
		boolean setResult = centerDataService.setIfNotExist(ttlKey, ThreadHolder.defaultTimeFormatterHolder.get().format(new Date()), verifyCodeResendInterval);
		if(!setResult) {
			String value = centerDataService.get(valueKey);
			//如果value为空，那么还是要重置
			if (StringUtils.isNotBlank(value)){
				logger.debug("用户手机号[" + username + "]已在[" + value + "]时提交验证码申请，还没到限制时间" + verifyCodeResendInterval + ",不允许发送注册短信");
				return value;
			} else {
				//强制删除ttl
				centerDataService.setForce(ttlKey, ThreadHolder.defaultTimeFormatterHolder.get().format(new Date()), verifyCodeResendInterval);
			}
		}

		int verifyCodeTtl = configService.getIntValue(DataName.VERIFY_CODE_TTL_SEC.toString(), ownerId);
		if(verifyCodeTtl < 1){
			verifyCodeTtl = 600;
		}
		centerDataService.setForce(valueKey, randomCode, verifyCodeTtl);
		return randomCode;

	}

	private User _loginByToken(long uuid, String token,  long ownerId) {
		
		String systemCode = configService.getSystemCode();

		User frontUser = frontUserService.select(uuid);
		if(frontUser == null) {
			logger.error("根据令牌中的uuid={}找不到用户", uuid);
			return null;
		}
		
		String userPublicKey = frontUser.getExtra(DataName.publicKey.name());
		if (StringUtils.isBlank(userPublicKey)){
			logger.warn("尝试登录的用户没有publicKey配置");
			return null;
		}
				
		String secret = uuid + frontUser.getUsername() + userPublicKey;
		logger.info("准备使用密钥：{}校验令牌：{}", secret, token);
		Key key = Keys.hmacShaKeyFor(secret.getBytes());
		Jws<Claims> jwt = null;
		try {
			jwt = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		if(jwt == null) {
			logger.error("用户:{}令牌:{}校验失败:{}", uuid, token);
	    	return null;
		}
		String issuer = jwt.getBody().getIssuer();
		if(!issuer.equals(systemCode)) {
			logger.error("用户:{}令牌:{}由于签发者不一致:{}<=>{},校验失败:{}", uuid, issuer, systemCode, token);
	    	return null;
		}
 	    long jwtUuid = NumericUtils.parseLong(jwt.getBody().get("uuid"));
	    if(jwtUuid == 0 || jwtUuid != uuid) {
	    	logger.error("令牌中的uuid:{}与请求的uuid:{}不一致", jwtUuid, uuid);
	    	return null;
	    } 
	    
	    if(frontUser.getOwnerId() != ownerId) {
	    	logger.error("用户:{}的ownerId与会话中的的ownerId:{}不一致", frontUser.getUuid(), frontUser.getOwnerId(), ownerId);

	    }

		return frontUser;	 
	}



	@Override
	public void logout(HttpServletRequest request,
			HttpServletResponse response, User user) {
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		if(user == null){
			logger.debug("请求中未找到用户，删除所有用户令牌");
			CookieUtils.removeCookie(request, response,Constants.SESSION_TOKEN_NAME + "_s",siteDomainRelation.getCookieDomain());	
			CookieUtils.removeCookie(request, response,Constants.SESSION_TOKEN_NAME + "_p",siteDomainRelation.getCookieDomain());	
			CookieUtils.removeCookie(request, response,Constants.SESSION_TOKEN_NAME + "_f",siteDomainRelation.getCookieDomain());
			return;
		}
		try{
			Cookie[] cookies = request.getCookies();
			if(cookies == null || cookies.length < 1){
				return;
			}
			if(user.getUserTypeId() == UserTypes.sysUser.id){
				if(logger.isDebugEnabled()){
					logger.debug("尝试删除系统用户[" + user.getUsername() + "/" + user.getUuid() + "]的Cookie.");
				}
				CookieUtils.removeCookie(request, response,Constants.SESSION_TOKEN_NAME + "_s",siteDomainRelation.getCookieDomain());	
			} else if(user.getUserTypeId() == UserTypes.partner.id){
				if(logger.isDebugEnabled()){
					logger.debug("尝试删除合作伙伴[" + user.getUsername() + "/" + user.getUuid() + "]的Cookie.");
				}
				CookieUtils.removeCookie(request, response,Constants.SESSION_TOKEN_NAME + "_p",siteDomainRelation.getCookieDomain());	
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("尝试删除终端用户[" + user.getUsername() + "/" + user.getUuid() + "]的Cookie.");
				}
				CookieUtils.removeCookie(request, response,Constants.SESSION_TOKEN_NAME + "_f",siteDomainRelation.getCookieDomain());				
				CookieUtils.removeCookie(request, response, Constants.SESSION_USER_NAME,siteDomainRelation.getCookieDomain());
				CookieUtils.removeCookie(request, response, "outUuid",siteDomainRelation.getCookieDomain());

				String cookie = CookieUtils.getCookie(request, Constants.SESSION_TOKEN_NAME + "_f");
				logger.debug("XXXXXXX>退出后读取Cookie:" + Constants.SESSION_TOKEN_NAME + "_f" + "=" + cookie);
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return;		
	}

	@Override
	public User login(HttpServletRequest request, HttpServletResponse response,
			User user) {
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		if(siteDomainRelation == null){
			logger.error("根据主机名[" + request.getServerName() + "]找不到指定的站点关系");
			return null;
		}
		if(user == null){
			logger.error("尝试直接登录的用户是空");
			return null;
		}
		if(user.getUsername() == null || user.getUsername().equals("")){
			logger.error("尝试直接登录的用户名是空");
			return null;
		}
		if(StringUtils.isBlank(user.getUserPassword()) && StringUtils.isBlank(user.getAuthKey())){
			logger.error("尝试直接登录的用户密码:" + user.getUserPassword() + "或密钥:" + user.getAuthKey() + ",都是空:" + JsonUtils.toStringFull(user));
			return null;
		}
		if(user.getUserPassword() != null) {
			user.setUserPassword(SecurityUtils.correctPassword(user.getUserPassword()));
		}
		user.setLastLoginIp(IpUtils.getClientIp(request));

		//生成客户端Token
		generateUserToken(user);
		writeSessionCookie(user, request, response);
		return user;
	}




	@Override
	public int checkSecAuth(HttpServletRequest request,  HttpServletResponse response, User partner, String userPassword) {


		if(isSecAuthed(request, response, partner)){
			return OpResult.success.getId();
		}
		Crypt crypt = new Crypt();
		crypt.setAesKey(aesKey);
		if(partner.getUserPassword() == null){
			//重新获取密码
			partner = partnerService.select(partner.getUuid());
		}
		boolean passwordIsOk = false;
		logger.debug("用户[" + partner.getUuid() + "]当前密码是[" + partner.getUserPassword() + "]，提供二次验证的密码是:" + userPassword);
		if(partner.getUserPassword().length() == 64){
			//是加密后的密码
			String shaPassword = Crypt.passwordEncode(userPassword);
			if(shaPassword.equals(partner.getUserPassword())){
				passwordIsOk = true;
			}
		} else {
			if(userPassword.equals(partner.getUserPassword())){
				passwordIsOk = true;
			}
		}

		if(!passwordIsOk){
			return EisError.AUTH_FAIL.getId();
		}

		//写入二次验证数据加密Cookie		return 0;
		StringBuffer sb = new StringBuffer();
		sb.append(partner.getUserTypeId());
		sb.append("|");
		sb.append(partner.getUuid());
		sb.append("|");
		sb.append(new Date().getTime());
		String value = null;
		try {
			value = crypt.aesEncryptBase64(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		String ttl = SecurityLevelUtils.getConfig(securityLevel, "SEC_AUTH_TTL");
		if(StringUtils.isBlank(ttl)){
			ttl = String.valueOf(Constants.COOKIE_SEC_AUTH_COOKIE_DEFAULT_TTL);
		}
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());

		CookieUtils.addCookie(request, response, Constants.COOKIE_SEC_AUTH_COOKIE_NAME, value, Integer.parseInt(ttl),siteDomainRelation.getCookieDomain());
		return OpResult.success.getId();
	}

	@Override
	public int strictAuthorize(HttpServletRequest request,  HttpServletResponse response, User partner, String userPassword) {



		Crypt crypt = new Crypt();
		crypt.setAesKey(aesKey);
		if(partner.getUserPassword() == null){
			//重新获取密码
			partner = partnerService.select(partner.getUuid());
		}

		String data = ServletRequestUtils.getStringParameter(request, "data", null);
		
		String clearData;
		try {
			//data = URLDecoder.decode(data, Constants.DEFAULT_CHARSET);
			clearData = new String(crypt.aesDecryptHex(data),Constants.DEFAULT_CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("无法对数据进行解密:" + data);
			return EisError.DECRYPT_ERROR.id;
		}
		Map<String,String> dataMap = HttpUtils.getRequestDataMap(clearData);

		String tokenName = HttpUtils.getStringValueFromRequestMap(dataMap,"strictAuthToken");
		long strictAuthTtl = HttpUtils.getLongValueFromRequestMap(dataMap,"strictAuthTtl",0);

		String authType = HttpUtils.getStringValueFromRequestMap(dataMap,"strictAuthType");




		boolean passwordIsOk = false;
		logger.debug("用户[" + partner.getUuid() + "]当前密码是[" + partner.getUserPassword() + "]，需要验证的密码模式是:" + authType + ",密码token=" + tokenName + ",验证有效期是:" +  strictAuthTtl + "秒，提供二次验证的密码是:" + userPassword);

		String rightPassword = null;
		if(authType == null || authType.equalsIgnoreCase(Constants.DEFAULT_STRICT_AUTH_MODE)){
			rightPassword = partner.getAuthKey();
		} else {
			rightPassword = partner.getUserPassword();

		}
		if(rightPassword == null){
			logger.error("用户:" + partner.getUuid() + "未设置密码，密码模式是:" + authType);
			return EisError.usernameOrPasswordIsNull.id;

		}
		if(rightPassword.length() == 64){
			//是加密后的密码
			String shaPassword = Crypt.passwordEncode(userPassword);
			logger.debug("比较加密密码,正确密码是:{}，提交密码是:{}", rightPassword, shaPassword);
			if(shaPassword.equals(rightPassword)){
				passwordIsOk = true;
			}
		} else {
			logger.debug("比较明文密码,正确密码是:{}，提交密码是:{}", rightPassword.substring(0, rightPassword.length() /2), userPassword.substring(0, userPassword.length() / 2));
			if(userPassword.equals(rightPassword)){
				passwordIsOk = true;
			}
		}



		if(!passwordIsOk){
			return EisError.AUTH_FAIL.getId();
		}
		long tokenTime =  new Date().getTime() / 1000;
		logger.info("向用户Session中写入新的严格认证token时间[" + tokenName + "=" + tokenTime + "]");
		request.getSession().setAttribute(tokenName,tokenTime);		
		return OpResult.success.getId();
	}



	/*
	 * 检查用户Cookie中的二次验证数据是否有效
	 */
	@Override
	public boolean isSecAuthed(HttpServletRequest request, HttpServletResponse response, User partner) {
		Crypt crypt = new Crypt();
		crypt.setAesKey(aesKey);

		//先从Cookie中获取之前的二次验证数据
		String existCookieData = CookieUtils.getCookie(request, Constants.COOKIE_SEC_AUTH_COOKIE_NAME);
		if(existCookieData == null){
			logger.debug("用户[" + partner.getUuid() + "]Cookie中没有二次验证数据");
		} else {
			String clearExistData = null;
			try {
				clearExistData = new String(crypt.aesDecryptBase64(existCookieData),Constants.DEFAULT_CHARSET);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("无法对用户[" + partner.getUuid() + "]Cookie中的二次验证数据进行解密:" + existCookieData);

			}

			String[] data = clearExistData.split("\\|");
			if(data.length < 3){
				logger.error("用户[" + partner.getUuid() + "]Cookie中的二次验证数据异常:" + clearExistData);
			} else {
				logger.debug("检查用户[" + partner.getUuid() + "]Cookie中的二次验证数据:" + clearExistData);
				if(data[0].equals(String.valueOf(partner.getUserTypeId()))
						&& data[1].equals(String.valueOf(partner.getUuid()))){
					long ts =  Long.parseLong(data[2]);
					Date beginTime = new Date(ts);
					String ttl = SecurityLevelUtils.getConfig(securityLevel, "SEC_AUTH_TTL");
					if(StringUtils.isBlank(ttl)){
						ttl = String.valueOf(Constants.COOKIE_SEC_AUTH_COOKIE_DEFAULT_TTL);
					}
					if((new Date().getTime() - ts) / 1000 > Long.parseLong(ttl)){
						logger.debug("Cookie中二次验证时间是" + sdf.format(beginTime) + "，有效期是:" + ttl + ",已过期");
						return false;
					}
					logger.debug("用户[" + partner.getUuid() + "]Cookie中的二次验证数据[" + sdf.format(beginTime) + "]，有效期是:" + ttl + ",仍然有效:" + clearExistData);
					return true;
				}							
			}

		}
		return false;
	}


	/*
	 * 检查用户的密码是否应强制更换
	 */
	@Override
	public boolean passwordNeedChange(User partner) {
		if(securityLevel == null){
			logger.debug("当前未定义安全级别规范，不进行密码有效期检查");
			return false;
		}
		long passwordTtl = 0L;
		String ttl =SecurityLevelUtils.getConfig(securityLevel, "PASSWORD_TTL");
		if(NumericUtils.isNumeric(ttl)){
			passwordTtl = Long.parseLong(ttl);
		}
		if(passwordTtl < 1){
			logger.debug("当前安全级别[" + securityLevel + "]未定义密码有效期");
			return false;
		}
		CriteriaMap passwordLogCriteria = CriteriaMap.create();
		passwordLogCriteria.put("uuid",partner.getUuid());
		passwordLogCriteria.put("password",partner.getUserPassword());
		//检查当前密码的创建时间
		List<PasswordLog> passwordLogList = passwordLogService.list(passwordLogCriteria);
		logger.debug("用户[" + partner.getUuid() + "]当前密码的记录是:" + (passwordLogList == null ? "空" : passwordLogList.size()));
		if(passwordLogList == null || passwordLogList.size() < 1){
			logger.debug("用户[" + partner.getUuid() + "]当前密码没有记录，认为不需要更改密码");
			return false;
		}
		Collections.sort(passwordLogList, new Comparator<PasswordLog>(){
			@Override
			public int compare(PasswordLog o1, PasswordLog o2) {
				if(o1.getCreateTime().after(o2.getCreateTime())){
					return 1;
				}
				return -1;
			}			
		});
		PasswordLog lastPasswordLog = passwordLogList.get(0);
		if(lastPasswordLog.getCreateTime() == null){
			logger.debug("用户[" + partner.getUuid() + "]当前密码的最近一条记录没有创建时间。");
			return false;
		}
		long createTtl = (new Date().getTime() - lastPasswordLog.getCreateTime().getTime()) / 1000;
		if(createTtl > passwordTtl){
			logger.debug("用户[" + partner.getUuid() + "]当前密码的最近一条创建时间是[" + sdf.format(lastPasswordLog.getCreateTime()) +  "]，已创建" + createTtl + "秒，超过了当前安全级别要求的时限:" + passwordTtl);
			return true;
		} else {
			logger.debug("用户[" + partner.getUuid() + "]当前密码的最近一条创建时间是[" + sdf.format(lastPasswordLog.getCreateTime()) +  "]，已创建" + createTtl + "秒，还未超过当前安全级别要求的时限:" + passwordTtl);
			return false;

		}
	}


	/*
	 * 检查将要设置的密码是否能通过系统安全性检查
	 * 例如长度、复杂度和使用历史等
	 */
	@Override
	public EisMessage passwordIsFine(User partner, String password) {
		if(securityLevel == null){
			logger.debug("当前未定义安全级别规范，不进行密码可用性检查");
			return new EisMessage(OpResult.success.getId(),OpResult.success.getId(),"密码符合规范");
		}
		int passwordMinLength = NumericUtils.getNumeric(SecurityLevelUtils.getConfig(securityLevel, "PASSWORD_MIN_LENGTH"));
		if(passwordMinLength > 0){
			if(password.length() < passwordMinLength){
				logger.debug("用户[" + partner.getUuid() + "]尝试新设的密码长度[" + password.length() + "]低于当前安全级别[" + securityLevelId + "]定义的最小密码长度:" + passwordMinLength);
				return new EisMessage(OpResult.failed.getId(),EisError.PASSWORD_TOO_SHORT.id,"密码长度至少需要" + passwordMinLength + "位");
			}
		}
		int passwordStrongGrade = NumericUtils.getNumeric(SecurityLevelUtils.getConfig(securityLevel, "PASSWORD_STRONG_GRADE"));
		String passwordStrongDesc = getDescForPasswordStrongGrade(passwordStrongGrade);
		if(passwordStrongGrade >= PasswordStrongGrade.CAPITIAL_AND_SMALL_LETTER.id){
			//需要大小写
			if(capitalCharPattern.matcher(password).find() && smallCharPattern.matcher(password).find()){
				logger.debug("密码符合安全级别[" + securityLevelId + "]要求的大小写同时存在要求");
			} else {
				logger.debug("密码不符合安全级别[" + securityLevelId + "]要求的大小写同时存在要求");
				return new EisMessage(OpResult.failed.getId(),EisError.passwordNotStrong.getId(),passwordStrongDesc);
			}			
		} 
		if(passwordStrongGrade >= PasswordStrongGrade.LETTER_AND_NUM.id){
			//需要前一级要求+数字组合
			if(digitalPattern.matcher(password).find()){
				logger.debug("密码符合安全级别[" + securityLevelId + "]要求的大小写和数字同时存在要求");
			} else {
				logger.debug("密码不符合安全级别[" + securityLevelId + "]要求的大小写和数字同时存在要求");
				return new EisMessage(OpResult.failed.getId(),EisError.passwordNotStrong.getId(),passwordStrongDesc);
			}			
		} 
		if(passwordStrongGrade >= PasswordStrongGrade.WORD_AND_SYMBOL.id){
			//需要前一级要求+标点符号
			if(symbolCharPattern.matcher(password).find()){
				logger.debug("密码符合安全级别[" + securityLevelId + "]要求的大小写、数字和标点符号同时存在要求");
			} else {
				logger.debug("密码不符合安全级别[" + securityLevelId + "]要求的大小写、数字和标点符号同时存在要求");
				return new EisMessage(OpResult.failed.getId(),EisError.passwordNotStrong.getId(), passwordStrongDesc);
			}			
		} 
		return new EisMessage(OpResult.success.getId(),OpResult.success.getId(),"密码符合规范");
		/*long passwordTtl = 0L;
		String ttl =SecurityLevelUtils.getConfig(securityLevel, "PASSWORD_TTL");
		if(NumericUtils.isNumeric(ttl)){
			passwordTtl = Long.parseLong(ttl);
		}
		if(passwordTtl < 1){
			logger.debug("当前安全级别[" + securityLevel + "]未定义密码有效期");
			return false;
		}
		PasswordLogCriteria passwordLogCriteria = new PasswordLogCriteria();
		passwordLogCriteria.setUuid(partner.getUuid());
		passwordLogCriteria.setPassword(partner.getUserPassword());
		//检查当前密码的创建时间
		List<PasswordLog> passwordLogList = passwordLogService.list(passwordLogCriteria);
		logger.debug("用户[" + partner.getUuid() + "]当前密码的记录是:" + (passwordLogList == null ? "空" : passwordLogList.size()));
		if(passwordLogList == null || passwordLogList.size() < 1){
			logger.debug("用户[" + partner.getUuid() + "]当前密码没有记录，认为不需要更改密码");
			return false;
		}
		Collections.sort(passwordLogList, new Comparator<PasswordLog>(){
			@Override
			public int compare(PasswordLog o1, PasswordLog o2) {
				if(o1.getCreateTime().after(o2.getCreateTime())){
					return 1;
				}
				return -1;
			}			
		});
		PasswordLog lastPasswordLog = passwordLogList.get(0);
		if(lastPasswordLog.getCreateTime() == null){
			logger.debug("用户[" + partner.getUuid() + "]当前密码的最近一条记录没有创建时间。");
			return false;
		}
		long createTtl = (new Date().getTime() - lastPasswordLog.getCreateTime().getTime()) / 1000;
		if(createTtl > passwordTtl){
			logger.debug("用户[" + partner.getUuid() + "]当前密码的最近一条创建时间是[" + sdf.format(lastPasswordLog.getCreateTime()) +  "]，已创建" + createTtl + "秒，超过了当前安全级别要求的时限:" + passwordTtl);
			return true;
		} else {
			logger.debug("用户[" + partner.getUuid() + "]当前密码的最近一条创建时间是[" + sdf.format(lastPasswordLog.getCreateTime()) +  "]，已创建" + createTtl + "秒，还未超过当前安全级别要求的时限:" + passwordTtl);
			return false;

		}*/
	}



	private String getDescForPasswordStrongGrade(int passwordStrongGrade) {
		if(passwordStrongGrade >= PasswordStrongGrade.WORD_AND_SYMBOL.id){
			return "密码必须同时有大小写字母、数字和标点";
		}
		if(passwordStrongGrade >= PasswordStrongGrade.LETTER_AND_NUM.id){
			return "密码必须同时有大小写字母和数字";
		}
		if(passwordStrongGrade >= PasswordStrongGrade.CAPITIAL_AND_SMALL_LETTER.id){
			return "密码必须同时有大小写字母";
		}
		return null;
	}

	/**
	 * 以重定向方式把用户转发到前往的URL
	 * 并把登录后返回URL写入Cookie
	 */
	@Override
	public String redirectByResponse(HttpServletRequest request, HttpServletResponse response,int userTypeId,  String toUrl,
			String backUrl, String cookieDomain){
		String cookieName = null;
		if(userTypeId == UserTypes.sysUser.id){
			cookieName = COOKIE_REDIRECT_COOKIE_NAME + "_s";
		} else if(userTypeId == UserTypes.partner.id){
			cookieName = COOKIE_REDIRECT_COOKIE_NAME + "_p";
		} else {
			cookieName = COOKIE_REDIRECT_COOKIE_NAME + "_f";
		}
		/*if(backUrl == null){
			logger.debug("重定向请求未提供);
			backUrl = request.getRequestURI();
		}
		if(backUrl.endsWith("json")){
			backUrl = "/";
		}*/
		if(backUrl != null && !backUrl.endsWith(".json")){
			try {
				backUrl = java.net.URLEncoder.encode(backUrl,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			CookieUtils.addCookie(request, response, cookieName, backUrl , Constants.COOKIE_MAX_TTL, cookieDomain);
			logger.debug("向Cookie中写入新的重定向地址:" + backUrl);
		} else {
			logger.debug("请求重定向但未提供返回URL或返回URL是json:" + backUrl);

		}
		response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
		try {
			toUrl = java.net.URLDecoder.decode(toUrl,"UTF-8");
			response.sendRedirect(toUrl);		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public User forceLogin(HttpServletRequest request,
			HttpServletResponse response, String authKey){
		if(StringUtils.isBlank(authKey)){
			logger.warn("尝试登录的authKey为空");
			return null;
		}
		CriteriaMap userCriteria = new CriteriaMap();
		userCriteria.put("currentStatus", new int[] {UserStatus.normal.id});
		userCriteria.put("authKey",authKey);
		List<User> loginUserList = null;

		loginUserList = frontUserService.list(userCriteria);			

		if(loginUserList == null || loginUserList.size() < 1){
			logger.info("未能查找到authKey=" + authKey + "的正常状态前端用户");
			return null;
		}
		if(loginUserList.size() != 1){
			logger.info("查找到authKey=" + authKey + "的正常状态前端用户数量不唯一:" + loginUserList.size());
			return null;
		}
		User user = loginUserList.get(0);
		user.setLastLoginTimestamp(new java.util.Date());
		user.setLastLoginIp(IpUtils.getClientIp(request));
		//生成客户端Token
		generateUserToken(user);

		//updateUserRememberName(request, response, user);
		return loginUserList.get(0);

	}

	@Override
	public void writeSessionCookie(User user, HttpServletRequest request, HttpServletResponse response){
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		String sessionTokenName = Constants.SESSION_TOKEN_NAME;
		if(user.getUserTypeId() == UserTypes.partner.id){
			sessionTokenName += "_p";
		} else 	{
			sessionTokenName += "_f";
		}
		logger.debug("为用户[" + user.getUuid() + "]写入SSO Token:" + sessionTokenName + "=>" + user.getSsoToken());
		CookieUtils.addCookie(request, response, sessionTokenName, user.getSsoToken(),sessionTimeout,siteDomainRelation.getCookieDomain());
	}
	@Override
	public void writeSessionCookie(UserVo vo, HttpServletRequest request, HttpServletResponse response){
		SiteDomainRelation siteDomainRelation = siteDomainRelationService.getByHostName(request.getServerName());
		String sessionTokenName = Constants.SESSION_TOKEN_NAME;
		if(vo.getUserTypeId() == UserTypes.partner.id){
			sessionTokenName += "_p";
		} else 	{
			sessionTokenName += "_f";
		}
		logger.debug("为用户[" + vo.getUuid() + "]写入SSO Token:" + sessionTokenName + "=>" + vo.getSsoToken());
		CookieUtils.addCookie(request, response, sessionTokenName, vo.getSsoToken(),sessionTimeout,siteDomainRelation.getCookieDomain());
	}

	@Override
	public String createJwtToken(String username) {
			final String   REGISTER_TEMP_AES_KEY = configService.getProperty(DataName.REGISTER_TEMP_AES_KEY.name());
			final String SERVER_PRIVATE_KEY = configService.getProperty(DataName.SERVER_PRIVATE_KEY.name());
			if(StringUtils.isBlank(SERVER_PRIVATE_KEY)){
				logger.error("系统未配置系统私钥");
				throw new EisException(EisError.KEY_NOT_FOUND.id,"Server key error");
			}
			String k = SERVER_PRIVATE_KEY.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
			Key privateKey = null;
			try {
				PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decodeBase64(k));
				privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
			} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			AES aes = cn.hutool.crypto.SecureUtil.aes(REGISTER_TEMP_AES_KEY.getBytes(StandardCharsets.UTF_8));
			//   aes.setIv("1234567812345678".getBytes(StandardCharsets.UTF_8));
			String token = aes.encryptBase64(username);
			JwtBuilder builder = Jwts.builder().claim("token",token).setHeaderParam("exp", String.valueOf(DateUtils.addSeconds(new Date(), 3600).getTime()));
			String jwt = builder.signWith(privateKey).compact();
			return jwt;

	}

}
