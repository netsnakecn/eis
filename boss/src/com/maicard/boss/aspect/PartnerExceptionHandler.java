package com.maicard.boss.aspect;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.maicard.base.BaseService;
import com.maicard.core.constants.EisError;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.NumericUtils; 

/**
 * 统一管理管理后台抛出的异常
 * 对于抛出userNotFoundInSession代码的EisException，将判断是否为json请求，如果不是则自动重定向到/user/login.shtml
 * 
 * @date 2019-7-8
 *
 */
@ControllerAdvice
public class PartnerExceptionHandler extends BaseService{
	@ExceptionHandler({ Exception.class })
	public ResponseEntity<String>  handException(HttpServletRequest request, Exception e) throws Exception {
		e.printStackTrace();
		EisMessage msg = null;
		//boolean isAjax = HttpUtils.isAjax(request);
		if(e instanceof EisException ee) {
			msg = EisMessage.error(NumericUtils.parseInt(ee.getErrorCode()),ee.getMessage());
		} else {
			msg = EisMessage.error(EisError.systemException.id,e.getMessage());
		}
		//为了保证跟正常逻辑中的结构一致
		ModelMap map = new ModelMap();
		map.put("message",msg);
		String result = JsonUtils.toStringFull(map);
		
		HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/json;charset=UTF-8");
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
		/*
		ModelAndView mav = new ModelAndView(CommonStandard.partnerMessageView);
		mav.getModelMap().put("message", new EisMessage(EisError.systemException.id, e.getMessage()));
		return mav;*/
	}

	/*@ExceptionHandler({ EisException.class })
	public ModelAndView handFrontException(HttpServletRequest request, EisException e) throws Exception {
		if(logger.isDebugEnabled()) {
			//e.printStackTrace();
		} else {
			logger.error("发生异常:" + e.getMessage());
		}
		
		e.printStackTrace();

		ModelAndView mav = new ModelAndView(CommonStandard.frontMessageView);

		if(!HttpUtils.isJsonAccess(request)) {
			if(e.getErrorCode().equalsIgnoreCase(String.valueOf(EisError.userNotFoundInSession.id))){
				//不是json访问，重定向到登录界面
				mav.setViewName("redirect:/user/login.shtml");
				mav.setStatus(HttpStatus.TEMPORARY_REDIRECT);
				return mav;
			}
		}
		
		mav.getModelMap().put("message", new EisMessage(NumericUtils.parseInt(e.getErrorCode()), e.getMessage()));
		return mav;
	}*/
}
