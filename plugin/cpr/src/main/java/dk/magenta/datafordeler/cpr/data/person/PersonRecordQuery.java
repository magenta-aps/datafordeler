package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.records.person.data.*;

import java.time.LocalDateTime;
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
    public static final String PERSONBIRTHDATE = BirthTimeDataRecord.DB_FIELD_BIRTH_DATETIME;


    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONBIRTHDATE)
    private LocalDateTime birthTimeAfter;

    public void setBirthTimeAfter(LocalDateTime birthTimeAfter) {
        this.birthTimeAfter = birthTimeAfter;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONBIRTHDATE)
    private LocalDateTime birthTimeBefore;

    public void setBirthTimeBefore(LocalDateTime birthTimeBefore) {
        this.birthTimeBefore = birthTimeBefore;
        this.updatedParameters();
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONEVENT)
    private final List<String> personevents = new ArrayList<>();

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

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONEVENTTIME)
    private OffsetDateTime personeventTimeBefore;

    public void setEventTimeBefore(String personeventTimeBefore) {
        this.personeventTimeAfter = OffsetDateTime.parse(personeventTimeBefore, DateTimeFormatter.ISO_DATE_TIME);
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONDATAEVENT)
    private final List<String> persondataevents = new ArrayList<>();

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

    @QueryField(type = QueryField.FieldType.STRING, queryName = PERSONDATAEVENTTIME)
    private OffsetDateTime persondataeventTimeBefore;

    public void setDataEventTimeBefore(OffsetDateTime personeventTimeBefore) {
        this.persondataeventTimeBefore = personeventTimeBefore;
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : new String[]{
                PERSONNUMMER, FORNAVNE, EFTERNAVN, KOMMUNEKODE, VEJKODE, DOOR,
                FLOOR, HOUSENO, BUILDINGNO, PERSONEVENT, PERSONDATAEVENT
        }) {
            map.put(key, this.getParameter(key));
        }
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        for (String key : new String[]{PERSONNUMMER, KOMMUNEKODE, VEJKODE}) {
            ensureNumeric(key, parameters.getI(key));
        }
        for (String key : new String[]{
                PERSONNUMMER, FORNAVNE, EFTERNAVN, KOMMUNEKODE, VEJKODE, DOOR, FLOOR, HOUSENO, BUILDINGNO,
        }) {
            this.setParameter(key, parameters.getI(key));
        }
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

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("pnr", PersonEntity.DB_FIELD_CPR_NUMBER);
        joinHandles.put("firstname", PersonEntity.DB_FIELD_NAME + BaseQuery.separator + NameDataRecord.DB_FIELD_FIRST_NAMES);
        joinHandles.put("lastname", PersonEntity.DB_FIELD_NAME + BaseQuery.separator + NameDataRecord.DB_FIELD_LAST_NAME);
        joinHandles.put("municipalitycode", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE);
        joinHandles.put("roadcode", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_ROAD_CODE);
        joinHandles.put("floor", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_FLOOR);
        joinHandles.put("door", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_DOOR);
        joinHandles.put("housenumber", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_HOUSENUMBER);
        joinHandles.put("bnr", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_BUILDING_NUMBER);
        joinHandles.put("birthtime.GTE", PersonEntity.DB_FIELD_BIRTHTIME + BaseQuery.separator + BirthTimeDataRecord.DB_FIELD_BIRTH_DATETIME);
        joinHandles.put("birthtime.LTE", PersonEntity.DB_FIELD_BIRTHTIME + BaseQuery.separator + BirthTimeDataRecord.DB_FIELD_BIRTH_DATETIME);

        joinHandles.put("bnr_or_housenumber", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_BUILDING_NUMBER + "," + PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_HOUSENUMBER);
        joinHandles.put("custodyPnr", PersonEntity.DB_FIELD_CUSTODY + BaseQuery.separator + CustodyDataRecord.DB_FIELD_RELATION_PNR);
        joinHandles.put("personevent", PersonEntity.DB_FIELD_EVENT + BaseQuery.separator + PersonEventDataRecord.DB_FIELD_EVENT);
        joinHandles.put("personeventTime.GTE", PersonEntity.DB_FIELD_EVENT + BaseQuery.separator + PersonEventDataRecord.DB_FIELD_TIMESTAMP);
        joinHandles.put("personeventTime.LTE", PersonEntity.DB_FIELD_EVENT + BaseQuery.separator + PersonEventDataRecord.DB_FIELD_TIMESTAMP);
        joinHandles.put("persondataevent", PersonEntity.DB_FIELD_DATAEVENT + BaseQuery.separator + PersonDataEventDataRecord.DB_FIELD_FIELD);
        joinHandles.put("persondataeventTime.GTE", PersonEntity.DB_FIELD_DATAEVENT + BaseQuery.separator + PersonDataEventDataRecord.DB_FIELD_TIMESTAMP);
        joinHandles.put("persondataeventTime.LTE", PersonEntity.DB_FIELD_DATAEVENT + BaseQuery.separator + PersonDataEventDataRecord.DB_FIELD_TIMESTAMP);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
		super.setupConditions();
        this.addCondition("pnr", PERSONNUMMER, String.class);
        this.addCondition("firstname", FORNAVNE, String.class);
        this.addCondition("lastname", EFTERNAVN, String.class);
        this.addCondition("municipalitycode", KOMMUNEKODE, Integer.class);
        this.addCondition("roadcode", VEJKODE, Integer.class);
        this.addCondition("floor", FLOOR, String.class);
        this.addCondition("door", DOOR, String.class);
        this.addCondition("housenumber", HOUSENO, String.class);
        this.addCondition("bnr", BUILDINGNO, String.class);
        this.addCondition("custodyPnr", CUSTODYPNR, String.class);
        this.addCondition("municipalitycode", this.getKommunekodeRestriction(), Integer.class);
        this.addCondition("birthtime.GTE", Condition.Operator.GTE, this.birthTimeAfter, LocalDateTime.class, true);
        this.addCondition("birthtime.LTE", Condition.Operator.LTE, this.birthTimeBefore, LocalDateTime.class, true);

        this.addCondition("personevent", this.personevents);
        this.addCondition("personeventTime.GTE", Condition.Operator.GTE, this.personeventTimeAfter, OffsetDateTime.class, true);
        this.addCondition("personeventTime.LTE", Condition.Operator.GTE, this.personeventTimeBefore, OffsetDateTime.class, true);
        this.addCondition("persondataevent", this.persondataevents);
        this.addCondition("persondataeventTime.GTE", Condition.Operator.GTE, this.persondataeventTimeAfter, OffsetDateTime.class, true);
        this.addCondition("persondataeventTime.LTE", Condition.Operator.LTE, this.persondataeventTimeBefore, OffsetDateTime.class, true);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.parametersEmpty();
    }

}
