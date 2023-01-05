package com.maicard.th;

import com.maicard.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库中存储为JSON格式化字符串
 * 返回为String[]
 *
 *
 * @author NetSnake
 * @date 2017年3月13日
 *
 */

@Slf4j
public class JsonArrayTypeHandler implements TypeHandler<String[]>{

	@Override
	public String[] getResult(ResultSet arg0, String arg1) throws SQLException {
		try{
			String v = arg0.getString(arg1);
			if(v == null){
				return null;
			}
			return JsonUtils.getInstance().readValue(v,String[].class);
		}catch(Exception e){
			 e.printStackTrace();
		}		
		return null;
	}

	@Override
	public  String[]  getResult(ResultSet arg0, int arg1) throws SQLException {
		try{
			String v = arg0.getString(arg1);
			if(v == null){
				return null;
			}
			return JsonUtils.getInstance().readValue(v,String[].class);
		}catch(Exception e){
		 e.printStackTrace();
		}

		return null;
	}

	@Override
	public  String[]  getResult(CallableStatement arg0, int arg1)
			throws SQLException {
		try{
			String v = arg0.getString(arg1);
			if(v == null){
				return null;
			}
			return JsonUtils.getInstance().readValue(v,String[].class);
		}catch(Exception e){
			 e.printStackTrace();
		}

		return null;
	}

	@Override
	public void setParameter(PreparedStatement arg0, int arg1, String[] arg2,
			JdbcType arg3) throws SQLException {
		if(arg2 == null){
			arg0.setString(arg1, null);
			return;
		}
		try{
			String[] value = (String[])arg2;
			arg0.setString(arg1, JsonUtils.toStringFull(value));
			return;
		}catch(Exception e){
			 e.printStackTrace();
		}
		arg0.setString(arg1, null);


	}

}
