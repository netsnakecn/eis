package com.maicard.utils;


import java.security.Security;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.hutool.core.util.HexUtil;





public class Crypt {
	SecretKey desKey;
	SecretKey aesKey;

	public String rsaPublicKeyFile, rsaPrivateKeyFile;
	public String aesKeyFile;
	/*byte[] des3keyBytes = {0x11, 0x22, 0x4F, 0x58, (byte)0x88, 0x10, 0x40, 0x38
            , 0x28, 0x25, 0x79, 0x51, (byte)0xCB, (byte)0xDD, 0x55, 0x66
            , 0x77, 0x29, 0x74, (byte)0x98, 0x30, 0x40, 0x36, (byte)0xE2};*/
	private static final String HEX_CHARS = "0123456789abcdef";

	private static final String DEFAULT_ENCODING = "UTF-8";
	
	//public static final String AES_MODE = "AES/ECB/PKCS5Padding";
	public static final String AES_MODE = "AES/CBC/PKCS7Padding";
	public static final String PROVIDER = "BC";
    public static final String IV = "1234567890123456";


	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
	}
	
	
	public void setAesKey(String key){
		setAesKey( key.getBytes());

	}

	public void setAesKey(byte[] key) {
        
		try {
			aesKey =  new SecretKeySpec(key, "AES");  
		}catch(Exception e){
			e.printStackTrace();
		}		   
	}

	
	


	public boolean aesGenKey(){
		try{
			KeyGenerator kg= KeyGenerator.getInstance(AES_MODE, PROVIDER);			
			kg.init(128);
			SecretKey key = kg.generateKey();
			byte[] keyRaw = key.getEncoded();
			System.out.println(keyRaw);

			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public byte[] aesEncrypt(byte[] src) {
		try{ 
			Cipher c1 = Cipher.getInstance(AES_MODE, PROVIDER);
	        IvParameterSpec iv = new IvParameterSpec(IV.getBytes("UTF-8"));
			c1.init(Cipher.ENCRYPT_MODE, aesKey, iv);
			byte[] encoded = c1.doFinal(src);
			return encoded;

		}catch(Exception e){
			e.printStackTrace();
			return null;

		}

	}
	
	public String aesEncryptBase64(String text) throws Exception {
		return Base64.encodeBase64String(aesEncrypt(text.getBytes(DEFAULT_ENCODING)));
	}
	
	public byte[] aesDecryptBase64(String base64) throws Exception {
		return aesDecrypt(Base64.decodeBase64(base64));
	}
	
	public String aesEncryptHex(String text) throws Exception {
		return HexUtil.encodeHexStr(aesEncrypt(text.getBytes(DEFAULT_ENCODING)));
	}
	
	public byte[] aesDecryptHex(String hex) throws Exception {
		return aesDecrypt(HexUtil.decodeHex(hex));
	}
	
	
	public static String passwordEncode(String src){
		return DigestUtils.sha256Hex(DigestUtils.md5Hex(src));
	}
	
	

	

	public byte[] aesDecrypt(byte[] dst) {
		System.out.println("AES KEY=" + aesKey);
		try{ 
			//SecretKeySpec  key= new SecretKeySpec(this.aeskeyBytes, "AES");
			Cipher c1 = Cipher.getInstance(AES_MODE, PROVIDER);
	        IvParameterSpec iv = new IvParameterSpec(IV.getBytes("UTF-8"));
			c1.init(Cipher.DECRYPT_MODE, aesKey, iv);
			byte[] decoded = c1.doFinal(dst);
			return decoded;

		}catch(Exception e){
			e.printStackTrace();
			return null;

		}

	}

	public static byte[] hexStringToByte(String hex) {  
		int len = (hex.length() / 2);  
		byte[] result = new byte[len];  
		char[] achar = hex.toCharArray();  
		for (int i = 0; i < len; i++) {  
			int pos = i * 2;  
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));  
		}  
		return result;  
	}  

	private static byte toByte(char c) {  
		byte b = (byte) "0123456789ABCDEF".indexOf(c);  
		return b;  
	}  

	public static String byteToHex(byte b)		   {
		char Digest[] = { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		char[]ch = new char[2];
		ch[0] = Digest[(b>>>4&0X0F)];
		ch[1] = Digest[b&0X0F];
		return new String(ch);
	}

	public static String base64Encode(String src){
		return new String(new Base64().encode(src.getBytes())).replaceAll("\r\n", "").replaceAll("\n", "");

	}
	public static String base64Decode(String src){
		return new String(new Base64().decode(src.getBytes())).replaceAll("\r\n", "").replaceAll("\n", "");

	}

	public static String toHexString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_CHARS.charAt(b[i] >>> 4 & 0x0F));
			sb.append(HEX_CHARS.charAt(b[i] & 0x0F));
		}
		return sb.toString();
	}

	public static byte[] toByteArray(String s) {
		byte[] buf = new byte[s.length() / 2];
		int j = 0;
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) ((Character.digit(s.charAt(j++), 16) << 4) | Character
					.digit(s.charAt(j++), 16));
		}
		return buf;
	}

	

	public static String shortMd5(String src){
		if(src == null || src.length() != 32){
			return null;
		}
		String result = "";
		for(int i = 0; i < src.length(); i++){
			if(i % 2 == 0){
				result += src.charAt(i);
			}
		}
		return result;
	}
	public static byte[] base64Encode(byte[] sourceBytes) {
		return new Base64().encode(sourceBytes);
		//String dest =  new String(new Base64().encode(sourceBytes)).replaceAll("\r\n", "").replaceAll("\n", "");
		//return dest.getBytes();
	}

	public static byte[] base64Decode(byte[] sourceBytes) {
		return new Base64().decode(sourceBytes);
	}




}
