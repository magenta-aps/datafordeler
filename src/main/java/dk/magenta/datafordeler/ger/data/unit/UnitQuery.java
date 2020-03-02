package dk.magenta.datafordeler.ger.data.unit;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.ger.data.GerQuery;

import java.util.*;

public class UnitQuery extends GerQuery<UnitEntity> {

    public static final String NAME = UnitEntity.IO_FIELD_NAME;

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

    public List<String> getName() {
        return name;
    }

    public void setName(String name) {
        this.name.clear();
        this.addName(name);
    }

    public void addName(String name) {
        if (name != null) {
            this.name.add(name);
            this.increaseDataParamCount();
        }
    }

    public static final String DEID = UnitEntity.IO_FIELD_DEID;

    @QueryField(type = QueryField.FieldType.STRING, queryName = DEID)
    private List<UUID> deid = new ArrayList<>();

    public List<String> getDeid() {
        return name;
    }

    public void setDeid(String deid) {
        this.setDeid(UUID.fromString(deid));
    }

    public void setDeid(UUID deid) {
        this.deid.clear();
        this.addDeid(deid);
    }

    public void addDeid(UUID deid) {
        if (deid != null) {
            this.deid.add(deid);
            this.increaseDataParamCount();
        }
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(NAME, this.name);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(UnitEntity.DB_FIELD_NAME , this.name, String.class);
        }
        if (this.deid != null && !this.deid.isEmpty()) {
            lookupDefinition.put(UnitEntity.DB_FIELD_DEID, this.deid, UUID.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setName(parameters.getFirst(NAME));
    }

}
