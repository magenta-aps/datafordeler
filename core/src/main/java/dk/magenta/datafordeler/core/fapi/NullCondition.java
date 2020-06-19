package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.exception.QueryBuildException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A single field somewhere in the query structure, being compared to null
 */
public class NullCondition extends Condition {
    private String left;
    private Operator operator;

    public NullCondition(MultiCondition parent, String left, Operator operator) throws QueryBuildException {
        super(parent);
        this.left = left;
        if (operator == Operator.EQ || operator == Operator.NE) {
            this.operator = operator;
        } else {
            throw new QueryBuildException("Cannot use operator "+operator+" for null comparison");
        }
    }

    public String toHql() {
        StringJoiner s = new StringJoiner(" ");
        s.add(this.left);
        s.add("is");
        if (this.operator == Operator.NE) {
            s.add("not");
        }
        s.add("null");
        return s.toString();
    }

    public Map<String, Object> getParameters() {
        return Collections.emptyMap();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 1;
    }

}
