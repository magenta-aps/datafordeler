package dk.magenta.datafordeler.cvr.query;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for a query for Participants, defining fields and database lookup
 */
public class ParticipantRecordQuery extends BaseQuery {

    public static final String UNITNUMBER = ParticipantRecord.IO_FIELD_UNIT_NUMBER;
    public static final String ASSOCIATED_COMPANY_CVR = CompanyRecord.IO_FIELD_CVR_NUMBER;
    public static final String NAVN = ParticipantRecord.IO_FIELD_NAMES;
    public static final String KOMMUNEKODE = Municipality.IO_FIELD_CODE;
    public static final String VEJKODE = AddressRecord.IO_FIELD_ROADCODE;
    public static final String FORRETNINGSNOEGLE = ParticipantRecord.IO_FIELD_BUSINESS_KEY;
    public static final String LASTUPDATED = CvrBitemporalRecord.IO_FIELD_LAST_UPDATED;

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : new String[]{
                UNITNUMBER, ASSOCIATED_COMPANY_CVR, NAVN, KOMMUNEKODE, VEJKODE, FORRETNINGSNOEGLE
        }) {
            map.put(key, this.getParameter(key));
        }
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        for (String key : new String[]{UNITNUMBER, ASSOCIATED_COMPANY_CVR, KOMMUNEKODE, VEJKODE, FORRETNINGSNOEGLE}) {
            ensureNumeric(key, parameters.getI(key));
        }
        for (String key : new String[]{
                UNITNUMBER, ASSOCIATED_COMPANY_CVR, NAVN, KOMMUNEKODE, VEJKODE, FORRETNINGSNOEGLE
        }) {
            this.setParameter(key, parameters.getI(key));
        }
        for (String key : new String[]{
                LASTUPDATED
        }) {
            String value = parameters.getFirst(key);
            ensureTemporal(key, value);
            this.setParameter(key, value);
        }
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.parametersEmpty();
    }

    @Override
    protected Object castFilterParam(Object input, String filter) {
        return super.castFilterParam(input, filter);
    }

    @Override
    public String getEntityClassname() {
        return ParticipantRecord.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "cvr_participant";
    }


    public static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("unit", ParticipantRecord.DB_FIELD_UNIT_NUMBER);
        joinHandles.put("cvr", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + RelationCompanyRecord.DB_FIELD_CVRNUMBER);
        joinHandles.put("name", ParticipantRecord.DB_FIELD_NAMES + BaseQuery.separator + SecNameRecord.DB_FIELD_NAME);
        joinHandles.put("municipalitycode", ParticipantRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + Municipality.DB_FIELD_CODE);
        joinHandles.put("roadcode", ParticipantRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_ROADCODE);
        joinHandles.put("businessKey", ParticipantRecord.DB_FIELD_BUSINESS_KEY);
        joinHandles.put("unitCode", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + RelationParticipantRecord.DB_FIELD_UNITNUMBER);
        joinHandles.put("lastUpdated", CvrBitemporalRecord.DB_FIELD_LAST_UPDATED);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("unit", UNITNUMBER, Long.class);
        this.addCondition("cvr", ASSOCIATED_COMPANY_CVR, Long.class);
        this.addCondition("name", NAVN, String.class);
        this.addCondition("municipalitycode", KOMMUNEKODE, Integer.class);
        this.addCondition("roadcode", VEJKODE, Integer.class);
        this.addCondition("businessKey", FORRETNINGSNOEGLE, Long.class);
        this.addCondition("lastUpdated", Condition.Operator.GT, this.getParameter(LASTUPDATED).asOffsetDateTime(), OffsetDateTime.class, false);
    }
}
