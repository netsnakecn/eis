package com.maicard.pe;

import java.beans.PropertyEditorSupport;


/**
 * 辅助Spring进行绑定时，处理页面传来的空白值绑定到int
 *
 *
 * @author NetSnake
 * @date 2015年11月12日
 *
 */
public class IntValuePropertyEditor extends PropertyEditorSupport  {
	private int value;


	@Override
	public void setAsText(String str){
		if(str == null || str.trim().equals("")){
			return;
		}
		str = str.trim();
		if(isInt(str)){
			value = Integer.parseInt(str);
		}
	

	}

	@Override
	public Integer getValue(){
		return value;
	}

	
	private boolean isInt(String str){
		str = str.trim();
		for (int i = str.length();--i>=0;){   
			if (!Character.isDigit(str.charAt(i))){
				return false;
			}
		}
		return true;
	}

}
