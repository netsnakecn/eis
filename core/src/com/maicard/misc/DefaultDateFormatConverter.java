package com.maicard.misc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.maicard.core.constants.Constants;

/**
 * 全局为Spring日期格式做转换
 * Controller中不再需要用initBinder进行配置
 * 需要在Spring配置文件中设置convertService
 *
 *
 * @author NetSnake
 * @date 2016年1月29日
 *
 */
public class DefaultDateFormatConverter  implements Converter<String, Date> {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());


	private final String dateOnlyFormat = "yyyy-MM-dd";
	public Date convert(String source) {    
		if(StringUtils.isBlank(source)){
			return null;
		}
		source = source.trim();
		SimpleDateFormat dateFormat = null;

		if(source.length() != Constants.DEFAULT_TIME_FORMAT.length()){
			source = source.substring(0,dateOnlyFormat.length());
			dateFormat = new SimpleDateFormat(dateOnlyFormat);			
		} else {
			dateFormat = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT);
		}
		
		dateFormat.setLenient(false); 
		try {
			return dateFormat.parse(source);
		} catch (ParseException e) {
			logger.warn("无法解析的日期:{}", source);
		}
		return null;
	}   
}
