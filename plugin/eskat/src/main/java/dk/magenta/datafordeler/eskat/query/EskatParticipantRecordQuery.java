package dk.magenta.datafordeler.eskat.query;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;

import java.time.LocalDate;
import java.util.*;

/**
 * Container for a query for Participants, defining fields and database lookup
 */
public class EskatParticipantRecordQuery extends ParticipantRecordQuery {

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> cvrnumber = new ArrayList<>();

    public List<String> getCvrnumber() {
        return this.cvrnumber;
    }

    public void addCvrnumber(String cvrnumber) {
        if (cvrnumber != null) {
            this.cvrnumber.add(cvrnumber);
            this.updatedParameters();
        }
    }

    public void setCvrnumber(String cvrnumber) {
        this.clearCvrnumber();
        this.addCvrnumber(cvrnumber);
    }

    public void setCvrnumber(Collection<String> cvrnumbers) {
        this.clearCvrnumber();
        if (cvrnumbers != null) {
            for (String cvrnumber : cvrnumbers) {
                this.addCvrnumber(cvrnumber);
            }
        }
    }

    public void clearCvrnumber() {
        this.cvrnumber.clear();
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> companyNames = new ArrayList<>();

    public List<String> getCompanyNames() {
        return this.companyNames;
    }

    public void addCompanyNames(String companyName) {
        if (companyName != null) {
            this.companyNames.add(companyName);
            this.updatedParameters();
        }
    }

    public void setCompanyNames(String companyName) {
        this.clearCompanyNames();
        this.addCompanyNames(companyName);
    }

    public void setCompanyNames(Collection<String> companyNames) {
        this.clearCompanyNames();
        if (companyNames != null) {
            for (String companyName : companyNames) {
                this.addCompanyNames(companyName);
            }
        }
    }

    public void clearCompanyNames() {
        this.companyNames.clear();
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAVN)
    private List<String> companyStatuses = new ArrayList<>();

    public List<String> getStatuses() {
        return this.companyStatuses;
    }

    public void addStatuses(String status) {
        if (companyStatuses != null) {
            this.companyStatuses.add(status);
            this.updatedParameters();
        }
    }

    public void setStatuses(String status) {
        this.clearStatuses();
        this.addStatuses(status);
    }

    public void setStatuses(Collection<String> companyStatuses) {
        this.clearStatuses();
        if (companyStatuses != null) {
            for (String status : companyStatuses) {
                this.addStatuses(status);
            }
        }
    }

    public void clearStatuses() {
        this.companyStatuses.clear();
        this.updatedParameters();
    }


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
    public Map<String, Object> getSearchParameters() {
        Map<String, Object> map = super.getSearchParameters();
        return map;
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
        this.addCondition("cvrNumber", this.getCvrnumber(), Long.class);
        this.addCondition("names", this.getCvrnumber(), Long.class);
        this.addCondition("companyNames", this.getCompanyNames(), String.class);
        this.addCondition("companyStatus", this.getStatuses(), String.class);

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
