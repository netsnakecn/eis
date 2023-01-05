package com.maicard.misc;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.maicard.core.constants.Constants;
import com.maicard.security.entity.SecurityLevel;
import com.maicard.utils.Crypt;
import com.maicard.utils.SecurityLevelUtils;
import com.maicard.utils.SecurityUtils;

import java.util.Properties;


/**
 * 读取加密的properties文件，解密后返回明文
 *
 *
 * @author NetSnake
 * @date 2015年12月18日
 *
 */
@SuppressWarnings("deprecation")
public class EncryptPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer{

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final String[] needEncryptNames = new String[]{"username","password"};
	
	private final int securityLevel = SecurityLevelUtils.getSecurityLevel();

	protected Properties properties;


	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		this.properties = props;
	}
	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return this.properties.getProperty(key, defaultValue);
	}


	private boolean needCrypt(String key){
		for(String needKey : needEncryptNames){
			if(key.toLowerCase().contains(needKey.toLowerCase())){
				return true;
			}
		}
		return false;
	}
	@Override
	protected String convertProperty(String propertyName, String propertyValue) { 
		if(securityLevel <= SecurityLevel.SECURITY_LEVEL_NORMAL){
			logger.debug("当前安全级别为" + securityLevel + ",不进行配置文件解密");
			return super.convertProperty(propertyName, propertyValue);
		}
		if (needCrypt(propertyName)) { //如果在加密属性名单中发现该属性
			logger.debug("准备对属性[" + propertyName + "]进行解密，解密前:" + propertyValue);
			String decryptedValue = decrypt(propertyValue);
			if (decryptedValue != null) {  //!=null说明正常
				propertyValue = decryptedValue; //设置解决后的值
			} else {//说明解密失败
				logger.error("对属性[ " + propertyName + "=" + propertyValue + "]解密失败");
			}
		}
		return super.convertProperty(propertyName, propertyValue);  //将处理过的值传给父类继续处理
	}
	
	private String decrypt(String propertyValue) {
		String key = null;
		try{
			key = SecurityUtils.readAesKey();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(key == null){
			logger.error("无法读取本地密钥");
			return null;
		}
		
		Crypt crypt = new Crypt();
		crypt.setAesKey(key);
		
		try{
			return new String(crypt.aesDecryptBase64(propertyValue),Constants.DEFAULT_CHARSET);
		}catch(Exception e){
			logger.error("无法对数据[" + propertyValue + "]进行解密:" + ExceptionUtils.getFullStackTrace(e));
		}
		return null;
	}


}
