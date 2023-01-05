package com.maicard.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.core.exception.EisException;
import com.maicard.misc.TrustAnyHostSSLProtocolSocketFactory;


public class HttpUtils {

	protected static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	public static final int CONNECTION_TIMEOUT = 3000;
	public static final int SO_TIMEOUT = 30000;

	public static HttpClient getHttpClient(String host, int port, String protocol) {

		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		HttpClient httpClients = new HttpClient(manager);
		httpClients.getHostConfiguration().setHost(host, port, protocol);
		httpClients.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT);
		httpClients.getParams().setSoTimeout(SO_TIMEOUT);
		return httpClients;

	}


	public static HttpClient getHttpClient(String url) {
		String[] data = HttpUtils.parseHostAndPort(url);
		String protocol = url.startsWith("https") ? "https" : "http";
		return getHttpClient(data[0], NumericUtils.parseInt(data[1]),protocol);

	}

	protected static void setHeader(HttpMethod httpMethod){
		httpMethod.setFollowRedirects(false);
		httpMethod.getParams().setContentCharset("UTF-8");
		httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpMethod.addRequestHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.2; zh-CN; rv:1.8.0.4) Gecko/20060508 Firefox/1.5.0.4");
		httpMethod.addRequestHeader("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		httpMethod.addRequestHeader("Accept-Language", "zh-cn,en;q=0.5");
		httpMethod.addRequestHeader("UA-cpu", "x86");
		httpMethod.addRequestHeader("Connection", "Keep-Alive");
		httpMethod.addRequestHeader("Accept-Charset", "gbk,utf-8;q=0.7,*;q=0.7");
		httpMethod.addRequestHeader("Accept-Encoding", "deflate");

	}
	public static String sendData(String url){
		return sendData(url, true);

	}

	/**
	 * 发送请求并返回结果
	 * @param url
	 * @param handlerRedirect 如果为true，则跟随302跳转直到最后得到页面，否则在302时将返回302地址
	 * @return
	 */
	public static String sendData(String url, boolean handlerRedirect){
		String[] hosts = parseHostAndPort(url);
		if(hosts == null || hosts.length < 2){
			throw new EisException("无法解析URL[" + url + "]的主机和端口");
		}
		HttpClient hc = getHttpClient(url);
		hc.getHostConfiguration().setHost(hosts[0], Integer.parseInt(hosts[1]));
		hc.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT);
		hc.getParams().setSoTimeout(SO_TIMEOUT);
		GetMethod method = new GetMethod(url);
		setHeader(method);
		try{
			int status = hc.executeMethod(method);
			if(!handlerRedirect && status == HttpStatus.SC_MOVED_TEMPORARILY){
				//302跳转
				String location  = method.getResponseHeader("Location").getValue();
				/*if(!location.startsWith("http")){
					System.out.println("当前请求:" + url + "的URI是:" + method.getURI().get
				}*/
				return location;
			}
			String responseCharset = method.getResponseCharSet();
			if(StringUtils.isBlank(responseCharset)){
				responseCharset = "UTF-8";
			} else {
				logger.debug("对方的编码是:" + responseCharset);
			}
			BufferedReader br = new   BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(),responseCharset));
			String temp = "";
			StringBuffer sb = new StringBuffer(100);
			while((temp = br.readLine()) != null){
				sb.append(temp + "\n");
			}
			//System.out.println(sb.toString());
			return sb.toString().trim();
		}catch(Exception e){
			//e.printStackTrace();
		}
		return null;
	}

	public static String postData(String url, String body){
		String[] hosts = parseHostAndPort(url);
		if(hosts == null || hosts.length < 2){
			throw new EisException("无法解析URL[" + url + "]的主机和端口");
		}
		HttpClient hc = getHttpClient(url);
		hc.getHostConfiguration().setHost(hosts[0], Integer.parseInt(hosts[1]));
		hc.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT);
		hc.getParams().setSoTimeout(SO_TIMEOUT);
		PostMethod method = new PostMethod(url);
		setHeader(method);
		try{
			RequestEntity requestEntity = new ByteArrayRequestEntity(body.getBytes("UTF-8"));
			method.setRequestEntity(requestEntity);  //new ByteArrayEntity(body.getBytes("UTF-8")));  
			hc.executeMethod(method);
			String responseCharset = method.getResponseCharSet();
			if(StringUtils.isBlank(responseCharset)){
				responseCharset = "UTF-8";
			} else {
				logger.debug("对方的编码是:" + responseCharset);
			}
			BufferedReader br = new   BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(),responseCharset));
			String temp = "";
			StringBuffer sb = new StringBuffer(100);
			while((temp = br.readLine()) != null){
				sb.append(temp);
			}
			return sb.toString().trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;

	}

	public static String postData(String url, Part[] requestData){
		String[] hosts = parseHostAndPort(url);
		if(hosts == null || hosts.length < 2){
			throw new EisException("无法解析URL[" + url + "]的主机和端口");
		}
		HttpClient hc = getHttpClient(url);
		hc.getHostConfiguration().setHost(hosts[0], Integer.parseInt(hosts[1]));
		PostMethod method = new PostMethod(url);
		setHeader(method);
		try{
			method.setRequestEntity(new MultipartRequestEntity(requestData, method.getParams()));  //new ByteArrayEntity(body.getBytes("UTF-8")));  
			hc.executeMethod(method);
			String responseCharset = method.getResponseCharSet();
			if(StringUtils.isBlank(responseCharset)){
				responseCharset = "UTF-8";
			} else {
				logger.debug("对方的编码是:" + responseCharset);
			}
			BufferedReader br = new   BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(),responseCharset));
			String temp = "";
			StringBuffer sb = new StringBuffer(100);
			while((temp = br.readLine()) != null){
				sb.append(temp);
			}
			return sb.toString().trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;

	}

	public static String[]  parseHostAndPort(String url){
		if(StringUtils.isBlank(url)){
			return new String[0];
		}
		String[] result = new String[2];
		//System.out.println("url:" + url);
		String tempUrl = url.replaceAll("^\\w+://", "");
		int offset = tempUrl.indexOf("/");
		String tempString;
		if(offset > -1){
			tempString = tempUrl.substring(0,offset);
		} else {
			tempString = tempUrl;
		}
		//System.out.println("url:" + tempString);
		Matcher matcher = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+").matcher(tempString);
		String host;
		String port;
		if(matcher.find()){//全IP地址
			tempString = tempString.replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+", "");
			host = matcher.group(0);
			//System.out.println("发现全IP地址:" + host);
			port = tempString.replaceAll(":", "");
			if(port == null || port.equals("")){
				port = "80";
			}//520t
			result[0] = host;
			result[1] = port;
			logger.debug("解析URL:"  + url + "结果,主机:" + result[0] + ",端口:" + result[1]);
			return result;
		}
		//System.out.println("url3:" + tempString);
		matcher = Pattern.compile("\\d+$").matcher(tempString);
		if(matcher.find()){
			String tempPort = matcher.group(0);
			if(tempPort != null){
				result[0] = tempString.replaceAll(tempPort, "").replaceAll(":$", "");
				result[1] = tempPort;
				logger.debug("解析URL:"  + tempUrl + "结果,主机:" + result[0] + ",端口:" + result[1]);
			}
		} else {
			result[0] = tempString.replaceAll(":$", "");
			result[1] = "80";
		}

		return result;
	}
	public static String postData(String url, NameValuePair[] requestData) {
		String[] hosts = parseHostAndPort(url);
		if(hosts == null || hosts.length < 2){
			throw new EisException("无法解析URL[" + url + "]的主机和端口");
		}
		HttpClient hc = getHttpClient(url);

		Protocol myhttps = new Protocol("https", new TrustAnyHostSSLProtocolSocketFactory(), 443);   
		Protocol.registerProtocol("https", myhttps);  
		logger.info("注册信任所有https证书");

		hc.getHostConfiguration().setHost(hosts[0], Integer.parseInt(hosts[1]));
		hc.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT);
		hc.getParams().setSoTimeout(SO_TIMEOUT);
		PostMethod method = new PostMethod(url);
		method.addParameters(requestData);
		setHeader(method);
		try{
			hc.executeMethod(method);
			String responseCharset = method.getResponseCharSet();
			if(StringUtils.isBlank(responseCharset)){
				responseCharset = "UTF-8";
			} else {
				logger.debug("对方的编码是:" + responseCharset);
			}
			BufferedReader br = new   BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(),responseCharset));
			String temp = "";
			StringBuffer sb = new StringBuffer(100);
			while((temp = br.readLine()) != null){
				sb.append(temp);
			}
			return sb.toString().trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;		
	}

	public static String postData(String url, Map<String,Object> map, boolean handlerRedirect) {
		String[] hosts = parseHostAndPort(url);
		if(hosts == null || hosts.length < 2){
			throw new EisException("无法解析URL[" + url + "]的主机和端口");
		}
		HttpClient hc = getHttpClient(url);

		Protocol myhttps = new Protocol("https", new TrustAnyHostSSLProtocolSocketFactory(), 443);   
		Protocol.registerProtocol("https", myhttps);  
		//logger.info("注册信任所有https证书");

		hc.getHostConfiguration().setHost(hosts[0], Integer.parseInt(hosts[1]));
		hc.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT);
		hc.getParams().setSoTimeout(SO_TIMEOUT);
		PostMethod method = new PostMethod(url);
		NameValuePair[] requestData = new NameValuePair[map.size()];
		int i = 0;
		for(Entry<String,Object> entry : map.entrySet()) {
			requestData[i] = new NameValuePair(entry.getKey(), entry.getValue().toString());
			i++;
		}
		method.addParameters(requestData);
		setHeader(method);
		try{

			int status = hc.executeMethod(method);
			if(!handlerRedirect && (status == HttpStatus.SC_MOVED_TEMPORARILY || status == HttpStatus.SC_MOVED_PERMANENTLY)){
				//302或301
				String location  = method.getResponseHeader("Location").getValue();
				return location;
			}
			String responseCharset = method.getResponseCharSet();
			if(StringUtils.isBlank(responseCharset)){
				responseCharset = "UTF-8";
			} else {
				//logger.debug("对方的编码是:" + responseCharset);
			}
			BufferedReader br = new   BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(),responseCharset));
			String temp = "";
			StringBuffer sb = new StringBuffer(100);
			while((temp = br.readLine()) != null){
				sb.append(temp + "\n");
			}
			//System.out.println(sb.toString());
			return sb.toString().trim();


		}catch(Exception e){
			e.printStackTrace();
		}
		return null;		
	}



	public static String parseDomain(String url){
		if(StringUtils.isBlank(url)){
			return null;
		}
		String data = url.replaceFirst("^http://|^https://", "").replaceAll(":\\d+", "").replaceAll("\\/.+$", "");
		String[] level2DomainName = new String[]{".com.cn",".net.cn",".gov.cn",".org.cn"};
		String[] level1DomainName = new String[]{".com",".co",".net",".org",".cn",".me",".biz",".name",".info",".so",".tel",".mobi",".asia",".cc",".tv"};
		String realDomain = null;
		String domainSuffix = null;
		for(String domain : level2DomainName){
			if(data.endsWith(domain)){
				realDomain = data.replaceAll(domain + "$", "");
				String[] domainPart = realDomain.split("\\.");
				realDomain = domainPart[domainPart.length-1];
				domainSuffix = domain;
				realDomain += domainSuffix;
				return realDomain;
			}
		}
		if(realDomain == null){
			for(String domain : level1DomainName){
				if(data.endsWith(domain)){
					realDomain = data.replaceAll(domain + "$", "");
					String[] domainPart = realDomain.split("\\.");
					domainSuffix = domain;
					realDomain = domainPart[domainPart.length-1];
					realDomain += domainSuffix;
					return realDomain;
				}
			}
		}
		return null;
	}

	public static Map<String,String> getRequestDataMap(HttpServletRequest request){
		Map<String,String> map = new HashMap<String,String>();
		for(Object key : request.getParameterMap().keySet()){
			Object o = request.getParameterMap().get(key.toString());
			String value = null;
			if(o.getClass().isArray()) {
				String[] valueArray = (String[])o;
				if(valueArray != null){
					if( valueArray.length > 1){
						for(String v : valueArray){				
							value+=v + ",";
						}
						value = value.substring(0, value.length() - 1);
					} else {
						value = valueArray[0];
					}
					map.put(key.toString(), value.toString());
				}
			} else {
				map.put(key.toString(), o.toString());
			}

		}
		Enumeration<String> e  = request.getHeaderNames();
		while(e.hasMoreElements()){
			String headerName = e.nextElement();
			map.put(headerName, request.getHeader(headerName));
		}
		if(map.get("clientIp") == null) {
			map.put("clientIp",IpUtils.getClientIp(request));
		}
		return map;
	}


	public static Map<String,Object> getRequestDataMap(HttpServletRequest request, boolean addCommonHeader){
		Map<String,Object> map = new HashMap<String,Object>();
		for(Object key : request.getParameterMap().keySet()){
			Object o = request.getParameterMap().get(key.toString());
			String value = null;
			if(o.getClass().isArray()) {
				String[] valueArray = (String[])o;
				if(valueArray != null){
					if( valueArray.length > 1){
						for(String v : valueArray){				
							value+=v + ",";
						}
						value = value.substring(0, value.length() - 1);
					} else {
						value = valueArray[0];
					}
					map.put(key.toString(), value.toString());
				}
			} else {
				map.put(key.toString(), o);
			}

		}
		if(addCommonHeader) {
			Enumeration<String> e  = request.getHeaderNames();
			while(e.hasMoreElements()){
				String headerName = e.nextElement();
				map.put(headerName, request.getHeader(headerName));
			}
			if(map.get("clientIp") == null) {
				map.put("clientIp",IpUtils.getClientIp(request));
			}
		}
		return map;
	}


	public static Map<String,String> getRequestDataMap(String requestString){
		HashMap<String,String> dataMap = new HashMap<String,String>();

		if(StringUtils.isBlank(requestString)){
			logger.warn("尝试处理的字符串为空");
			return dataMap;
		}
		String[] data = null;
		if(requestString.indexOf("?") > 0){
			data = requestString.split("\\?");
		} else {
			data = new String[]{requestString};
		}
		String[] tempData = data[0].split("&");

		if(tempData.length < 0){
			logger.warn("尝试处理的字符串无法用&分割:" + data[0]);	
			return dataMap;
		}
		for(String d : tempData){
			//logger.debug("XXXXXXX 处理请求字符串:" + d);
			int offset = d.indexOf("=");
			if(offset < 0){
				logger.error("请求字符串错误:{}", d);
				continue;
			}
			String key = d.substring(0,offset);
			String value = d.substring(offset+1);

			//logger.debug("放入参数:{}=>{}", key, value);
			dataMap.put(key,value);

		}
		return dataMap;

	}

	/**
	 * 根据request生成请求主机，即/前面的部分
	 */
	public static String generateUrlPrefix(HttpServletRequest request){
		String protocol = request.getScheme();//request.getProtocol().toLowerCase().startsWith("https") ? "https" : "http";
		int port = request.getServerPort();
		if(port == 443) {
			protocol = "https";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(protocol);
		sb.append("://");
		sb.append(request.getServerName());
		if(protocol.equals("https") && port == 443){
			return sb.toString();
		}
		if(protocol.equals("http") && port == 80){
			return sb.toString();
		}
		sb.append(':');
		sb.append(port);
		return sb.toString();
	}

	/**
	 * 根据一个URL去掉所有的请求串，得到其前面部分，即http://aaa.com
	 * @param url
	 * @return
	 */
	public static String generateUrlPrefix(String url){
		url = url.toLowerCase();
		if(!url.startsWith("http")) {
			logger.error("非法的URL:" + url);
			return null;
		}
		String[] data = url.split("://");
		String schema = data[0];
		int offset = data[1].indexOf("/");
		String data2 = data[1].substring(0,offset);

		return schema + "://" + data2;
	}


	public static String generateRequestString(Map<String, String> dataMap) {
		StringBuffer sb = new StringBuffer();
		if(dataMap == null || dataMap.size() < 1){
			return null;
		}
		List<String> keys = new ArrayList<String>(dataMap.keySet());
		Collections.sort(keys);
		for(String key : keys){
			sb.append(key);
			sb.append("=");
			sb.append(dataMap.get(key));
			sb.append("&");
		}
		return sb.toString().replaceAll("&$", "");
	}
	public static String generateRequestString(HttpServletRequest request) {

		StringBuffer sb = new StringBuffer();

		for(Object key : request.getParameterMap().keySet()){
			sb.append(key);
			sb.append("=");
			sb.append(request.getParameter(key.toString()));
			sb.append("&");
		}
		return sb.toString().replaceAll("&$", "");
	}

	public static String filterUnSafeHtml(String src){
		String safe = src.replaceAll("eval\\((.*)\\)", "");
		safe = safe.replaceAll("[\\\"\\\']*javascript:(.*)[\\\"\\\']", "\"\"");
		safe = safe.replaceAll("script", "");  
		//Whitelist whiteList = Whitelist.none();
		//whiteList.addTags("div");
		//safe = Jsoup.clean(src, whiteList);
		if(logger.isDebugEnabled()){
			logger.debug("对参数[" + src + "]值进行安全过滤，过滤后的数据：" + safe);
		}
		return safe;
	}

	public static String getStringValueFromRequestMap(Map<String,String> requestMap, String dataCode){
		if(requestMap == null || requestMap.size() < 1 || dataCode == null){
			return null;
		}
		if(requestMap.containsKey(dataCode)){
			return requestMap.get(dataCode);
		}
		return null;
	}

	public static boolean getBooleanValueFromRequestMap(Map<String,String> requestMap, String dataCode){
		if(requestMap == null || requestMap.size() < 1 || dataCode == null){
			return false;
		}
		if(requestMap.get(dataCode) != null && requestMap.get(dataCode).equalsIgnoreCase("true")){
			return true;
		}
		return false;
	}


	public static int getIntValueFromRequestMap(Map<String,String> requestDataMap, String dataCode, int defaultValue){
		if(requestDataMap == null || requestDataMap.size() < 1 || dataCode == null){
			return defaultValue;
		}
		if(NumericUtils.isIntNumber(requestDataMap.get(dataCode))){
			return Integer.parseInt(requestDataMap.get(dataCode));
		}
		return defaultValue;
	}

	public static long getLongValueFromRequestMap(Map<String,String> requestDataMap, String dataCode, int defaultValue){
		if(requestDataMap == null || requestDataMap.size() < 1 || dataCode == null){
			return defaultValue;
		}
		if(NumericUtils.isIntNumber(requestDataMap.get(dataCode))){
			return Long.parseLong(requestDataMap.get(dataCode));
		}
		return defaultValue;
	}

	public static float getFloatValueFromRequestMap(Map<String,String> requestMap, String dataCode, float defaultValue){
		if(requestMap == null || requestMap.size() < 1 || dataCode == null){
			return defaultValue;
		}
		if(NumericUtils.isFloatNumber(requestMap.get(dataCode))){
			return Float.parseFloat(requestMap.get(dataCode));
		}
		return defaultValue;
	}
	public static String getNameValuePairDesc(NameValuePair[] data) {
		StringBuffer sb = new StringBuffer();
		if(data == null || data.length < 1){
			return null;
		}
		for(NameValuePair nameValuePair : data){
			sb.append(nameValuePair.getName()).append("=>").append(nameValuePair.getValue()).append(',');
		}
		return sb.toString().replaceAll(",$", "");
		// TODO Auto-generated method stub

	}
	public static String readPostData(HttpServletRequest request) {
		BufferedReader input = null;
		try {
			input = request.getReader();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(input == null){
			logger.error("请求中不包含任何请求数据");
			return null;
		}
		StringBuilder sb = new StringBuilder(); 

		String message = null;
		String line = null;
		try {
			while( (line = input.readLine()) != null){
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		message = sb.toString();
		return message;
	}

	public static boolean isJsonAccess(HttpServletRequest request) {
		logger.debug("判断请求:contentType={},requestURI={}是否json", request.getContentType(), request.getRequestURI());

		if(request.getRequestURI().endsWith(".json") || isAjax(request)) {
			return true;
		}
		if(request.getContentType() != null && request.getContentType().trim().endsWith("json")) {
			return true;
		}
		return false;
	}
	public static NameValuePair[] generateNameValuePair(String resultString) {
		Map<String,String> map = getRequestDataMap(resultString);
		NameValuePair[] data = new NameValuePair[map.size()];
		int i = 0;
		for(Entry<String,String> entry : map.entrySet()) {
			data[i] = new NameValuePair(entry.getKey(), entry.getValue());
			i++;
		}
		return data;
	}
	public static boolean isAjax(HttpServletRequest request) {
		String requestType = request.getHeader("X-Requested-With");
		if(requestType != null && requestType.equalsIgnoreCase("XmlHttpRequest")) {
			return true;
		}
		return false;
	}
}
