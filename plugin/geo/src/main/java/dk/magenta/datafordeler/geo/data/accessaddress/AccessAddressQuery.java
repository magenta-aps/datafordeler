package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;
import dk.magenta.datafordeler.geo.data.locality.LocalityQuery;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityQuery;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeQuery;
import dk.magenta.datafordeler.geo.data.road.RoadQuery;

import java.util.*;

/**
 * Created by lars on 19-05-17.
 */
public class AccessAddressQuery extends SumiffiikQuery<AccessAddressEntity> {

    public static final String BNR = AccessAddressEntity.IO_FIELD_BNR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = BNR)
    private final List<String> bnr = new ArrayList<>();


    public static final String ROAD = AccessAddressRoadRecord.IO_FIELD_ROAD_CODE;

    @QueryField(type = QueryField.FieldType.INT, queryName = ROAD)
    private final List<String> roadCode = new ArrayList<>();


    public static final String MUNICIPALITY = AccessAddressRoadRecord.IO_FIELD_MUNICIPALITY_CODE;

    @QueryField(type = QueryField.FieldType.INT, queryName = MUNICIPALITY)
    private final List<String> municipalityCode = new ArrayList<>();


    public static final String ROAD_UUID = AccessAddressEntity.IO_FIELD_ROAD + "_uuid";

    @QueryField(type = QueryField.FieldType.STRING, queryName = ROAD_UUID)
    private final List<UUID> roadUUID = new ArrayList<>();


    public static final String LOCALITY_UUID = AccessAddressEntity.IO_FIELD_LOCALITY + "_uuid";

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY_UUID)
    private final List<UUID> localityUUID = new ArrayList<>();

    public static final String LOCALITY = AccessAddressEntity.IO_FIELD_LOCALITY;

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY)
    private final List<String> locality = new ArrayList<>();


    public static final String HOUSE_NUMBER = AccessAddressEntity.IO_FIELD_HOUSE_NUMBER;

    @QueryField(type = QueryField.FieldType.STRING, queryName = HOUSE_NUMBER)
    private final List<String> houseNumber = new ArrayList<>();


    public List<String> getBnr() {
        return bnr;
    }

    public void clearBnr() {
        this.bnr.clear();
        this.updatedParameters();
    }

    public void setBnr(String bnr) {
        this.clearBnr();
        this.addBnr(bnr);
    }

    public void setBnr(Collection<String> bnrs) {
        this.clearBnr();
        for (String bnr : bnrs) {
            this.addBnr(bnr);
        }
    }

    public void addBnr(String bnr) {
        if (bnr != null) {
            this.bnr.add(bnr);
            this.updatedParameters();
        }
    }


    public List<String> getRoadCode() {
        return roadCode;
    }

    public void setRoadCode(String roadCode) {
        this.roadCode.clear();
        this.updatedParameters();
        this.addRoadCode(roadCode);
    }

    public void setRoadCode(int roadCode) {
        this.setRoadCode(Integer.toString(roadCode));
    }

    public void addRoadCode(String roadCode) {
        if (roadCode != null) {
            this.roadCode.add(roadCode);
            this.updatedParameters();
        }
    }

    public void addRoadCode(int roadCode) {
        this.addRoadCode(Integer.toString(roadCode));
    }


    public List<String> getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode.clear();
        this.addMunicipalityCode(municipalityCode);
        this.updatedParameters();
    }

    public void addMunicipalityCode(String municipalityCode) {
        if (municipalityCode != null) {
            this.municipalityCode.add(municipalityCode);
            this.updatedParameters();
        }
    }

    public void setMunicipalityCode(int municipalityCode) {
        this.setMunicipalityCode(Integer.toString(municipalityCode));
    }

    public void addMunicipalityCode(int municipalityCode) {
        this.addMunicipalityCode(Integer.toString(municipalityCode));
    }


    public List<UUID> getRoadUUID() {
        return roadUUID;
    }

    public void setRoadUUID(UUID roadUUID) {
        this.clearRoadUUID();
        this.addRoadUUID(roadUUID);
    }

    public void addRoadUUID(UUID roadUUID) {
        if (roadUUID != null) {
            this.roadUUID.add(roadUUID);
            this.updatedParameters();
        }
    }

    public void clearRoadUUID() {
        this.roadUUID.clear();
        this.updatedParameters();
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
        }
    }

    public void clearLocalityUUID() {
        this.localityUUID.clear();
        this.updatedParameters();
    }

    public void clearHouseNumber() {
        houseNumber.clear();
    }


    public List<String> getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber.clear();
        this.addHouseNumber(houseNumber);
    }

    public void addHouseNumber(String houseNumber) {
        if (houseNumber != null) {
            this.houseNumber.add(houseNumber);
        }
    }


    private UUID uuid;

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
        this.updatedParameters();
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(BNR, this.bnr);
        map.put(ROAD, this.roadCode);
        map.put(ROAD_UUID, this.roadUUID);
        map.put(MUNICIPALITY, this.municipalityCode);
        return map;
    }

    @Override
    protected boolean isEmpty() {
        return super.isEmpty() && this.bnr.isEmpty() && this.houseNumber.isEmpty() && this.roadCode.isEmpty() && this.roadUUID.isEmpty() && this.localityUUID.isEmpty() && this.municipalityCode.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setBnr(parameters.getFirst(BNR));

        this.setRoadCode(parameters.getFirst(ROAD));
        this.setHouseNumber(parameters.getFirst(HOUSE_NUMBER));
        this.setMunicipalityCode(parameters.getFirst(MUNICIPALITY));

        String roadUUID = parameters.getFirst(ROAD_UUID);
        if (roadUUID != null) {
            try {
                this.setRoadUUID(UUID.fromString(roadUUID));
            } catch (IllegalArgumentException e) {
                throw new InvalidClientInputException("Parameter " + ROAD_UUID + " must be a uuid", e);
            }
        }
        String localityUUID = parameters.getFirst(LOCALITY_UUID);
        if (localityUUID != null) {
            try {
                this.setLocalityUUID(UUID.fromString(localityUUID));
            } catch (IllegalArgumentException e) {
                throw new InvalidClientInputException("Parameter " + LOCALITY_UUID + " must be a uuid", e);
            }
        }
    }

    @Override
    public String getEntityClassname() {
        return AccessAddressEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_accessaddress";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("bnr", AccessAddressEntity.DB_FIELD_BNR);
        joinHandles.put("housenumber", AccessAddressEntity.DB_FIELD_HOUSE_NUMBER + BaseQuery.separator + AccessAddressHouseNumberRecord.DB_FIELD_NUMBER);
        // Comma-separation in value means that the handle should fit together with a counterpart with an equal number of commas. The separated paths with be ORed together with their counterparts
        joinHandles.put("bnr_or_housenumber", AccessAddressEntity.DB_FIELD_BNR + "," + AccessAddressEntity.DB_FIELD_HOUSE_NUMBER + BaseQuery.separator + AccessAddressHouseNumberRecord.DB_FIELD_NUMBER);
        joinHandles.put("roadcode", AccessAddressEntity.DB_FIELD_ROAD + BaseQuery.separator + AccessAddressRoadRecord.DB_FIELD_ROAD_CODE);
        joinHandles.put("municipalitycode", AccessAddressEntity.DB_FIELD_ROAD + BaseQuery.separator + AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE);
        joinHandles.put("localitycode", AccessAddressEntity.DB_FIELD_LOCALITY + BaseQuery.separator + AccessAddressLocalityRecord.DB_FIELD_CODE);
        joinHandles.put("postcode", AccessAddressEntity.DB_FIELD_POSTCODE + BaseQuery.separator + AccessAddressPostcodeRecord.DB_FIELD_CODE);
        joinHandles.put("id", AccessAddressEntity.DB_FIELD_IDENTIFICATION);
        joinHandles.put("uuid", AccessAddressEntity.DB_FIELD_IDENTIFICATION + BaseQuery.separator + Identification.DB_FIELD_UUID);
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
        this.addCondition("bnr", this.bnr);
        this.addCondition("housenumber", this.houseNumber);
        this.addCondition("roadcode", this.roadCode, Integer.class);
        this.addCondition("municipalitycode", this.municipalityCode, Integer.class);
        this.addCondition("localitycode", this.locality);
        if (this.uuid != null) {
            this.addCondition("uuid", Collections.singletonList(this.uuid.toString()), UUID.class);
        }
    }

    public RoadQuery addRelatedRoadQuery() {
        RoadQuery roadQuery = new RoadQuery();
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put("municipalitycode", "municipalitycode");
        joinHandles.put("roadcode", "code");
        this.addRelated(roadQuery, joinHandles);
        return roadQuery;
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

    public PostcodeQuery addRelatedPostcodeQuery() {
        PostcodeQuery postcodeQuery = new PostcodeQuery();
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put("postcode", "code");
        this.addRelated(postcodeQuery, joinHandles);
        return postcodeQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessAddressQuery that = (AccessAddressQuery) o;
        return Objects.equals(bnr, that.bnr) &&
                Objects.equals(roadCode, that.roadCode) &&
                Objects.equals(municipalityCode, that.municipalityCode) &&
                Objects.equals(roadUUID, that.roadUUID) &&
                Objects.equals(localityUUID, that.localityUUID) &&
                Objects.equals(locality, that.locality) &&
                Objects.equals(houseNumber, that.houseNumber) &&
                Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bnr, roadCode, municipalityCode, roadUUID, localityUUID, locality, houseNumber, uuid);
    }
}
