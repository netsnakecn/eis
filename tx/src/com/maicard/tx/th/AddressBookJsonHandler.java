package com.maicard.tx.th;


import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.base.BaseService;
import com.maicard.tx.entity.AddressBook;
import com.maicard.utils.JsonUtils; 

/**
 * 在AddressBook对象和JSON数据之间进行转换
 * 
 *
 * @author NetSnake
 * @date 2016-02-27
 *
 */
public class AddressBookJsonHandler extends BaseService implements TypeHandler<AddressBook>{

	static ObjectMapper om = JsonUtils.getNoDefaultValueInstance();


	@Override
	public AddressBook getResult(ResultSet arg0, String arg1) throws SQLException {
		return convert(arg0.getString(arg1));
	}

	@Override
	public AddressBook getResult(ResultSet arg0, int arg1) throws SQLException {
		return convert(arg0.getString(arg1));
	}

	@Override
	public AddressBook getResult(CallableStatement arg0, int arg1)
			throws SQLException {
		return convert(arg0.getString(arg1));

	}

	@Override
	public void setParameter(PreparedStatement arg0, int arg1, AddressBook price,
			JdbcType arg3) throws SQLException {
		if(price == null){
			arg0.setString(arg1, null);
			return;
		}
		arg0.setString(arg1, JsonUtils.toStringFull(price));
		return;




	}

	private AddressBook convert(String text) {
		if(text == null || text.trim().equals("")){
			return null;
		}
		try{
			return om.readValue(text, AddressBook.class);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("无法转换数据库内容为对象:" + e.getMessage());
		}		
		return null;	

	}

	/*public static void main(String[] argv){
		AddressBook p = new AddressBook();
		p.setAddressBook(1000);
		try {
			System.out.println(om.writeValueAsString(p));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

}
