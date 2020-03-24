package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
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
    private List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = ADDRESSING_NAME)
    private List<String> addressingName = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY)
    private List<String> locality = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY)
    private List<UUID> localityUUID = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = MUNICIPALITY)
    private List<String> municipalityCode = new ArrayList<>();


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


    public List<String> getAddressingName() {
        return addressingName;
    }

    public void setAddressingName(String addressingName) {
        this.addressingName.clear();
        this.addAddressingName(addressingName);
    }

    public void addAddressingName(String addressingName) {
        if (addressingName != null) {
            this.addressingName.add(addressingName);
            this.increaseDataParamCount();
        }
    }

    public List<String> getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality.clear();
        this.addLocality(locality);
    }

    public void addLocality(String locality) {
        if (locality != null) {
            this.locality.add(locality);
            this.increaseDataParamCount();
        }
    }



    public List<UUID> getLocalityUUID() {
        return localityUUID;
    }

    public void setLocalityUUID(UUID localityUUID) {
        this.localityUUID.clear();
        this.addLocalityUUID(localityUUID);
    }

    public void addLocalityUUID(UUID localityUUID) {
        if (localityUUID != null) {
            this.localityUUID.add(localityUUID);
            this.increaseDataParamCount();
        }
    }

    public List<String> getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode.clear();
        this.addMunicipalityCode(municipalityCode);
    }

    public void addMunicipalityCode(String municipalityCode) {
        if (municipalityCode != null) {
            this.municipalityCode.add(municipalityCode);
            this.increaseDataParamCount();
        }
    }

    public void setMunicipalityCode(int municipalityCode) {
        this.setMunicipalityCode(Integer.toString(municipalityCode));
    }

    public void addMunicipalityCode(int municipalityCode) {
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
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();

        if (this.code != null && !this.code.isEmpty()) {
            lookupDefinition.put(GeoRoadEntity.DB_FIELD_CODE, this.code, Integer.class);
        }
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_NAME + BaseLookupDefinition.separator + RoadNameRecord.DB_FIELD_NAME,
                    this.name,
                    String.class
            );
        }
        if (this.addressingName != null && !this.addressingName.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_NAME + BaseLookupDefinition.separator + RoadNameRecord.DB_FIELD_ADDRESSING_NAME,
                    this.addressingName,
                    String.class
            );
        }
        if (this.locality != null && !this.locality.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_LOCALITY + BaseLookupDefinition.separator + RoadLocalityRecord.DB_FIELD_CODE,
                    this.locality,
                    String.class
            );
        }
        if (this.localityUUID != null && !this.localityUUID.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_LOCALITY + BaseLookupDefinition.separator + RoadLocalityRecord.DB_FIELD_REFERENCE + BaseLookupDefinition.separator + Identification.DB_FIELD_UUID,
                    this.localityUUID,
                    UUID.class
            );
        }

        if (this.municipalityCode != null && !this.municipalityCode.isEmpty()) {
            lookupDefinition.put(GeoRoadEntity.DB_FIELD_MUNICIPALITY + BaseLookupDefinition.separator + RoadMunicipalityRecord.DB_FIELD_CODE, this.municipalityCode, Integer.class);
        }
        return lookupDefinition;
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


    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("code", GeoRoadEntity.DB_FIELD_CODE);
        joinHandles.put("name", GeoRoadEntity.DB_FIELD_NAME + BaseQuery.separator + RoadNameRecord.DB_FIELD_NAME);
        joinHandles.put("addressingname", GeoRoadEntity.DB_FIELD_NAME + BaseQuery.separator + RoadNameRecord.DB_FIELD_ADDRESSING_NAME);
        joinHandles.put("localitycode", GeoRoadEntity.DB_FIELD_LOCALITY + BaseQuery.separator + RoadLocalityRecord.DB_FIELD_CODE);
        joinHandles.put("localityuuid", GeoRoadEntity.DB_FIELD_LOCALITY + BaseQuery.separator + RoadLocalityRecord.DB_FIELD_REFERENCE + BaseQuery.separator + Identification.DB_FIELD_UUID);
        joinHandles.put("municipalitycode", GeoRoadEntity.DB_FIELD_MUNICIPALITY + BaseQuery.separator + RoadMunicipalityRecord.DB_FIELD_CODE);


        //RoadNameRecord.class, namealias, member
        //        GeoRoadEntity.class namealias.entity
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
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
}
