package com.maicard.flow.th;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.maicard.base.BaseService;
import com.maicard.flow.entity.StepCondition;
import com.maicard.utils.JsonUtils;

public class StepConditionSetTypeHandler  extends BaseService implements TypeHandler<Set<StepCondition>>{

	@Override
	public Set<StepCondition> getResult(ResultSet arg0, String arg1) throws SQLException {
		String text = arg0.getString(arg1);
		return convert(text);

	}

	@Override
	public Set<StepCondition> getResult(ResultSet arg0, int arg1) throws SQLException {
		String text = arg0.getString(arg1);
		return convert(text);
	}

	@Override
	public Set<StepCondition> getResult(CallableStatement arg0, int arg1) throws SQLException {
		String text = arg0.getString(arg1);
		return convert(text);
	}

	@Override
	public void setParameter(PreparedStatement arg0, int arg1, Set<StepCondition> arg2, JdbcType arg3)
			throws SQLException {
		if(arg2 == null){
			arg0.setString(arg1, null);
			return;
		}
		try {
			arg0.setString(arg1, JsonUtils.getNoDefaultValueInstance().writeValueAsString(arg2));
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}		

		arg0.setString(arg1, null);		
	}
	
	private Set<StepCondition> convert(String text) {
		if(text == null || text.trim().equals("")){
			return null;
		}
		try{
			return JsonUtils.getInstance().readValue(text, new TypeReference<Set<StepCondition>>(){});
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("无法转换数据库内容到HashMap:" + e.getMessage());
		}		
		return null;	
	}

}
