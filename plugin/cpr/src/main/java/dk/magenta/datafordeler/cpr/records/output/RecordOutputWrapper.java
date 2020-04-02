package dk.magenta.datafordeler.cpr.records.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.core.util.DoubleListHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.cpr.data.CprEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Component
@Deprecated
public abstract class RecordOutputWrapper<E extends CprEntity> extends OutputWrapper<E> {

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

    public abstract Set<String> getRemoveFieldNames();

    protected abstract void fillContainer(OutputContainer container, E item, Mode mode);

    protected abstract ObjectNode fallbackOutput(Mode mode, OutputContainer recordOutput, CprBitemporality mustContain);

    protected class OutputContainer {

        private DoubleListHashMap<CprBitemporality, String, ObjectNode> bitemporalData = new DoubleListHashMap<>();

        private ListHashMap<String, JsonNode> nontemporalData = new ListHashMap<>();

        private HashSet<String> trySingle = new HashSet<>();
        private HashSet<String> forceList = new HashSet<>();

        public <T extends CprBitemporalRecord> void addBitemporal(String key, Set<T> records) {
            this.addBitemporal(key, records, null, false, false);
        }

        public <T extends CprBitemporalRecord> void addBitemporal(String key, Set<T> records, boolean unwrapSingle) {
            this.addBitemporal(key, records, null, unwrapSingle, false);
        }

        public <T extends CprBitemporalRecord> void addBitemporal(String key, Set<T> records, Function<T, ObjectNode> converter) {
            this.addBitemporal(key, records, converter, false, false);
        }

        public <T extends CprBitemporalRecord> void addBitemporal(String key, Set<T> records, Function<T, ObjectNode> converter, boolean unwrapSingle, boolean forceArray) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.objectMapper;
            for (T record : records) {
                if (record != null) {
                    ObjectNode value = (converter != null) ? converter.apply(record) : objectMapper.valueToTree(record);
                    this.bitemporalData.add(record.getBitemporality(), key, value);
                }
            }
            if (forceArray) {
                this.forceList.add(key);
            }
            if (unwrapSingle) {
                this.trySingle.add(key);
            }
        }

        public <T extends CprNontemporalRecord> void addNontemporal(String key, T record) {
            this.addNontemporal(key, Collections.singleton(record), null, false, false);
        }

        public <T extends CprNontemporalRecord> void addNontemporal(String key, Function<T, JsonNode> converter, T record) {
            this.addNontemporal(key, Collections.singleton(record), converter, false, false);
        }

        public <T extends CprNontemporalRecord> void addNontemporal(String key, Set<T> records) {
            this.addNontemporal(key, records, null, false, false);
        }

