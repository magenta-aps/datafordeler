package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
public class UnitAddressQuery extends SumiffiikQuery<UnitAddressEntity> {

/*
    public static final String BNR = UnitAddressEntity.IO_FIELD_BNR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = BNR)
    private List<String> bnr = new ArrayList<>();


    public static final String ROAD = AccessAddressEntity.IO_FIELD_ROAD;

    @QueryField(type = QueryField.FieldType.INT, queryName = ROAD)
    private List<String> road = new ArrayList<>();


    public static final String MUNICIPALITY = AccessAddressEntity.IO_FIELD_MUNICIPALITY;

    @QueryField(type = QueryField.FieldType.INT, queryName = MUNICIPALITY)
    private List<String> municipality = new ArrayList<>();
*/


    public static final String HOUSE_NUMBER = AccessAddressEntity.IO_FIELD_HOUSE_NUMBER;

    @QueryField(type = QueryField.FieldType.STRING, queryName = HOUSE_NUMBER)
    private List<String> houseNumber = new ArrayList<>();


/*
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
*/



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




    public static final String FLOOR = UnitAddressEntity.IO_FIELD_FLOOR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = FLOOR)
    private List<String> floor = new ArrayList<>();


    public List<String> getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor.clear();
        this.addFloor(floor);
    }

    public void addFloor(String floor) {
        if (floor != null) {
            this.floor.add(floor);
            this.increaseDataParamCount();
        }
    }



    public static final String DOOR = UnitAddressEntity.IO_FIELD_DOOR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = DOOR)
    private List<String> door = new ArrayList<>();


    public List<String> getDoor() {
        return door;
    }

    public void setDoor(String door) {
        this.door.clear();
        this.addDoor(door);
    }

    public void addDoor(String door) {
        if (door != null) {
            this.door.add(door);
            this.increaseDataParamCount();
        }
    }





    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        //map.put(BNR, this.bnr);
        //map.put(ROAD, this.road);
        map.put(HOUSE_NUMBER, this.houseNumber);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        /*if (this.bnr != null && !this.bnr.isEmpty()) {
            lookupDefinition.put(UnitAddressEntity.DB_FIELD_ACCESS_ADDRESS + AccessAddressEntity.DB_FIELD_BNR, this.bnr, String.class);
        }*/
        /*if (this.houseNumber != null && !this.houseNumber.isEmpty()) {
            lookupDefinition.put(UnitAddressEntity.DB_ + BaseLookupDefinition.separator + AccessAddressHouseNumberRecord.DB_FIELD_NAME, this.houseNumber, String.class);
        }*/
        /*if (this.road != null && !this.road.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_ROAD + BaseLookupDefinition.separator + AccessAddressRoadRecord.DB_FIELD_CODE, this.road, Integer.class);
        }
        if (this.municipality != null && !this.municipality.isEmpty()) {
            lookupDefinition.put(AccessAddressEntity.DB_FIELD_MUNICIPALITY + BaseLookupDefinition.separator + AccessAddressMunicipalityRecord.DB_FIELD_CODE, this.municipality, Integer.class);
        }*/
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        //this.setBnr(parameters.getFirst(BNR));
        //this.setRoad(parameters.getFirst(ROAD));
    }

    @Override
    public String getEntityClassname() {
        return UnitAddressEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_unitaddress";
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("floor", UnitAddressEntity.DB_FIELD_FLOOR + BaseLookupDefinition.separator + UnitAddressFloorRecord.DB_FIELD_FLOOR);
        joinHandles.put("door", UnitAddressEntity.DB_FIELD_DOOR + BaseLookupDefinition.separator + UnitAddressDoorRecord.DB_FIELD_DOOR);
        joinHandles.put("accessaddress_id", UnitAddressEntity.DB_FIELD_ACCESS_ADDRESS);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws Exception {
        this.addCondition("floor", this.floor);
        this.addCondition("door", this.door);
    }

    public AccessAddressQuery addRelatedAccessAddressQuery() {
        AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put("accessaddress_id", "id");
        this.addRelated(accessAddressQuery, joinHandles);
        return accessAddressQuery;
    }

}
