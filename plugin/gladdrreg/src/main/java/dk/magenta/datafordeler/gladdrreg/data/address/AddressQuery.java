package dk.magenta.datafordeler.gladdrreg.data.address;

import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.gladdrreg.data.SumiffiikQuery;

import java.util.*;

/**
 * Created by lars on 19-05-17.
 */
public class AddressQuery extends SumiffiikQuery<AddressEntity> {

    public static final String HOUSE_NUMBER = AddressData.IO_FIELD_HOUSENUMBER;
    public static final String FLOOR = AddressData.IO_FIELD_FLOOR;
    public static final String ROOM = AddressData.IO_FIELD_ROOM;

    @QueryField(type = QueryField.FieldType.STRING, queryName = HOUSE_NUMBER)
    private List<String> houseNumber = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = FLOOR)
    private List<String> floor = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = ROOM)
    private List<String> room = new ArrayList<>();

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

    public List<String> getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room.clear();
        this.addRoom(room);
    }

    public void addRoom(String room) {
        if (room != null) {
            this.room.add(room);
            this.increaseDataParamCount();
        }
    }

    private List<String> road = new ArrayList<>();

    public void setRoad(String road) {
        this.road.clear();
        this.addRoad(road);
    }

    public void addRoad(String road) {
        if (road != null) {
            this.road.add(road);
            this.increaseDataParamCount();
        }
    }

    private List<String> bnr = new ArrayList<>();

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

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(HOUSE_NUMBER, this.houseNumber);
        map.put(FLOOR, this.floor);
        map.put(ROOM, this.room);
        return map;
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.houseNumber != null && !this.houseNumber.isEmpty()) {
            lookupDefinition.put(AddressData.DB_FIELD_HOUSENUMBER, this.houseNumber, String.class);
        }
        if (this.floor != null && !this.floor.isEmpty()) {
            lookupDefinition.put(AddressData.DB_FIELD_FLOOR, this.floor, String.class);
        }
        if (this.room != null && !this.room.isEmpty()) {
            lookupDefinition.put(AddressData.DB_FIELD_ROOM, this.room, String.class);
        }
        if (this.road != null && !this.road.isEmpty()) {
            lookupDefinition.put(AddressData.DB_FIELD_ROAD + LookupDefinition.separator + Identification.DB_FIELD_UUID, this.road, UUID.class);
        }
        if (this.bnr != null && !this.bnr.isEmpty()) {
            lookupDefinition.put(AddressData.DB_FIELD_BNUMBER + LookupDefinition.separator + Identification.DB_FIELD_UUID, this.bnr, UUID.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        super.setFromParameters(parameters);
        this.setHouseNumber(parameters.getFirstI(HOUSE_NUMBER));
        this.setFloor(parameters.getFirstI(FLOOR));
        this.setRoom(parameters.getFirstI(ROOM));
    }

    @Override
    public Class<AddressEntity> getEntityClass() {
        return AddressEntity.class;
    }

    @Override
    public Class getDataClass() {
        return AddressData.class;
    }

}
