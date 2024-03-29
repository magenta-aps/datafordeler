package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.exception.QueryBuildException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container class for conditions.
 * Conditions are basically arranged in a tree, with SingleConditions as leaves and MultiConditions as branch points,
 * holding info on whether its leaf nodes should be ANDed or ORed in hql
 * Traversing the tree with toHql() or getParameters() should yield the sum of leaf nodes, structured appropriately
 */
public class MultiCondition extends Condition {
    private final HashSet<Condition> conditions = new HashSet<>();
    private final String operator;
    private final HashMap<String, Integer> placeholderCounters = null;

    public MultiCondition() {
        this("AND");
    }

    public MultiCondition(MultiCondition parent) throws QueryBuildException {
        this(parent, "AND");
    }

    public MultiCondition(String operator) {
        this.operator = operator;
    }

    public MultiCondition(MultiCondition parent, String operator) throws QueryBuildException {
        super(parent);
        this.operator = operator;
    }

    public void add(Condition condition) {
        this.conditions.add(condition);
    }

    public void addAll(Collection<Condition> conditions) {
        for (Condition condition : conditions) {
            this.add(condition);
        }
    }

    public void remove(Condition condition) {
        this.conditions.remove(condition);
    }

    public String toHql() {
        // Join leaf nodes' hql together with our operator ("AND" or "OR")
        return this.conditions.stream()
                .filter(c -> !c.isEmpty())
                .map(c -> (c.size() == 1) ? c.toHql() : ("(" + c.toHql() + ")"))
                .collect(Collectors.joining(" " + this.operator + " "));
    }

    public boolean isEmpty() {
        for (Condition condition : this.conditions) {
            if (condition instanceof SingleCondition || condition instanceof NullCondition/* || condition instanceof JoinedQuery*/) {
                return false;
            } else if (condition instanceof MultiCondition) {
                MultiCondition m = (MultiCondition) condition;
                if (!m.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public int size() {
        return this.conditions.size();
    }


    public Map<String, Object> getParameters() {
        // Collect parameters from our leaf nodes
        HashMap<String, Object> parameters = new HashMap<>();
        for (Condition condition : this.conditions) {
            parameters.putAll(condition.getParameters());
        }
        return parameters;
    }

    public MultiCondition asMultiCondition() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiCondition that = (MultiCondition) o;
        return conditions.equals(that.conditions) && operator.equals(that.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditions, operator);
    }
}
