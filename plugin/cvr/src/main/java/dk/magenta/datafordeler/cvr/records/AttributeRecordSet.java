package dk.magenta.datafordeler.cvr.records;

import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.cvr.RecordSet;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class AttributeRecordSet extends RecordSet<AttributeRecord> {

    public AttributeRecordSet(Collection<AttributeRecord> c) {
        super(new HashSet<>(c));
    }

    public List<AttributeRecord> byType(String type) {
        ArrayList<AttributeRecord> filtered = new ArrayList<>();
        for (AttributeRecord attributeRecord : this) {
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
            if (valueType == null || attributeRecord.getValueType().equals(valueType)) {
                // Because CVR data is yet reg-unclosed, we can have valueA (effect open) superseded by valueB (effect closed)
                // and we should only consider the latest registration(s) (valueB)
                List<AttributeValueRecord> currentValues = attributeRecord.getValues().currentRegistration();
                if (currentValues.size() > 1) {
                    OffsetDateTime latestRegistration = currentValues.stream().map(CvrBitemporalRecord::getRegistrationFrom).max(Comparator.naturalOrder()).get();
                    currentValues = currentValues.stream().filter(v -> Equality.equal(v.getRegistrationFrom(), latestRegistration)).collect(Collectors.toList());
                }
                // Taking only the most recently registered, filter out any with expired effect. Now we have zero or one values
                currentValues = currentValues.stream().filter(v -> v.hasEffectAt(OffsetDateTime.now())).collect(Collectors.toList());
                if (!currentValues.isEmpty()) {
                    String stringValue = currentValues.get(0).getValue();
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
