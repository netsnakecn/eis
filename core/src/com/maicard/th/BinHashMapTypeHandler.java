package com.maicard.th;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.maicard.base.BaseService;


/**
 * 使用Java自身的序列化功能把<String,Object>类型的Hash表以Blob形式存放在数据库中
 * 
 *
 * @author NetSnake
 * @date 2015年11月29日
 *
 */
public class BinHashMapTypeHandler extends BaseService implements TypeHandler<HashMap<String,Object>>{

	@Override
	public HashMap<String,Object> getResult(ResultSet arg0, String arg1) throws SQLException {
		Blob blob = arg0.getBlob(arg1);
		return bin2Map(blob);
	}

	@Override
	public HashMap<String,Object> getResult(ResultSet arg0, int arg1) throws SQLException {
		Blob blob = arg0.getBlob(arg1);
		return bin2Map(blob);
	}

	@Override
	public HashMap<String,Object> getResult(CallableStatement arg0, int arg1)
			throws SQLException {
		Blob blob = arg0.getBlob(arg1);
		return bin2Map(blob);
	}

	@Override
	public void setParameter(PreparedStatement arg0, int arg1, HashMap<String,Object> arg2,
			JdbcType arg3) throws SQLException {
		if(arg2 == null){
			arg0.setString(arg1, null);
			return;
		}
		try {
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(arg2);
			oos.flush();
			arg0.setBytes(arg1,fos.toByteArray());
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}		

		arg0.setString(arg1, null);


	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> bin2Map(Blob blob) {
		if(blob == null){
			return null;
		}
		try{
			ObjectInputStream ois = new ObjectInputStream(blob.getBinaryStream());
			return (HashMap<String,Object>)ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("无法转换数据库内容到HashMap:" + e.getMessage());
		}		
		return null;
		
	}

}
