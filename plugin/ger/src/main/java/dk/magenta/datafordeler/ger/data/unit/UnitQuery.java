package dk.magenta.datafordeler.ger.data.unit;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.ger.data.GerQuery;

import java.util.*;
import java.util.stream.Collectors;

public class UnitQuery extends GerQuery<UnitEntity> {

    public static final String NAME = UnitEntity.IO_FIELD_NAME;

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private final List<String> name = new ArrayList<>();

    public List<String> getName() {
        return name;
    }

    public void clearName() {
        this.name.clear();
        this.updatedParameters();
    }

    public void setName(String name) {
        this.clearName();
        this.addName(name);
    }

    public void addName(String name) {
        if (name != null) {
            this.name.add(name);
        }
    }

    public static final String DEID = UnitEntity.IO_FIELD_DEID;

    @QueryField(type = QueryField.FieldType.STRING, queryName = DEID)
    private final List<UUID> deid = new ArrayList<>();

    public List<String> getDeid() {
        return name;
    }

    public void setDeid(String deid) {
        this.setDeid(UUID.fromString(deid));
    }

    public void clearDeid() {
        this.deid.clear();
        this.updatedParameters();
    }

    public void setDeid(UUID deid) {
        this.clearDeid();
        this.addDeid(deid);
    }

    public void addDeid(UUID deid) {
        if (deid != null) {
            this.deid.add(deid);
            this.updatedParameters();
        }
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(NAME, this.name);
        return map;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.name.isEmpty() && this.deid.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setName(parameters.getFirstI(NAME));
    }

    @Override
    public String getEntityClassname() {
        return UnitEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "ger_unit";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("deid", UnitEntity.DB_FIELD_DEID);
        joinHandles.put("name", UnitEntity.DB_FIELD_NAME);
        joinHandles.put("ger", UnitEntity.DB_FIELD_NAME);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("deid", this.deid.stream().map(UUID::toString).collect(Collectors.toList()), UUID.class);
        this.addCondition("name", this.name);
        this.addCondition("gernr", this.getGerNr(), Integer.class);
    }

}
