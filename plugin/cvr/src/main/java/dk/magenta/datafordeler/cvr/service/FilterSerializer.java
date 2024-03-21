package dk.magenta.datafordeler.cvr.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.records.CvrRecord;

import java.io.IOException;
import java.util.Set;
import java.util.function.Function;

public class FilterSerializer extends JsonSerializer<CvrRecord> {

        private JsonSerializer<Object> defaultSerializer;
        private Function<CvrRecord, Boolean> filter;

        public FilterSerializer(JsonSerializer<Object> serializer, Function<CvrRecord, Boolean> filter) {
            defaultSerializer = serializer;
            this.filter = filter;
        }

        @Override
        public void serialize(CvrRecord value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            if (this.filter.apply(value)) {

                defaultSerializer.serialize(value, jgen, provider);
            }
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, CvrRecord value) {
            return (value == null || !this.filter.apply(value));
        }
}
