package dk.magenta.datafordeler.geo.data.locality;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityQuery;

import java.util.*;

/**
 * Created by lars on 19-05-17.
 */
public class LocalityQuery extends SumiffiikQuery<GeoLocalityEntity> {

    public static final String CODE = GeoLocalityEntity.IO_FIELD_CODE;
    public static final String CODE_ALIAS = "lokalitetskode";
    public static final String NAME = GeoLocalityEntity.IO_FIELD_NAME;
    public static final String NAME_ALIAS = "lokalitetsnavn";
    public static final String MUNICIPALITY = GeoLocalityEntity.IO_FIELD_MUNICIPALITY;
    public static final String STATUS = GeoLocalityEntity.IO_FIELD_STATUS;

    @QueryField(type = QueryField.FieldType.STRING, queryName = CODE)
    private final List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private final List<String> name = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = MUNICIPALITY)
    private final List<String> municipality = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = STATUS)
    private final List<String> status = new ArrayList<>();

    public List<String> getCode() {
        return code;
    }

    public void setCode(String code) throws InvalidClientInputException {
        this.code.clear();
        this.updatedParameters();
        this.addCode(code);
    }

    public void setCode(Collection<String> codes) throws InvalidClientInputException {
        this.code.clear();
        for (String code : codes) {
            this.addCode(code);
        }
    }

    public void addCode(Collection<String> codes) throws InvalidClientInputException {
        if (codes != null) {
            for (String code : codes) {
                this.addCode(code);
            }
            this.updatedParameters();
        }
    }

    public void addCode(String code) throws InvalidClientInputException {
        if (code != null) {
            ensureNumeric(CODE, code);
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

    public void setName(Collection<String> names) throws InvalidClientInputException {
        this.name.clear();
        for (String name : names) {
            this.addName(name);
        }
    }

    public void addName(Collection<String> names) {
        if (names != null) {
            for (String name : names) {
                if (name != null) {
                    this.name.add(name);
                }
            }
            this.updatedParameters();
        }
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

    public void setMunicipality(Collection<String> municipalities) {
        this.municipality.clear();
        this.updatedParameters();
        for (String municipality : municipalities) {
            this.addMunicipality(municipality);
        }
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
    public boolean isEmpty() {
        return super.isEmpty() && this.code.isEmpty() && this.name.isEmpty() && this.municipality.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.addCode(parameters.getI(CODE));
        this.addCode(parameters.getI(CODE_ALIAS));
        this.addName(parameters.getI(NAME));
        this.addName(parameters.getI(NAME_ALIAS));
        this.setMunicipality(parameters.getI(MUNICIPALITY));
    }

    @Override
    public String getEntityClassname() {
        return GeoLocalityEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_locality";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("code", GeoLocalityEntity.DB_FIELD_CODE);
        joinHandles.put("name", GeoLocalityEntity.DB_FIELD_NAME + BaseQuery.separator + LocalityNameRecord.DB_FIELD_NAME);
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
