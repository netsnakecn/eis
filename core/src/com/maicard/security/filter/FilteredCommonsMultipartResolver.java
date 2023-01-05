package com.maicard.security.filter;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import com.maicard.core.constants.Constants;
import com.maicard.utils.HttpUtils;


/**
 * 对使用multipart方式上传的数据进行安全过滤<br/>
 * 需要在Spring配置文件中配置对应的multipartResolver bean<br/>
 * 普通数据由过滤器执行<br/>
 * 
 * @see HttpParameterFilter
 *
 *
 * @author NetSnake
 * @date 2016年1月20日
 *
 */
public class FilteredCommonsMultipartResolver extends CommonsMultipartResolver {

	protected static final Logger logger = LoggerFactory.getLogger(FilteredCommonsMultipartResolver.class);



	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		String encoding = request.getCharacterEncoding();
		logger.debug("进行mulitpart转换,编码:" + encoding);
		try {
			request.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		MultipartHttpServletRequest multipartRequest = null;

		Assert.notNull(request, "Request must not be null");

		MultipartParsingResult parsingResult = super.parseRequest(request);
		Map<String,String[]> parameterMap = parsingResult.getMultipartParameters();
		for(String key :parameterMap.keySet()){

			String[] valueArray = parameterMap.get(key);
			if(valueArray == null || valueArray.length  < 1){
				if(logger.isDebugEnabled())logger.debug("忽略空参数[" + key + "]");
				continue;
			}
			for(int i = 0; i < valueArray.length; i++){
				valueArray[i] = HttpUtils.filterUnSafeHtml(valueArray[i]);
				/*if(key.equals("title")){
					logger.debug("篡改数据");
					valueArray[i] = "被篡改了哈";
				}*/
			}
			
		}
		multipartRequest = new DefaultMultipartHttpServletRequest(request, parsingResult.getMultipartFiles(), parameterMap, parsingResult.getMultipartParameterContentTypes());
		return multipartRequest;
	}




}
