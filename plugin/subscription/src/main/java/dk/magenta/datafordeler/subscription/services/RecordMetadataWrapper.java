package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalDataRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecordMetadataWrapper {

    @Autowired
    private ObjectMapper objectMapper;


    public ObjectNode fillContainer(String pnr, String fieldname, CprBitemporalPersonRecord valueBeforeEvent, CprBitemporalPersonRecord valueAfterEvent) {

        ObjectNode root = this.objectMapper.createObjectNode();
        root.put(PersonEntity.IO_FIELD_CPR_NUMBER, pnr);

        JsonNode nodeBeforeDataEvent = null;
        JsonNode nodeAfterDataEvent = null;

        if (valueBeforeEvent != null) {
            nodeBeforeDataEvent = objectMapper.convertValue(valueBeforeEvent, JsonNode.class);
        }

        if (valueAfterEvent != null) {
            nodeAfterDataEvent = objectMapper.convertValue(valueAfterEvent, JsonNode.class);
        }

        root.put("before_" + fieldname, cleanupObjectNode(nodeBeforeDataEvent));
        root.put("after_" + fieldname, cleanupObjectNode(nodeAfterDataEvent));
        return root;
    }


    public ObjectNode fillContainer(String cvr, String fieldname, CvrBitemporalDataRecord valueBeforeEvent, CvrBitemporalDataRecord valueAfterEvent) {

        ObjectNode root = this.objectMapper.createObjectNode();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        root.put(CompanyRecord.IO_FIELD_CVR_NUMBER, cvr);

        JsonNode nodeBeforeDataEvent = null;
        JsonNode nodeAfterDataEvent = null;

        if (valueBeforeEvent != null) {
            nodeBeforeDataEvent = mapper.convertValue(valueBeforeEvent, JsonNode.class);
        }

        if (valueAfterEvent != null) {
            nodeAfterDataEvent = mapper.convertValue(valueAfterEvent, JsonNode.class);
        }

        root.put("before_" + fieldname, cleanupObjectNode(nodeBeforeDataEvent));
        root.put("after_" + fieldname, cleanupObjectNode(nodeAfterDataEvent));
        return root;
    }


    private JsonNode cleanupObjectNode(JsonNode objectNode) {
        if (objectNode != null) {
            ((ObjectNode) objectNode).remove("cnt");
            ((ObjectNode) objectNode).remove("sidstOpdateret");
            ((ObjectNode) objectNode).remove("undone");
            ((ObjectNode) objectNode).remove("origin");
            ((ObjectNode) objectNode).remove("originDate");
            ((ObjectNode) objectNode).remove("technicalCorrection");
            ((ObjectNode) objectNode).remove("undo");
            ((ObjectNode) objectNode).remove("correctors");
            ((ObjectNode) objectNode).remove("fieldName");
            ((ObjectNode) objectNode).remove("sameAs");
            ((ObjectNode) objectNode).remove("replacesId");
            ((ObjectNode) objectNode).remove("replacedById");
            ((ObjectNode) objectNode).remove("myndighed");
            ((ObjectNode) objectNode).remove("registreringFra");
            ((ObjectNode) objectNode).remove("registreringTil");
            ((ObjectNode) objectNode).remove("virkningFraUsikker");
            ((ObjectNode) objectNode).remove("virkningTil");
            ((ObjectNode) objectNode).remove("virkningTilUsikker");
            ((ObjectNode) objectNode).remove("virkningFra");
        }
        return objectNode;
    }

}
