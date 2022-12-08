package dk.magenta.datafordeler.gladdrreg.data.district;

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
public class DistrictQuery extends SumiffiikQuery<DistrictEntity> {

    public static final String CODE = DistrictData.IO_FIELD_CODE;
    public static final String ABBREV = DistrictData.IO_FIELD_ABBREV;
    public static final String NAME = DistrictData.IO_FIELD_NAME;

    @QueryField(type = QueryField.FieldType.INT, queryName = CODE)
    private List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = ABBREV)
    private List<String> abbrev = new ArrayList<>();

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

    public List<String> getAbbrev() {
        return abbrev;
    }

    public void setAbbrev(String abbrev) {
        this.abbrev.clear();
        this.addAbbrev(abbrev);
    }

    public void addAbbrev(String abbrev) {
        if (abbrev != null) {
            this.abbrev.add(abbrev);
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
        map.put(ABBREV, this.abbrev);
        map.put(NAME, this.name);
        return map;
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.code != null && !this.code.isEmpty()) {
            lookupDefinition.put(DistrictData.DB_FIELD_CODE, this.code, Integer.class);
        }
        if (this.abbrev != null && !this.abbrev.isEmpty()) {
            lookupDefinition.put(DistrictData.DB_FIELD_ABBREV, this.abbrev, String.class);
        }
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(DistrictData.DB_FIELD_NAME, this.name, String.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirstI(CODE));
        this.setAbbrev(parameters.getFirstI(ABBREV));
        this.setName(parameters.getFirstI(NAME));
    }

    @Override
    public Class<DistrictEntity> getEntityClass() {
        return DistrictEntity.class;
    }

    @Override
    public Class getDataClass() {
        return DistrictData.class;
    }

}
