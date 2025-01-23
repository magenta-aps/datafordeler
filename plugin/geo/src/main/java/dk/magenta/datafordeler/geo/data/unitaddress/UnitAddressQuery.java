package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressQuery;

import java.util.*;

/**
 * Created by lars on 19-05-17.
 */
public class UnitAddressQuery extends SumiffiikQuery<UnitAddressEntity> {


    public static final String BNR = AccessAddressEntity.IO_FIELD_BNR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = BNR)
    private final List<String> bnr = new ArrayList<>();


    public static final String HOUSE_NUMBER = AccessAddressEntity.IO_FIELD_HOUSE_NUMBER;

    @QueryField(type = QueryField.FieldType.STRING, queryName = HOUSE_NUMBER)
    private final List<String> houseNumber = new ArrayList<>();


    public static final String ACCESSADDRESS_ID = "accessAddressId";

    @QueryField(type = QueryField.FieldType.LONG, queryName = ACCESSADDRESS_ID)
    private final List<Identification> accessAddress = new ArrayList<>();



    public List<String> getBnr() {
        return bnr;
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

    public void clearBnr() {
        this.bnr.clear();
        this.updatedParameters();
    }

    public void addBnr(String bnr) {
        if (bnr != null) {
            this.bnr.add(bnr);
            this.updatedParameters();
        }
    }


    public List<String> getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber.clear();
        this.updatedParameters();
        this.addHouseNumber(houseNumber);
    }

    public void addHouseNumber(String houseNumber) {
        if (houseNumber != null) {
            this.houseNumber.add(houseNumber);
            this.updatedParameters();
        }
    }


    public void addAccessAddress(AccessAddressEntity accessAddressEntity) {
        if (accessAddressEntity != null) {
            this.accessAddress.add(accessAddressEntity.getIdentification());
            this.updatedParameters();
        }
    }


    public static final String FLOOR = UnitAddressEntity.IO_FIELD_FLOOR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = FLOOR)
    private final List<String> floor = new ArrayList<>();


    public List<String> getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor.clear();
        this.updatedParameters();
        this.addFloor(floor);
    }

    public void addFloor(String floor) {
        if (floor != null) {
            this.floor.add(floor);
            this.updatedParameters();
        }
    }




    public static final String DOOR = UnitAddressEntity.IO_FIELD_DOOR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = DOOR)
    private final List<String> door = new ArrayList<>();


    public List<String> getDoor() {
        return floor;
    }

    public void setDoor(String door) {
        this.door.clear();
        this.updatedParameters();
        this.addDoor(door);
    }

    public void addDoor(String door) {
        if (door != null) {
            this.door.add(door);
            this.updatedParameters();
        }
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(BNR, this.bnr);
        //map.put(ROAD, this.road);
        map.put(HOUSE_NUMBER, this.houseNumber);
        map.put(FLOOR, this.floor);
        map.put(DOOR, this.door);
        map.put(ACCESSADDRESS_ID, this.accessAddress);
        return map;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.floor.isEmpty() && this.bnr.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setBnr(parameters.getI(BNR));
    }

    @Override
    public String getEntityClassname() {
        return UnitAddressEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_unitaddress";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();
    public static final String HANDLE_ACCESSADDRESS = UnitAddressEntity.DB_FIELD_ACCESS_ADDRESS+DatabaseEntry.REF;

    static {
        joinHandles.put("floor", UnitAddressEntity.DB_FIELD_FLOOR + BaseQuery.separator + UnitAddressFloorRecord.DB_FIELD_FLOOR);
        joinHandles.put("door", UnitAddressEntity.DB_FIELD_DOOR + BaseQuery.separator + UnitAddressDoorRecord.DB_FIELD_DOOR);
        joinHandles.put(HANDLE_ACCESSADDRESS, UnitAddressEntity.DB_FIELD_ACCESS_ADDRESS);
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
        this.addCondition("floor", this.floor);
        this.addCondition("door", this.door);
        this.addCondition(HANDLE_ACCESSADDRESS, this.accessAddress, Identification.class);
    }

    public AccessAddressQuery addRelatedAccessAddressQuery() {
        AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put(HANDLE_ACCESSADDRESS, "id");

        accessAddressQuery.addRelatedPostcodeQuery();
        accessAddressQuery.addRelatedLocalityQuery();

        this.addRelated(accessAddressQuery, joinHandles);
        return accessAddressQuery;
    }

}
