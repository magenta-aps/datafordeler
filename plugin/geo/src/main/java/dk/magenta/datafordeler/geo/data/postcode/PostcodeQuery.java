package dk.magenta.datafordeler.geo.data.postcode;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
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
    private final List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private final List<String> name = new ArrayList<>();

    public List<String> getCode() {
        return code;
    }

    public void setCode(String code) throws InvalidClientInputException {
        this.code.clear();
        this.updatedParameters();
        this.addAnr(code);
    }

    public void addAnr(String anr) throws InvalidClientInputException {
        if (anr != null) {
            ensureNumeric(CODE, anr);
            this.code.add(anr);
            this.updatedParameters();
        }
    }


    public List<String> getName() {
        return name;
    }

    public void setName(String name) {
        this.name.clear();
        this.updatedParameters();
        this.addBnr(name);
    }

    public void addBnr(String bnr) {
        if (bnr != null) {
            this.name.add(bnr);
            this.updatedParameters();
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
    public boolean isEmpty() {
        return super.isEmpty() && this.code.isEmpty() && this.name.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirstI(CODE));
        this.setName(parameters.getFirstI(NAME));
    }

    @Override
    public String getEntityClassname() {
        return PostcodeEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_postcode";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("code", PostcodeEntity.DB_FIELD_CODE);
        joinHandles.put("name", PostcodeEntity.DB_FIELD_NAME + BaseQuery.separator + PostcodeNameRecord.DB_FIELD_NAME);
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> handles = new HashMap<>();
        handles.putAll(super.joinHandles());
        handles.putAll(joinHandles);
        return handles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("code", this.code, Integer.class);
        this.addCondition("name", this.name);
    }

}
