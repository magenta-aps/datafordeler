package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.magenta.datafordeler.core.database.Identification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Configures the Jackson ObjectMapper (serializer and deserializer for JSON)
 * The standard configuration in Jackson can handle most of what we need out of
 * the box; we only need to add special cases here, like OffsetDateTime
 */
@Configuration
public class ObjectMapperConfiguration {
    @Value("${spring.jackson.serialization.indent-output:false}")
    private boolean indent;

    /**
     * Creates a module to serialize and deserialize objects of type "java.time.OffsetDateTime"
     *
     * @return The created module
     */
    private SimpleModule getOffsetDateTimeModule() {
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
            @Override
            public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime));
            }
        });
        dateModule.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                    jsonParser.nextToken();
                }
                String tokenValue = jsonParser.getValueAsString();
                if (tokenValue != null) {
                    return OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(tokenValue));
                } else {
                    return null;
                }
            }
        });
        return dateModule;
    }


    /**
     * Creates a module to serialize and deserialize objects of type "java.time.LocalDate"
     *
     * @return The created module
     */
    private SimpleModule getLocalDateModule() {
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(DateTimeFormatter.ISO_LOCAL_DATE.format(localDate));
            }
        });
        dateModule.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                    jsonParser.nextToken();
                }
                String tokenValue = jsonParser.getValueAsString();
                if (tokenValue != null) {
                    return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(tokenValue));
                } else {
                    return null;
                }
            }
        });
        return dateModule;
    }


    /**
     * Creates a module to serialize and deserialize objects of type "java.time.LocalDateTime"
     *
     * @return The created module
     */
    private SimpleModule getLocalDateTimeModule() {
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime));
            }
        });
        dateModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                    jsonParser.nextToken();
                }
                String tokenValue = jsonParser.getValueAsString();
                if (tokenValue != null) {
                    return LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(tokenValue));
                } else {
                    return null;
                }
            }
        });
        return dateModule;
    }

    /**
     * ObjectMapper configuration; add serializers here
     */
    @Primary
    @Bean()
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, indent);
        objectMapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(this.getOffsetDateTimeModule());
        objectMapper.registerModule(this.getLocalDateModule());
        objectMapper.registerModule(this.getLocalDateTimeModule());
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        hibernate6Module.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        objectMapper.registerModule(hibernate6Module);  // To auto-load lazyloaded references
        return objectMapper;
    }

    /**
     * Creates a module to serialize and deserialize objects of type "java.time.OffsetDateTime"
     *
     * @return The created module
     */
    private SimpleModule getIdentificationModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Identification.class, new JsonSerializer<Identification>
                () {
            @Override
            public void serialize(Identification id, JsonGenerator gen,
                                  SerializerProvider prov) throws IOException {
                gen.writeString(id.getDomain() + id.getUuid());
            }
        });
        return module;
    }

    /**
     * ObjectMapper configuration; add serializers here
     */
    @Bean()
    public CsvMapper csvMapper() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        csvMapper.registerModule(new JavaTimeModule());
        csvMapper.registerModule(this.getOffsetDateTimeModule());
        csvMapper.registerModule(this.getIdentificationModule());
        return csvMapper;
    }
}
