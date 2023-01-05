package com.maicard.core.ds;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.maicard.core.annotation.IgnoreDs;

/**
 * Created with IntelliJ IDEA.
 * @author: iron
 * Date: 16-12-6
 * Time: 上午9:54
 * AOP根据类的注解设置数据源key
 */
public class DataSourceSwitcherAdapter extends MethodHandlerAspectInterceptorAdapter {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	static final String defaultSource = "default";

	@Override
	public Object doAround(ProceedingJoinPoint jp) throws Throwable {
		Signature  signature = jp.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;  
		Method method = methodSignature.getMethod();
		
		String className = jp.getTarget().getClass().getSimpleName();
		
		boolean isTs = method.isAnnotationPresent(Transactional.class);
		logger.debug("检查方法{}=>{}的数据源切换，是否为事务:{}", className, method.getName(), isTs);
		
		
		
		//FIXME 最好是在配置切面时指定忽略该注解
		if(method.isAnnotationPresent(IgnoreDs.class)){
					logger.debug("忽略指定了IgnoreDs的方法:{} => {}，不进行数据源切换，当前数据源:{}", className, method.getName(), DataSourceSwitcher.getDataSourceType());
			return jp.proceed();
		}
		if(jp.getTarget().getClass().isAnnotationPresent(IgnoreDs.class)){
			logger.debug("忽略指定了IgnoreDs的类:{}，不进行数据源切换,当前数据源:{}", className, DataSourceSwitcher.getDataSourceType());
			return jp.proceed();
		}
		
		
		
		Class<?> target=jp.getTarget().getClass();
		try {
			DataSourceMapper dataSourceMapper=target.getAnnotation(DataSourceMapper.class);
			if(null!=dataSourceMapper && !StringUtils.isEmpty(dataSourceMapper.value())){
				DataSourceSwitcher.setDataSourceType(dataSourceMapper.value());
				logger.debug("Switching DataSource as annotation["+target.getName()+", type=" +(StringUtils.isEmpty(DataSourceSwitcher.getDataSourceType())? defaultSource :dataSourceMapper.value())+"]");
			} else {
				//否则应当执行到default
				logger.debug("Switching DataSource to default without annotation["+target.getName()+", type=" + defaultSource);
				DataSourceSwitcher.setDataSourceType(defaultSource);


			}
			return jp.proceed();
		} finally{//执行完成后将数据源标示清楚回复默认数据源

			DataSourceSwitcher.clear();
			logger.debug("Cleaned DataSource ["+target.getName()+", type=" +(StringUtils.isEmpty(DataSourceSwitcher.getDataSourceType())?"default":DataSourceSwitcher.getDataSourceType())+"]");
		}
	}
}
