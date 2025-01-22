package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson Adapter for serializing and deserializing OffsetDatetime objects
 */
public class OffsetDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {
    @Override
    public String marshal(OffsetDateTime offsetDateTime) {
        return OffsetDateTimeAdapter.toString(offsetDateTime);
    }

    public static String toString(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime) : null;
    }

    @Override
    public OffsetDateTime unmarshal(String stringRepresentation) {
        return OffsetDateTimeAdapter.fromString(stringRepresentation);
    }

    public static OffsetDateTime fromString(String stringRepresentation) {
        return stringRepresentation != null ? OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(stringRepresentation)) : null;
    }

    public StdSerializer<OffsetDateTime> getSerializer() {
        return new StdSerializer<OffsetDateTime>(OffsetDateTime.class) {
            @Override
            public void serialize(OffsetDateTime offsetDateTime, JsonGenerator
                    jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(OffsetDateTimeAdapter.this.marshal(offsetDateTime));

            }
        };
    }

    public StdDeserializer<OffsetDateTime> getDeserializer() {
        return new StdDeserializer<OffsetDateTime>(OffsetDateTime.class) {
            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                    jsonParser.nextToken();
                }
                String tokenValue = jsonParser.getValueAsString();
                if (tokenValue != null) {
                    return OffsetDateTimeAdapter.this.unmarshal(tokenValue);
                } else {
                    return null;
                }
            }
        };
    }
}
