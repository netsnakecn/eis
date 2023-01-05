package com.maicard.pe;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;

import com.maicard.utils.NumericUtils;



/**
 * 用于执行反射功能时<br/>
 * 对int[]型可变参数进行转换<br/>
 * 例如Criteria类中的currentStatus<br/>
 * 使用<br/>
 * PropertyEditorManager.registerEditor(int[].class, IntArrayEditor.class);
 *
 *
 * @author NetSnake
 * @date 2015年11月12日
 *
 */
public class IntArrayPropertyEditor extends PropertyEditorSupport  {
	private int[] array;


	@Override
	public void setAsText(String str){
		String[] data = str.split(",");
		List<Integer> numbers = new ArrayList<Integer>();
		if(data != null && data.length > 0){
			for(int i = 0; i < data.length; i++){
				if(isInt(data[i])){
					numbers.add(Integer.parseInt(data[i].trim()));
				}
			}
		}
		if(numbers.size() > 0){
			array = NumericUtils.list2Array(numbers);
		}


	}

	@Override
	public int[] getValue(){
		return array;
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
