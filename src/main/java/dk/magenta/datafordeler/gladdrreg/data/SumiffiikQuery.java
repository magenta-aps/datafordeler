package dk.magenta.datafordeler.gladdrreg.data;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.core.util.ListHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
public abstract class SumiffiikQuery<E extends Entity> extends CommonQuery<E> {

    public static final String SUMIFFIIK = "sumiffiik";
    public static final String SUMIFFIIK_DOMAIN = "sumiffiik_domain";

    @QueryField(type = QueryField.FieldType.STRING, queryName = SUMIFFIIK)
    private String sumiffiik;

    @QueryField(type = QueryField.FieldType.STRING, queryName = SUMIFFIIK_DOMAIN)
    private String sumiffiik_domain;

    public String getSumiffiik() {
        return sumiffiik;
    }

    public void setSumiffiik(String sumiffiik) {
        this.sumiffiik = sumiffiik;
    }

    public String getSumiffiik_domain() {
        return sumiffiik_domain;
    }

    public void setSumiffiik_domain(String sumiffiik_domain) {
        this.sumiffiik_domain = sumiffiik_domain;
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put("sumiffiik", this.sumiffiik);
        map.put("sumiffiik_domain", this.sumiffiik_domain);
        return map;
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.sumiffiik != null) {
            lookupDefinition.put("sumiffiik", this.sumiffiik, String.class);
        }
        if (this.sumiffiik_domain != null) {
            lookupDefinition.put("sumiffiik_domain", this.sumiffiik_domain, String.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        super.setFromParameters(parameters);
        this.setSumiffiik(parameters.getFirst(SUMIFFIIK));
        this.setSumiffiik_domain(parameters.getFirst(SUMIFFIIK_DOMAIN));
    }
}
