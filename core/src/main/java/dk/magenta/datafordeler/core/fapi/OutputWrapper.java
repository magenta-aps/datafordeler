package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class OutputWrapper<E extends IdentifiedEntity> {

    public enum Mode {
        RVD,
        RDV,
        DRV,
        LEGACY,
        DATAONLY
    }

    protected static HashMap<Class, OutputWrapper> wrapperMap = new HashMap<>();

    protected void register(Class entityClass) {
        System.out.println("Registering wrapper"+this+" for "+entityClass.getCanonicalName()+" in wrapperMap "+System.identityHashCode(wrapperMap));
        wrapperMap.put(entityClass, this);
    }

    // Override either of these
    public Object wrapResult(E input, BaseQuery query) {
        return null;
    }

    public Object wrapResult(E input, BaseQuery query, Mode mode) {
        return this.wrapResult(input, query);
    }

    public Object wrapResultSet(ResultSet<E> input, BaseQuery query, Mode mode) {
        return null;
    }

    public final List<Object> wrapResultSets(Collection<ResultSet<E>> input, BaseQuery query, Mode mode) {
        ArrayList<Object> result = new ArrayList<>();
        for (ResultSet<E> item : input) {
            if (item != null) {
                result.add(this.wrapResultSet(item, query, mode));
            }
        }
        return result;
    }

    public final List<Object> wrapResults(Collection<E> input, BaseQuery query, Mode mode) {
        ArrayList<Object> result = new ArrayList<>();
        for (E item : input) {
            if (item != null) {
                result.add(this.wrapResult(item, query, mode));
            }
        }
        return result;
    }

    public static class NodeWrapper {
        private final ObjectNode node;

        public NodeWrapper(ObjectNode node) {
            this.node = node;
        }

        public ObjectNode getNode() {
            return this.node;
        }

        public void put(String key, Boolean value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }

        public void put(String key, Short value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }

        public void put(String key, Integer value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }

        public void put(String key, Long value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }

        public void put(String key, String value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }

        public void putArray(String key, ArrayNode value) {
            if (value != null) {
                this.node.withArray(key).addAll(value);
            }
        }

        public void set(String key, JsonNode value) {
            if (value != null) {
                this.node.set(key, value);
            }
        }

        public void putPOJO(String key, Object value) {
            if (value != null) {
                this.node.putPOJO(key, value);
            }
        }
    }
}
