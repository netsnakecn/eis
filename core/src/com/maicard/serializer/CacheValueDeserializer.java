package com.maicard.serializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.maicard.core.constants.Constants;
import com.maicard.core.entity.CacheValue;
import com.maicard.utils.JsonUtils;

public class CacheValueDeserializer extends JsonDeserializer<CacheValue>{

	static final Logger logger = LoggerFactory.getLogger(CacheValueDeserializer.class);

	final static SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT);

	@Override
	public CacheValue deserialize(JsonParser jp, DeserializationContext paramDeserializationContext)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);

		String objectType = node.path("objectType").asText();
		if(objectType == null){
			objectType = "java.util.LinkedHashMap";
		}

		CacheValue cv = new CacheValue();
		cv.key = node.path("key").asText();
		cv.objectType = objectType;
		String value = node.path("value").toString();
		if(objectType.endsWith("HashMap")){
			logger.info("把数据:" + value + "转换为类型:" + objectType);
		}
		try {

			cv.value = JsonUtils.getInstance().readValue(value, Class.forName(objectType));
			String dateString = node.path("expireTime").asText();
			if(StringUtils.isNotBlank(dateString)){
				try {
					cv.expireTime = sdf.parse(node.path("expireTime").asText());
				} catch (ParseException e) {
				}
			}

		} catch (ClassNotFoundException  e) {
			e.printStackTrace();

			throw new IOException("无法将数据:" + value + "反序列化为对象:" + objectType + ", full data:" + node.toString());
		}

		return cv;
	}



}
