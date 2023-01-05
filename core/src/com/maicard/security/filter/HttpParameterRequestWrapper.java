package com.maicard.security.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import com.maicard.core.constants.Constants;
import com.maicard.utils.HttpUtils;

/**
 * 对用户输入数据进行安全替换和编码后再传递到程序
 * 
 *
 * @author NetSnake
 * @date 2015年12月29日
 *
 */
public class HttpParameterRequestWrapper extends HttpServletRequestWrapper  {

	protected static final Logger logger = LoggerFactory.getLogger(HttpParameterRequestWrapper.class);

	private Map<String, String[]> parameterMap;  
	private byte[] rawSubmitData;

	public HttpParameterRequestWrapper(HttpServletRequest request) throws IOException {  
		super(request);  
		if(request.getCharacterEncoding() == null){
			request.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		}
		this.parameterMap = new HashMap<String,String[]>(request.getParameterMap());  

		//XXX 有可能出现既有body请求，也有查询字符串的情况，因此不能这么判断
		//if(this.parameterMap == null || this.parameterMap.size() < 1){
		rawSubmitData = StreamUtils.copyToByteArray(request.getInputStream());
		if(rawSubmitData == null || rawSubmitData.length < 1){
			logger.debug("当前请求没有提交任何body数据");
		} else {
			StringBuffer sb = new StringBuffer() ;
			InputStream is = new ByteArrayInputStream(rawSubmitData);
			InputStreamReader isr = new InputStreamReader(is,request.getCharacterEncoding());  
			BufferedReader br = new BufferedReader(isr);
			String s = "" ;
			while((s=br.readLine())!=null){
				sb.append(s) ;
			}
			String sValue = HttpUtils.filterUnSafeHtml(sb.toString());
			rawSubmitData = sValue.getBytes(request.getCharacterEncoding());
			logger.debug("处理一个body数据请求，请求的数据是:" + sb.toString() + ",安全过滤后:" + new String(rawSubmitData, request.getCharacterEncoding()));
		}
		//return;
		//}
		Map<String,String[]> parameterMap = new HashMap<String,String[]>(request.getParameterMap());   

		for(String key : parameterMap.keySet()){			
			String[] src = request.getParameterValues(key);
			if(src == null || src.length < 1){
				if(logger.isDebugEnabled())logger.debug("忽略空参数[" + key + "]");
				continue;
			}
			//logger.debug("参数[" + key + "]的输入数据有:" + src.length + "个:" + src);
			String[] filterd = new String[src.length];
			for(int i = 0; i < src.length; i++){
				//String sValue = HttpUtils.filterUnSafeHtml(src[i]);
				//filterd[i] = sValue;
				filterd[i] = src[i];
			}
			this.parameterMap.put(key, filterd);
		}
	}  

	@Override  
	public String getParameter(String name) {  
		String result = "";  

		Object v = parameterMap.get(name);  
		if (v == null) {  
			result = null;  
		} else if (v instanceof String[]) {  
			String[] strArr = (String[]) v;  
			if(strArr == null || strArr.length < 1){
				result = null;
			} else {
				StringBuffer sb = new StringBuffer();
				for(String s : strArr){
					sb.append(s).append(',');
				}
				result = sb.toString().replaceAll(",$", "");
			}
		} else if (v instanceof String) {  
			result = (String) v;  
		} else {  
			result =  v.toString();  
		}  

		return result;  
	}  

	@Override  
	public Map<String, String[]> getParameterMap() {  
		return parameterMap;  
	}  

	@Override  
	public Enumeration<String> getParameterNames() {  
		return new Vector<String>(parameterMap.keySet()).elements();  
	}  

	@Override  
	public String[] getParameterValues(String name) {  
		String[] result = null;  

		Object v = parameterMap.get(name);  
		if (v == null) {  
			result =  null;  
		} else if (v instanceof String[]) {  
			result =  (String[]) v;  
		} else if (v instanceof String) {  
			result =  new String[] { (String) v };  
		} else {  
			result =  new String[] { v.toString() };  
		}  
		//	logger.debug("返回参数[" + name + "]的值，数量:" + (result == null ? "空" :  result.length));
		return result;  
	}  
	@Override  
	public BufferedReader getReader() throws IOException {  
		return new BufferedReader(new InputStreamReader(getInputStream()));  
	}  

	@Override  
	public ServletInputStream getInputStream() throws IOException {  
		if(rawSubmitData == null){
			logger.warn("过滤器中的RAW数据是空");
			return null;
		}
		final ByteArrayInputStream bais = new ByteArrayInputStream(rawSubmitData);  
		return new ServletInputStream() {  

			@Override  
			public int read() throws IOException {  
				return bais.read();  
			}

			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {

			}  
		};  
	}  

}
