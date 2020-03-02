package dk.magenta.datafordeler.cvr.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.util.DoubleHashMap;
import dk.magenta.datafordeler.cvr.data.shared.AttributeData;
import dk.magenta.datafordeler.cvr.data.shared.LifecycleData;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;
import dk.magenta.datafordeler.cvr.data.unversioned.Municipality;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class CvrOutputWrapper<T extends Entity> extends OutputWrapper<T> {

    private ObjectMapper objectMapper = new ObjectMapper();

    protected ObjectNode createVirkning(Effect virkning, OffsetDateTime lastUpdated) {
        return this.createVirkning(virkning, true, lastUpdated);
    }

    protected ObjectNode createVirkning(Effect virkning, boolean includeVirkningTil, OffsetDateTime lastUpdated) {
        ObjectNode output = objectMapper.createObjectNode();
        output.put(
                Effect.IO_FIELD_EFFECT_FROM,
                virkning.getEffectFrom() != null ? virkning.getEffectFrom().toString() : null
        );
        if (includeVirkningTil) {
            output.put(
                    Effect.IO_FIELD_EFFECT_TO,
                    virkning.getEffectTo() != null ? virkning.getEffectTo().toString() : null
            );
        }
        output.put(DataItem.IO_FIELD_LAST_UPDATED, lastUpdated != null ? lastUpdated.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return output;
    }

    protected void addEffectDataToRegistration(ObjectNode output, String key, JsonNode value) {
        if (!output.has(key)) {
            output.set(key, objectMapper.createArrayNode());
        }
        ArrayNode destination = ((ArrayNode) output.get(key));
        if (value.isArray()) {
            destination.addAll((ArrayNode) value);
        } else {
            destination.add(value);
        }
    }

    protected ObjectNode createIdentificationNode(Identification identification) {
        if (identification != null) {
            ObjectNode identificationObject = objectMapper.createObjectNode();
            identificationObject.put(Identification.IO_FIELD_UUID, identification.getUuid().toString());
            identificationObject.put(Identification.IO_FIELD_DOMAIN, identification.getDomain());
            return identificationObject;
        }
        return null;
    }

    protected ObjectNode createAddressNode(Effect virkning, OffsetDateTime lastUpdated, Address adresse) {
        ObjectNode adresseNode = createVirkning(virkning, lastUpdated);

        adresseNode.put(Address.IO_FIELD_ROADCODE, adresse.getRoadCode());
        adresseNode.put(Address.IO_FIELD_HOUSE_FROM, adresse.getHouseNumberFrom());
        adresseNode.put(Address.IO_FIELD_FLOOR, adresse.getFloor());
        adresseNode.put(Address.IO_FIELD_DOOR, adresse.getDoor());
        adresseNode.put(Address.IO_FIELD_POSTDISTRICT, adresse.getPostdistrikt());
        adresseNode.put(Address.IO_FIELD_ROADNAME, adresse.getRoadName());
        adresseNode.put(Address.IO_FIELD_HOUSE_TO, adresse.getHouseNumberTo());
        adresseNode.put(Address.IO_FIELD_POSTCODE, adresse.getPostnummer());
        adresseNode.put(Address.IO_FIELD_CITY, adresse.getSupplementalCityName());
        adresseNode.put(Address.IO_FIELD_TEXT, adresse.getAddressText());
        adresseNode.put(Address.IO_FIELD_COUNTRYCODE, adresse.getCountryCode());

        Municipality kommune = adresse.getMunicipality();
        if (kommune != null) {
            adresseNode.put(Municipality.IO_FIELD_CODE, kommune.getCode());
            adresseNode.put(Municipality.IO_FIELD_NAME, kommune.getName());
        }
        return adresseNode;
    }

    protected ArrayNode createAttributeNode(Effect virkning, OffsetDateTime timestamp, Set<AttributeData> attributes) {
        ArrayNode listNode = objectMapper.createArrayNode();
        DoubleHashMap<String, String, ArrayList<AttributeData>> sorted = new DoubleHashMap<>();
        for (AttributeData attribute : attributes) {
            ArrayList<AttributeData> list = sorted.get(attribute.getType(), attribute.getValueType());
            if (list == null) {
                list = new ArrayList<>();
                sorted.put(attribute.getType(), attribute.getValueType(), list);
            }
            int sequenceNumber = attribute.getSequenceNumber();
            if (sequenceNumber > list.size()) {
                list.add(attribute);
            } else if (sequenceNumber >= 0) {
                list.add(sequenceNumber, attribute);
            }
        }

        for (String type : sorted.keySet()) {
            HashMap<String, ArrayList<AttributeData>> typeMap = sorted.get(type);
            for (String valuetype : typeMap.keySet()) {
                List<AttributeData> attributeList = typeMap.get(valuetype);
                ObjectNode attributeNode = createVirkning(virkning, timestamp);
                attributeNode.put(AttributeData.IO_FIELD_TYPE, type);
                attributeNode.put(AttributeData.IO_FIELD_VALUETYPE, valuetype);
                ArrayNode valueList = objectMapper.createArrayNode();
                attributeNode.set(AttributeData.IO_FIELD_VALUES, valueList);
                for (AttributeData data : attributeList) {
                    valueList.add(data.getValue());
                }
                listNode.add(attributeNode);
            }
        }
        return listNode;
    }

    protected ObjectNode createIndustryNode(Effect virkning, OffsetDateTime lastUpdated, Industry industry) {
        ObjectNode industryNode = createVirkning(virkning, lastUpdated);
        industryNode.put(Industry.IO_FIELD_NAME, industry.getIndustryText());
        industryNode.put(Industry.IO_FIELD_CODE, industry.getIndustryCode());
        return industryNode;
    }

    protected ObjectNode createLifecycleNode(Effect virkning, OffsetDateTime lastUpdated, LifecycleData lifecycle) {
        ObjectNode node = createVirkning(virkning, lastUpdated);
        if (lifecycle.getStartDate() != null) {
            node.put(LifecycleData.IO_FIELD_START, this.getUTCDate(lifecycle.getStartDate()).format(DateTimeFormatter.ISO_DATE));
        }
        if (lifecycle.getEndDate() != null) {
            node.put(LifecycleData.IO_FIELD_END, this.getUTCDate(lifecycle.getEndDate()).format(DateTimeFormatter.ISO_DATE));
        }
        return node;
    }

    protected ObjectNode createSimpleNode(Effect virkning, OffsetDateTime lastUpdated, String key, Boolean value) {
        ObjectNode node = createVirkning(virkning, lastUpdated);
        node.put(key, value);
        return node;
    }

    protected ObjectNode createSimpleNode(Effect virkning, OffsetDateTime lastUpdated, String key, String value) {
        ObjectNode node = createVirkning(virkning, lastUpdated);
        node.put(key, value);
        return node;
    }

    protected ObjectNode createSimpleNode(Effect virkning, OffsetDateTime lastUpdated, String key, Long value) {
        ObjectNode node = createVirkning(virkning, lastUpdated);
        node.put(key, value);
        return node;
    }

    protected ObjectNode createListNode(Effect virkning, OffsetDateTime lastUpdated, String key, Collection<Long> value) {
        ObjectNode node = createVirkning(virkning, lastUpdated);
        ArrayNode listNode = objectMapper.createArrayNode();
        for (Long v : value) {
            listNode.add(v);
        }
        node.set(key, listNode);
        return node;
    }

    protected static LocalDate getUTCDate(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(ZoneId.of("UTC")).toLocalDate();
    }

}
