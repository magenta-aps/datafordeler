package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;
import dk.magenta.datafordeler.geo.data.locality.LocalityQuery;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityQuery;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lars on 19-05-17.
 */
public class RoadQuery extends SumiffiikQuery<GeoRoadEntity> {

    public static final String CODE = GeoRoadEntity.IO_FIELD_CODE;
    public static final String NAME = GeoRoadEntity.IO_FIELD_NAME;
    public static final String ADDRESSING_NAME = RoadNameRecord.IO_FIELD_ADDRESSING_NAME;
    public static final String MUNICIPALITY = RoadMunicipalityRecord.IO_FIELD_CODE;
    public static final String LOCALITY = RoadLocalityRecord.IO_FIELD_CODE;

    public RoadQuery() {
    }

    @QueryField(type = QueryField.FieldType.INT, queryName = CODE)
    private final List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private final List<String> name = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = ADDRESSING_NAME)
    private final List<String> addressingName = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY)
    private final List<String> locality = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY)
    private final List<UUID> localityUUID = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = MUNICIPALITY)
    private final List<String> municipalityCode = new ArrayList<>();


    public List<String> getCode() {
        return code;
    }

    public void setCode(String code) throws InvalidClientInputException {
        this.code.clear();
        this.updatedParameters();
        this.addCode(code);
    }

    public void setCode(int code) throws InvalidClientInputException {
        this.addCode(Integer.toString(code));
    }

    public void addCode(String code) throws InvalidClientInputException {
        if (code != null) {
            ensureNumeric(CODE, code);
            this.code.add(code);
            this.updatedParameters();
        }
    }
    public void addCode(int code) throws InvalidClientInputException {
        this.addCode(Integer.toString(code));
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


    public List<String> getAddressingName() {
        return addressingName;
    }

    public void setAddressingName(String addressingName) {
        this.addressingName.clear();
        this.updatedParameters();
        this.addAddressingName(addressingName);
    }

    public void addAddressingName(String addressingName) {
        if (addressingName != null) {
            this.addressingName.add(addressingName);
            this.updatedParameters();
        }
    }

    public List<String> getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality.clear();
        this.updatedParameters();
        this.addLocality(locality);
    }

    public void addLocality(String locality) {
        if (locality != null) {
            this.locality.add(locality);
            this.updatedParameters();
        }
    }


    public List<UUID> getLocalityUUID() {
        return localityUUID;
    }

    public void setLocalityUUID(UUID localityUUID) {
        this.localityUUID.clear();
        this.updatedParameters();
        this.addLocalityUUID(localityUUID);
    }

    public void addLocalityUUID(UUID localityUUID) {
        if (localityUUID != null) {
            this.localityUUID.add(localityUUID);
            this.updatedParameters();
        }
    }

    public List<String> getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) throws InvalidClientInputException {
        this.municipalityCode.clear();
        this.updatedParameters();
        this.addMunicipalityCode(municipalityCode);
    }

    public void addMunicipalityCode(String municipalityCode) throws InvalidClientInputException {
        if (municipalityCode != null) {
            ensureNumeric(MUNICIPALITY, municipalityCode);
            this.municipalityCode.add(municipalityCode);
            this.updatedParameters();
        }
    }

    public void setMunicipalityCode(int municipalityCode) throws InvalidClientInputException {
        this.setMunicipalityCode(Integer.toString(municipalityCode));
    }

    public void addMunicipalityCode(int municipalityCode) throws InvalidClientInputException {
        this.addMunicipalityCode(Integer.toString(municipalityCode));
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(CODE, this.code);
        map.put(NAME, this.name);
        map.put(ADDRESSING_NAME, this.addressingName);
        map.put(LOCALITY, this.locality);
        map.put(MUNICIPALITY, this.municipalityCode);
        return map;
    }

    @Override
    protected boolean isEmpty() {
        return super.isEmpty() && this.municipalityCode.isEmpty() && this.code.isEmpty() && this.name.isEmpty() && this.addressingName.isEmpty() && this.locality.isEmpty() && this.localityUUID.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirst(CODE));
        this.setName(parameters.getFirst(NAME));
        this.setAddressingName(parameters.getFirst(ADDRESSING_NAME));
        this.setLocality(parameters.getFirst(LOCALITY));
        this.setMunicipalityCode(parameters.getFirst(MUNICIPALITY));
    }

    @Override
    public String getEntityClassname() {
        return GeoRoadEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_road";
    }


    @Override
    public Set<String> getJoinHandles() {
        return super.getJoinHandles();
    }


    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("code", GeoRoadEntity.DB_FIELD_CODE);
        joinHandles.put("name", GeoRoadEntity.DB_FIELD_NAME + BaseQuery.separator + RoadNameRecord.DB_FIELD_NAME);
        joinHandles.put("addressingname", GeoRoadEntity.DB_FIELD_NAME + BaseQuery.separator + RoadNameRecord.DB_FIELD_ADDRESSING_NAME);
        joinHandles.put("localitycode", GeoRoadEntity.DB_FIELD_LOCALITY + BaseQuery.separator + RoadLocalityRecord.DB_FIELD_CODE);
        joinHandles.put("localityuuid", GeoRoadEntity.DB_FIELD_LOCALITY + BaseQuery.separator + RoadLocalityRecord.DB_FIELD_REFERENCE + BaseQuery.separator + Identification.DB_FIELD_UUID);
        joinHandles.put("municipalitycode", GeoRoadEntity.DB_FIELD_MUNICIPALITY + BaseQuery.separator + RoadMunicipalityRecord.DB_FIELD_CODE);
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
        this.addCondition("addressingname", this.addressingName);
        this.addCondition("localitycode", this.locality);
        this.addCondition("localityuuid", this.localityUUID.stream().map(UUID::toString).collect(Collectors.toList()), UUID.class);
        this.addCondition("municipalitycode", this.municipalityCode, Integer.class);
    }

    public MunicipalityQuery addRelatedMunicipalityQuery() {
        MunicipalityQuery municipalityQuery = new MunicipalityQuery();
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put("municipalitycode", "code");
        this.addRelated(municipalityQuery, joinHandles);
        return municipalityQuery;
    }

    public LocalityQuery addRelatedLocalityQuery() {
        LocalityQuery localityQuery = new LocalityQuery();
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put("localitycode", "code");
        this.addRelated(localityQuery, joinHandles);
        return localityQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoadQuery roadQuery = (RoadQuery) o;
        return Objects.equals(code, roadQuery.code) &&
                Objects.equals(name, roadQuery.name) &&
                Objects.equals(addressingName, roadQuery.addressingName) &&
                Objects.equals(locality, roadQuery.locality) &&
                Objects.equals(localityUUID, roadQuery.localityUUID) &&
                Objects.equals(municipalityCode, roadQuery.municipalityCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, addressingName, locality, localityUUID, municipalityCode);
    }
}
