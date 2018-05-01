package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.fapi.Query;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A LookupDefinition is a way of defining how to look up entities based on the data hierarchy within them.
 * Since the data in an entity is spread out over multiple tables, it is difficult to do a database select
 * on a field when you don't know where it is. So, DataItem subclasses and Query subclasses should implement
 * a method returning a LookupDefinition.
 *
 * Examples of FieldDefinitions:
 *
 * path: coreData.attribute, value: "123", class: java.lang.Integer
 * This means that for a given DataItem table, join its "coreData" table and match on attribute = 123, where
 * the value has been cast to an integer
 *
 * This is done because queries insert values as strings, possibly containing wildcards, but if there is no wildcard
 * we must cast to the correct type before the value is inserted in hibernate
 *
 * path: $.foo, value: "42", class: java.lang.Integer
 * This means that we should look in the Entity table (instead of the DataItem table) for a match on foo = 42
 *
 * All contained FieldDefinitions will be AND'ed together, and if a FieldDefinition value
 * in the resulting query
 *
 *
 *
 * Full usage example:
 *
 * LookupDefinition lookupDefinition = new LookupDefinition(Collections.singletonMap("foo.bar", 42))
 *
 *
 *
 * Class<D> dClass = FooDataItem.class
 * String dataItemKey = "d";
 * String entityKey = "e";
 * String join = lookupDefinition.getHqlJoinString(dataItemKey, entityKey);
 * String where = lookupDefinition.getHqlWhereString(dataItemKey, entityKey);
 *  org.hibernate.query.Query<D> query = session.createQuery(
 *      "select " + dataItemKey + " from " + dClass.getCanonicalName() + " "+dataItemKey+
 *      " " + join + " where " + where, dClass
 *      );
 * HashMap<String, Object> parameters = lookupDefinition.getHqlParameters(dataItemKey, entityKey);
 * for (String key : parameters.keySet()) {
 *    query.setParameter(key, parameters.get(key));
 * }
 *
 * This would look up items of the FooDataItem class where the subtable foo has a variable bar with value 42.
 * See also the various uses in QueryManager, which perform database lookups based on LookupDefinitions from
 * Query and DataItem objects
 */
public class LookupDefinition {

    private Query query = null;
    private boolean matchNulls = false;
    public static final String separator = ".";
    public static final String entityref = "$";
    public static final String registrationref = "r";
    public static final String effectref = "v";
    public static final char escape = '\\';
    protected static final String quotedSeparator = Pattern.quote(separator);
    protected ArrayList<FieldDefinition> fieldDefinitions = new ArrayList<>();
    protected Class<? extends DataItem> dataClass;
    private int nextId = 0;
    private boolean definitionsAnded = true;

    public enum Operator {
        EQ("="),
        LT("<"),
        GT(">"),
        LTE("<="),
        GTE(">="),
        NE("!=");
        private final String name;
        Operator(String s) {
            name = s;
        }
        public String toString() {
            return this.name;
        }
    }

    public class FieldDefinition {

        public String path;
        public Object value;
        public Class type;
        public Operator operator = Operator.EQ;
        public int id;
        public HashSet<FieldDefinition> anded = new HashSet<>();
        public HashSet<FieldDefinition> ored = new HashSet<>();

        public FieldDefinition(String path, Object value) {
            this(path, value, value != null ? value.getClass() : null, Operator.EQ);
        }
        public FieldDefinition(String path, Object value, Class type) {
            this(path, value, type, Operator.EQ);
        }
        public FieldDefinition(String path, Object value, Class type, Operator operator) {
            this.path = path;
            this.value = value;
            this.type = type;
            this.operator = operator;
            this.id = LookupDefinition.this.nextId++;
        }

        public boolean onEntity() {
            return this.path.startsWith(entityref);
        }

        public String getOperatorSign() {
            return this.operator.toString();
        }
/*
        public FieldDefinition and(FieldDefinition next) {
            FieldDefinition lastInChain = this;
            while (lastInChain.nextInChain != null) {
                lastInChain = lastInChain.nextInChain;
            }
            lastInChain.nextInChain = next;
            return next;
        }*/
        public void and(FieldDefinition other) {
            this.anded.add(other);
        }
        public void or(FieldDefinition other) {
            this.ored.add(other);
        }

