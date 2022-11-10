package dk.magenta.datafordeler.gladdrreg.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.fapi.Query;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public abstract class GladdrregOutputWrapper<E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends SumiffiikData<V, D>> extends OutputWrapper<E> {

    private ObjectMapper objectMapper;

    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public Object wrapResult(E input, BaseQuery query) {
        objectMapper = new ObjectMapper();

        // Root
        ObjectNode root = objectMapper.createObjectNode();

        root.put(Entity.IO_FIELD_UUID, input.getUUID().toString());
        root.put(Entity.IO_FIELD_DOMAIN, input.getDomain());
        ArrayNode registreringer = objectMapper.createArrayNode();
        root.set(Entity.IO_FIELD_REGISTRATIONS, registreringer);

        for (R registration : input.getRegistrations()) {
            registreringer.add(wrapRegistrering(registration));
        }

        return root;

    }

    protected ObjectNode wrapRegistrering(R input) {
        ObjectNode output = objectMapper.createObjectNode();
        output.put(
                Registration.IO_FIELD_REGISTRATION_FROM,
                input.getRegistrationFrom() != null ? input.getRegistrationFrom().toString() : null
        );
        output.put(
                Registration.IO_FIELD_REGISTRATION_TO,
                input.getRegistrationTo() != null ? input.getRegistrationTo().toString() : null
        );

        for (V virkning : input.getEffects()) {
            for (D baseData : virkning.getDataItems()) {
                ObjectNode dataNode = objectMapper.createObjectNode();
                OffsetDateTime timestamp = baseData.getLastUpdated();

                dataNode.put(
                        Effect.IO_FIELD_EFFECT_FROM,
                        virkning.getEffectFrom() != null ? virkning.getEffectFrom().toString() : null
                );
                dataNode.put(
                        Effect.IO_FIELD_EFFECT_TO,
                        virkning.getEffectTo() != null ? virkning.getEffectTo().toString() : null
                );
                dataNode.put(
                        DataItem.IO_FIELD_LAST_UPDATED,
                        timestamp != null ? timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
                );
                //baseData.output(objectMapper, dataNode);
                this.wrapData(dataNode, baseData);
                addEffectDataToRegistration(output, "data", dataNode);
            }
        }
        return output;
    }

    protected void addEffectDataToRegistration(ObjectNode output, String key, JsonNode value) {
        if (!output.has(key) || output.get(key).isNull()) {
            output.set(key, objectMapper.createArrayNode());
        }
        ((ArrayNode) output.get(key)).add(value);
    }

    protected void wrapData(ObjectNode objectNode, D dataItem) {
        this.put(objectNode, CommonData.IO_FIELD_ACTIVE, dataItem.isActive());
        this.put(objectNode, CommonData.IO_FIELD_STATE, dataItem.getState());
        this.put(objectNode, CommonData.IO_FIELD_NOTE, dataItem.getNote());
        this.put(objectNode, SumiffiikData.IO_FIELD_SUMIFFIIK, dataItem.getSumiffiik());
        this.put(objectNode, SumiffiikData.IO_FIELD_DOMAIN, dataItem.getSumiffiik_domain());
    }

    protected ObjectNode serializeIdentification(Identification identification) {
        if (identification != null) {
            ObjectNode node = this.getObjectMapper().createObjectNode();
            node.put(Identification.IO_FIELD_UUID, identification.getUuid().toString());
            node.put(Identification.IO_FIELD_DOMAIN, identification.getDomain());
            return node;
        }
        return null;
    }

    protected void put(ObjectNode node, String key, boolean value) {
        node.put(key, value);
    }

    protected void put(ObjectNode node, String key, int value) {
        node.put(key, value);
    }

    protected void put(ObjectNode node, String key, String value) {
        if (value != null) {
            node.put(key, value);
        }
    }

    protected void put(ObjectNode node, String key, Identification value) {
        if (value != null) {
            node.set(key, this.serializeIdentification(value));
        }
    }

}
