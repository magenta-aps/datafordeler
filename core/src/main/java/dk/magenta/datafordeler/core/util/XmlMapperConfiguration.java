package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;

@Configuration
public class XmlMapperConfiguration {

    /**
     * Creates a module to serialize and deserialize objects of type "java.time.OffsetDateTime"
     *
     * @return The created module
     */
    private SimpleModule getOffsetDateTimeModule() {
        SimpleModule dateModule = new SimpleModule();
        OffsetDateTimeAdapter adapter = new OffsetDateTimeAdapter();
        dateModule.addSerializer(adapter.getSerializer());
        dateModule.addDeserializer(OffsetDateTime.class, adapter.getDeserializer());
        return dateModule;
    }

    /**
     * XmlMapper configuration; add serializers here
     */
    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper(new JacksonXmlModule());
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.registerModule(this.getOffsetDateTimeModule());
        return xmlMapper;
    }

}
