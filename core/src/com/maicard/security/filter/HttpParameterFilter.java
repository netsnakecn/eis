package com.maicard.security.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.core.constants.Constants;



/**
 * 2016.2.10<br/>
 * 增加了对纯HTTP POST数据的支持<br/>
 * 对这类没有参数的POST提交，也进行过滤处理<br/>
 * 
 * 
 * 对用户输入的数据进行过滤，需要在web.xml配置本过滤器<br/>
 * 本过滤器调用一个HttpServletRequestWrapper扩展<br/>
 * 把所有输入数据进行一个安全替换<br/>
 * 并进行URL编码后再传递给程序<br/>
 * mulitpart形式的表单由FilteredCommonsMultipartResolver进行处理
 *
 * @see HttpParameterRequestWrapper
 * @see FilteredCommonsMultipartResolver
 *
 * @author NetSnake
 * @date 2015年12月29日
 *
 */
public class HttpParameterFilter  implements Filter{  

	protected static final Logger logger = LoggerFactory.getLogger(HttpParameterFilter.class);

	@Override  
	public void init(FilterConfig filterConfig) throws ServletException {  
		logger.debug("初始化HTTP参数安全过滤器");

	}  

	@Override  
	public void doFilter(ServletRequest request, ServletResponse response,  
			FilterChain chain) throws IOException, ServletException {  
		HttpServletRequest request2 = (HttpServletRequest)request;
		boolean isMultiPart = ServletFileUpload.isMultipartContent(request2);
		logger.debug("尝试过滤请求[" + request2.getMethod() + "=>" + request2.getRequestURI() + "]，是否multipart:" + isMultiPart + "，字符集:" + request.getCharacterEncoding());
		if(request.getCharacterEncoding() == null){
			request.setCharacterEncoding(Constants.DEFAULT_CHARSET);
			request2.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		}
		if(isMultiPart){
			logger.debug("不过滤multipart请求");
			chain.doFilter(request, response);
			return;
		}
		Map<String,String[]> parameterMap = new HashMap<String,String[]>(request.getParameterMap());   
		if(parameterMap == null || parameterMap.size() < 1){
			if(request2.getMethod().equalsIgnoreCase("POST") && !isMultiPart){
				request = new HttpParameterRequestWrapper((HttpServletRequest)request); 
			} else {
				//没有参数也可能是multipart请求，这种将由FilteredCommonsMultipartResolver进行过滤
				if(logger.isDebugEnabled()){
					logger.debug("当前请求[" + request2.getMethod() + "=>" + request2.getRequestURI() + "]中没有任何参数，或者是multipart请求，停止过滤");
				}
			}
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("当前请求参数数量是:" + parameterMap.size() + "，进行过滤");
			}
			request = new HttpParameterRequestWrapper((HttpServletRequest)request);   
		}
		if(chain == null){
			logger.warn("传入的FilterChain是空");
		} else {
			try{
				chain.doFilter(request, response); 
			}catch(Exception e){
				logger.error("无法进行Filter");
				e.printStackTrace();
			}
		}
		return;
	}
	//}  

	@Override  
	public void destroy() {  
	}  

}  


