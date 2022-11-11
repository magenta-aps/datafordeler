package dk.magenta.datafordeler.eskat.query;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyParticipantRelationRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;

import java.time.LocalDate;
import java.util.Map;

/**
 * Container for a query for Participants, defining fields and database lookup
 */
public class EskatParticipantRecordQuery extends ParticipantRecordQuery {

    public static final String ASSOCIATED_COMPANY_CVR = CompanyRecord.IO_FIELD_CVR_NUMBER;
    public static final String ASSOCIATED_COMPANY_NAME = CompanyRecord.IO_FIELD_NAMES;
    public static final String ASSOCIATED_COMPANY_STATUS = CompanyRecord.IO_FIELD_STATUS;

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private LocalDate relationstartTimeGTE;

    public void setRelationStartTimeGTE(LocalDate relationstartTimeGTE) {
        this.relationstartTimeGTE = relationstartTimeGTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private LocalDate relationstartTimeLTE;

    public void setRelationStartTimeLTE(LocalDate relationstartTimeLTE) {
        this.relationstartTimeLTE = relationstartTimeLTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private LocalDate relationendTimeGTE;

    public void setRelationEndTimeGTE(LocalDate relationendTimeGTE) {
        this.relationendTimeGTE = relationendTimeGTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private LocalDate relationendTimeLTE;

    public void setRelationEndTimeLTE(LocalDate relationendTimeLTE) {
        this.relationendTimeLTE = relationendTimeLTE;
        this.updatedParameters();
    }


    @Override
    protected boolean isEmpty() {
        return this.parametersEmpty()
                && this.relationstartTimeGTE == null && this.relationstartTimeLTE == null
                && this.relationendTimeGTE == null && this.relationendTimeLTE == null;
    }

    public boolean isSearchSet() {
        return !this.isEmpty();
    }


    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        for (String key : new String[]{
                ASSOCIATED_COMPANY_CVR, ASSOCIATED_COMPANY_NAME, ASSOCIATED_COMPANY_STATUS,
        }) {
            this.setParameter(key, parameters.getI(key));
        }
    }

    @Override
    protected Map<String, String> joinHandles() {

        Map<String, String> joinHandles = super.joinHandles();

        joinHandles.put("cvrNumber", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "cvrNumber");
        joinHandles.put("companyNames", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "names" + BaseQuery.separator + "name");
        joinHandles.put("companyStatus", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "companyStatus" + BaseQuery.separator + "status");
        joinHandles.put("companyStatusStart.GTE", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        joinHandles.put("companyStatusStart.LTE", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        joinHandles.put("companyStatusEnd.GTE", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validTo");
        joinHandles.put("companyStatusEnd.LTE", ParticipantRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION + BaseQuery.separator + "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validTo");
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("cvrNumber", ASSOCIATED_COMPANY_CVR, Long.class);
        this.addCondition("companyNames", ASSOCIATED_COMPANY_NAME, String.class);
        this.addCondition("companyStatus", ASSOCIATED_COMPANY_STATUS, String.class);

        if (this.relationstartTimeGTE != null) {
            this.addCondition("companyStatusStart.GTE", Condition.Operator.GTE, this.relationstartTimeGTE, LocalDate.class, false);
        }
        if (this.relationstartTimeLTE != null) {
            this.addCondition("companyStatusStart.LTE", Condition.Operator.LTE, this.relationstartTimeLTE, LocalDate.class, false);
        }
        if (this.relationendTimeGTE != null) {
            this.addCondition("companyStatusStart.GTE", Condition.Operator.GTE, this.relationendTimeGTE, LocalDate.class, false);
        }
        if (this.relationendTimeLTE != null) {
            this.addCondition("companyStatusStart.LTE", Condition.Operator.LTE, this.relationendTimeLTE, LocalDate.class, false);
        }
    }


}
