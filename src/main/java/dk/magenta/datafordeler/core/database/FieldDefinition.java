package dk.magenta.datafordeler.core.database;

import java.util.*;

public class FieldDefinition {

    public String path;
    public Object value;
    public Class type;
    public BaseLookupDefinition.Operator operator = BaseLookupDefinition.Operator.EQ;
    public int id;
    public HashSet<FieldDefinition> anded = new HashSet<>();
    public HashSet<FieldDefinition> ored = new HashSet<>();
    public boolean inverted = false;

    public FieldDefinition(String path, Object value) {
        this(path, value, value != null ? value.getClass() : null, BaseLookupDefinition.Operator.EQ);
    }

    public FieldDefinition(String path, Object value, Class type) {
        this(path, value, type, BaseLookupDefinition.Operator.EQ);
    }

    public FieldDefinition(String path, Object value, Class type, BaseLookupDefinition.Operator operator) {
        this.path = path;
        this.value = value;
        this.type = type;
        this.operator = operator;
    }


    public boolean onEntity() {
        return this.path.startsWith(BaseLookupDefinition.entityref);
    }

    public boolean onIdentification() {
        return this.path.startsWith(BaseLookupDefinition.entityref + BaseLookupDefinition.separator + Entity.DB_FIELD_IDENTIFICATION);
    }

    public String getOperatorSign() {
        return this.operator.toString();
    }

    public void and(FieldDefinition other) {
        this.anded.add(other);
    }
    public void or(FieldDefinition other) {
        this.ored.add(other);
    }

    public void invert() {
        this.inverted = true;
    }

    public FieldDefinition and(String path, Object value, Class fieldClass) {
        FieldDefinition other = new FieldDefinition(path, value, fieldClass, BaseLookupDefinition.Operator.EQ);
        this.and(other);
        return other;
    }

    public FieldDefinition and(String path, Object value, Class fieldClass, BaseLookupDefinition.Operator operator) {
        FieldDefinition other = new FieldDefinition(path, value, fieldClass, operator);
        this.and(other);
        return other;
    }

    public FieldDefinition or(String path, Object value, Class fieldClass) {
        FieldDefinition other = new FieldDefinition(path, value, fieldClass, BaseLookupDefinition.Operator.EQ);
        this.or(other);
        return other;
    }

    public FieldDefinition or(String path, Object value, Class fieldClass, BaseLookupDefinition.Operator operator) {
        FieldDefinition other = new FieldDefinition(path, value, fieldClass, operator);
        this.or(other);
        return other;
    }

    public Map<String, Object> getParameterMap(String rootKey, String entityKey) {
        HashMap<String, Object> map = new HashMap<>();
        String path = this.path;
        String parameterPath = BaseLookupDefinition.getParameterPath(rootKey, entityKey, path) + "_" + this.id;
        Object value = this.value;
        Class type = this.type;
        if (value != null) {
            if (value instanceof List) {
                List list = (List) value;
                HashSet<Object> nonWildcardItems = new HashSet<>();
                for (int i=0; i<list.size(); i++) {
                    Object item = list.get(i);
                    if (BaseLookupDefinition.parameterValueWildcard(item)) {
                        map.put(parameterPath + "_" + i, BaseLookupDefinition.replaceWildcard(item));
                    } else if (!this.getOperatorSign().equals("=")) {
                        map.put(parameterPath + "_" + i, BaseLookupDefinition.castValue(type, item));
                    } else {
                        nonWildcardItems.add(BaseLookupDefinition.castValue(type, item));
                    }
                }
                if (!nonWildcardItems.isEmpty()) {
                    map.put(parameterPath + "_" + "list", nonWildcardItems);
                }
            } else {
                if (BaseLookupDefinition.parameterValueWildcard(value)) {
                    map.put(parameterPath, BaseLookupDefinition.replaceWildcard(value));
                } else {
                    map.put(parameterPath, BaseLookupDefinition.castValue(type, value));
                }
            }
        }
        for (FieldDefinition other : this.anded) {
            map.putAll(other.getParameterMap(rootKey, entityKey));
        }
        for (FieldDefinition other : this.ored) {
            map.putAll(other.getParameterMap(rootKey, entityKey));
        }
        return map;
    }
}
