package com.maicard.money.th;


import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.base.BaseService;
import com.maicard.money.entity.BankAccount;
import com.maicard.utils.JsonUtils; 

/**
 * 在BankAccount对象和JSON数据之间进行转换
 * 
 *
 * @author NetSnake
 * @date 2016-11-25
 *
 */
public class BankAccountJsonHandler extends BaseService implements TypeHandler<BankAccount>{
	
	static ObjectMapper om = JsonUtils.getNoDefaultValueInstance();


	@Override
	public BankAccount getResult(ResultSet arg0, String arg1) throws SQLException {
		return convert(arg0.getString(arg1));
	}

	@Override
	public BankAccount getResult(ResultSet arg0, int arg1) throws SQLException {
		return convert(arg0.getString(arg1));
	}

	@Override
	public BankAccount getResult(CallableStatement arg0, int arg1)
			throws SQLException {
		return convert(arg0.getString(arg1));

	}

	@Override
	public void setParameter(PreparedStatement arg0, int arg1, BankAccount price,
			JdbcType arg3) throws SQLException {
		if(price == null){
			arg0.setString(arg1, null);
			return;
		}
		om.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
		try {
			arg0.setString(arg1, om.writeValueAsString(price));
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}		

		arg0.setString(arg1, null);


	}
	
	private BankAccount convert(String text) {
		if(text == null || text.trim().equals("")){
			return null;
		}
		try{
			return om.readValue(text, BankAccount.class);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("无法转换数据库内容到对象:" + e.getMessage());
		}		
		return null;	
		
	}
	
	/*public static void main(String[] argv){
		BankAccount p = new BankAccount();
		p.setMoney(1000);
		try {
			System.out.println(om.writeValueAsString(p));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

}
