package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.records.person.data.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Container for a query for Persons, defining fields and database lookup
 */
public class PersonRecordQuery extends BaseQuery {

    public static final String PERSONNUMMER = PersonEntity.IO_FIELD_CPR_NUMBER;
    public static final String FORNAVNE = NameDataRecord.IO_FIELD_FIRST_NAMES;
    public static final String EFTERNAVN = NameDataRecord.IO_FIELD_LAST_NAME;
    public static final String KOMMUNEKODE = AddressDataRecord.IO_FIELD_MUNICIPALITY_CODE;
    public static final String VEJKODE = AddressDataRecord.IO_FIELD_ROAD_CODE;
    public static final String DOOR = AddressDataRecord.IO_FIELD_DOOR;
    public static final String FLOOR = AddressDataRecord.IO_FIELD_FLOOR;
    public static final String HOUSENO = AddressDataRecord.IO_FIELD_HOUSENUMBER;
    public static final String BUILDINGNO = AddressDataRecord.IO_FIELD_BUILDING_NUMBER;
    public static final String CUSTODYPNR = CustodyDataRecord.IO_FIELD_RELATION_PNR;
    public static final String PERSONEVENT = PersonEventDataRecord.IO_FIELD_EVENT;
    public static final String PERSONEVENTTIME = PersonEventDataRecord.DB_FIELD_TIMESTAMP;
    public static final String PERSONDATAEVENT = PersonDataEventDataRecord.DB_FIELD_FIELD;
    public static final String PERSONDATAEVENTTIME = PersonDataEventDataRecord.DB_FIELD_TIMESTAMP;

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONNUMMER)
    private List<String> personnumre = new ArrayList<>();

    public Collection<String> getPersonnumre() {
        return this.personnumre;
    }

    public void addPersonnummer(String personnummer) {
        this.personnumre.add(personnummer);
        if (personnummer != null) {
            this.updatedParameters();
        }
    }

    public void setPersonnummer(String personnummer) {
        this.clearPersonnumre();
        this.addPersonnummer(personnummer);
    }

    public void clearPersonnumre() {
        this.personnumre.clear();
        this.updatedParameters();
    }

    public void setPersonnumre(Collection<String> personnumre) {
        this.clearPersonnumre();
        if (personnumre != null) {
            this.personnumre.addAll(personnumre);
            this.updatedParameters();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = FORNAVNE)
    private List<String> fornavn = new ArrayList<>();

    public List<String> getFornavn() {
        return fornavn;
    }

    public void clearFornavn() {
        this.fornavn.clear();
        this.updatedParameters();
    }
    public void addFornavn(String fornavn) {
        this.fornavn.add(fornavn);
        if (fornavn != null) {
            this.updatedParameters();
        }
    }

    public void setFornavne(Collection<String> fornavne) {
        this.clearFornavn();
        if (fornavne != null) {
            this.fornavn.addAll(fornavne);
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = EFTERNAVN)
    private List<String> efternavn = new ArrayList<>();

    public List<String> getEfternavn() {
        return efternavn;
    }
    public void clearEfternavn() {
        this.efternavn.clear();
        this.updatedParameters();
    }

    public void setEfternavn(String efternavn) {
        this.clearEfternavn();
        this.efternavn.add(efternavn);
        if (efternavn != null) {
            this.updatedParameters();
        }
    }
    public void setEfternavne(Collection<String> efternavne) {
        this.clearEfternavn();
        if (efternavne != null) {
            this.efternavn.addAll(efternavne);
            this.updatedParameters();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekoder = new ArrayList<>();

    public Collection<String> getKommunekoder() {
        return this.kommunekoder;
    }

    public void addKommunekode(String kommunekode) {
        if (kommunekode != null) {
            this.kommunekoder.add(kommunekode);
            this.updatedParameters();
        }
    }

    public void addKommunekode(int kommunekode) {
        this.addKommunekode(String.format("%03d", kommunekode));
    }

    public void clearKommunekode() {
        this.kommunekoder.clear();
        this.updatedParameters();
    }
    public void setKommunekoder(Collection<String> kommunekoder) {
        this.clearKommunekode();
        if (kommunekoder != null) {
            this.kommunekoder.addAll(kommunekoder);
            this.updatedParameters();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = VEJKODE)
    private List<String> vejkoder = new ArrayList<>();

    public Collection<String> getVejkoder() {
        return this.vejkoder;
    }

    public void addVejkode(String vejkode) {
        if (vejkode != null) {
            this.vejkoder.add(vejkode);
            this.updatedParameters();
        }
    }

    public void addVejkode(int vejkode) {
        this.addVejkode(String.format("%03d", vejkode));
    }

    public void clearVejkode() {
        this.vejkoder.clear();
        this.updatedParameters();
    }
    public void setVejkoder(Collection<String> vejkoder) {
        this.clearVejkode();
        if (vejkoder != null) {
            this.vejkoder.addAll(vejkoder);
            this.updatedParameters();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = DOOR)
    private List<String> doors = new ArrayList<>();

    public Collection<String> getDoors() {
        return this.doors;
    }

    public void addDoor(String door) {
        this.doors.add(door);
        this.updatedParameters();
    }

    public void clearDoor() {
        this.doors.clear();
        this.updatedParameters();
    }
    public void setDoors(Collection<String> doors) {
        this.clearDoor();
        if (doors != null) {
            this.doors.addAll(doors);
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = CUSTODYPNR)
    private List<String> custodyPnr = new ArrayList<>();

    public Collection<String> getCustodyPnr() {
        return this.custodyPnr;
    }

    public void addCustodyPnr(String custodyPnr) {
        this.custodyPnr.add(custodyPnr);
        this.updatedParameters();
    }

    public void clearCustodyPnr() {
        this.custodyPnr.clear();
        this.updatedParameters();
    }
    public void setCustodyPnr(Collection<String> custodyPnr) {
        this.clearCustodyPnr();
        if (custodyPnr != null) {
            this.custodyPnr.addAll(custodyPnr);
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = FLOOR)
    private List<String> floors = new ArrayList<>();

    public Collection<String> getFloors() {
        return this.floors;
    }

    public void addFloor(String floor) {
        this.floors.add(floor);
        this.updatedParameters();
    }

    public void clearFloor() {
        this.floors.clear();
        this.updatedParameters();
    }
    public void setFloors(Collection<String> floors) {
        this.floors.clear();
        if (floors != null) {
            this.floors.addAll(floors);
            this.updatedParameters();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = HOUSENO)
    private List<String> houseNos = new ArrayList<>();

    public Collection<String> getHouseNos() {
        return this.houseNos;
    }

    public void addHouseNo(String houseNo) {
        this.houseNos.add(houseNo);
        this.updatedParameters();
    }

    public void clearHouseNo() {
        this.houseNos.clear();
        this.updatedParameters();
    }
    public void setHouseNos(Collection<String> houseNos) {
        this.houseNos.clear();
        if (houseNos != null) {
            this.houseNos.addAll(houseNos);
            this.updatedParameters();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = BUILDINGNO)
    private List<String> buildingNos = new ArrayList<>();

    public Collection<String> getBuildingNos() {
        return this.buildingNos;
    }

    public void addBuildingNo(String houseNo) {
        this.buildingNos.add(houseNo);
        this.updatedParameters();
    }

    public void clearBuildingNo() {
        this.buildingNos.clear();
        this.updatedParameters();
    }
    public void setBuildingNos(Collection<String> buildingNos) {
        this.buildingNos.clear();
        if (buildingNos != null) {
            this.buildingNos.addAll(buildingNos);
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONEVENT)
    private List<String> personevents = new ArrayList<>();

    public Collection<String> getEvents() {
        return this.personevents;
    }

    public void addEvent(String personevent) {
        this.personevents.add(personevent);
        if (personevent != null) {
            this.updatedParameters();
        }
    }

    public void setEvent(String personevent) {
        this.clearEvents();
        this.addEvent(personevent);
    }

    public void clearEvents() {
        this.personevents.clear();
        this.updatedParameters();
    }

    public void setEvents(Collection<String> personevent) {
        this.clearEvents();
        if (personevent != null) {
            this.personevents.addAll(personevent);
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONEVENTTIME)
    private OffsetDateTime personeventTimeAfter;

    public void setEventTimeAfter(String personeventTimeAfter) {
        this.personeventTimeAfter = OffsetDateTime.parse(personeventTimeAfter, DateTimeFormatter.ISO_DATE_TIME);
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONDATAEVENT)
    private List<String> persondataevents = new ArrayList<>();

    public Collection<String> getDataEvents() {
        return this.persondataevents;
    }

    public void addDataEvent(String persondataevent) {
        this.persondataevents.add(persondataevent);
        if (persondataevent != null) {
            this.updatedParameters();
        }
    }

    public void setDataEvent(String personevent) {
        this.clearDataEvents();
        this.addDataEvent(personevent);
    }

    public void clearDataEvents() {
        this.persondataevents.clear();
        this.updatedParameters();
    }

    public void setDataEvents(Collection<String> personevent) {
        this.clearEvents();
        if (personevent != null) {
            this.persondataevents.addAll(personevent);
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONDATAEVENTTIME)
    private OffsetDateTime persondataeventTimeAfter;

    public void setDataEventTimeAfter(OffsetDateTime personeventTimeAfter) {
        this.persondataeventTimeAfter = personeventTimeAfter;
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(PERSONNUMMER, this.personnumre);
        map.put(FORNAVNE, this.fornavn);
        map.put(EFTERNAVN, this.efternavn);
        map.put(KOMMUNEKODE, this.kommunekoder);
        map.put(VEJKODE, this.vejkoder);
        map.put(DOOR, this.doors);
        map.put(FLOOR, this.floors);
        map.put(HOUSENO, this.houseNos);
        map.put(BUILDINGNO, this.buildingNos);
        map.put(PERSONEVENT, this.personevents);
        map.put(PERSONDATAEVENT, this.persondataevents);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setPersonnumre(parameters.get(PERSONNUMMER));
        this.setFornavne(parameters.get(FORNAVNE));
        this.setEfternavne(parameters.get(EFTERNAVN));
        this.setKommunekoder(parameters.get(KOMMUNEKODE));
        this.setVejkoder(parameters.get(VEJKODE));
        this.setDoors(parameters.get(DOOR));
        this.setFloors(parameters.get(FLOOR));
        this.setHouseNos(parameters.get(HOUSENO));
        this.setBuildingNos(parameters.get(BUILDINGNO));
        this.setEvents(parameters.get(PERSONEVENT));
        this.setDataEvents(parameters.get(PERSONDATAEVENT));
    }

    @Override
    public String getEntityIdentifier() {
        return "cpr_person";
    }

    @Override
    public String getEntityClassname() {
        return PersonEntity.class.getCanonicalName();
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("pnr", PersonEntity.DB_FIELD_CPR_NUMBER);
        joinHandles.put("firstname", PersonEntity.DB_FIELD_NAME + LookupDefinition.separator + NameDataRecord.DB_FIELD_FIRST_NAMES);
        joinHandles.put("lastname", PersonEntity.DB_FIELD_NAME + LookupDefinition.separator + NameDataRecord.DB_FIELD_LAST_NAME);
        joinHandles.put("municipalitycode", PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE);
        joinHandles.put("roadcode", PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_ROAD_CODE);
        joinHandles.put("floor", PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_FLOOR);
        joinHandles.put("door", PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_DOOR);
        joinHandles.put("housenumber", PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_HOUSENUMBER);
        joinHandles.put("bnr", PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_BUILDING_NUMBER);

        joinHandles.put("bnr_or_housenumber", PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_BUILDING_NUMBER + "," + PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_HOUSENUMBER);
        joinHandles.put("custodyPnr", PersonEntity.DB_FIELD_CUSTODY + LookupDefinition.separator + CustodyDataRecord.DB_FIELD_RELATION_PNR);
        joinHandles.put("personevent", PersonEntity.DB_FIELD_EVENT + LookupDefinition.separator + PersonEventDataRecord.DB_FIELD_EVENT);
        joinHandles.put("personeventTime.GTE", PersonEntity.DB_FIELD_EVENT + LookupDefinition.separator + PersonEventDataRecord.DB_FIELD_TIMESTAMP);
        joinHandles.put("persondataevent", PersonEntity.DB_FIELD_DATAEVENT + LookupDefinition.separator + PersonDataEventDataRecord.DB_FIELD_FIELD);
        joinHandles.put("persondataeventTime.GTE", PersonEntity.DB_FIELD_DATAEVENT + LookupDefinition.separator + PersonDataEventDataRecord.DB_FIELD_TIMESTAMP);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }


    private List<String> birth_gt;
    private List<String> birth_lt;

    protected void setupConditions() throws QueryBuildException {
        this.addCondition("pnr", this.personnumre);
        this.addCondition("firstname", this.fornavn);
        this.addCondition("lastname", this.efternavn);
        this.addCondition("municipalitycode", this.kommunekoder, Integer.class);
        this.addCondition("roadcode", this.vejkoder, Integer.class);
        this.addCondition("floor", this.floors);
        this.addCondition("door", this.doors);
        this.addCondition("housenumber", this.houseNos);
        this.addCondition("bnr", this.buildingNos);
        this.addCondition("municipalitycode", this.getKommunekodeRestriction(), Integer.class);
        this.addCondition("custodyPnr", this.custodyPnr);
        this.addCondition("personevent", this.personevents);
        this.addCondition("personeventTime.GTE", Condition.Operator.GTE, this.personeventTimeAfter, OffsetDateTime.class, true);
        this.addCondition("persondataevent", this.persondataevents);
        this.addCondition("persondataeventTime.GTE", Condition.Operator.GTE, this.persondataeventTimeAfter, OffsetDateTime.class, true);
    }



    @Override
    protected boolean isEmpty() {
        return this.personnumre.isEmpty() && this.fornavn.isEmpty() && this.efternavn.isEmpty() && this.kommunekoder.isEmpty() && this.vejkoder.isEmpty() && this.houseNos.isEmpty() && this.buildingNos.isEmpty() && this.floors.isEmpty() && this.doors.isEmpty() && this.personevents.isEmpty() && this.personeventTimeAfter == null && this.persondataevents.isEmpty() && this.personeventTimeAfter == null;
    }

}