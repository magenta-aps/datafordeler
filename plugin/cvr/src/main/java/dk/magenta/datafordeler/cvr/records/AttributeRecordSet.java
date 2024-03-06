package dk.magenta.datafordeler.cvr.records;

import dk.magenta.datafordeler.core.util.BitemporalityComparator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;
import org.springframework.data.util.Pair;


public class AttributeRecordSet extends HashSet<AttributeRecord> {

    public AttributeRecordSet(Collection<? extends AttributeRecord> c) {
        super(c);
    }

    public List<AttributeRecord> byType(String type) {
        ArrayList<AttributeRecord> filtered = new ArrayList<>();
        for (AttributeRecord attributeRecord : this) {
            System.out.println("type: "+attributeRecord.getType());
            if (Objects.equals(attributeRecord.getType(), type)) {
                filtered.add(attributeRecord);
            }
        }
        return filtered;
    }

    public List<AttributeValueRecord> currentValueRecordsByType(String type) {
        ArrayList<AttributeValueRecord> values = new ArrayList<>();
        for (AttributeRecord attributeRecord : this.byType(type)) {
            values.addAll(attributeRecord.getValues().current());
        }
        return values;
    }

    public List<Object> getCurrentAttributeValues(String type, String valueType) {
        return this.getCurrentAttributeValues(type, valueType, true);
    }

    public List<Object> getCurrentAttributeValues(String type, String valueType, boolean parse) {
        ArrayList<Object> values = new ArrayList<>();
        for (AttributeRecord attributeRecord : this.byType(type)) {
            System.out.println("valuetype: "+attributeRecord.getValueType());
            if (valueType == null || attributeRecord.getValueType().equals(valueType)) {
                for (AttributeValueRecord valueRecord : attributeRecord.getValues().current()) {
                    String stringValue = valueRecord.getValue();
                    if (parse) {
                        Object value = null;
                        switch (attributeRecord.getValueType()) {
                            case "boolean":
                                value = Boolean.parseBoolean(stringValue);
                                break;
                            case "integer":
                                value = Integer.parseInt(stringValue);
                                break;
                            case "decimal":
                                value = Double.parseDouble(stringValue);
                                break;
                            case "date":
                                value = LocalDate.parse(stringValue);
                                break;
                            case "string":
                                value = stringValue;
                                break;
                        }
                        values.add(value);
                    } else {
                        values.add(stringValue);
                    }
                }
            }
        }
        return values;
    }

}
