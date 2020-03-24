package dk.magenta.datafordeler.plugindemo.fapi;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.plugindemo.model.DemoDataRecord;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DemoRecordQuery extends BaseQuery {

    public static final String POSTNR = "postnr";
    public static final String BYNAVN = "bynavn";

    public DemoRecordQuery(){}

    @QueryField(type = QueryField.FieldType.INT, queryName = POSTNR)
    private String postnr;

    @QueryField(type = QueryField.FieldType.STRING, queryName = BYNAVN)
    private String bynavn;

    public String getPostnr() {
        return postnr;
    }

    public void setPostnr(int postnr) {
        this.postnr = Integer.toString(postnr);
    }

    public void setPostnr(String postnr) {
        this.postnr = postnr;
        if (postnr != null) {
            this.increaseDataParamCount();
        }
    }

    public String getBynavn() {
        return bynavn;
    }

    public void setBynavn(String bynavn) {
        this.bynavn = bynavn;
        if (bynavn != null) {
            this.increaseDataParamCount();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("postnr", this.postnr);
        map.put("bynavn", this.bynavn);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = new BaseLookupDefinition();
        if (this.postnr != null) {
            lookupDefinition.put(BaseLookupDefinition.entityref + BaseLookupDefinition.separator + "postnr", this.postnr, Integer.class);
        }
        if (this.bynavn != null) {
            lookupDefinition.put(DemoEntityRecord.DB_FIELD_NAME + BaseLookupDefinition.separator + DemoDataRecord.DB_FIELD_NAME, this.bynavn, String.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap listHashMap) {
        this.setPostnr(listHashMap.get(POSTNR, 0));
        this.setBynavn(listHashMap.get(BYNAVN, 0));
    }

    @Override
    public String getEntityClassname() {
        return Entity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "entity";
    }

    @Override
    protected Map<String, String> joinHandles() {
        return Collections.emptyMap();
    }

    @Override
    protected void setupConditions() {
        // this.addCondition();
    }

}
