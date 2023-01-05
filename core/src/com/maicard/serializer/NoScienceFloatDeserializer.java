package com.maicard.serializer;

import java.io.IOException;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.maicard.core.constants.Constants;


/**
 * 不使用科学计数法来显示float
 *
 *
 * @author NetSnake
 * @date 2017-06-28
 */
public class NoScienceFloatDeserializer extends JsonSerializer<Float> {

	final DecimalFormat df = new DecimalFormat(Constants.DEFAULT_DECIMAL_FORMAT);


	@Override
	public void serialize(Float value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {        
		gen.writeNumber(df.format(value));
	}

}