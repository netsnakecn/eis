package com.maicard.th;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

/**
 * 数据库中存储逗号分割的字符串
 * 返回为String[]
 *
 *
 * @author NetSnake
 * @date 2017年3月13日
 *
 */

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(String[].class)
public class StringArrayTypeHandler implements TypeHandler<String[]>{

	@Override
	public String[] getResult(ResultSet arg0, String arg1) throws SQLException {
		try{
			return arg0.getString(arg1).split(",");
		}catch(Exception e){
			//e.printStackTrace();
		}		
		return null;
	}

	@Override
	public  String[]  getResult(ResultSet arg0, int arg1) throws SQLException {
		try{
			return arg0.getString(arg1).split(",");
		}catch(Exception e){
		//	e.printStackTrace();
		}

		return null;
	}

	@Override
	public  String[]  getResult(CallableStatement arg0, int arg1)
			throws SQLException {
		try{
			return arg0.getString(arg1).split(",");
		}catch(Exception e){
			//e.printStackTrace();
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
			String data = "";
			for(String key : value){
				data += key;
				data += ",";
			}
			data = data.replaceAll(",$", "");
			arg0.setString(arg1, data);
			return;
		}catch(Exception e){
			//e.printStackTrace();
		}
		arg0.setString(arg1, null);


	}

}
