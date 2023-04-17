package org.billing.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Date;

    public class IsoDateDeserializer extends StdDeserializer<Date> {
        public IsoDateDeserializer() {
            this(null);
        }

        public IsoDateDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            System.out.println(node);
            String dateString = node.get("$date").asText();
            return new Date(Long.parseLong(dateString));
        }
    }
