package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;

import java.util.*;
import java.util.stream.Collectors;


public class MultiClassLookupDefinition extends BaseLookupDefinition {

    private HashSet<MultiClassQuery.SubQuery> subQueries;

    public MultiClassLookupDefinition(Set<MultiClassQuery.SubQuery> subQueries) {
        this.subQueries = new HashSet<>(subQueries);
    }

    public String getHqlWhereString(String rootKey, String entityKey) {
        StringJoiner wheres = new StringJoiner(" AND ");
        for (MultiClassQuery.SubQuery s : this.subQueries) {
            String where = s.getLookupDefinition().getHqlWhereString(s.ident, s.ident, "");
            if (where != null && !where.isEmpty()) {
                wheres.add(where);
            }
            if (s.joiner != null && !s.joiner.isEmpty()) {
                wheres.add(s.joiner);
            }
        }
        return wheres.toString();
    }

    public String getHqlJoinString(String rootKey, String entityKey) {
        StringJoiner joins = new StringJoiner(" ");
        for (MultiClassQuery.SubQuery s : this.subQueries) {
            String join = s.getLookupDefinition().getHqlJoinString(s.ident, s.ident);
            if (join != null && !join.isEmpty()) {
                joins.add(join);
            }
        }
        return joins.toString();
    }

    public HashMap<String, Object> getHqlParameters(String rootKey, String entityKey) {
        HashMap<String, Object> map = new HashMap<>();
        for (MultiClassQuery.SubQuery s : this.subQueries) {
            map.putAll(s.getLookupDefinition().getHqlParameters(s.ident, s.ident));
        }
        return map;
    }

    public String getIdents() {
        return this.subQueries.stream().map(s -> s.ident).collect(Collectors.joining(","));
    }
    public String getTables() {
        return this.subQueries.stream().map(s -> s.entityClassname + " " + s.ident).collect(Collectors.joining(","));
    }

}
