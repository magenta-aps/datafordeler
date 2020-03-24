package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.core.util.DoubleListHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class RecordOutputWrapper<E extends IdentifiedEntity> extends OutputWrapper<E> {

    public static final String EFFECTS = "virkninger";
    public static final String EFFECT_FROM = "virkningFra";
    public static final String EFFECT_TO = "virkningTil";
    public static final String REGISTRATIONS = "registreringer";
    public static final String REGISTRATION_FROM = "registreringFra";
    public static final String REGISTRATION_TO = "registreringTil";

    public abstract ObjectMapper getObjectMapper();

    protected JsonModifier getModifier(ResultSet resultSet) { return null; }

    private HashMap<String, JsonModifier> modifiers = new HashMap<>();

    // RVD
    private final Set<String> rvdNodeRemoveFields = new HashSet<>(Arrays.asList(
            "registreringFra",
            "registreringTil",
            "registrationFrom",
            "registrationTo",
            "virkningFra",
            "virkningTil",
            "sidstOpdateret",
            "lastUpdated"
    ));
    private final Set<String> rdvNodeRemoveFields = rvdNodeRemoveFields;

    private Set<String> dataonlyNodeRemoveFields = rvdNodeRemoveFields;

    public Set<String> getRemoveFieldNames(Mode mode) {
        switch (mode) {
            case RVD:
                return this.rvdNodeRemoveFields;
            case RDV:
                return this.rdvNodeRemoveFields;
            case DATAONLY:
                return this.dataonlyNodeRemoveFields;
        }
        return Collections.emptySet();
    }

    protected abstract void fillContainer(OutputContainer container, E item, Mode m);

    protected abstract ObjectNode fallbackOutput(Mode mode, OutputContainer recordOutput, Bitemporality mustContain);


    public Object wrapResultSet(ResultSet<E> input, BaseQuery query, Mode mode) {
        for (Class associatedEntityClass : input.getAssociatedEntityClasses()) {
            OutputWrapper wrapper = wrapperMap.get(associatedEntityClass);
            if (wrapper instanceof RecordOutputWrapper) {
                // Find any JsonModifiers from this outputwrapper of associated entityclass, and add them to our cache
                RecordOutputWrapper recordOutputWrapper = (RecordOutputWrapper) wrapper;
                JsonModifier modifier = recordOutputWrapper.getModifier(input);
                if (modifier != null) {
                    this.modifiers.put(modifier.getName(), modifier);
                }
            }
        }
        Object result = this.wrapResult(input.getPrimaryEntity(), query, mode);
        return result;
    }

    protected Map<Class, List<String>> getEligibleModifierNames() {
        return Collections.emptyMap();
    }
    private List<JsonModifier> getEligibleModifiers(Class cls) {
        List<String> modifierNames = this.getEligibleModifierNames().get(cls);
        if (modifierNames == null) {
            return Collections.emptyList();
        }

        return modifierNames.stream().map(name -> this.modifiers.get(name)).collect(Collectors.toList());
    }


    protected class OutputContainer {

        protected DoubleListHashMap<Bitemporality, String, JsonNode> bitemporalData = new DoubleListHashMap<>();

        public DoubleListHashMap<Bitemporality, String, JsonNode> getBitemporalData() {
            return this.bitemporalData;
        }

        protected ListHashMap<String, JsonNode> nontemporalData = new ListHashMap<>();

        protected HashSet<String> forcedArrayKeys = new HashSet<>();

        public boolean isArrayForced(String key) {
            return this.forcedArrayKeys.contains(key);
        }

        public <T extends Monotemporal> void addMonotemporal(String key, Set<T> records) {
            this.addMonotemporal(key, records, null, false, false);
        }

        public <T extends Monotemporal> void addMonotemporal(String key, Set<T> records, boolean unwrapSingle) {
            this.addMonotemporal(key, records, null, unwrapSingle, false);
        }

        public <T extends Monotemporal> void addMonotemporal(String key, Set<T> records, Function<T, JsonNode> converter) {
            this.addMonotemporal(key, records, converter, false, false);
        }

        public <T extends Monotemporal> void addMonotemporal(String key, Set<T> records, Function<T, JsonNode> converter, boolean unwrapSingle, boolean forceArray) {
            this.addTemporal(key, records, converter, unwrapSingle, forceArray, t -> t.getMonotemporality().asBitemporality());
        }

        public <T extends Bitemporal> void addBitemporal(String key, Set<T> records) {
            this.addBitemporal(key, records, null, false, false);
        }

        public <T extends Bitemporal> void addBitemporal(String key, Set<T> records, boolean unwrapSingle) {
            this.addBitemporal(key, records, null, unwrapSingle, false);
        }

        public <T extends Bitemporal> void addBitemporal(String key, Set<T> records, Function<T, JsonNode> converter) {
            this.addBitemporal(key, records, converter, false, false);
        }

        public <T extends Bitemporal> void addBitemporal(String key, Set<T> records, Function<T, JsonNode> converter, boolean unwrapSingle, boolean forceArray) {
            this.addTemporal(key, records, converter, unwrapSingle, forceArray, Bitemporal::getBitemporality);
        }

        private <T extends Monotemporal> void addTemporal(String key, Set<T> records, Function<T, JsonNode> converter, boolean unwrapSingle, boolean forceArray, Function<T, Bitemporality> bitemporalityExtractor) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.getObjectMapper();
            for (T record : records) {
                if (record != null) {
                    JsonNode value = (converter != null) ? converter.apply(record) : objectMapper.valueToTree(record);
                    Bitemporality bitemporality = bitemporalityExtractor.apply(record);
                    if (value instanceof ObjectNode) {
                        ObjectNode oValue = (ObjectNode) value;
                        for (JsonModifier modifier : RecordOutputWrapper.this.getEligibleModifiers(record.getClass())) {
                            if (modifier != null) {
                                modifier.modify(oValue);
                            }
                        }
                        if (unwrapSingle && value.size() == 1) {
                            this.bitemporalData.add(bitemporality, key, oValue.get(oValue.fieldNames().next()));
                            continue;
                        }
                    }
                    this.bitemporalData.add(bitemporality, key, value);
                }
            }
            if (forceArray) {
                this.forcedArrayKeys.add(key);
            }
        }

        public <T extends Nontemporal> void addNontemporal(String key, T record) {
            this.addNontemporal(key, Collections.singleton(record), null, false, false);
        }

        public <T extends Nontemporal> void addNontemporal(String key, Function<T, JsonNode> converter, T record) {
            this.addNontemporal(key, Collections.singleton(record), converter, false, false);
        }

        public <T extends Nontemporal> void addNontemporal(String key, Set<T> records) {
            this.addNontemporal(key, records, null, false, false);
        }

        public <T extends Nontemporal> void addNontemporal(String key, Set<T> records, Function<T, JsonNode> converter, boolean unwrapSingle, boolean forceArray) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.getObjectMapper();
            for (T record : records) {
                JsonNode value = (converter != null) ? converter.apply(record) : objectMapper.valueToTree(record);
                if (value instanceof ObjectNode) {
                    ObjectNode oValue = (ObjectNode) value;
                    for (JsonModifier modifier : RecordOutputWrapper.this.getEligibleModifiers(record.getClass())) {
                        modifier.modify(oValue);
                    }
                    if (unwrapSingle && value.size() == 1) {
                        this.nontemporalData.add(key, oValue.get(oValue.fieldNames().next()));
                        continue;
                    }
                }
                this.nontemporalData.add(key, value);
            }
            if (forceArray) {
                this.forcedArrayKeys.add(key);
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


        public ObjectNode getRVD(Bitemporality mustOverlap) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.getObjectMapper();
            ArrayNode registrationsNode = objectMapper.createArrayNode();
            ArrayList<Bitemporality> bitemporalities = new ArrayList<>(this.bitemporalData.keySet());
            ListHashMap<OffsetDateTime, Bitemporality> startTerminators = new ListHashMap<>();
            ListHashMap<OffsetDateTime, Bitemporality> endTerminators = new ListHashMap<>();
            for (Bitemporality bitemporality : bitemporalities) {
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

            HashSet<Bitemporality> presentBitemporalities = new HashSet<>();

            for (int i=0; i<terminators.size(); i++) {
                OffsetDateTime t = terminators.get(i);
                List<Bitemporality> startingHere = startTerminators.get(t);
                List<Bitemporality> endingHere = endTerminators.get(t);
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
                            ArrayList<Bitemporality> sortedEffects = new ArrayList<>(presentBitemporalities);
                            sortedEffects.sort(BitemporalityComparator.EFFECT);
                            Bitemporality lastEffect = null;
                            ObjectNode effectNode = null;
                            for (Bitemporality bitemporality : sortedEffects) {
                                if (lastEffect == null || effectNode == null || !lastEffect.equalEffect(bitemporality)) {
                                    effectNode = objectMapper.createObjectNode();
                                    effectsNode.add(effectNode);
                                }
                                effectNode.put(EFFECT_FROM, formatTime(bitemporality.effectFrom));
                                effectNode.put(EFFECT_TO, formatTime(bitemporality.effectTo));
                                HashMap<String, ArrayList<JsonNode>> records = this.bitemporalData.get(bitemporality);
                                for (String key : records.keySet()) {
                                    List<JsonNode> nodes = records.get(key);
                                    nodes = this.removeFields(nodes, Mode.RVD);
                                    this.setValue(objectMapper, effectNode, key, nodes);
                                }
                                lastEffect = bitemporality;
                            }
                        }
                    }
                }
            }
            ObjectNode output = objectMapper.createObjectNode();
            output.set(REGISTRATIONS, registrationsNode);
            return output;
        }

        private List<JsonNode> removeFields(List<JsonNode> nodes, Mode mode) {
            Set<String> removeFieldNames = RecordOutputWrapper.this.getRemoveFieldNames(mode);
            if (removeFieldNames != null) {
                nodes = this.filterNodes(nodes, node -> {
                    if (node instanceof ObjectNode) {
                        ObjectNode objectNode = (ObjectNode) node;
                        objectNode.remove(removeFieldNames);
                        if (objectNode.size() == 1) {
                            node = objectNode.get(objectNode.fieldNames().next());
                        }
                    }
                    return node;
                });
            }
            return nodes;
        }


        public ObjectNode getRDV(Bitemporality mustOverlap) {
            return this.getRDV(mustOverlap, null, null);
        }
        public ObjectNode getRDV(Bitemporality mustOverlap, Map<String, String> keyConversion, Function<Pair<String, ObjectNode>, ObjectNode> dataConversion) {

            ObjectMapper objectMapper = RecordOutputWrapper.this.getObjectMapper();
            ArrayNode registrationsNode = objectMapper.createArrayNode();
            ArrayList<Bitemporality> bitemporalities = new ArrayList<>(this.bitemporalData.keySet());
            ListHashMap<OffsetDateTime, Bitemporality> startTerminators = new ListHashMap<>();
            ListHashMap<OffsetDateTime, Bitemporality> endTerminators = new ListHashMap<>();
            for (Bitemporality bitemporality : bitemporalities) {
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
            HashSet<Bitemporality> presentBitemporalities = new HashSet<>();
            for (int i=0; i<terminators.size(); i++) {
                OffsetDateTime t = terminators.get(i);
                List<Bitemporality> startingHere = startTerminators.get(t);
                List<Bitemporality> endingHere = endTerminators.get(t);
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

                            for (Bitemporality bitemporality : presentBitemporalities) {
                                HashMap<String, ArrayList<JsonNode>> records = this.bitemporalData.get(bitemporality);
                                for (String key : records.keySet()) {

                                    String outKey = (keyConversion != null && keyConversion.containsKey(key)) ? keyConversion.get(key) : key;

                                    ArrayNode dataNode = (ArrayNode) registrationNode.get(outKey);
                                    if (dataNode == null) {
                                        dataNode = objectMapper.createArrayNode();
                                        registrationNode.set(outKey, dataNode);
                                    }
                                    List<JsonNode> nodes = records.get(key);
                                    nodes = this.removeFields(nodes, Mode.RDV);
                                    for (JsonNode node : nodes) {
                                        ObjectNode oNode;
                                        if (node instanceof ObjectNode) {
                                            oNode = (ObjectNode) node;
                                        } else {
                                            oNode = objectMapper.createObjectNode();
                                            oNode.set("value", node);
                                        }
                                        oNode.put(EFFECT_FROM, formatTime(bitemporality.effectFrom));
                                        oNode.put(EFFECT_TO, formatTime(bitemporality.effectTo));
                                        if (dataConversion != null) {
                                            oNode = dataConversion.apply(Pair.of(key, oNode));
                                        }
                                        dataNode.add(oNode);
                                    }

                                    //this.setValue(objectMapper, registrationNode, key, nodes);
                                }
                            }
                        }
                    }
                }
            }
            ObjectNode output = objectMapper.createObjectNode();
            output.set(REGISTRATIONS, registrationsNode);
            return output;
        }


        // DRV
        public ObjectNode getDRV(Bitemporality mustOverlap) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.getObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (Bitemporality bitemporality : this.bitemporalData.keySet()) {
                if (bitemporality.overlaps(mustOverlap)) {
                    HashMap<String, ArrayList<JsonNode>> data = this.bitemporalData.get(bitemporality);
                    for (String key : data.keySet()) {
                        ArrayNode arrayNode = (ArrayNode) objectNode.get(key);
                        if (arrayNode == null) {
                            arrayNode = objectMapper.createArrayNode();
                            objectNode.set(key, arrayNode);
                        }
                        List<JsonNode> nodes = data.get(key);
                        nodes = this.removeFields(nodes, Mode.DRV);
                        arrayNode.addAll(nodes);
                    }
                }
            }
            return objectNode;
        }

        // DataOnly
        public ObjectNode getDataOnly(Bitemporality mustOverlap) {
            ObjectMapper objectMapper = RecordOutputWrapper.this.getObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (Bitemporality bitemporality : this.bitemporalData.keySet()) {
                if (bitemporality.overlaps(mustOverlap)) {
                    HashMap<String, ArrayList<JsonNode>> data = this.bitemporalData.get(bitemporality);
                    for (String key : data.keySet()) {
                        ArrayNode arrayNode = (ArrayNode) objectNode.get(key);
                        if (arrayNode == null) {
                            arrayNode = objectMapper.createArrayNode();
                            objectNode.set(key, arrayNode);
                        }
                        List<JsonNode> nodes = data.get(key);
                        nodes = this.removeFields(nodes, Mode.DATAONLY);
                        arrayNode.addAll(nodes);
                    }
                }
            }
            return objectNode;
        }

        public ObjectNode getBase() {
            ObjectMapper objectMapper = RecordOutputWrapper.this.getObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (String key : this.nontemporalData.keySet()) {
                this.setValue(objectMapper, objectNode, key, this.nontemporalData.get(key));
            }
            return objectNode;
        }

        private void setValue(ObjectMapper objectMapper, ObjectNode objectNode, String key, List<JsonNode> values) {
            if (values.size() == 1 && !this.isArrayForced(key)) {
                objectNode.set(key, values.get(0));
            } else {
                ArrayNode array = objectMapper.createArrayNode();
                objectNode.set(key, array);
                for (JsonNode value : values) {
                    array.add(value);
                }
            }
        }

        private List<JsonNode> filterNodes(List<JsonNode> nodes, Function<JsonNode, JsonNode> filterMethod) {
            List<JsonNode> outNodes = new ArrayList<>();
            for (JsonNode node : nodes) {
                outNodes.add(filterMethod.apply(node));
            }
            return outNodes;
        }
    }

    @Override
    public Object wrapResult(E record, BaseQuery query, Mode mode) {
        Bitemporality mustContain = new Bitemporality(
                query.getRegistrationToAfter(),
                query.getRegistrationFromBefore(),
                query.getEffectToAfter(),
                query.getEffectFromBefore()
        );
        return this.getNode(record, mustContain, mode);
    }

    protected OutputContainer createOutputContainer() {
        return new OutputContainer();
    }

    public ObjectNode getNode(E record, Bitemporality overlap, Mode mode) {
        ObjectNode root = this.getObjectMapper().createObjectNode();
        if (record.getIdentification() != null) {
            root.put(Identification.IO_FIELD_UUID, record.getIdentification().getUuid().toString());
            root.put(Identification.IO_FIELD_DOMAIN, record.getIdentification().getDomain());
        }
        OutputContainer recordOutput = this.createOutputContainer();
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
            case DATAONLY:
                root.setAll(recordOutput.getDataOnly(overlap));
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
