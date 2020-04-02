package dk.magenta.datafordeler.plugindemo.fapi;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.plugindemo.model.DemoDataRecord;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DemoRecordQuery extends BaseQuery {

    public static final String POSTNR = "postnr";
    public static final String BYNAVN = "bynavn";

    public DemoRecordQuery(){}

    @QueryField(type = QueryField.FieldType.INT, queryName = POSTNR)
    private ArrayList<String> postnr = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = BYNAVN)
    private ArrayList<String> bynavn = new ArrayList<>();

    public void setPostnr(int postnr) {
        this.postnr.add(Integer.toString(postnr));
    }

    public void setPostnr(String postnr) {
        if (postnr != null) {
            this.postnr.add(postnr);
        }
    }

    public void setBynavn(String bynavn) {
        if (bynavn != null) {
            this.bynavn.add(bynavn);
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
    protected boolean isEmpty() {
        return this.postnr.isEmpty() && this.bynavn.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap listHashMap) {
        this.setPostnr(listHashMap.get(POSTNR, 0));
        this.setBynavn(listHashMap.get(BYNAVN, 0));
    }

    @Override
    public String getEntityClassname() {
        return DemoEntityRecord.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "entity";
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("postnr", DemoEntityRecord.DB_FIELD_ADDRESS_NUMBER);
        joinHandles.put("bynavn", DemoEntityRecord.DB_FIELD_NAME + BaseQuery.separator + DemoDataRecord.DB_FIELD_NAME);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        this.addCondition("postnr", this.postnr, Integer.class);
        this.addCondition("bynavn", this.bynavn);
    }

}
