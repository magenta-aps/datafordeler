package dk.magenta.datafordeler.geo.data.municipality;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;

import java.util.*;

/**
 * Created by lars on 19-05-17.
 */
public class MunicipalityQuery extends SumiffiikQuery<GeoMunicipalityEntity> {

    public static final String CODE = GeoMunicipalityEntity.IO_FIELD_CODE;
    public static final String NAME = GeoMunicipalityEntity.IO_FIELD_NAME;

    @QueryField(type = QueryField.FieldType.INT, queryName = CODE)
    private final List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private final List<String> name = new ArrayList<>();

    public List<String> getCode() {
        return code;
    }

    public void setCode(int code) throws InvalidClientInputException {
        this.setCode(Integer.toString(code));
    }

    public void addCode(int code) throws InvalidClientInputException {
        this.addCode(Integer.toString(code));
    }

    public void setCode(String code) throws InvalidClientInputException {
        this.code.clear();
        this.addCode(code);
    }

    public void addCode(String code) throws InvalidClientInputException {
        if (code != null) {
            ensureNumeric(CODE, code);
            this.code.add(code);
        }
    }

    public void clearCode() {
        this.code.clear();
        this.updatedParameters();
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
        }
    }

    public void clearName() {
        this.name.clear();
        this.updatedParameters();
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
        return GeoMunicipalityEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_municipality";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("code", GeoMunicipalityEntity.DB_FIELD_CODE);
        joinHandles.put("name", GeoMunicipalityEntity.DB_FIELD_NAME + BaseQuery.separator + MunicipalityNameRecord.DB_FIELD_NAME);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MunicipalityQuery that = (MunicipalityQuery) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }
}
