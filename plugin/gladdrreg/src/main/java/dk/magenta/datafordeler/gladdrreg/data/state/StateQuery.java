package dk.magenta.datafordeler.gladdrreg.data.state;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.gladdrreg.data.SumiffiikQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
public class StateQuery extends SumiffiikQuery<StateEntity> {

    public static final String CODE = StateData.IO_FIELD_CODE;
    public static final String NAME = StateData.IO_FIELD_NAME;
    public static final String DESCRIPTION = StateData.IO_FIELD_DESCRIPTION;

    @QueryField(type = QueryField.FieldType.INT, queryName = CODE)
    private List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = DESCRIPTION)
    private List<String> description = new ArrayList<>();

    public List<String> getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code.clear();
        this.addCode(code);
    }

    public void addCode(String code) {
        if (code != null) {
            this.code.add(code);
            this.increaseDataParamCount();
        }
    }

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

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description.clear();
        this.addDescription(description);
    }

    public void addDescription(String description) {
        if (description != null) {
            this.description.add(description);
            this.increaseDataParamCount();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(CODE, this.code);
        map.put(NAME, this.name);
        map.put(DESCRIPTION, this.description);
        return map;
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.code != null && !this.code.isEmpty()) {
            lookupDefinition.put(StateData.DB_FIELD_CODE, this.code, String.class);
        }
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(StateData.DB_FIELD_NAME, this.name, String.class);
        }
        if (this.description != null && !this.description.isEmpty()) {
            lookupDefinition.put(StateData.DB_FIELD_DESCRIPTION, this.description, String.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirstI(CODE));
        this.setName(parameters.getFirstI(NAME));
        this.setDescription(parameters.getFirstI(DESCRIPTION));
    }

    @Override
    public Class<StateEntity> getEntityClass() {
        return StateEntity.class;
    }

    @Override
    public Class getDataClass() {
        return StateData.class;
    }

}
