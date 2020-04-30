package dk.magenta.datafordeler.cvr.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.RecordOutputWrapper;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;

import java.util.*;
import java.util.function.Function;

import static dk.magenta.datafordeler.core.fapi.OutputWrapper.Mode.LEGACY;

public abstract class CvrRecordOutputWrapper<E extends CvrEntityRecord> extends RecordOutputWrapper<E> {

    private final Set<String> removeFieldNames = new HashSet<>(Arrays.asList(new String[]{
            CvrBitemporalRecord.IO_FIELD_PERIOD,
            CvrBitemporalRecord.IO_FIELD_LAST_UPDATED,
            CvrBitemporalRecord.IO_FIELD_LAST_LOADED,
            CvrBitemporalRecord.IO_FIELD_DAFO_UPDATED
    }));

    private final Set<String> removeDataOnlyFields = new HashSet<>(Arrays.asList(new String[]{
            "antalAarsvaerk",
            "antalAnsatte",
            "antalAnsatteMin",
            "antalAnsatteMax",
            "registreringFra",
            "registreringTil",
            "virkningFra",
            "virkningTil",
    }));



    @Override
    public Set<String> getRemoveFieldNames(Mode mode) {
        switch(mode) {
            case DATAONLY:
                removeFieldNames.addAll(removeDataOnlyFields);
        }



        return this.removeFieldNames;
    }

    @Override
    protected ObjectNode fallbackOutput(Mode mode, OutputContainer recordOutput, Bitemporality mustContain) {
        return null;
    }

    @Override
    protected OutputContainer createOutputContainer() {
        return new CvrOutputContainer();
    }

    @Override
    public ObjectNode getNode(E record, Bitemporality overlap, Mode mode) {
        if (mode == LEGACY) {
            return this.getObjectMapper().valueToTree(record);
        }

        ObjectNode root = super.getNode(record, overlap, mode);

        CvrOutputContainer metadataRecordOutput = new CvrOutputContainer();
        this.fillMetadataContainer(metadataRecordOutput, record, mode);
        ObjectNode metaNode = this.getObjectMapper().createObjectNode();
        root.set("metadata", metaNode);
        metaNode.setAll(metadataRecordOutput.getBase());
        switch (mode) {
            case RVD:
                metaNode.setAll(metadataRecordOutput.getRVD(overlap));
                break;
            case RDV:
                metaNode.setAll(metadataRecordOutput.getRDV(overlap));
                break;
            case DRV:
                metaNode.setAll(metadataRecordOutput.getDRV(overlap));
                break;
            case DATAONLY:
                metaNode.setAll(metadataRecordOutput.getDataOnly(overlap));
                break;
            default:
                metaNode.setAll(this.fallbackOutput(mode, metadataRecordOutput, overlap));
                break;
        }
        return root;
    }

    private ObjectNode createItemNode(CvrRecord record) {
        return this.getObjectMapper().createObjectNode();
    }

    protected abstract void fillMetadataContainer(OutputContainer container, E item, Mode m);

    protected JsonNode createAddressNode(AddressRecord record) {
        ObjectNode adresseNode = this.createItemNode(record);
        adresseNode.put(AddressRecord.IO_FIELD_ROADCODE, record.getRoadCode());
        adresseNode.put(AddressRecord.IO_FIELD_HOUSE_FROM, record.getHouseNumberFrom());
        adresseNode.put(AddressRecord.IO_FIELD_FLOOR, record.getFloor());
        adresseNode.put(AddressRecord.IO_FIELD_DOOR, record.getDoor());
        adresseNode.put(AddressRecord.IO_FIELD_POSTDISTRICT, record.getPostdistrikt());
        adresseNode.put(AddressRecord.IO_FIELD_ROADNAME, record.getRoadName());
        adresseNode.put(AddressRecord.IO_FIELD_HOUSE_TO, record.getHouseNumberTo());
        adresseNode.put(AddressRecord.IO_FIELD_POSTCODE, record.getPostnummer());
        adresseNode.put(AddressRecord.IO_FIELD_CITY, record.getSupplementalCityName());
        adresseNode.put(AddressRecord.IO_FIELD_TEXT, record.getAddressText());
        adresseNode.put(AddressRecord.IO_FIELD_COUNTRYCODE, record.getCountryCode());
        AddressMunicipalityRecord kommune = record.getMunicipality();
        if (kommune != null) {
            adresseNode.put(Municipality.IO_FIELD_CODE, kommune.getMunicipalityCode());
            adresseNode.put(Municipality.IO_FIELD_NAME, kommune.getMunicipalityName());
        }
        return adresseNode;
    }

    protected JsonNode createLifecycleNode(LifecycleRecord record) {
        return BooleanNode.getTrue();
    }

    protected class CvrOutputContainer extends OutputContainer {


