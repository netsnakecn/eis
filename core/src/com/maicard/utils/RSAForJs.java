package com.maicard.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import javax.crypto.Cipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 
 *  XXX js端的padding方式似乎会与标准的不兼容，因此不要把本工具用于通用RSA加解密
 *  只能用于登录js中对应的
 * 
 * RSA加密工具
 * 必须先在jre和jdk中注册新的安全供应jce工具org.bouncycastle.jce.provider.BouncyCastleProvider
 *
 * 参考
 * http://blog.csdn.net/yys79/article/details/41514871
 * @author NetSnake
 * @date 2015年12月21日
 *
 */
public class RSAForJs {
	protected static final Logger logger = LoggerFactory.getLogger(RSAForJs.class);

	/** 
     * RSA最大加密明文大小 
     */  
    //private static final int MAX_ENCRYPT_BLOCK = 117;  
      
    /** 
     * RSA最大解密密文大小 
     */  
    //private static final int MAX_DECRYPT_BLOCK = 128;  
	
	private static final int KEY_SIZE = 1024;	
	//private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";
	
	

	public static final String PROVIDER = "BC";

	static{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	public static KeyPair genKeyPair(){
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(KEY_SIZE, new SecureRandom());
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 使用RSA公钥文本进行加密
	 * 
	 *
	 * @author NetSnake
	 * @date 2018-05-23
	 */
	/*public static byte[] encryptWithPublicKey(byte[] src, String publicKeyStr) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException{
		KeyFactory factory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Crypt.toByteArray(publicKeyStr));
		RSAPublicKey publicKey = (RSAPublicKey)factory.generatePublic(x509EncodedKeySpec);

		publicKey.getModulus().toString();
		Cipher cipher = Cipher.getInstance(algorithm);    
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);    
		return cipher.doFinal(src);
	}
	*/
	
	/*
	public static byte[] encryptByPublicKey(byte[] data, String publicKeyStr)  
            throws Exception {  
		if(StringUtils.isBlank(publicKeyStr)){
			throw new Exception("加密时公钥不能为空");
		}
        byte[] keyBytes = Crypt.toByteArray(publicKeyStr);  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
		KeyFactory keyFactory = KeyFactory.getInstance("RSA", PROVIDER);    
        RSAPublicKey publicKey = (RSAPublicKey)keyFactory.generatePublic(x509KeySpec);  
        // 对数据加密  
        Cipher cipher = Cipher.getInstance(ALGORITHM);  
        logger.debug("使用算法:{}进行公钥加密", keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
        int inputLen = data.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;  
        // 对数据分段加密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {  
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);  
            } else {  
                cache = cipher.doFinal(data, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * MAX_ENCRYPT_BLOCK;  
        }  
        byte[] encryptedData = out.toByteArray();  
        out.close();  
        return encryptedData;  
    }  
	*/
	/*public static byte[] decryptWithPrivateKey(byte[] src, String privateKeyStr) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException{
		KeyFactory factory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Crypt.toByteArray(privateKeyStr));
		RSAPrivateKey privateKey = (RSAPrivateKey)factory.generatePrivate(x509EncodedKeySpec);

		privateKey.getModulus().toString();
		Cipher cipher = Cipher.getInstance(algorithm);    
		cipher.init(Cipher.DECRYPT_MODE, privateKey);    
		return cipher.doFinal(src);
	}*/
	
	/*
	public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKeyStr)  
            throws Exception {  
		if(StringUtils.isBlank(privateKeyStr)){
			throw new Exception("解密时私钥不能为空");
		}
        byte[] keyBytes = Crypt.toByteArray(privateKeyStr);  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
		//KeyFactory keyFactory = KeyFactory.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());    
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");//, new org.bouncycastle.jce.provider.BouncyCastleProvider());    
		RSAPrivateKey privateKey = (RSAPrivateKey)keyFactory.generatePrivate(pkcs8KeySpec);  
        Cipher cipher = Cipher.getInstance(ALGORITHM, PROVIDER);  
        logger.debug("使用算法:{}/{}进行私钥解密", keyFactory.getAlgorithm(), cipher.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);  
        int inputLen = encryptedData.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;  
        // 对数据分段解密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {  
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);  
            } else {  
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * MAX_DECRYPT_BLOCK;  
        }  
        byte[] decryptedData = out.toByteArray();  
        out.close();  
        return decryptedData;  
    }  */
	/*
	 * 用RSA公钥进行加密
	 */
	/*
	public static String encrypt(String src, String modulus, String exponent){
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", PROVIDER);    
			//KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);    
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));  
			RSAPublicKey publicKey = (RSAPublicKey)keyFactory.generatePublic(keySpec);

			Cipher cipher = Cipher.getInstance(ALGORITHM);    
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);    
			
			// 模长    
			int key_len = publicKey.getModulus().bitLength() / 8;    
			// 加密数据长度 <= 模长-11    
			String[] datas = splitString(src, key_len - 11);    
			String dst = "";    
			//如果明文长度大于模长-11则要分组加密    
			for (String s : datas) {    
				dst += bcd2Str(cipher.doFinal(s.getBytes()));    
			}    
			return dst;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}*/

	/*
	 * 用RSA私钥系数和指数转换为私钥后，调用其他方法进行解密
	 */
	/*
	public static String decrypt(String src, String modulus, String exponent){
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");    
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(modulus), new BigInteger(exponent));  
			RSAPrivateKey privateKey = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
			return decrypt(src, privateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	*/
	/*
	 * 用RSA私钥进行解密
	 */
	public static String decrypt(String src, RSAPrivateKey privateKey){
		try {
			Cipher cipher = Cipher.getInstance("RSA",PROVIDER);    
			cipher.init(Cipher.DECRYPT_MODE, privateKey);  
			
			int key_len = privateKey.getModulus().bitLength() / 8;    
			byte[] bytes = (src.getBytes());
			
			byte[] bcd = ASCII_To_BCD(bytes, bytes.length);    
			//如果密文长度大于模长则要分组解密    
			String dst = "";    
			byte[][] arrays = splitArray(bcd, key_len);    
			for(byte[] arr : arrays){    
				dst += new String(cipher.doFinal(arr)); 
			}    			
			return dst;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**  
	 *拆分数组   
	 */    
	private static byte[][] splitArray(byte[] data,int len){    
		int x = data.length / len;    
		int y = data.length % len;    
		int z = 0;    
		if(y!=0){    
			z = 1;    
		}    
		byte[][] arrays = new byte[x+z][];    
		byte[] arr;    
		for(int i=0; i<x+z; i++){    
			arr = new byte[len];    
			if(i==x+z-1 && y!=0){    
				System.arraycopy(data, i*len, arr, 0, y);    
			}else{    
				System.arraycopy(data, i*len, arr, 0, len);    
			}    
			arrays[i] = arr;    
		}    
		return arrays;    
	}  
	/**  
	 * 拆分字符串  
	 */    
	
	/*
	private static String[] splitString(String string, int len) {    
		int x = string.length() / len;    
		int y = string.length() % len;    
		int z = 0;    
		if (y != 0) {    
			z = 1;    
		}    
		String[] strings = new String[x + z];    
		String str = "";    
		for (int i=0; i<x+z; i++) {    
			if (i==x+z-1 && y!=0) {    
				str = string.substring(i*len, i*len+y);    
			}else{    
				str = string.substring(i*len, i*len+len);    
			}    
			strings[i] = str;    
		}    
		return strings;    
	}    */

	private static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {    
		byte[] bcd = new byte[asc_len / 2];    
		int j = 0;    
		for (int i = 0; i < (asc_len + 1) / 2; i++) {    
			bcd[i] = asc_to_bcd(ascii[j++]);    
			bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));    
		}    
		return bcd;    
	}    

	private static byte asc_to_bcd(byte asc) {    
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
	/**  
	 * BCD转字符串  
	 */    
	public static String bcd2Str(byte[] bytes) {    
		char temp[] = new char[bytes.length * 2], val;    

		for (int i = 0; i < bytes.length; i++) {    
			val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);    
			temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');    

			val = (char) (bytes[i] & 0x0f);    
			temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');    
		}    
		return new String(temp);    
	}   
}