        public <T extends CprNontemporalRecord> void addNontemporal(String key, Set<T> records, Function<T, JsonNode> converter, boolean unwrapSingle, boolean forceArray) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.objectMapper;
            for (T record : records) {
                JsonNode value = (converter != null) ? converter.apply(record) : objectMapper.valueToTree(record);
                this.nontemporalData.add(key, value);
            }
            if (forceArray) {
                this.forceList.add(key);
            }
            if (unwrapSingle) {
                this.trySingle.add(key);
            }
        }

        public void addNontemporal(String key, Boolean data) {
            this.nontemporalData.add(key, data != null ? (data ? BooleanNode.getTrue() : BooleanNode.getFalse()) : null);
        }

        public void addNontemporal(String key, Integer data) {
            this.nontemporalData.add(key, data != null ? new IntNode(data) : null);
        }

        public void addNontemporal(String key, Long data) {
            this.nontemporalData.add(key, data != null ? new LongNode(data) : null);
        }

        public void addNontemporal(String key, String data) {
            this.nontemporalData.add(key, data != null ? new TextNode(data) : null);
        }

        public void addNontemporal(String key, LocalDate data) {
            this.nontemporalData.add(key, data != null ? new TextNode(data.format(DateTimeFormatter.ISO_LOCAL_DATE)) : null);
        }

        public void addNontemporal(String key, OffsetDateTime data) {
            this.nontemporalData.add(key, data != null ? new TextNode(data.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)) : null);
        }

        public ObjectNode getRVD(CprBitemporality mustOverlap) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.objectMapper;
            ArrayNode registrationsNode = objectMapper.createArrayNode();
            ArrayList<CprBitemporality> bitemporalities = new ArrayList<>(this.bitemporalData.keySet());
            ListHashMap<OffsetDateTime, CprBitemporality> startTerminators = new ListHashMap<>();
            ListHashMap<OffsetDateTime, CprBitemporality> endTerminators = new ListHashMap<>();
            for (CprBitemporality bitemporality : bitemporalities) {
                startTerminators.add(bitemporality.registrationFrom, bitemporality);
                endTerminators.add(bitemporality.registrationTo, bitemporality);
            }
            HashSet<OffsetDateTime> allTerminators = new HashSet<>();
            allTerminators.addAll(startTerminators.keySet());
            allTerminators.addAll(endTerminators.keySet());
            // Create a sorted list of all timestamps where Bitemporalities either begin or end
            ArrayList<OffsetDateTime> terminators = new ArrayList<>(allTerminators);
            terminators.sort(Comparator.nullsFirst(OffsetDateTime::compareTo));
            terminators.add(null);
            HashSet<CprBitemporality> presentBitemporalities = new HashSet<>();
            for (int i=0; i<terminators.size(); i++) {
                OffsetDateTime t = terminators.get(i);
                List<CprBitemporality> startingHere = startTerminators.get(t);
                List<CprBitemporality> endingHere = endTerminators.get(t);
                if (startingHere != null) {
                    presentBitemporalities.addAll(startingHere);
                }
                if (endingHere != null) {
                    presentBitemporalities.removeAll(endingHere);
                }
                if (i < terminators.size() - 1) {
                    OffsetDateTime next = terminators.get(i + 1);
                    if (!presentBitemporalities.isEmpty()) {
                        if (mustOverlap == null || mustOverlap.overlapsRegistration(t, next)) {
                            ObjectNode registrationNode = objectMapper.createObjectNode();
                            registrationsNode.add(registrationNode);
                            registrationNode.put(REGISTRATION_FROM, formatTime(t));
                            registrationNode.put(REGISTRATION_TO, formatTime(next));
                            ArrayNode effectsNode = objectMapper.createArrayNode();
                            registrationNode.set(EFFECTS, effectsNode);
                            ArrayList<CprBitemporality> sortedEffects = new ArrayList<>(presentBitemporalities);
                            sortedEffects.sort(BitemporalityComparator.effect(CprBitemporality.class));
                            CprBitemporality lastEffect = null;
                            ObjectNode effectNode = null;
                            for (CprBitemporality bitemporality : sortedEffects) {
                                if (lastEffect == null || effectNode == null || !lastEffect.equalEffect(bitemporality)) {
                                    effectNode = objectMapper.createObjectNode();
                                    effectsNode.add(effectNode);
                                }
                                effectNode.put(EFFECT_FROM, formatTime(bitemporality.effectFrom, true));
                                effectNode.put(EFFECT_TO, formatTime(bitemporality.effectTo, true));
                                HashMap<String, ArrayList<ObjectNode>> records = this.bitemporalData.get(bitemporality);
                                for (String key : records.keySet()) {
                                    this.setValue(objectMapper, effectNode, key, records.get(key));
                                }
                                lastEffect = bitemporality;
                            }
                        }
                    }
                }
            }
            ObjectNode output = objectMapper.createObjectNode();
            output.set("registreringer", registrationsNode);
            return output;
        }
        public ObjectNode getRDV(CprBitemporality mustOverlap) {
            return this.getRDV(mustOverlap, null, null);
        }
        public ObjectNode getRDV(CprBitemporality mustOverlap, Map<String, String> keyConversion, Function<Pair<String, ObjectNode>, ObjectNode> dataConversion) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.objectMapper;
            ArrayNode registrationsNode = objectMapper.createArrayNode();
            ArrayList<CprBitemporality> bitemporalities = new ArrayList<>(this.bitemporalData.keySet());
            ListHashMap<OffsetDateTime, CprBitemporality> startTerminators = new ListHashMap<>();
            ListHashMap<OffsetDateTime, CprBitemporality> endTerminators = new ListHashMap<>();
            for (CprBitemporality bitemporality : bitemporalities) {
                startTerminators.add(bitemporality.registrationFrom, bitemporality);
                endTerminators.add(bitemporality.registrationTo, bitemporality);
            }
            HashSet<OffsetDateTime> allTerminators = new HashSet<>();
            allTerminators.addAll(startTerminators.keySet());
            allTerminators.addAll(endTerminators.keySet());
            // Create a sorted list of all timestamps where Bitemporalities either begin or end
            ArrayList<OffsetDateTime> terminators = new ArrayList<>(allTerminators);
            terminators.sort(Comparator.nullsFirst(OffsetDateTime::compareTo));
            terminators.add(null);
            HashSet<CprBitemporality> presentBitemporalities = new HashSet<>();
            for (int i=0; i<terminators.size(); i++) {
                OffsetDateTime t = terminators.get(i);
                List<CprBitemporality> startingHere = startTerminators.get(t);
                List<CprBitemporality> endingHere = endTerminators.get(t);
                if (startingHere != null) {
                    presentBitemporalities.addAll(startingHere);
                }
                if (endingHere != null) {
                    presentBitemporalities.removeAll(endingHere);
                }
                if (i < terminators.size() - 1) {
                    OffsetDateTime next = terminators.get(i + 1);
                    if (!presentBitemporalities.isEmpty()) {
                        if (mustOverlap == null || mustOverlap.overlapsRegistration(t, next)) {
                            ObjectNode registrationNode = objectMapper.createObjectNode();
                            registrationsNode.add(registrationNode);
                            registrationNode.put(REGISTRATION_FROM, formatTime(t));
                            registrationNode.put(REGISTRATION_TO, formatTime(next));

                            for (CprBitemporality bitemporality : presentBitemporalities) {
                                HashMap<String, ArrayList<ObjectNode>> data = this.bitemporalData.get(bitemporality);
                                for (String key : data.keySet()) {

                                    String outKey = (keyConversion != null && keyConversion.containsKey(key)) ? keyConversion.get(key) : key;

                                    ArrayNode dataNode = (ArrayNode) registrationNode.get(outKey);
                                    if (dataNode == null) {
                                        dataNode = objectMapper.createArrayNode();
                                        registrationNode.set(outKey, dataNode);
                                    }
                                    for (ObjectNode item : data.get(key)) {
                                        item.remove("registreringFra");
                                        item.remove("registreringTil");
                                        item.put("virkningFra", formatTime(bitemporality.effectFrom));
                                        item.put("virkningTil", formatTime(bitemporality.effectTo));
                                        if (dataConversion != null) {
                                            item = dataConversion.apply(Pair.of(key, item));
                                        }
                                        dataNode.add(item);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ObjectNode output = objectMapper.createObjectNode();
            output.set("registreringer", registrationsNode);
            return output;
        }

        public ObjectNode getDRV(CprBitemporality mustOverlap) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.objectMapper;
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (CprBitemporality bitemporality : this.bitemporalData.keySet()) {
                if (bitemporality.overlaps(mustOverlap)) {
                    HashMap<String, ArrayList<ObjectNode>> data = this.bitemporalData.get(bitemporality);
                    for (String key : data.keySet()) {
                        ArrayNode subDataNode = (ArrayNode) objectNode.get(key);
                        if (subDataNode == null) {
                            subDataNode = objectMapper.createArrayNode();
                            objectNode.set(key, subDataNode);
                        }
                        for (JsonNode d : data.get(key)) {
                            subDataNode.add(d);
                        }
                    }
                }
            }
            return objectNode;
        }

        public ObjectNode getBase() {
            ObjectMapper objectMapper = RecordOutputWrapper.this.objectMapper;
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (String key : this.nontemporalData.keySet()) {
                this.setValue(objectMapper, objectNode, key, this.nontemporalData.get(key));
            }
            return objectNode;
        }

        private void setValue(ObjectMapper objectMapper, ObjectNode objectNode, String key, List<? extends JsonNode> values) {
            if (values.size() == 1 && !this.forceList.contains(key)) {
                objectNode.set(key, this.prepareNode(key, values.get(0)));
            } else {
                ArrayNode array = objectMapper.createArrayNode();
                objectNode.set(key, array);
                for (JsonNode value : values) {
                    array.add(this.prepareNode(key, value));
                }
            }
        }

        private JsonNode prepareNode(String key, JsonNode node) {
            if (node instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) node;
                objectNode.remove(RecordOutputWrapper.this.getRemoveFieldNames());
                if (objectNode.size() == 1 && this.trySingle.contains(key)) {
                    return objectNode.get(objectNode.fieldNames().next());
                }
            }
            return node;
        }
    }


    protected final ObjectNode getNode(E record, CprBitemporality overlap, Mode mode) {
        ObjectNode root = this.objectMapper.createObjectNode();
        root.put(Entity.IO_FIELD_UUID, record.getIdentification().getUuid().toString());
        root.put(Entity.IO_FIELD_DOMAIN, record.getIdentification().getDomain());
        OutputContainer recordOutput = new OutputContainer();
        this.fillContainer(recordOutput, record, mode);
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

    protected static String formatTime(OffsetDateTime time) {
        return formatTime(time, false);
    }

    protected static String formatTime(OffsetDateTime time, boolean asDateOnly) {
        if (time == null) return null;
        return time.format(asDateOnly ? DateTimeFormatter.ISO_LOCAL_DATE : DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    protected static String formatTime(LocalDate time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    protected static LocalDate getUTCDate(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(ZoneId.of("UTC")).toLocalDate();
    }

}