        public <T extends CvrBitemporalRecord> void addCvrBitemporal(String key, Set<T> records) {
            this.addCvrBitemporal(key, records, null, false, false);
        }

        public <T extends CvrBitemporalRecord> void addCvrBitemporal(String key, Set<T> records, boolean unwrapSingle) {
            this.addCvrBitemporal(key, records, null, unwrapSingle, false);
        }

        public <T extends CvrBitemporalRecord> void addCvrBitemporal(String key, Set<T> records, Function<T, JsonNode> converter) {
            this.addCvrBitemporal(key, records, converter, false, false);
        }

        public <T extends CvrBitemporalRecord> void addCvrBitemporal(String key, Set<T> records, Function<T, JsonNode> converter, boolean unwrapSingle, boolean forceArray) {
            ObjectMapper objectMapper = CvrRecordOutputWrapper.this.getObjectMapper();
            for (T record : records) {
                if (record != null) {
                    JsonNode value = (converter != null) ? converter.apply(record) : objectMapper.valueToTree(record);
                    if (value instanceof ObjectNode) {
                        ObjectNode oValue = (ObjectNode) value;
                        oValue.put(REGISTRATION_FROM, formatTime(record.getRegistrationFrom()));
                        oValue.put(REGISTRATION_TO, formatTime(record.getRegistrationTo()));
                        oValue.put(EFFECT_FROM, formatTime(record.getValidFrom()));
                        oValue.put(EFFECT_TO, formatTime(record.getValidTo()));
                    }
                    this.bitemporalData.add(record.getBitemporality(), key, value);
                }
            }
            if (forceArray) {
                this.forcedArrayKeys.add(key);
            }
        }

        public <T extends CvrNontemporalRecord> void addCvrNontemporal(String key, T record) {
            this.addCvrNontemporal(key, Collections.singleton(record), null, false, false);
        }

        public <T extends CvrNontemporalRecord> void addCvrNontemporal(String key, Function<T, JsonNode> converter, T record) {
            this.addCvrNontemporal(key, Collections.singleton(record), converter, false, false);
        }

        public <T extends CvrNontemporalRecord> void addCvrNontemporal(String key, Set<T> records) {
            this.addCvrNontemporal(key, records, null, false, false);
        }

        public <T extends CvrNontemporalRecord> void addCvrNontemporal(String key, Set<T> records, Function<T, JsonNode> converter, boolean unwrapSingle, boolean forceArray) {
            ObjectMapper objectMapper = CvrRecordOutputWrapper.this.getObjectMapper();
            for (T record : records) {
                JsonNode value = (converter != null) ? converter.apply(record) : objectMapper.valueToTree(record);
                if (value instanceof ObjectNode) {
                    ObjectNode oValue = (ObjectNode) value;
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

        public void addAttribute(String key, Set<AttributeRecord> attributes) {
            ObjectMapper objectMapper = CvrRecordOutputWrapper.this.getObjectMapper();
            for (AttributeRecord attribute : attributes) {
                ObjectNode attributeNode = objectMapper.createObjectNode();
                attributeNode.put(AttributeRecord.IO_FIELD_TYPE, attribute.getType());
                attributeNode.put(AttributeRecord.IO_FIELD_SEQUENCENUMBER, attribute.getSequenceNumber());
                attributeNode.put(AttributeRecord.IO_FIELD_VALUETYPE, attribute.getValueType());
                attributeNode.set(AttributeRecord.IO_FIELD_VALUES, objectMapper.createArrayNode());

                ListHashMap<Bitemporality, AttributeValueRecord> valueBuckets = new ListHashMap<>();
                for (AttributeValueRecord valueRecord : attribute.getValues()) {
                    valueBuckets.add(valueRecord.getBitemporality(), valueRecord);
                }

                for (Bitemporality bitemporality : valueBuckets.keySet()) {
                    ObjectNode instance = attributeNode.deepCopy();
                    this.bitemporalData.add(bitemporality, key, instance);
                    ArrayNode valueList = (ArrayNode) instance.get(AttributeRecord.IO_FIELD_VALUES);
                    for (AttributeValueRecord valueRecord : valueBuckets.get(bitemporality)) {
                        valueList.add(valueRecord.getValue());
                    }
                }
            }
        }
    }


    public Map<Class, List<String>> getEligibleModifierNames() {
        HashMap<Class, List<String>> map = new HashMap<>();
        ArrayList<String> addressModifiers = new ArrayList<>();
        addressModifiers.add("geo_municipality");
        addressModifiers.add("geo_road");
        addressModifiers.add("geo_accessaddress");
        addressModifiers.add("geo_locality");
        addressModifiers.add("cpr_road");
        map.put(AddressRecord.class, addressModifiers);
        return map;
    }
}
