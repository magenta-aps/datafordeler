package dk.magenta.datafordeler.geo.data.locality;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
public class LocalityQuery extends SumiffiikQuery<GeoLocalityEntity> {

    public static final String CODE = GeoLocalityEntity.IO_FIELD_CODE;
    public static final String NAME = GeoLocalityEntity.IO_FIELD_NAME;
    public static final String MUNICIPALITY = GeoLocalityEntity.IO_FIELD_MUNICIPALITY;
    public static final String STATUS = GeoLocalityEntity.IO_FIELD_STATUS;

    @QueryField(type = QueryField.FieldType.STRING, queryName = CODE)
    private List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = MUNICIPALITY)
    private List<String> municipality = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = STATUS)
    private List<String> status = new ArrayList<>();

    public List<String> getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code.clear();
        this.updatedParameters();
        this.addCode(code);
    }

    public void addCode(String code) {
        if (code != null) {
            this.code.add(code);
            this.updatedParameters();
        }
    }

    public List<String> getName() {
        return name;
    }

    public void setName(String name) {
        this.name.clear();
        this.updatedParameters();
        this.addName(name);
    }

    public void addName(String name) {
        if (name != null) {
            this.name.add(name);
            this.updatedParameters();
        }
    }

    public List<String> getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality.clear();
        this.updatedParameters();
        this.addMunicipality(municipality);
    }

    public void addMunicipality(String municipality) {
        if (municipality != null) {
            this.municipality.add(municipality);
            this.updatedParameters();
        }
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status.clear();
        this.updatedParameters();
        this.addStatus(status);
    }
    public void setStatus(int status) {
        this.setStatus(Integer.toString(status));
    }

    public void addStatus(String status) {
        if (status != null) {
            this.status.add(status);
            this.updatedParameters();
        }
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(CODE, this.code);
        map.put(NAME, this.name);
        map.put(MUNICIPALITY, this.municipality);
        return map;
    }

    @Override
    protected boolean isEmpty() {
        return super.isEmpty() && this.code.isEmpty() && this.name.isEmpty() && this.municipality.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirst(CODE));
        this.setName(parameters.getFirst(NAME));
        this.setMunicipality(parameters.getFirst(MUNICIPALITY));
    }

    @Override
    public String getEntityClassname() {
        return GeoLocalityEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_locality";
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("code", GeoLocalityEntity.DB_FIELD_CODE);
        joinHandles.put("name", GeoLocalityEntity.DB_FIELD_NAME);
        joinHandles.put("municipalitycode", GeoLocalityEntity.DB_FIELD_MUNICIPALITY + BaseQuery.separator + LocalityMunicipalityRecord.DB_FIELD_CODE);
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
        this.addCondition("code", this.code);
        this.addCondition("name", this.name);
        this.addCondition("municipalitycode", this.municipality, Integer.class);
    }


    public MunicipalityQuery addRelatedMunicipalityQuery() {
        MunicipalityQuery municipalityQuery = new MunicipalityQuery();
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put("municipalitycode", "code");
        this.addRelated(municipalityQuery, joinHandles);
        return municipalityQuery;
    }

}
