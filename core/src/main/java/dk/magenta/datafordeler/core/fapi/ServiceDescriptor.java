package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.util.StringJoiner;

import java.lang.reflect.Field;
import java.util.*;

public abstract class ServiceDescriptor {

    public class ServiceQueryField {

        @JsonProperty
        public ArrayList<String> names = new ArrayList<>();

        @JsonProperty
        public String type;

        public String namesAsString() {
            return new StringJoiner("|", "[", "]").addAll(this.names).toString();
        }

    }

    private final Plugin plugin;
    private final String serviceName;
    private final String serviceAddress;
    private final Class<? extends BaseQuery> queryClass;


    public ServiceDescriptor(Plugin plugin, String serviceName, String serviceAddress, Class<? extends BaseQuery> queryClass) {
        this.plugin = plugin;
        this.serviceName = serviceName;
        if (serviceAddress.endsWith("/")) {
            serviceAddress = serviceAddress.substring(0, serviceAddress.length() - 1);
        }
        this.serviceAddress = serviceAddress;
        this.queryClass = queryClass;
    }

    @JsonIgnore
    public Plugin getPlugin() {
        return this.plugin;
    }

    @JsonProperty(value = "service_name")
    public String getServiceName() {
        return this.serviceName;
    }

    @JsonProperty(value = "metadata_url")
    public String getMetaAddress() {
        return this.serviceAddress;
    }

    @JsonIgnore
    public String getServiceAddress() {
        return this.serviceAddress;
    }

    @JsonProperty(value = "type")
    public abstract String getType();

    @JsonProperty(value = "search_queryfields")
    public List<ServiceQueryField> getFields() {
        ArrayList<ServiceQueryField> fields = new ArrayList<>();
        for (Class queryClass = this.queryClass; queryClass != null; queryClass = queryClass.getSuperclass()) {
            for (Field field : getAllFields(queryClass)) {
                QueryField qf = field.getAnnotation(QueryField.class);
                ServiceQueryField queryField = new ServiceQueryField();
                if (!QueryField.EMPTY.equals(qf.queryName())) {
                    queryField.names.add(qf.queryName());
                }
                if (qf.queryNames().length > 0) {
                    queryField.names.addAll(Arrays.asList(qf.queryNames()));
                }
                queryField.type = qf.type().name().toLowerCase();
                fields.add(queryField);
            }
        }
        return fields;
    }

    protected static Set<Field> getAllFields(Class queryClass) {
        HashSet<Field> fields = new HashSet<>();
        if (queryClass != null) {
            if (queryClass != Query.class) {
                fields.addAll(getAllFields(queryClass.getSuperclass()));
            }
            for (Field field : queryClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(QueryField.class)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

}
