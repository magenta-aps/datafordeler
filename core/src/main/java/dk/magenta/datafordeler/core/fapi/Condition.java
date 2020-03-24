package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.exception.QueryBuildException;

import java.util.List;
import java.util.Map;

public abstract class Condition {

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

    private Condition parent;

    public Condition() {
    }

    public Condition(Condition parent) throws QueryBuildException {
        for (Condition p = parent; p != null; p = p.parent) {
            if (p == this) {
                throw new QueryBuildException("Cyclic reference");
            }
        }
        this.parent = parent;
    }

    public abstract String toHql();

    public abstract Map<String, Object> getParameters();
}
