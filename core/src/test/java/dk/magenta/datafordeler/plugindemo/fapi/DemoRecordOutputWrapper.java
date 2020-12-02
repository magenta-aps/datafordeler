package dk.magenta.datafordeler.plugindemo.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.RecordOutputWrapper;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.DoubleListHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;

@Component
public class DemoRecordOutputWrapper extends RecordOutputWrapper<DemoEntityRecord> {
    
    @Autowired
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(RecordOutputWrapper<DemoEntityRecord>.OutputContainer container, DemoEntityRecord item, Mode m) {
        container.addNontemporal(DemoEntityRecord.IO_FIELD_ADDRESS_NUMBER, item.getPostnr());
        container.addBitemporal(DemoEntityRecord.IO_FIELD_NAME, item.getName(), true);
    }

    @Override
    protected ObjectNode fallbackOutput(Mode mode, RecordOutputWrapper.OutputContainer recordOutput, Bitemporality mustContain) {
        if (mode == Mode.LEGACY) {
            HashMap<String, String> keyConversion = new HashMap<>();
            return recordOutput.getRDV(mustContain, keyConversion, null);
        }
        return null;
    }

    protected class OutputContainer extends RecordOutputWrapper.OutputContainer {

        private DoubleListHashMap<Bitemporality, String, ObjectNode> bitemporalData = new DoubleListHashMap<>();

        private ListHashMap<String, JsonNode> nontemporalData = new ListHashMap<>();

        private HashSet<String> trySingle = new HashSet<>();
        private HashSet<String> forceList = new HashSet<>();

        private JsonNode prepareNode(String key, JsonNode node) {
            if (node instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) node;
                objectNode.remove(DemoRecordOutputWrapper.this.getRemoveFieldNames(    Mode.LEGACY    ));
                if (objectNode.size() == 1 && this.trySingle.contains(key)) {
                    return objectNode.get(objectNode.fieldNames().next());
                }
            }
            return node;
        }
    }

}
