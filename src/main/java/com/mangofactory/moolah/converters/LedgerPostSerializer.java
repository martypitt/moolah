package com.mangofactory.moolah.converters;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.mangofactory.moolah.LedgerPost;

public class LedgerPostSerializer extends JsonSerializer<LedgerPost> {

	@Override
	public void serialize(LedgerPost ledgerPost, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("transactionId", ledgerPost.getTransactionId());
		jgen.writeStringField("description", ledgerPost.getDescription());
		jgen.writeStringField("value", ledgerPost.getValue().toString());
		jgen.writeNumberField("transactionDate", ledgerPost.getTransactionDate().getMillis());
		jgen.writeEndObject();
		
	}

}
