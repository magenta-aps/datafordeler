package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.plugin.Plugin;

@Deprecated
public class SoapServiceDescriptor extends ServiceDescriptor {

    public SoapServiceDescriptor(Plugin plugin, String serviceName, String metaAddress, Class<? extends BaseQuery> queryClass) {
        super(plugin, serviceName, metaAddress, queryClass);
    }

    @JsonProperty(value = "wsdl_url")
    public String getWsdlAddress() {
        return this.getServiceAddress() + "?wsdl";
    }

    @Override
    @JsonProperty(value = "type")
    public String getType() {
        return "soap";
    }

}
