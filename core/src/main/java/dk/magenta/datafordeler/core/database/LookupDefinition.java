package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.fapi.BaseQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

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
public class LookupDefinition extends BaseLookupDefinition {

    protected Class<? extends DataItem> dataClass;

    public LookupDefinition(Class<? extends DataItem> dataClass) {
        this.dataClass = dataClass;
    }

    public LookupDefinition(BaseQuery query, Class<? extends DataItem> dataClass) {
        this(dataClass);
        this.query = query;
    }

    public LookupDefinition(Map<String, Object> map, Class<? extends DataItem> dataClass) {
        this(dataClass);
        this.putAll(map);
    }

    public Class<? extends DataItem> getDataClass() {
        return this.dataClass;
    }

    public boolean usingRVDModel() {
        return true;
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

        String whereContainer = entityKey + " %s IN (" +
                "SELECT " + entityKey + " FROM " + this.dataClass.getCanonicalName() + " " + dataItemKey +
                " JOIN " + dataItemKey + ".effects " + effectref +
                " JOIN " + effectref + ".registration " + registrationref +
                " JOIN " + registrationref + ".entity " + entityKey +
                " %s " +
                " WHERE %s" +
                ")";
        StringJoiner extraWhere = new StringJoiner(this.definitionsAnded ? " AND " : " OR ");
        for (FieldDefinition fieldDefinition : this.fieldDefinitions) {
            if (fieldDefinition.onIdentification()) {
                extraWhere.add("(" + (fieldDefinition.inverted ? "NOT ":"") + this.getHqlWherePart(dataItemKey, entityKey, fieldDefinition, false) + ")");
            } else if (fieldDefinition.onEntity()) {
                extraWhere.add("(" + (fieldDefinition.inverted ? "NOT ":"") + this.getHqlWherePart(dataItemKey, entityKey, fieldDefinition, true) + ")");
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
                String inversion = fieldDefinition.inverted ? "NOT" : "";
                extraWhere.add(String.format(whereContainer, inversion, join, where));
            }
        }

        if (extraWhere.length() > 0) {
            return prefix + " " + extraWhere.toString();
        }
        return "";
    }

}
