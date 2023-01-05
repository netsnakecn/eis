package com.maicard.core.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import com.alibaba.fastjson.JSON;
import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.utils.PagingUtils;

import javax.annotation.PostConstruct;

/**
 * 检查查询条件，设置其中的限制行数，和部分应当强制为数组的查询参数
 * @author NetSnake
 * @date 2019-08-13

 *
 */

@Aspect
public class QueryParamsCheckAspect extends BaseService{

	public static final String[] forceArrayParams = new String[] {"currentStatus"};
	private final String express = "execution (* com..*.service..*.list*(com.maicard.base.CriteriaMap)) || execution(* com..*.service..*.count(com.maicard.base.CriteriaMap))";//"execution(* com..*.service..*.list*(..)) || execution(* com..*.service..*.count(..))";
	//private final String express = "args(com.maicard.base.CriteriaMap)";

	public static int ROW_PER_PAGE = 20;

	@PostConstruct
	public void init(){
		logger.info("初始化查询参数切面,express:" + express);
	}

	//@Before("execution(* com.maicard..*.dao.mybatis.*.*(..)) || execution(* com.maicard..*.dao.mapper..*.*(..)) ")
	@Before(express)
	//@Before("execution(* com..*.service..*.list*(..))")
	public void checkParams(JoinPoint joinPoint) throws Throwable{

		String beanName = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();


		logger.debug("切入数据参数检查, bean：{}, method:{},参数个数:{}",beanName, methodName,  joinPoint.getArgs().length);

		//String beanName = ImplNameTranslate.translate(joinPoint.getTarget().getClass().getSimpleName());// StringUtils.uncapitalize(joinPoint.getTarget().getClass().getSimpleName()).replace(CommonStandard.implBeanNameSuffix, "");
		//String methodName = joinPoint.getSignature().getName();
		//BaseEntity eisParameters = null;
		Object[] params = joinPoint.getArgs();
		if(params == null || params.length < 1) {
			logger.debug("忽略参数为空的方法1:{} {}", beanName, methodName);
			return;
		}
		Object param = params[0];
		if(param == null) {
			logger.debug("忽略参数为空的方法2:{} {}", beanName, methodName);
			return;
		}
		if(!(param instanceof CriteriaMap)) {
			logger.debug("忽略参数不是查询条件的方法:{} {},参数类型:{}", beanName, methodName, param.getClass().getName());
			return;
		}
		CriteriaMap criteria = (CriteriaMap)param;
		
		CriteriaMap oldCriteria = criteria.clone();
		
		for(String paramName : forceArrayParams) {
			criteria.fixToArray(paramName); 
		}
		if(methodName.equalsIgnoreCase("count") || methodName.contains("count")) {
			return;
		}
		
		boolean noPaging = criteria.getBooleanValue("noPaging");
		if(!methodName.contains("Page")) {
			noPaging = true;
		} 
		int limits = criteria.getIntValue("limits");
		int starts = criteria.getIntValue("starts");
		if(limits <= 0 || starts <= 0) {
			if(!noPaging) {
				int rows = criteria.getIntValue("rows");
				if(rows <= 0){
					rows = ROW_PER_PAGE;
				}
				int page = criteria.getIntValue("page");
				if(page <= 0){
					page = 1;
				}
				PagingUtils.paging(criteria, rows, page);
			} else {
				PagingUtils.paging(criteria, Constants.MAX_ROW_LIMIT);
			}
		}
		for(String paramName : forceArrayParams) {
			criteria.fixToArray(paramName);
			/*
			Object paramObj = criteria.get(paramName);
			if(paramObj != null) {
				if(paramObj.getClass().isArray()) {
					continue;
				} else {
					int paramValue = criteria.getIntValue(paramName);
					criteria.put(paramName, new int[] {paramValue});
				}
			}*/
		}
		logger.debug("查询是否不翻页:{},原有参数:{}经调整后:{}",  noPaging, JSON.toJSONString(oldCriteria), JSON.toJSONString(criteria));
	}
}
