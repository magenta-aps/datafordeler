package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalDataMetaRecord;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalDataRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonRecordMetadataWrapper {

    @Autowired
    private ObjectMapper objectMapper;



    public ObjectNode fillContainer(String pnr, String fieldname, CprBitemporalPersonRecord valueBeforeEvent, CprBitemporalPersonRecord valueAfterEvent)  {

        ObjectNode root = this.objectMapper.createObjectNode();
        ObjectMapper mapper = new ObjectMapper();
        root.put(PersonEntity.IO_FIELD_CPR_NUMBER, pnr);

        JsonNode nodeBeforeDataEvent = null;
        JsonNode nodeAfterDataEvent = null;

        if(valueBeforeEvent!=null) {
            nodeBeforeDataEvent = mapper.convertValue(valueBeforeEvent, JsonNode.class);
        }

        if(valueAfterEvent!=null) {
            nodeAfterDataEvent = mapper.convertValue(valueAfterEvent, JsonNode.class);
        }

        if(nodeBeforeDataEvent != null) {
            ((ObjectNode) nodeBeforeDataEvent).remove("cnt");
            ((ObjectNode) nodeBeforeDataEvent).remove("sidstOpdateret");
            ((ObjectNode) nodeBeforeDataEvent).remove("undone");
            ((ObjectNode) nodeBeforeDataEvent).remove("origin");
            ((ObjectNode) nodeBeforeDataEvent).remove("originDate");
            ((ObjectNode) nodeBeforeDataEvent).remove("technicalCorrection");
            ((ObjectNode) nodeBeforeDataEvent).remove("undo");
            ((ObjectNode) nodeBeforeDataEvent).remove("correctors");
            ((ObjectNode) nodeBeforeDataEvent).remove("fieldName");
            ((ObjectNode) nodeBeforeDataEvent).remove("sameAs");
            ((ObjectNode) nodeBeforeDataEvent).remove("replacesId");
            ((ObjectNode) nodeBeforeDataEvent).remove("replacedById");
            ((ObjectNode) nodeBeforeDataEvent).remove("myndighed");
            ((ObjectNode) nodeBeforeDataEvent).remove("registreringFra");
            ((ObjectNode) nodeBeforeDataEvent).remove("registreringTil");
            ((ObjectNode) nodeBeforeDataEvent).remove("virkningFraUsikker");
            ((ObjectNode) nodeBeforeDataEvent).remove("virkningTil");
            ((ObjectNode) nodeBeforeDataEvent).remove("virkningTilUsikker");
            ((ObjectNode) nodeBeforeDataEvent).remove("virkningFra");
        }


        if(nodeAfterDataEvent != null) {
            ((ObjectNode) nodeAfterDataEvent).remove("cnt");
            ((ObjectNode) nodeAfterDataEvent).remove("sidstOpdateret");
            ((ObjectNode) nodeAfterDataEvent).remove("undone");
            ((ObjectNode) nodeAfterDataEvent).remove("origin");
            ((ObjectNode) nodeAfterDataEvent).remove("originDate");
            ((ObjectNode) nodeAfterDataEvent).remove("technicalCorrection");
            ((ObjectNode) nodeAfterDataEvent).remove("undo");
            ((ObjectNode) nodeAfterDataEvent).remove("correctors");
            ((ObjectNode) nodeAfterDataEvent).remove("fieldName");
            ((ObjectNode) nodeAfterDataEvent).remove("sameAs");
            ((ObjectNode) nodeAfterDataEvent).remove("replacesId");
            ((ObjectNode) nodeAfterDataEvent).remove("replacedById");
            ((ObjectNode) nodeAfterDataEvent).remove("myndighed");
            ((ObjectNode) nodeAfterDataEvent).remove("registreringFra");
            ((ObjectNode) nodeAfterDataEvent).remove("registreringTil");
            ((ObjectNode) nodeAfterDataEvent).remove("virkningFraUsikker");
            ((ObjectNode) nodeAfterDataEvent).remove("virkningTil");
            ((ObjectNode) nodeAfterDataEvent).remove("virkningTilUsikker");
            ((ObjectNode) nodeAfterDataEvent).remove("virkningFra");
        }
        root.put("before_"+fieldname, nodeBeforeDataEvent);
        root.put("after_"+fieldname, nodeAfterDataEvent);
        return root;
    }



    public ObjectNode fillContainer(String pnr, String fieldname, CvrBitemporalDataRecord valueBeforeEvent, CvrBitemporalDataRecord valueAfterEvent)  {

        ObjectNode root = this.objectMapper.createObjectNode();
        ObjectMapper mapper = new ObjectMapper();
        root.put(PersonEntity.IO_FIELD_CPR_NUMBER, pnr);

        JsonNode nodeBeforeDataEvent = null;
        JsonNode nodeAfterDataEvent = null;

        if(valueBeforeEvent!=null) {
            nodeBeforeDataEvent = mapper.convertValue(valueBeforeEvent, JsonNode.class);
        }

        if(valueAfterEvent!=null) {
            nodeAfterDataEvent = mapper.convertValue(valueAfterEvent, JsonNode.class);
        }

        if(nodeBeforeDataEvent != null) {
            ((ObjectNode) nodeBeforeDataEvent).remove("cnt");
            ((ObjectNode) nodeBeforeDataEvent).remove("sidstOpdateret");
            ((ObjectNode) nodeBeforeDataEvent).remove("undone");
            ((ObjectNode) nodeBeforeDataEvent).remove("origin");
            ((ObjectNode) nodeBeforeDataEvent).remove("originDate");
            ((ObjectNode) nodeBeforeDataEvent).remove("technicalCorrection");
            ((ObjectNode) nodeBeforeDataEvent).remove("undo");
            ((ObjectNode) nodeBeforeDataEvent).remove("correctors");
            ((ObjectNode) nodeBeforeDataEvent).remove("fieldName");
            ((ObjectNode) nodeBeforeDataEvent).remove("sameAs");
            ((ObjectNode) nodeBeforeDataEvent).remove("replacesId");
            ((ObjectNode) nodeBeforeDataEvent).remove("replacedById");
            ((ObjectNode) nodeBeforeDataEvent).remove("myndighed");
            ((ObjectNode) nodeBeforeDataEvent).remove("registreringFra");
            ((ObjectNode) nodeBeforeDataEvent).remove("registreringTil");
            ((ObjectNode) nodeBeforeDataEvent).remove("virkningFraUsikker");
            ((ObjectNode) nodeBeforeDataEvent).remove("virkningTil");
            ((ObjectNode) nodeBeforeDataEvent).remove("virkningTilUsikker");
            ((ObjectNode) nodeBeforeDataEvent).remove("virkningFra");
        }


        if(nodeAfterDataEvent != null) {
            ((ObjectNode) nodeAfterDataEvent).remove("cnt");
            ((ObjectNode) nodeAfterDataEvent).remove("sidstOpdateret");
            ((ObjectNode) nodeAfterDataEvent).remove("undone");
            ((ObjectNode) nodeAfterDataEvent).remove("origin");
            ((ObjectNode) nodeAfterDataEvent).remove("originDate");
            ((ObjectNode) nodeAfterDataEvent).remove("technicalCorrection");
            ((ObjectNode) nodeAfterDataEvent).remove("undo");
            ((ObjectNode) nodeAfterDataEvent).remove("correctors");
            ((ObjectNode) nodeAfterDataEvent).remove("fieldName");
            ((ObjectNode) nodeAfterDataEvent).remove("sameAs");
            ((ObjectNode) nodeAfterDataEvent).remove("replacesId");
            ((ObjectNode) nodeAfterDataEvent).remove("replacedById");
            ((ObjectNode) nodeAfterDataEvent).remove("myndighed");
            ((ObjectNode) nodeAfterDataEvent).remove("registreringFra");
            ((ObjectNode) nodeAfterDataEvent).remove("registreringTil");
            ((ObjectNode) nodeAfterDataEvent).remove("virkningFraUsikker");
            ((ObjectNode) nodeAfterDataEvent).remove("virkningTil");
            ((ObjectNode) nodeAfterDataEvent).remove("virkningTilUsikker");
            ((ObjectNode) nodeAfterDataEvent).remove("virkningFra");
        }
        root.put("before_"+fieldname, nodeBeforeDataEvent);
        root.put("after_"+fieldname, nodeAfterDataEvent);
        return root;
    }
}
