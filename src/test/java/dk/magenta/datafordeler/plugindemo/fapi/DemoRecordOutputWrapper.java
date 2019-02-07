package dk.magenta.datafordeler.plugindemo.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.fapi.RecordOutputWrapper;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.DoubleListHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.plugindemo.model.DemoDataRecord;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;

@Component
public class DemoRecordOutputWrapper extends RecordOutputWrapper<DemoEntityRecord> {

    public static final String EFFECTS = "virkninger";
    public static final String EFFECT_FROM = "virkningFra";
    public static final String EFFECT_TO = "virkningTil";
    public static final String REGISTRATIONS = "registreringer";
    public static final String REGISTRATION_FROM = "registreringFra";
    public static final String REGISTRATION_TO = "registreringTil";
    
    @Autowired
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(RecordOutputWrapper<DemoEntityRecord>.OutputContainer container, DemoEntityRecord item) {
        container.addNontemporal(DemoEntityRecord.IO_FIELD_ADDRESS_NUMBER, item.getPostnr());
        container.addBitemporal(DemoEntityRecord.IO_FIELD_ADDRESS_NAME, item.getName(), true);
        System.out.println("item.getName() has "+item.getName().size()+" items");
        for (DemoDataRecord foo : item.getName()) {
            System.out.println(foo.getBitemporality());
        }
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


    public final ObjectNode getNode(DemoEntityRecord record, Bitemporality overlap, Mode mode) {
        ObjectNode root = this.objectMapper.createObjectNode();
        root.put(Entity.IO_FIELD_UUID, record.getIdentification().getUuid().toString());
        root.put(Entity.IO_FIELD_DOMAIN, record.getIdentification().getDomain());
        OutputContainer recordOutput = new OutputContainer();
        this.fillContainer(recordOutput, record);
        root.setAll(recordOutput.getBase());
        switch (mode) {
            case RVD:
                root.setAll(recordOutput.getRVD(overlap));
                break;
            case RDV:
                root.setAll(recordOutput.getRDV(overlap));
                break;
            case DRV:
                root.setAll(recordOutput.getDRV(overlap));
                break;
            default:
                root.setAll(this.fallbackOutput(mode, recordOutput, overlap));
                break;
        }
        return root;
    }

}
