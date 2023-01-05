package com.maicard.pe;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.util.Date;

import com.maicard.core.constants.EisError;
import com.maicard.core.exception.EisException;
import com.maicard.misc.ThreadHolder;



/**
 * 用于执行反射功能时<br/>
 * 把String类型转换为Date类型<br/>
 * 使用<br/>
 * PropertyEditorManager.registerEditor(Date.class, StringDatePropertyEditor.class);
 *
 *
 * @author NetSnake
 * @date 2016-07-07
 *
 */
public class StringDatePropertyEditor extends PropertyEditorSupport  {
	private  Date date;
	


	@Override
	public void setAsText(String str){
		if(str.length() < 10) {
			throw new EisException(EisError.PARAMETER_ERROR.id,"错误的日期格式:" + str);
		}
		if(str.length() == 10) {
			str += " 00:00:00";
		}
 		try {
			date = ThreadHolder.defaultTimeFormatterHolder.get().parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Date getValue(){
		return date;
	}


}
