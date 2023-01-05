package com.maicard.utils;

import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.DisplayLevel;
import com.maicard.core.constants.EncType;
import com.maicard.core.constants.Operate;
import com.maicard.core.entity.DataDefine;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;


public class SecurityUtils {
	
	protected static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	private static int cryptedPasswordLength = 64;
	private static final String HEX_CHARS = "0123456789abcdef";
	private static final String digestKey = "jeyRofsyefhi_Biocsub";

	public static String passwordEncode(String src){
		return DigestUtils.sha256Hex(DigestUtils.md5Hex(src));
	}
	
	public static long formatFrontUuid(int serverId, long uuid) {
		return Long.parseLong(serverId + new DecimalFormat("#00000").format(uuid));
	}
	
	public static String correctPassword(String password){
		if(StringUtils.isBlank(password)){
			return null;
		}
		if(password.length() == cryptedPasswordLength){
			return password;
		}
		return passwordEncode(password);
		
	}
	public static CriteriaMap request2Criteria(HttpServletRequest request){
		CriteriaMap  privilegeCriteria = CriteriaMap.create();
		String uri = request.getRequestURI();
		String[] requestPath = uri.split("/");
		if(requestPath == null || requestPath.length  < 2){
			logger.warn("当前请求的URI为空或长度不足:" + uri);
			return null;
		}
		//过滤掉可能的文件后缀
		String objectTypeCode = requestPath[1].replaceAll("\\.\\w*$", "");
		if(requestPath.length == 2){
			privilegeCriteria.put("objectTypeCode",objectTypeCode);
			if(request.getMethod().equals("POST")){
				privilegeCriteria.put("operateCode",Operate.create.name());
			} else {
				privilegeCriteria.put("operateCode",Operate.list.name());			
			}
			logger.debug("当前请求[" + uri + "]解析为:对对象" + privilegeCriteria.get("objectTypeCode") + "进行" + privilegeCriteria.get("operateCode") + "操作");
			return privilegeCriteria;
		}
		/*
		 * 如果URI请求有三部分
		 * 那么第2部分就是操作对象的代码objectTypeCode
		 * 第3部分只能是GET请求来获取页面
		 */
		if(requestPath.length == 3){
			privilegeCriteria.put("objectTypeCode",objectTypeCode);
			boolean isValidOperator = false;
			String opereateCode = requestPath[2].replaceAll("\\.\\w*", "");
			if(OperateUtils.getOperateEnum().contains(opereateCode)){
				isValidOperator = true;
			}
			/*for(String op : operateEnum){
				if(op.equals(opereateCode)){
					isValidOperator = true;
					break;
				}
			}*/
			if(!isValidOperator){
				logger.debug("操作码[" + opereateCode + "]不合法");
				return null;
			}
			if(opereateCode.equalsIgnoreCase("index")){
				opereateCode = Operate.list.name();
			}
			privilegeCriteria.put("operateCode", opereateCode);			
			logger.debug("当前请求[" + uri + "]解析为:对对象" + privilegeCriteria.get("objectTypeCode") + "进行" + privilegeCriteria.get("operateCode") + "操作");
			return privilegeCriteria;

		}
		/*
		 * URI请求有四个部分
		 * 第2部分：是操作对象的代码objectTypeCode
		 * 第3部分：是操作代码operateCode/privilegeCode
		 * 第4部分：二级操作码或操作对象的ID
		 */
		logger.info(requestPath[0]+"|"+requestPath[1]+"|"+requestPath[2]+"|"+requestPath[3]);
		privilegeCriteria.put("objectTypeCode",objectTypeCode);
		boolean isValidOperator = false;
		for(Operate op : Operate.values()){
			if(op.name().equals(requestPath[2])){
				isValidOperator = true;
			//	logger.info(isValidOperator+"有权限！！！");
				break;
			}
		}
		if(!isValidOperator){
			logger.warn("错误的操作码:" + requestPath[2]);
			return null;
		}
		privilegeCriteria.put("operateCode",requestPath[2]);
		String targetObject = requestPath[3].replaceAll("\\.\\w*", "");
		if(NumericUtils.isNumeric(targetObject)){
			//最后一部分是请求的一个对象的ID
			privilegeCriteria.put("objectId",targetObject);
			logger.debug("当前请求[" + uri + "]解析为:对对象" + privilegeCriteria.get("objectTypeCode") + "#" + targetObject + "进行" + privilegeCriteria.get("operateCode") + "操作");
		} else {
			//最后一部分呢时请求对象的某个属性
			privilegeCriteria.put("objectId","self");
			privilegeCriteria.put("objectAttribute",targetObject);	
			logger.debug("当前请求[" + uri + "]解析为:对对象" + privilegeCriteria.get("objectTypeCode") + "的属性" + privilegeCriteria.get("objectAttribute") + "进行" + privilegeCriteria.get("operateCode") + "操作");
		}
		/*
		String objectId = "";
		try{
			objectId =requestPath[3].replaceAll("\\.\\w*", "");
		}catch(Exception e){
			e.printStackTrace();
		}
		if(objectId ==""){
			return null;
		}
		privilegeCriteria.setObjectId(objectId);*/
		return privilegeCriteria;
	}
	
