package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.exception.QueryBuildException;

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

    private MultiCondition parent;

    public Condition() {
    }

    public Condition(MultiCondition parent) throws QueryBuildException {
        for (Condition p = parent; p != null; p = p.parent) {
            if (p == this) {
                throw new QueryBuildException("Cyclic reference");
            }
        }
        this.parent = parent;
    }

    protected MultiCondition getParent() {
        return this.parent;
    }

    public abstract String toHql();

    public abstract Map<String, Object> getParameters();

    public abstract boolean isEmpty();

    public abstract int size();


    public MultiCondition asMultiCondition() {
        MultiCondition multiCondition;
        try {
            multiCondition = new MultiCondition(this.getParent());
        } catch (QueryBuildException e) {
            e.printStackTrace();
            return null;
        }
        this.getParent().add(multiCondition);
        this.getParent().remove(this);
        multiCondition.add(this);
        return multiCondition;
    }
}
