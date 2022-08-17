package dk.magenta.datafordeler.cvr.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.*;
import dk.magenta.datafordeler.cvr.DirectLookup;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.CompanyForm;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Container for a query for Companies, defining fields and database lookup
 */
public class CompanyRecordQuery extends BaseQuery {

    public static final String CVRNUMMER = CompanyRecord.IO_FIELD_CVR_NUMBER;
    public static final String REKLAMEBESKYTTELSE = CompanyRecord.IO_FIELD_ADVERTPROTECTION;
    public static final String NAVN = CompanyRecord.IO_FIELD_NAMES;
    public static final String TELEFONNUMMER = CompanyRecord.IO_FIELD_PHONE;
    public static final String TELEFAXNUMMER = CompanyRecord.IO_FIELD_FAX;
    public static final String EMAILADRESSE = CompanyRecord.IO_FIELD_EMAIL;
    public static final String VIRKSOMHEDSFORM = CompanyRecord.IO_FIELD_FORM;
    public static final String KOMMUNEKODE = Municipality.IO_FIELD_CODE;
    public static final String VEJKODE = AddressRecord.IO_FIELD_ROADCODE;
    public static final String HUSNUMMER = "husnummer";
    public static final String ETAGE = AddressRecord.IO_FIELD_FLOOR;
    public static final String DOOR = AddressRecord.IO_FIELD_DOOR;
    public static final String ORGANIZATIONTYPE = "organisationstype";
    public static final String LASTUPDATED = CvrBitemporalRecord.IO_FIELD_LAST_UPDATED;
    public static final String COMPANYDATAEVENT = CompanyDataEventRecord.DB_FIELD_FIELD;
    public static final String COMPANYDATAEVENTTIME = CompanyDataEventRecord.DB_FIELD_TIMESTAMP;


    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENT)
    private List<String> companydataevents = new ArrayList<>();

    public Collection<String> getDataEvents() {
        return this.companydataevents;
    }

    public void addDataEvent(String persondataevent) {
        this.companydataevents.add(persondataevent);
        if (persondataevent != null) {
            this.updatedParameters();
        }
    }

    public void setDataEvent(String personevent) {
        this.clearDataEvents();
        this.addDataEvent(personevent);
    }

    public void clearDataEvents() {
        this.companydataevents.clear();
        this.updatedParameters();
    }

    public void setDataEvents(Collection<String> personevent) {
        this.clearDataEvents();
        if (personevent != null) {
            this.companydataevents.addAll(personevent);
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private OffsetDateTime companyrecordeventTimeAfter;

    public void setDataEventTimeAfter(OffsetDateTime companyrecordeventTimeAfter) {
        this.companyrecordeventTimeAfter = companyrecordeventTimeAfter;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private OffsetDateTime companyrecordeventTimeBefore;

    public void setDataEventTimeBefore(OffsetDateTime companyrecordeventTimeBefore) {
        this.companyrecordeventTimeBefore = companyrecordeventTimeBefore;
        this.updatedParameters();
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : new String[]{
                CVRNUMMER, REKLAMEBESKYTTELSE, NAVN, TELEFONNUMMER, TELEFAXNUMMER,
                EMAILADRESSE, VIRKSOMHEDSFORM, KOMMUNEKODE, VEJKODE, HUSNUMMER,
                ETAGE, DOOR, ORGANIZATIONTYPE, LASTUPDATED
        }) {
            map.put(key, this.getParameters(key));
        }
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        for (String key : new String[]{
                CVRNUMMER, NAVN, TELEFONNUMMER, TELEFAXNUMMER,
                EMAILADRESSE, VIRKSOMHEDSFORM, KOMMUNEKODE, VEJKODE, HUSNUMMER,
                ETAGE, DOOR, ORGANIZATIONTYPE
        }) {
            this.setParameter(key, parameters.getI(key));
        }

        for (String key : new String[]{
                REKLAMEBESKYTTELSE, LASTUPDATED
        }) {
            this.setParameter(key, parameters.getFirst(key));
        }
    }

    @Override
    protected boolean isEmpty() {
        return this.parametersEmpty();
    }

    @Override
    protected Object castFilterParam(Object input, String filter) {
        return super.castFilterParam(input, filter);
    }

    @Override
    public String getEntityClassname() {
        return CompanyRecord.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "cvr_company";
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("cvr", CompanyRecord.DB_FIELD_CVR_NUMBER);
        joinHandles.put("formcode", CompanyRecord.DB_FIELD_FORM + BaseQuery.separator + FormRecord.DB_FIELD_FORM + BaseQuery.separator + CompanyForm.DB_FIELD_CODE);
        joinHandles.put("advertprotection", CompanyRecord.DB_FIELD_ADVERTPROTECTION);
        joinHandles.put("name", CompanyRecord.DB_FIELD_NAMES + BaseQuery.separator + SecNameRecord.DB_FIELD_NAME);
        joinHandles.put("phone", CompanyRecord.DB_FIELD_PHONE + BaseQuery.separator + ContactRecord.DB_FIELD_DATA);
        joinHandles.put("fax", CompanyRecord.DB_FIELD_FAX + BaseQuery.separator +ContactRecord.DB_FIELD_DATA);
        joinHandles.put("email", CompanyRecord.DB_FIELD_EMAIL + BaseQuery.separator + ContactRecord.DB_FIELD_DATA);
        joinHandles.put("municipalitycode", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + Municipality.DB_FIELD_CODE);
        joinHandles.put("roadcode", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_ROADCODE);
        joinHandles.put("housenumberfrom", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_HOUSE_FROM);
        joinHandles.put("housenumberto", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_HOUSE_TO);
        joinHandles.put("floor", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_FLOOR);
        joinHandles.put("door", CompanyRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_DOOR);
        joinHandles.put("participantUnitNumber", CompanyRecord.DB_FIELD_PARTICIPANTS + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_PARTICIPANT_RELATION + BaseQuery.separator + RelationParticipantRecord.DB_FIELD_UNITNUMBER);
        joinHandles.put("participantOrganizationType", CompanyRecord.DB_FIELD_PARTICIPANTS + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_ORGANIZATIONS + BaseQuery.separator + OrganizationRecord.DB_FIELD_MAIN_TYPE);
        joinHandles.put("lastUpdated", CvrBitemporalRecord.DB_FIELD_LAST_UPDATED);
        joinHandles.put("companyrecordeventTime.GTE", CompanyRecord.DB_FIELD_DATAEVENT + BaseQuery.separator + CompanyDataEventRecord.DB_FIELD_TIMESTAMP);
        joinHandles.put("companyrecordeventTime.LTE", CompanyRecord.DB_FIELD_DATAEVENT + BaseQuery.separator + CompanyDataEventRecord.DB_FIELD_TIMESTAMP);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        this.addCondition("cvr", CVRNUMMER, Integer.class);
        this.addCondition("formcode", VIRKSOMHEDSFORM, String.class);
        this.addCondition("advertprotection", REKLAMEBESKYTTELSE, Boolean.class);
        this.addCondition("name", NAVN, String.class);
        this.addCondition("phone", TELEFONNUMMER, String.class);
        this.addCondition("fax", TELEFAXNUMMER, String.class);
        this.addCondition("email", EMAILADRESSE, String.class);
        this.addCondition("municipalitycode", KOMMUNEKODE, Integer.class);
        this.addCondition("roadcode", VEJKODE, Integer.class);
        this.addCondition("floor", ETAGE, String.class);
        this.addCondition("door", DOOR, String.class);
        this.addCondition("municipalitycode", this.getKommunekodeRestriction(), Integer.class);

        List<String> husnummer = this.getParameters(HUSNUMMER);
        if (husnummer != null && !husnummer.isEmpty()) {
            // Match hvis housenumberfrom == value   eller   housenumberfrom <= value og housenumberto >= value
            MultiCondition multiCondition = new MultiCondition(this.getCondition(), "OR");
            this.addCondition(multiCondition);
            this.makeCondition(multiCondition, "housenumberfrom", Condition.Operator.EQ, husnummer, Integer.class, false);
            MultiCondition rangeCondition = new MultiCondition(multiCondition);
            multiCondition.add(rangeCondition);
            this.makeCondition(rangeCondition, "housenumberfrom", Condition.Operator.LTE, husnummer, Integer.class, false);
            this.makeCondition(rangeCondition, "housenumberto", Condition.Operator.GTE, husnummer, Integer.class, false);
        }

        List<String> organizationType = this.getParameters(ORGANIZATIONTYPE);
        if (organizationType != null) {
            this.addCondition("participantOrganizationType", organizationType);
        }
        QueryParameter lastUpdated = this.getParameters(LASTUPDATED);
        if (lastUpdated != null) {
            this.addCondition("lastUpdated", Condition.Operator.GT, lastUpdated, OffsetDateTime.class, false);
        }
        if (this.companyrecordeventTimeAfter != null) {
            this.addCondition("companyrecordeventTime.GTE", Condition.Operator.GTE, this.companyrecordeventTimeAfter, OffsetDateTime.class, false);
        }
        if (this.companyrecordeventTimeBefore != null) {
            this.addCondition("companyrecordeventTime.LTE", Condition.Operator.LTE, this.companyrecordeventTimeBefore, OffsetDateTime.class, false);
        }
    }


    public ObjectNode getDirectLookupQuery(ObjectMapper objectMapper) {
        boolean anySet = false;
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode query = DirectLookup.addObject(objectMapper, root, "query");
        ObjectNode bool = DirectLookup.addObject(objectMapper, query, "bool");
        ArrayNode must = DirectLookup.addList(objectMapper, bool, "must");

        List<String> cvrNumre = this.getParameters(CVRNUMMER);
        if (cvrNumre != null && !cvrNumre.isEmpty()) {
            ObjectNode wrap = DirectLookup.addObject(objectMapper, must);
            ObjectNode terms = DirectLookup.addObject(objectMapper, wrap, "terms");
            ArrayNode cvrNumber = DirectLookup.addList(objectMapper, terms, "Vrvirksomhed.cvrNummer");
            for (String cvr : cvrNumre) {
                cvrNumber.add(cvr);
            }
            anySet = true;
        }

        if (!anySet) {
            return null;
        }

        return root;
    }
}