	public static String readAesKey() throws Exception{	

		String mac =  _readMac();
		/*if(masterKey == null){
			try{
				masterKey = readKeyNative();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
		if(mac == null){
			throw new Exception("System access error!");
		}*/
		String key = DigestUtils.sha256Hex(DigestUtils.md5Hex(mac + digestKey)).substring(0, 16);
		//System.out.println("读取到本机信息:" + mac  + ",,返回对应AES密钥:" + key);
		return key;
	}

	private static String _readMac(){
		try{
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface ni = en.nextElement();
				if(ni.getName().equals("lo")){
					continue;
				}
				if(ni.getHardwareAddress() != null && ni.getHardwareAddress().length > 0){
					return toHexString(ni.getHardwareAddress());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String toHexString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_CHARS.charAt(b[i] >>> 4 & 0x0F));
			sb.append(HEX_CHARS.charAt(b[i] & 0x0F));
		}
		return sb.toString();
	}
	
	

	public static CriteriaMap uri2PrivilegeParams(HttpServletRequest request){
		CriteriaMap privilegeCriteria = CriteriaMap.create();
		String uri = request.getRequestURI();
		String[] requestPath = uri.split("/");
		if(requestPath == null || requestPath.length  < 2){
			logger.warn("当前请求的URI为空或长度不足:" + uri);
			return privilegeCriteria;
		}
		//过滤掉可能的文件后缀
		String objectTypeCode = requestPath[1].replaceAll("\\.\\w*$", "");
		/*
		 * 如果请求URI只有两部分
		 * 那么最后一部分就是操作对象的代码objectTypeCode
		 * HTTP请求方式GET/POST就是操作代码privilegeCode
		 * GET就是list，POST就是create
		 */
		privilegeCriteria.put("objectTypeCode",objectTypeCode);
		if(requestPath.length == 2){
			if(request.getMethod().equals("POST")){
				privilegeCriteria.put("operateCode",Operate.create.name());
			} else {
				privilegeCriteria.put("operateCode",Operate.list.name());
			}
			logger.debug("当前请求[" + uri + "]解析为:对对象" + objectTypeCode + "进行" + privilegeCriteria.get("operateCode") + "操作");
			return privilegeCriteria;
		}
		/*
		 * 如果URI请求有三部分
		 * 那么第2部分就是操作对象的代码objectTypeCode
		 * 第3部分只能是GET请求来获取页面
		 */
		if(requestPath.length == 3){
			boolean isValidOperator = false;
			String opereateCode = requestPath[2].replaceAll("\\.\\w*", "");
			if(OperateUtils.getOperateEnum().contains(opereateCode)){
				isValidOperator = true;
			}
			/*for(String op : operateEnum){
				if(op.equals(opereateCode)){
					isValidOperator = true;
					break;
				}
			}*/
			if(!isValidOperator){
				logger.debug("操作码[" + opereateCode + "]不合法");
				return null;
			}
			privilegeCriteria.put("operateCode",opereateCode);
			logger.debug("当前请求[" + uri + "]解析为:对对象" + objectTypeCode + "进行" + privilegeCriteria.get("operateCode") + "操作");
			return privilegeCriteria;

		}
		/*
		 * URI请求有四个部分
		 * 第2部分：是操作对象的代码objectTypeCode
		 * 第3部分：是操作代码operateCode/privilegeCode
		 * 第4部分：二级操作码或操作对象的ID
		 */
		logger.info(requestPath[0]+"|"+requestPath[1]+"|"+requestPath[2]+"|"+requestPath[3]);
		boolean isValidOperator = false;
		for(Operate op : Operate.values()){
			if(op.name().equals(requestPath[2])){
				isValidOperator = true;
			//	logger.info(isValidOperator+"有权限！！！");
				break;
			}
		}
		if(!isValidOperator){
			logger.warn("错误的操作码:" + requestPath[2]);
			return privilegeCriteria;
		}
		String operateCode = requestPath[2];
		privilegeCriteria.put("operateCode",operateCode);
		String targetObject = requestPath[3].replaceAll("\\.\\w*", "");
		if(NumericUtils.isNumeric(targetObject)){
			//最后一部分是请求的一个对象的ID
			privilegeCriteria.put("objectId",NumericUtils.parseLong(targetObject));
			logger.debug("当前请求[" + uri + "]解析为:对对象" + objectTypeCode + "#" + targetObject + "进行" + privilegeCriteria.get("operateCode") + "操作");
		} else {
			//最后一部分是请求对象的某个属性
			privilegeCriteria.put("objectId","self");
			privilegeCriteria.put("objectAttribute",targetObject);	
			logger.debug("当前请求[" + uri + "]解析为:对对象" + objectTypeCode + "的属性" + targetObject + "进行" + privilegeCriteria.get("operateCode") + "操作");
		}
		return privilegeCriteria;
	}
	
