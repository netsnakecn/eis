package com.maicard.serializer;

import java.io.IOException;
import java.util.Map;

import cn.hutool.core.util.NumberUtil;
import com.maicard.utils.NumericUtils;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.maicard.core.entity.ExtraData;
import org.apache.commons.lang.math.NumberUtils;

/**
 * 把ExtraData或其子类的Map输出为更简洁的格式
 * 
 * 
 * @author GHOST
 * @date 2018-10-22
 *
 */
@Slf4j
public class ExtraDataMapSerializer extends JsonSerializer<Map<String,ExtraData>>{



	@Override
	public void serialize(Map<String, ExtraData> map, JsonGenerator jsonGenerator, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jsonGenerator.writeStartObject();
		if(map != null && map.size() > 0) {
			for(ExtraData dd : map.values()) {
				if(StringUtils.isBlank(dd.getDataValue())) {
					continue;
				}
				/* if(NumericUtils.isNumeric(dd.getDataValue())){
				 	jsonGenerator.writeStringField(dd.getDataCode(), String.valueOf(dd.getDataValue()));
					 continue;
				 }*/
				try {
					jsonGenerator.writeStringField(dd.getDataCode(), dd.getDataValue().toString());
				}catch(Exception e){
					log.error("Can not cover value:" + dd.getDataValue() + " to string");
					e.printStackTrace();
				}
			}
		}
		jsonGenerator.writeEndObject();

	}
}
