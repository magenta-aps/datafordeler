package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;

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
    private List<String> road = new ArrayList<>();


    public static final String MUNICIPALITY = AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE;

    @QueryField(type = QueryField.FieldType.INT, queryName = MUNICIPALITY)
    private List<String> municipality = new ArrayList<>();


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




    public List<String> getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road.clear();
        this.addRoad(road);
    }

    public void setRoad(int road) {
        this.setRoad(Integer.toString(road));
    }

    public void addRoad(String road) {
        if (road != null) {
            this.road.add(road);
            this.increaseDataParamCount();
        }
    }





    public List<String> getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality.clear();
        this.addMunicipality(municipality);
    }

    public void setMunicipality(int municipality) {
        this.setMunicipality(Integer.toString(municipality));
    }

    public void addMunicipality(String municipality) {
        if (municipality != null) {
            this.municipality.add(municipality);
            this.increaseDataParamCount();
        }
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
        map.put(ROAD, this.road);
        map.put(ROAD_UUID, this.roadUUID);
        map.put(MUNICIPALITY, this.municipality);
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
        if (this.road != null && !this.road.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_ROAD + BaseLookupDefinition.separator + AccessAddressRoadRecord.DB_FIELD_ROAD_CODE, this.road, Integer.class);
        }
        if (this.roadUUID != null && !this.roadUUID.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_ROAD + BaseLookupDefinition.separator + AccessAddressRoadRecord.DB_FIELD_ROAD_REFERENCE + BaseLookupDefinition.separator + Identification.DB_FIELD_UUID, this.roadUUID, UUID.class);
        }
        if (this.localityUUID != null && !this.localityUUID.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_LOCALITY + BaseLookupDefinition.separator + AccessAddressLocalityRecord.DB_FIELD_REFERENCE + BaseLookupDefinition.separator + Identification.DB_FIELD_UUID, this.localityUUID, UUID.class);
        }
        if (this.municipality != null && !this.municipality.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_ROAD + BaseLookupDefinition.separator + AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE, this.municipality, Integer.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setBnr(parameters.getFirst(BNR));
        this.setRoad(parameters.getFirst(ROAD));
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

}
