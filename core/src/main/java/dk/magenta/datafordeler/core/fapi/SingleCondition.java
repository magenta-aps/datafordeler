package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.exception.QueryBuildException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A single field somewhere in the query structure, being compared to one or more
 * input values.
 */
public class SingleCondition extends Condition {
    private final String left;
    private final ArrayList<Object> wildcardValues = new ArrayList<>();
    private final ArrayList<Object> staticValues = new ArrayList<>();
    private final Operator operator;
    private final String placeholder;

    public SingleCondition(MultiCondition parent, String left, Object right, Operator operator, String placeholder, Class type) throws QueryBuildException {
        this(parent, left, Collections.singletonList(right), operator, placeholder, type);
    }

    public SingleCondition(MultiCondition parent, String left, List<Object> right, Operator operator, String placeholder, Class type) throws QueryBuildException {
        super(parent);
        if (right.isEmpty()) {
            throw new QueryBuildException("No comparison value for " + left);
        }
        this.left = left;
        this.operator = operator;
        this.placeholder = placeholder;

        for (Object value : right) {
            if (value instanceof String && hasWildcard((String) value)) {
                this.wildcardValues.add(replaceWildcard(value));
            } else {
                Object v = castValue(type, value);
                this.staticValues.add(v);
            }
        }
    }

    public String toHql() {
        ArrayList<String> s = new ArrayList<>(this.wildcardValues.size() + this.staticValues.size());

        // Wildcards must be cast on database level - DB values may be non-strings, so cast them to string before comparison
        for (int i = 0; i < this.wildcardValues.size(); i++) {
            s.add("cast(" + this.left + " as string) like :" + this.placeholder + "_w" + i + " escape '\\'");
        }
        if (!this.staticValues.isEmpty()) {
            if (this.operator == Operator.EQ) {
                // If operator is equality, we're fine with doing a "where x in :list"
                s.add(this.left + " IN :" + this.placeholder);
            } else {
                // Inequalities must be handles separately, like "where x < :list_1 or x < :list_2"
                for (int i = 0; i < this.staticValues.size(); i++) {
                    s.add(this.left + " " + this.operator + " :" + this.placeholder + "_u" + i);
                }
            }
        }
        return (s.size() == 1) ? s.get(0) : s.stream().map(x -> "(" + x + ")").collect(Collectors.joining(" OR "));
    }

    public Map<String, Object> getParameters() {
        // Put values in a map with placeholders as keys.
        // Items that are treated together as a list ("where x in :list") go in a list.
        // The rest go separately. Order is important here, as placeholders are named based on iteration order
        HashMap<String, Object> parameters = new HashMap<>();
        for (int i = 0; i < this.wildcardValues.size(); i++) {
            String placeholder = this.placeholder + "_w" + i;
            Object value = this.wildcardValues.get(i);
            parameters.put(placeholder, value);
        }
        if (!this.staticValues.isEmpty()) {
            if (this.operator == Operator.EQ) {
                parameters.put(this.placeholder, this.staticValues);
            } else {
                for (int i = 0; i < this.staticValues.size(); i++) {
                    String placeholder = this.placeholder + "_u" + i;
                    Object value = this.staticValues.get(i);
                    parameters.put(placeholder, value);
                }
            }
        }
        return parameters;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 1;
    }

    private static boolean hasWildcard(String value) {
        return value.contains("*");
    }

    private static String replaceWildcard(Object value) {
        return ((String) value).replace("%", "\\%").replace("*", "%");
    }

    private static Object castValue(Class cls, Object value) {
        if (cls == null) {
            return value;
        }
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
        } else if (cls == OffsetDateTime.class && value instanceof String) {
            return OffsetDateTime.parse((String) value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else if (cls == LocalDateTime.class && value instanceof String) {
            return LocalDateTime.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return cls.cast(value);
    }
}
