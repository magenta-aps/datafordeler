package dk.magenta.datafordeler.geo.data.postcode;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
public class PostcodeQuery extends SumiffiikQuery<PostcodeEntity> {

    public static final String CODE = PostcodeEntity.IO_FIELD_CODE;
    public static final String NAME = PostcodeEntity.IO_FIELD_NAME;

    @QueryField(type = QueryField.FieldType.INT, queryName = CODE)
    private List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

    public List<String> getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code.clear();
        this.addAnr(code);
    }

    public void addAnr(String anr) {
        if (anr != null) {
            this.code.add(anr);
            this.increaseDataParamCount();
        }
    }



    public List<String> getName() {
        return name;
    }

    public void setName(String name) {
        this.name.clear();
        this.addBnr(name);
    }

    public void addBnr(String bnr) {
        if (bnr != null) {
            this.name.add(bnr);
            this.increaseDataParamCount();
        }
    }




    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(CODE, this.code);
        map.put(NAME, this.name);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.code != null && !this.code.isEmpty()) {
            lookupDefinition.put(PostcodeEntity.DB_FIELD_CODE, this.code, Integer.class);
        }
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(PostcodeEntity.DB_FIELD_NAME + BaseLookupDefinition.separator + PostcodeNameRecord.DB_FIELD_NAME, this.name, String.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirst(CODE));
        this.setName(parameters.getFirst(NAME));
    }

    @Override
    public String getEntityClassname() {
        return PostcodeEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_postcode";
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("code", PostcodeEntity.DB_FIELD_CODE);
        joinHandles.put("name", PostcodeEntity.DB_FIELD_NAME + BaseQuery.separator + PostcodeNameRecord.DB_FIELD_NAME);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws Exception {
        this.addCondition("code", this.code, Integer.class);
        this.addCondition("name", this.name);
    }

}
