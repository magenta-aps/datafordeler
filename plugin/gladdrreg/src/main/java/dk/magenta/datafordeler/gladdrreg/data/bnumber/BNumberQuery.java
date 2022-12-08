package dk.magenta.datafordeler.gladdrreg.data.bnumber;

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
public class BNumberQuery extends SumiffiikQuery<BNumberEntity> {

    public static final String CODE = BNumberData.IO_FIELD_CODE;
    public static final String TYPE = BNumberData.IO_FIELD_TYPE;
    public static final String NAME = BNumberData.IO_FIELD_CALLNAME;

    @QueryField(type = QueryField.FieldType.STRING, queryName = CODE)
    private List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = TYPE)
    private List<String> type = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

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

    public List<String> getType() {
        return type;
    }

    public void setType(String type) {
        this.type.clear();
        this.addType(type);
    }

    public void addType(String type) {
        if (type != null) {
            this.type.add(type);
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

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(CODE, this.code);
        map.put(TYPE, this.type);
        map.put(NAME, this.name);
        return map;
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.code != null && !this.code.isEmpty()) {
            lookupDefinition.put(BNumberData.DB_FIELD_CODE, this.code, String.class);
        }
        if (this.type != null && !this.type.isEmpty()) {
            lookupDefinition.put(BNumberData.DB_FIELD_TYPE, this.type, String.class);
        }
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(BNumberData.DB_FIELD_CALLNAME, this.name, String.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirstI(CODE));
        this.setType(parameters.getFirstI(TYPE));
        this.setName(parameters.getFirstI(NAME));
    }

    @Override
    public Class<BNumberEntity> getEntityClass() {
        return BNumberEntity.class;
    }

    @Override
    public Class getDataClass() {
        return BNumberData.class;
    }

}
