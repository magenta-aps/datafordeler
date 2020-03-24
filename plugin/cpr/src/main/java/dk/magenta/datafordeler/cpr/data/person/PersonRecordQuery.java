package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.ForcedJoinDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.records.person.PersonRecord;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameDataRecord;

import java.util.*;

/**
 * Container for a query for Persons, defining fields and database lookup
 */
public class PersonRecordQuery extends BaseQuery {

    public static final String PERSONNUMMER = PersonEntity.IO_FIELD_CPR_NUMBER;
    public static final String FORNAVNE = "fornavn";
    public static final String EFTERNAVN = "efternavn";
    public static final String KOMMUNEKODE = "cprKommunekode";
    public static final String VEJKODE = "cprVejkode";
    public static final String DOOR = "doer";
    public static final String FLOOR = "floor";
    public static final String HOUSENO = "houseno";
    public static final String BUILDINGNO = "buildingno";

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONNUMMER)
    private List<String> personnumre = new ArrayList<>();

    public Collection<String> getPersonnumre() {
        return this.personnumre;
    }

    public void addPersonnummer(String personnummer) {
        this.personnumre.add(personnummer);
        if (personnummer != null) {
            this.increaseDataParamCount();
            this.addParameter();
        }
    }

    public void setPersonnummer(String personnummer) {
        this.personnumre.clear();
        this.addPersonnummer(personnummer);
        this.addParameter();
    }

    public void setPersonnumre(Collection<String> personnumre) {
        this.personnumre.clear();
        if (personnumre != null) {
            this.personnumre.addAll(personnumre);
            this.increaseDataParamCount();
            this.addParameter();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = FORNAVNE)
    private List<String> fornavn = new ArrayList<>();

    public List<String> getFornavn() {
        return fornavn;
    }

    public void clearFornavn() {
        this.fornavn.clear();
        this.addParameter();
    }
    public void addFornavn(String fornavn) {
        this.fornavn.add(fornavn);
        if (fornavn != null) {
            this.increaseDataParamCount();
            this.addParameter();
        }
    }

    public void setFornavne(Collection<String> fornavne) {
        this.fornavn.clear();
        if (fornavne != null) {
            this.fornavn.addAll(fornavne);
            this.increaseDataParamCount();
            this.addParameter();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = EFTERNAVN)
    private List<String> efternavn = new ArrayList<>();

    public List<String> getEfternavn() {
        return efternavn;
    }
    public void clearEfternavn() {
        this.efternavn.clear();
        this.addParameter();
    }

    public void setEfternavn(String efternavn) {
        this.efternavn.clear();
        this.efternavn.add(efternavn);
        if (efternavn != null) {
            this.increaseDataParamCount();
            this.addParameter();
        }
    }
    public void setEfternavne(Collection<String> efternavne) {
        this.efternavn.clear();
        if (efternavne != null) {
            this.efternavn.addAll(efternavne);
            this.increaseDataParamCount();
            this.addParameter();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekoder = new ArrayList<>();

    public Collection<String> getKommunekoder() {
        return this.kommunekoder;
    }

    public void addKommunekode(String kommunekode) {
        this.kommunekoder.add(kommunekode);
        this.addParameter();
    }

    public void addKommunekode(int kommunekode) {
        this.addKommunekode(String.format("%03d", kommunekode));
    }

    public void clearKommunekode() {
        this.kommunekoder.clear();
        this.addParameter();
    }
    public void setKommunekoder(Collection<String> kommunekoder) {
        this.kommunekoder.clear();
        if (kommunekoder != null) {
            this.kommunekoder.addAll(kommunekoder);
            this.increaseDataParamCount();
            this.addParameter();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = VEJKODE)
    private List<String> vejkoder = new ArrayList<>();

    public Collection<String> getVejkoder() {
        return this.vejkoder;
    }

    public void addVejkode(String vejkode) {
        this.vejkoder.add(vejkode);
        this.addParameter();
    }

    public void addVejkode(int vejkode) {
        this.addVejkode(String.format("%03d", vejkode));
    }

    public void clearVejkode() {
        this.vejkoder.clear();
        this.addParameter();
    }
    public void setVejkoder(Collection<String> vejkoder) {
        this.vejkoder.clear();
        if (vejkoder != null) {
            this.vejkoder.addAll(vejkoder);
            this.addParameter();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = DOOR)
    private List<String> doors = new ArrayList<>();

    public Collection<String> getDoors() {
        return this.doors;
    }

    public void addDoor(String door) {
        this.doors.add(door);
        this.addParameter();
    }

    public void clearDoor() {
        this.doors.clear();
        this.addParameter();
    }
    public void setDoors(Collection<String> doors) {
        this.doors.clear();
        if (doors != null) {
            this.doors.addAll(doors);
            this.addParameter();
        }
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = FLOOR)
    private List<String> floors = new ArrayList<>();

    public Collection<String> getFloors() {
        return this.floors;
    }

    public void addFloor(String floor) {
        this.floors.add(floor);
        this.addParameter();
    }

    public void clearFloor() {
        this.floors.clear();
        this.addParameter();
    }
    public void setFloors(Collection<String> floors) {
        this.floors.clear();
        if (floors != null) {
            this.floors.addAll(floors);
            this.addParameter();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = HOUSENO)
    private List<String> houseNos = new ArrayList<>();

    public Collection<String> getHouseNos() {
        return this.houseNos;
    }

    public void addHouseNo(String houseNo) {
        this.houseNos.add(houseNo);
        this.addParameter();
    }

    public void clearHouseNo() {
        this.houseNos.clear();
        this.addParameter();
    }
    public void setHouseNos(Collection<String> houseNos) {
        this.houseNos.clear();
        if (houseNos != null) {
            this.houseNos.addAll(houseNos);
            this.addParameter();
        }
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = BUILDINGNO)
    private List<String> buildingNos = new ArrayList<>();

    public Collection<String> getBuildingNos() {
        return this.buildingNos;
    }

    public void addBuildingNo(String houseNo) {
        this.buildingNos.add(houseNo);
        this.addParameter();
    }

    public void clearBuildingNo() {
        this.buildingNos.clear();
        this.addParameter();
    }
    public void setBuildingNos(Collection<String> buildingNos) {
        this.buildingNos.clear();
        if (buildingNos != null) {
            this.buildingNos.addAll(buildingNos);
            this.addParameter();
        }
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(PERSONNUMMER, this.personnumre);
        map.put(FORNAVNE, this.fornavn);
        map.put(EFTERNAVN, this.efternavn);
        map.put(KOMMUNEKODE, this.kommunekoder);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setPersonnumre(parameters.get(PERSONNUMMER));
        this.setFornavne(parameters.get(FORNAVNE));
        this.setEfternavne(parameters.get(EFTERNAVN));
        this.setKommunekoder(parameters.get(KOMMUNEKODE));
        this.setDoors(parameters.get(DOOR));
        this.setFloors(parameters.get(FLOOR));
        this.setHouseNos(parameters.get(HOUSENO));
        this.setBuildingNos(parameters.get(BUILDINGNO));
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
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

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
    }


    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = new BaseLookupDefinition();
        
        if (!this.getPersonnumre().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_CPR_NUMBER, this.getPersonnumre(), String.class);
        }

        if (this.getFornavn() != null) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_NAME + LookupDefinition.separator + NameDataRecord.DB_FIELD_FIRST_NAMES, this.getFornavn(), String.class);
        }
        if (this.getEfternavn() != null) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_NAME + LookupDefinition.separator + NameDataRecord.DB_FIELD_LAST_NAME, this.getEfternavn(), String.class);
        }

        String addressPath = LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_ADDRESS;
        boolean joinedAddress = false;
        
        if (!this.getKommunekoder().isEmpty()) {
            lookupDefinition.put(addressPath + LookupDefinition.separator + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE, this.getKommunekoder(), Integer.class);
            joinedAddress = true;
        }
        if (!this.getKommunekodeRestriction().isEmpty()) {
            lookupDefinition.put(addressPath + LookupDefinition.separator + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE, this.getKommunekodeRestriction(), Integer.class);
            joinedAddress = true;
        }

        if (!this.getVejkoder().isEmpty()) {
            lookupDefinition.put(addressPath + LookupDefinition.separator + AddressDataRecord.DB_FIELD_ROAD_CODE, this.getVejkoder(), Integer.class);
            joinedAddress = true;
        }

        if (!this.getDoors().isEmpty()) {
            lookupDefinition.put(addressPath + LookupDefinition.separator + AddressDataRecord.DB_FIELD_DOOR, this.getDoors(), String.class);
            joinedAddress = true;
        }
        if (!this.getFloors().isEmpty()) {
            lookupDefinition.put(addressPath + LookupDefinition.separator + AddressDataRecord.DB_FIELD_FLOOR, this.getFloors(), String.class);
            joinedAddress = true;
        }
        if (!this.getHouseNos().isEmpty()) {
            lookupDefinition.put(addressPath + LookupDefinition.separator + AddressDataRecord.DB_FIELD_HOUSENUMBER, this.getHouseNos(), String.class);
            joinedAddress = true;
        }
        if (!this.getBuildingNos().isEmpty()) {
            lookupDefinition.put(addressPath + LookupDefinition.separator + AddressDataRecord.DB_FIELD_BUILDING_NUMBER, this.getBuildingNos(), String.class);
            joinedAddress = true;
        }


        return lookupDefinition;
    }

}