        public FieldDefinition and(String path, Object value, Class fieldClass) {
            FieldDefinition other = new FieldDefinition(path, value, fieldClass, Operator.EQ);
            this.and(other);
            return other;
        }

        public FieldDefinition and(String path, Object value, Class fieldClass, Operator operator) {
            FieldDefinition other = new FieldDefinition(path, value, fieldClass, operator);
            this.and(other);
            return other;
        }

        public FieldDefinition or(String path, Object value, Class fieldClass) {
            FieldDefinition other = new FieldDefinition(path, value, fieldClass, Operator.EQ);
            this.or(other);
            return other;
        }

        public FieldDefinition or(String path, Object value, Class fieldClass, Operator operator) {
            FieldDefinition other = new FieldDefinition(path, value, fieldClass, operator);
            this.or(other);
            return other;
        }

        private Map<String, Object> getParameterMap(String rootKey, String entityKey) {
            HashMap<String, Object> map = new HashMap<>();
            String path = this.path;
            String parameterPath = LookupDefinition.this.getParameterPath(rootKey, entityKey, path) + "_" + this.id;
            Object value = this.value;
            Class type = this.type;
            if (value != null) {
                if (value instanceof List) {
                    List list = (List) value;
                    HashSet<Object> nonWildcardItems = new HashSet<>();
                    for (int i=0; i<list.size(); i++) {
                        Object item = list.get(i);
                        if (parameterValueWildcard(item)) {
                            map.put(parameterPath + "_" + i, replaceWildcard(item));
                        } else if (!this.getOperatorSign().equals("=")) {
                            map.put(parameterPath + "_" + i, castValue(type, item));
                        } else {
                            nonWildcardItems.add(castValue(type, item));
                        }
                    }
                    if (!nonWildcardItems.isEmpty()) {
                        map.put(parameterPath + "_" + "list", nonWildcardItems);
                    }
                } else {
                    if (parameterValueWildcard(value)) {
                        map.put(parameterPath, replaceWildcard(value));
                    } else {
                        map.put(parameterPath, castValue(type, value));
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

    public LookupDefinition(Class<? extends DataItem> dataClass) {
        this.dataClass = dataClass;
    }

    public LookupDefinition(Query query, Class<? extends DataItem> dataClass) {
        this(dataClass);
        this.query = query;
    }

    public LookupDefinition(Map<String, Object> map, Class<? extends DataItem> dataClass) {
        this(dataClass);
        this.putAll(map);
    }

    public void orDefinitions() {
        this.definitionsAnded = false;
    }

    public void put(String path, Object value) {
        if (value != null) {
            if (value instanceof Collection) {
                for (Object member : (Collection) value) {
                    this.put(path, member);
                }
            } else {
                this.put(path, value, value.getClass());
            }
        }
    }

    public FieldDefinition put(String path, Object value, Class fieldClass) {
        FieldDefinition fieldDefinition = new FieldDefinition(path, value, fieldClass);
        this.fieldDefinitions.add(fieldDefinition);
        return fieldDefinition;
    }

    public FieldDefinition put(String path, Object value, Class fieldClass, Operator operator) {
        FieldDefinition fieldDefinition = new FieldDefinition(path, value, fieldClass, operator);
        this.fieldDefinitions.add(fieldDefinition);
        return fieldDefinition;
    }

    public void putAll(Map<String, Object> map) {
        for (String key : map.keySet()) {
            this.put(key, map.get(key));
        }
    }

    /**
     * Under a given top key, put a map of lookups into our hashmap.
     * For example: putAll("abc", {"def": 23, "ghi": 42}) will result in
     * {"abc.def": 23, "abc.ghi": 42}
     * @param topKey
     * @param map
     */
    public void putAll(String topKey, Map<String, Object> map) {
        for (String key : map.keySet()) {
            this.put(topKey + separator + key, map.get(key));
        }
    }

    public void setMatchNulls(boolean matchNulls) {
        this.matchNulls = matchNulls;
    }

    public Class<? extends DataItem> getDataClass() {
        return this.dataClass;
    }





    /**
     * Obtain the table join string, including all tables that have been added to the LookupDefinition
     * @param rootKey Root key, denoting the baseline for the join. This is most often the hql identifier
     *                for the DataItem table: if the HQL so far is "SELECT e from FooEntity JOIN e.registrations r JOIN r.effects v JOIN v.dataItems d",
     *                then "d" would be the rootKey to look up paths within the dataItem table
     * @param entityKey Entity key, denoting the hql identifier for the Entity table. In the above example, "e" would be the entityKey
     * @return join string, e.g. "JOIN d.abc d_abc JOIN e.foo e_foo"
     */
    public String getHqlJoinString(String rootKey, String entityKey) {
        ArrayList<String> joinTables = new ArrayList<>();
        for (FieldDefinition definition : this.fieldDefinitions) {
            joinTables.addAll(this.getHqlJoinParts(rootKey, definition));
        }
        if (!joinTables.isEmpty()) {
            String joinString = " JOIN ";
            StringJoiner s = new StringJoiner(joinString);
            for (String table : joinTables) {
                s.add(table);
            }
            return joinString + s.toString();
        }
        return "";
    }

    public boolean usingRVDModel() {
        return true;
    }

    protected ArrayList<String> getHqlJoinParts(String rootKey, FieldDefinition fieldDefinition) {
        ArrayList<String> joinTables = new ArrayList<>();
        String path = fieldDefinition.path;
        if (path.contains(separator)) {
            String[] parts = path.split(quotedSeparator);
            String firstPart = parts[0];
            if (firstPart.equals(entityref) || firstPart.equals(registrationref) || firstPart.equals(effectref)) {
                parts = Arrays.copyOfRange(parts, 1, parts.length);
            }

            StringBuilder fullParts = new StringBuilder(rootKey);
            for (int i = 0; i<parts.length - 1; i++) {
                String part = parts[i];
                String beforeAppend = fullParts.toString();
                fullParts.append("_").append(part);
                String joinEntry = beforeAppend + "." + part + " " + fullParts;
                if (!joinTables.contains(joinEntry)) {
                    joinTables.add(joinEntry);
                }
            }
        }
        if (!fieldDefinition.anded.isEmpty()) {
            for (FieldDefinition other : fieldDefinition.anded) {
                joinTables.addAll(this.getHqlJoinParts(rootKey, other));
            }
        }
        if (!fieldDefinition.ored.isEmpty()) {
            for (FieldDefinition other : fieldDefinition.ored) {
                joinTables.addAll(this.getHqlJoinParts(rootKey, other));
            }
        }
        return joinTables;
    }

    public String getHqlWhereString(String rootKey, String entityKey) {
        return this.getHqlWhereString(rootKey, entityKey, " AND ");
    }

    /**
     * Obtain the table where string, specifying the hql WHERE statement for each value in the LookupDefinition
     * Used in conjunction with getHqlJoinString (and using the same input keys).
     *
     * @param dataItemKey Root key, denoting the baseline for the join. This is most often the hql identifier
     *                for the DataItem table: if the HQL so far is "SELECT e from FooEntity JOIN e.registrations r JOIN r.effects v JOIN v.dataItems d",
     *                then "d" would be the rootKey to look up paths within the dataItem table
     * @param entityKey Entity key, denoting the hql identifier for the Entity table. In the above example, "e" would be the entityKey
     * @return where string, e.g. " AND d_abc.def = :d_abc_def AND d_abc.ghi = :d_abc_ghi
     */
    public String getHqlWhereString(String dataItemKey, String entityKey, String prefix) {

        String whereContainer = entityKey + " IN (" +
                "SELECT " + entityKey + " FROM " + this.dataClass.getCanonicalName() + " " + dataItemKey +
                " JOIN " + dataItemKey + ".effects " + effectref +
                " JOIN " + effectref + ".registration " + registrationref +
                " JOIN " + registrationref + ".entity " + entityKey +
                " %s " +
                " WHERE %s" +
                ")";
        StringJoiner extraWhere = new StringJoiner(this.definitionsAnded ? " AND " : " OR ");
        for (FieldDefinition fieldDefinition : this.fieldDefinitions) {
            if (fieldDefinition.onEntity()) {
                extraWhere.add("(" + this.getHqlWherePart(dataItemKey, entityKey, fieldDefinition, true) + ")");
            } else {
                List<String> joins = this.getHqlJoinParts(dataItemKey, fieldDefinition);
                String join = "";
                if (joins != null && !joins.isEmpty()) {
                    StringJoiner sj = new StringJoiner(" JOIN ", " JOIN ", "");
                    for (String s : joins) {
                        sj.add(s);
                    }
                    join = sj.toString();
                }

                String where = "(" + this.getHqlWherePart(dataItemKey, entityKey, fieldDefinition, true) + ")";
                extraWhere.add(String.format(whereContainer, join, where));
            }
        }

        if (extraWhere.length() > 0) {
            return prefix + " " + extraWhere.toString();
        }
        return "";
    }

    public List<String> getHqlWhereParts(String rootKey, String entityKey, boolean joinedTables) {
        ArrayList<String> strings = new ArrayList<>();
        for (FieldDefinition definition : this.fieldDefinitions) {
            String part = this.getHqlWherePart(rootKey, entityKey, definition, joinedTables);
            if (part != null) {
                strings.add(part);
            }
        }
        return strings;
    }
    protected String getHqlWherePart(String rootKey, String entityKey, FieldDefinition fieldDefinition) {
        return this.getHqlWherePart(rootKey, entityKey, fieldDefinition, false);
    }

    protected String getHqlWherePart(String rootKey, String entityKey, FieldDefinition fieldDefinition, boolean joinedTable) {
        String where = null;
        String path = fieldDefinition.path;
        String parameterPath = this.getParameterPath(rootKey, entityKey, path) + "_" + fieldDefinition.id;
        Object value = fieldDefinition.value;
        String variablePath = this.getVariablePath(rootKey, entityKey, path);
        if (joinedTable) {
            int lastIndex = variablePath.lastIndexOf(".");
            if (lastIndex != -1) {
                variablePath = variablePath.substring(0, lastIndex).replace('.', '_') + variablePath.substring(lastIndex); // Replace all '.' with '_' except the last one
            }
        }

        if (value == null) {
            if (this.matchNulls) {
                if (fieldDefinition.operator == Operator.EQ) {
                    where = variablePath + " is null";
                } else if (fieldDefinition.operator == Operator.NE) {
                    where = variablePath + " is not null";
                }
            }
        } else if (value instanceof List) {
            List list = (List) value;
            StringJoiner or = new StringJoiner(" OR ");
            boolean hasNonWildcardItems = false;
            for (int i = 0; i < list.size(); i++) {
                if (parameterValueWildcard(list.get(i))) {
                    or.add("cast(" + variablePath + " as string) like :" + parameterPath + "_" + i + " escape '" + LookupDefinition.escape + "'");
                } else if (!fieldDefinition.getOperatorSign().equals("=")) {
                    or.add(variablePath + " " + fieldDefinition.getOperatorSign() + " :" + parameterPath + "_" + i);
                } else {
                    hasNonWildcardItems = true;
                }
            }
            if (hasNonWildcardItems) {
                or.add(variablePath + " IN (:" + parameterPath + "_" + "list)");
            }
            if (or.length() > 0) {
                where = "(" + or.toString() + ")";
            }
        } else {
            if (parameterValueWildcard(value)) {
                where = "cast(" + variablePath + " as string) like :" + parameterPath + " escape '" + LookupDefinition.escape + "'";
            } else {
                where = variablePath + " " + fieldDefinition.getOperatorSign() + " :" + parameterPath;
            }
        }
        if (!fieldDefinition.anded.isEmpty()) {
            StringJoiner and = new StringJoiner(" AND ");
            and.add(where);
            for (FieldDefinition other : fieldDefinition.anded) {
                and.add(this.getHqlWherePart(rootKey, entityKey, other, joinedTable));
            }
            where = "(" + and.toString() + ")";
        }
        if (!fieldDefinition.ored.isEmpty()) {
            StringJoiner or = new StringJoiner(" OR ");
            or.add(where);
            for (FieldDefinition other : fieldDefinition.ored) {
                or.add(this.getHqlWherePart(rootKey, entityKey, other, joinedTable));
            }
            where = "(" + or.toString() + ")";
        }


        return where;
    }


    /**
     * Convert keys to a path.
     * @param rootKey table root key
     * @param entityKey entity key
     * @param key dot-spearated path, e.g. foo.bar.baz
     * @return converted path. e.g. (rootKey: "d", entityKey: "e", key: "foo.bar.baz") => "d_foo_bar_baz" or (rootKey: "d", entityKey: "e", key: "$.bar.baz") => "e_bar_baz"
     */
    protected String getVariablePath(String rootKey, String entityKey, String key) {
        int separatorIndex = key.indexOf(separator);
        String first = separatorIndex != -1 ? key.substring(0, separatorIndex) : key;
        String object = rootKey;
        if (first.equals(entityref)) {
            object = entityKey;
            key = key.substring(separatorIndex + 1);
        } else if (first.equals(registrationref)) {
            object = registrationref;
            key = key.substring(separatorIndex + 1);
        } else if (first.equals(effectref)) {
            object = effectref;
            key = key.substring(separatorIndex + 1);
        }
        object += "." + key;
        return object;
    }

    /**
     * Convert keys to a path.
     * @param rootKey table root key
     * @param entityKey entity key
     * @param key dot-spearated path, e.g. foo.bar.baz
     * @return converted path. e.g. (rootKey: "d", entityKey: "e", key: "foo.bar.baz") => "d_foo_bar_baz" or (rootKey: "d", entityKey: "e", key: "$.bar.baz") => "e_bar_baz"
     */
    protected String getParameterPath(String rootKey, String entityKey, String key) {
        //int separatorIndex = key.indexOf(separator);
        //String first = separatorIndex != -1 ? key.substring(0, separatorIndex) : key;
        String object = this.getVariablePath(rootKey, entityKey, key);
        /*if (first.equals(entityref)) {
            object = entityKey;
            key = key.substring(separatorIndex + 1);
        }*/
        object = object.replaceAll(quotedSeparator, "_");
        return object;
    }

    /**
     * Get the substring after the last separator. If no separator is found, the whole key
     * @param key
     * @return
     */
    protected String getFinal(String key) {
        if (key.contains(separator)) {
            return key.substring(key.lastIndexOf(separator) + 1);
        } else {
            return key;
        }
    }

    /**
     * Obtain the values defined in the LookupDefinition, with their paths normalized to match what would be output by getWhereString()
     * @param rootKey Root key, denoting the baseline for the join. This is most often the hql identifier
     *                for the DataItem table: if the HQL so far is "SELECT e from FooEntity JOIN e.registrations r JOIN r.effects v JOIN v.dataItems d",
     *                then "d" would be the rootKey to look up paths within the dataItem table
     * @param entityKey Entity key, denoting the hql identifier for the Entity table. In the above example, "e" would be the entityKey
     * @return Map to be used for filling the query parameters. E.g. {"d_abc_def": 23, "d_abc_ghi": 42}
     */
    public HashMap<String, Object> getHqlParameters(String rootKey, String entityKey) {
        HashMap<String, Object> map = new HashMap<>();
        for (FieldDefinition definition : this.fieldDefinitions) {
            map.putAll(definition.getParameterMap(rootKey, entityKey));
        }
        return map;
    }

    private static Object castValue(Class cls, Object value) {
        if (cls == null) {return value;}
        if ((cls == Long.TYPE || cls == Long.class) && !(value instanceof Long)) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        } else if ((cls == Integer.TYPE || cls == Integer.class) && !(value instanceof Integer)) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } else if ((cls == Boolean.TYPE || cls == Boolean.class) && !(value instanceof Boolean)) {
            return Query.booleanFromString(value.toString());
        } else if ((cls == UUID.class) && !(value instanceof UUID)) {
            return UUID.fromString(value.toString());
        }
        return cls.cast(value);
    }

    private static boolean parameterValueWildcard(Object value) {
        if (value instanceof String) {
            String stringValue = (String) value;
            //return stringValue.startsWith("*") || stringValue.endsWith("*");
            return stringValue.contains("*");
        }
        return false;
    }

    private static String replaceWildcard(Object item) {
        return ((String) item).replace("%", escape + "%").replace("*", "%");
    }

}