	public static String rsaDecrypt(String base64, String base64PrivateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException   {
		//PrivateKey pk  = getRsaPrivateKey(base64PrivateKey);		
		RSA rsa = SecureUtil.rsa(base64PrivateKey,null);
		byte[] bin = rsa.decryptFromBase64(base64, KeyType.PrivateKey);
		return new String(bin,Constants.DEFAULT_CHARSET);
	}
	
	public static String rsaDecrypt(byte[] encoded, String encodeType, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException   {
		
		if(StringUtils.isBlank(encodeType)) {
			encodeType = EncType.BASE64;
		}
		//PrivateKey pk  = getRsaPrivateKey(base64PrivateKey);	
		logger.info("RSA解密{}，编码格式:{}:" + encoded);
		RSA rsa = SecureUtil.rsa();
		rsa.setPrivateKey(privateKey);
		return new String(rsa.decrypt(encoded, KeyType.PrivateKey),Constants.DEFAULT_CHARSET);

		/*if(encodeType.equalsIgnoreCase(EncType.HEX)) {
			return new String(rsa.decrypt(bytes, keyType)(encoded, KeyType.PrivateKey),Constants.DEFAULT_CHARSET);
		} else if(encodeType.equalsIgnoreCase(EncType.BCD)) {
			return new String(rsa.decryptFromBcd(encoded, KeyType.PrivateKey),Constants.DEFAULT_CHARSET);
		} else {
			return new String(rsa.decryptFromBase64(encoded, KeyType.PrivateKey),Constants.DEFAULT_CHARSET);
		}*/
	}
	
	public static byte[] asc2bcd(byte[] ascii, int asc_len) {    
		byte[] bcd = new byte[asc_len / 2];    
		int j = 0;    
		for (int i = 0; i < (asc_len + 1) / 2; i++) {    
			bcd[i] = asc_to_bcd(ascii[j++]);    
			bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));    
		}    
		return bcd;    
	}    
	public static byte asc_to_bcd(byte asc) {    
		byte bcd;    

		if ((asc >= '0') && (asc <= '9'))    
			bcd = (byte) (asc - '0');    
		else if ((asc >= 'A') && (asc <= 'F'))    
			bcd = (byte) (asc - 'A' + 10);    
		else if ((asc >= 'a') && (asc <= 'f'))    
			bcd = (byte) (asc - 'a' + 10);    
		else    
			bcd = (byte) (asc - 48);    
		return bcd;    
	}

	public static boolean isValidDisplayLevel(DataDefine dataDefine, String displayLevel) {
		if(StringUtils.isBlank(displayLevel)){
			return true;
		}
		if(StringUtils.isBlank(dataDefine.getDisplayLevel())) {
			return true;
		}
		int currentWeight = DisplayLevel.findByName(dataDefine.getDisplayLevel());
		int targetLevel = DisplayLevel.findByName(displayLevel);
		return currentWeight <= targetLevel;
	}    

	public static String shortMd5(String src){
		String key = String.valueOf(System.currentTimeMillis());                 //自定义生成MD5加密字符串前的混合KEY 
		String[] chars = new String[]{          //要使用生成URL的字符 
				"a","b","c","d","e","f","g","h", 
				"i","j","k","l","m","n","o","p", 
				"q","r","s","t","u","v","w","x", 
				"y","z","0","1","2","3","4","5", 
				"6","7","8","9","A","B","C","D", 
				"E","F","G","H","I","J","K","L", 
				"M","N","O","P","Q","R","S","T", 
				"U","V","W","X","Y","Z" 
		}; 
		String md5 = DigestUtils.md5Hex(src + key);


		int hexLen = md5.length(); 
		int subHexLen = hexLen / 8; 
		String[] ShortStr = new String[4]; 

		for (int i = 0; i < subHexLen; i++) { 
			String outChars = ""; 
			int j = i + 1; 
			String subHex = md5.substring(i * 8, j * 8); 
			long idx = Long.valueOf("3FFFFFFF", 16) & Long.valueOf(subHex, 16); 

			for (int k = 0; k < 6; k++) { 
				int index = (int) (Long.valueOf("0000003D", 16) & idx); 
				outChars += chars[index]; 
				idx = idx >> 5; 
			} 
			ShortStr[i] = outChars; 
		}
		int rand = (int)Math.round((Math.random()*100))%3;
		//生成三位随机数
		String shortStr = ShortStr[rand];
		/*for(int i = 0; i < 3; i++){
			int r = (int)(Math.random()*62);
			shortStr += chars[r];
		}*/
		return shortStr;

	}

	public static String smsCode() {
		return String.valueOf(new Random().nextInt(999999)%(999999-100000+1) + 100000);
	}

	public static String getSsoName(String systemCode) {
		return "_" + systemCode + "_sso";
	}

	public static String getSsoUuidName(String systemCode) {
		return "_" + systemCode + "_suid";
	}
}
