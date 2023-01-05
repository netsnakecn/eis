package com.maicard.th;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maicard.utils.JsonUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class   JsonObjectTypeHandler implements TypeHandler {

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType) throws SQLException {
        if(o == null){
            preparedStatement.setString(i, null);
            return;
        }
        try{
            String value = JsonUtils.getSerializeInstance().writeValueAsString(o);
            preparedStatement.setString(i, value);
            return;
        }catch(Exception e){
            //e.printStackTrace();
        }
        preparedStatement.setString(i, null);

    }

    @Override
    public Object getResult(ResultSet resultSet, String s) throws SQLException {
        String v = resultSet.getString(s);
        if(v == null){
            return null;
        }
        try {
            return JsonUtils.getSerializeInstance().readValue(v, Object.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object getResult(ResultSet resultSet, int i) throws SQLException {
        String v = resultSet.getString(i);
        if(v == null){
            return null;
        }
        try {
            return JsonUtils.getSerializeInstance().readValue(v, Object.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }    }

    @Override
    public Object getResult(CallableStatement callableStatement, int i) throws SQLException {
        String v = callableStatement.getString(i);
        if(v == null){
            return null;
        }
        try {
            return JsonUtils.getSerializeInstance().readValue(v, Object.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }    }
}
