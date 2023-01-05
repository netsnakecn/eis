package com.maicard.th;


import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.maicard.base.BaseService;
import com.maicard.utils.JsonUtils;

/**
 * 使用jackson把<String,String>类型的Hash表以文本形式存放在数据库中
 * 读出时，把JSON文本转换为<String,String>类型的Hash表
 * 
 * 用于一些简单的基本配置键值对存放，并能在数据库直接进行修改
 *
 *
 * @author NetSnake
 * @date 2015年11月29日
 *
 */

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(HashMap.class)
public class TextHashMapTypeHandler extends BaseService implements TypeHandler<Map<String,String>>{

	@Override
	public Map<String,String> getResult(ResultSet arg0, String arg1) throws SQLException {
		String text = arg0.getString(arg1);
		return text2Map(text);
	}

	

	@Override
	public Map<String,String> getResult(ResultSet arg0, int arg1) throws SQLException {
		String text = arg0.getString(arg1);
		return text2Map(text);
	}

	@Override
	public Map<String,String> getResult(CallableStatement arg0, int arg1)
			throws SQLException {
		String text = arg0.getString(arg1);
		return text2Map(text);
		
	}

	@Override
	public void setParameter(PreparedStatement arg0, int arg1, Map<String,String> arg2,
			JdbcType arg3) throws SQLException {
		if(arg2 == null){
			arg0.setString(arg1, null);
			return;
		}
		try {
			arg0.setString(arg1, JsonUtils.getInstance().writeValueAsString(arg2));
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}		

		arg0.setString(arg1, null);


	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> text2Map(String text) {
		if(text == null || text.trim().equals("")){
			return null;
		}
		try{
			Object object = JsonUtils.getInstance().readValue(text, new TypeReference<Map<String,String>>(){});
			return (Map<String,String>)object;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("无法转换数据库内容到Map<String,String>:" + e.getMessage());
		}		
		return null;	
	}

}
