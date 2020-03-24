package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
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
    private List<String> bnr = new ArrayList<>();


    public static final String ROAD = AccessAddressEntity.IO_FIELD_ROAD;

    @QueryField(type = QueryField.FieldType.INT, queryName = ROAD)
    private List<String> roadCode = new ArrayList<>();


    public static final String MUNICIPALITY = AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE;

    @QueryField(type = QueryField.FieldType.INT, queryName = MUNICIPALITY)
    private List<String> municipalityCode = new ArrayList<>();


    public static final String ROAD_UUID = AccessAddressEntity.IO_FIELD_ROAD + "_uuid";

    @QueryField(type = QueryField.FieldType.STRING, queryName = ROAD_UUID)
    private List<UUID> roadUUID = new ArrayList<>();


    public static final String LOCALITY_UUID = AccessAddressEntity.IO_FIELD_LOCALITY + "_uuid";

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY_UUID)
    private List<UUID> localityUUID = new ArrayList<>();



    public static final String HOUSE_NUMBER = AccessAddressEntity.IO_FIELD_HOUSE_NUMBER;

    @QueryField(type = QueryField.FieldType.STRING, queryName = HOUSE_NUMBER)
    private List<String> houseNumber = new ArrayList<>();



    public List<String> getBnr() {
        return bnr;
    }

    public void setBnr(String bnr) {
        this.bnr.clear();
        this.addBnr(bnr);
    }

    public void addBnr(String bnr) {
        if (bnr != null) {
            this.bnr.add(bnr);
            this.increaseDataParamCount();
        }
    }




    public List<String> getRoadCode() {
        return roadCode;
    }

    public void setRoadCode(String roadCode) {
        this.roadCode.clear();
        this.addRoadCode(roadCode);
    }

    public void setRoadCode(int roadCode) {
        this.setRoadCode(Integer.toString(roadCode));
    }

    public void addRoadCode(String roadCode) {
        if (roadCode != null) {
            this.roadCode.add(roadCode);
            this.increaseDataParamCount();
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



    public List<UUID> getRoadUUID() {
        return roadUUID;
    }

    public void setRoadUUID(UUID roadUUID) {
        this.roadUUID.clear();
        this.addRoadUUID(roadUUID);
    }

    public void addRoadUUID(UUID roadUUID) {
        if (roadUUID != null) {
            this.roadUUID.add(roadUUID);
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
            this.increaseDataParamCount();
        }
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
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.bnr != null && !this.bnr.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_BNR, this.bnr, String.class);
        }
        if (this.houseNumber != null && !this.houseNumber.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_HOUSE_NUMBER + BaseLookupDefinition.separator + AccessAddressHouseNumberRecord.DB_FIELD_NUMBER, this.houseNumber, String.class);
        }
        if (this.roadCode != null && !this.roadCode.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_ROAD + BaseLookupDefinition.separator + AccessAddressRoadRecord.DB_FIELD_ROAD_CODE, this.roadCode, Integer.class);
        }
        if (this.roadUUID != null && !this.roadUUID.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_ROAD + BaseLookupDefinition.separator + AccessAddressRoadRecord.DB_FIELD_ROAD_REFERENCE + BaseLookupDefinition.separator + Identification.DB_FIELD_UUID, this.roadUUID, UUID.class);
        }
        if (this.localityUUID != null && !this.localityUUID.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_LOCALITY + BaseLookupDefinition.separator + AccessAddressLocalityRecord.DB_FIELD_REFERENCE + BaseLookupDefinition.separator + Identification.DB_FIELD_UUID, this.localityUUID, UUID.class);
        }
        if (this.municipalityCode != null && !this.municipalityCode.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_ROAD + BaseLookupDefinition.separator + AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE, this.municipalityCode, Integer.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setBnr(parameters.getFirst(BNR));
        this.setRoadCode(parameters.getFirst(ROAD));
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

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("bnr", AccessAddressEntity.DB_FIELD_BNR);
        joinHandles.put("housenumber", AccessAddressEntity.DB_FIELD_HOUSE_NUMBER + BaseQuery.separator + AccessAddressHouseNumberRecord.DB_FIELD_NUMBER);
        // Comma-separation in value means that the handle should fit together with a counterpart with an equal number of commas. The separated paths with be ORed together with their counterparts
        joinHandles.put("bnr_or_housenumber", AccessAddressEntity.DB_FIELD_BNR+","+AccessAddressEntity.DB_FIELD_HOUSE_NUMBER + BaseQuery.separator + AccessAddressHouseNumberRecord.DB_FIELD_NUMBER);
        joinHandles.put("roadcode", AccessAddressEntity.DB_FIELD_ROAD + BaseQuery.separator + AccessAddressRoadRecord.DB_FIELD_ROAD_CODE);
        joinHandles.put("municipalitycode", AccessAddressEntity.DB_FIELD_ROAD + BaseQuery.separator + AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE);
        joinHandles.put("localitycode", AccessAddressEntity.DB_FIELD_LOCALITY + BaseQuery.separator + AccessAddressLocalityRecord.DB_FIELD_CODE);
        joinHandles.put("postcode", AccessAddressEntity.DB_FIELD_POSTCODE + BaseQuery.separator + AccessAddressPostcodeRecord.DB_FIELD_CODE);
        joinHandles.put("id", AccessAddressEntity.DB_FIELD_IDENTIFICATION);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws Exception {
        this.addCondition("bnr", this.bnr);
        this.addCondition("housenumber", this.houseNumber);
        this.addCondition("roadcode", this.roadCode, Integer.class);
        this.addCondition("municipalitycode", this.municipalityCode, Integer.class);
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

}
