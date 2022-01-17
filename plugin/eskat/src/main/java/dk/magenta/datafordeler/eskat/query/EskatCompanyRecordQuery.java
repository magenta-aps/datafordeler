package dk.magenta.datafordeler.eskat.query;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.*;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.eskat.utils.DateConverter;

import java.time.LocalDate;
import java.util.*;

/**
 * Container for a query for Companies, defining fields and database lookup
 */
public class EskatCompanyRecordQuery extends CompanyRecordQuery {

    public static final String COMPANYDATAEVENTTIME = CompanyDataEventRecord.DB_FIELD_TIMESTAMP;


    private List<String> companyStatus = new ArrayList<>();

    public Collection<String> getCompanyStatus() {
        return this.companyStatus;
    }

    public void addCompanyStatus(String organizationType) {
        if (organizationType != null) {
            this.companyStatus.add(organizationType);
            this.updatedParameters();
        }
    }

    public void setCompanyStatus(int organizationType) {
        this.companyStatus.clear();
        this.addCompanyStatus(Integer.toString(organizationType));
    }
    public void setCompanyStatus(String organizationType) {
        this.companyStatus.clear();
        this.addCompanyStatus(organizationType);
    }

    public void setCompanyStatus(Collection<String> organizationTypes) {
        this.clearCompanyStatus();
        if (companyStatus != null) {
            for (String organizationType : organizationTypes) {
                this.addCompanyStatus(organizationType);
            }
        }
    }

    public void clearCompanyStatus() {
        this.companyStatus.clear();
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private LocalDate companyrecordValidityTimeGTE;

    public void setCompanyStatusValidityFromGTE(LocalDate companyrecordValidityTimeGTE) {
        this.companyrecordValidityTimeGTE = companyrecordValidityTimeGTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private LocalDate companyrecordValidityTimeLTE;

    public void setCompanyStatusValidityFromLTE(LocalDate companyrecordValidityTimeLTE) {
        this.companyrecordValidityTimeLTE = companyrecordValidityTimeLTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private LocalDate companyStartDateGTE;

    public void setCompanyStartDateGTE(LocalDate companyrecordValidityTimeLTE) {
        this.companyStartDateGTE = companyrecordValidityTimeLTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private LocalDate companyStartDateLTE;

    public void setCompanyStartDateLTE(LocalDate companyrecordValidityTimeLTE) {
        this.companyStartDateLTE = companyrecordValidityTimeLTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private LocalDate companyEndDateGTE;

    public void setCompanyEndDateGTE(LocalDate companyrecordValidityTimeLTE) {
        this.companyEndDateGTE = companyrecordValidityTimeLTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private LocalDate companyEndDateLTE;

    public void setCompanyEndDateLTE(LocalDate companyrecordValidityTimeLTE) {
        this.companyEndDateLTE = companyrecordValidityTimeLTE;
        this.updatedParameters();
    }



    @Override
    public Map<String, Object> getSearchParameters() {
        Map<String, Object> map = super.getSearchParameters();
        map.put("companyStatus", this.companyStatus);
        map.put("companyStatusValidity.GTE", this.companyrecordValidityTimeGTE);
        map.put("companyStatusValidity.LTE", this.companyrecordValidityTimeLTE);
        map.put("companyStartDate.GTE", this.companyStartDateGTE);
        map.put("companyStartDate.LTE", this.companyStartDateLTE);
        map.put("companyEndDate.GTE", this.companyEndDateGTE);
        map.put("companyEndDate.LTE", this.companyEndDateLTE);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        super.setFromParameters(parameters);
        this.setCompanyStatus(parameters.getI("companyStatus"));
        this.setCompanyStatusValidityFromGTE(DateConverter.parseDate(parameters.getFirst("companyStatusValidity.GTE")));
        this.setCompanyStatusValidityFromLTE(DateConverter.parseDate(parameters.getFirst("companyStatusValidity.LTE")));
    }

    @Override
    protected boolean isEmpty() {
        return super.isEmpty() && this.companyStatus.isEmpty() &&
                this.companyrecordValidityTimeGTE == null && this.companyrecordValidityTimeLTE == null &&
                this.companyStartDateGTE == null && this.companyStartDateLTE == null && this.companyEndDateGTE == null && this.companyEndDateLTE == null;
    }

    public boolean isSearchSet() {
        return !this.isEmpty();
    }

    @Override
    protected Map<String, String> joinHandles() {

        Map<String, String> joinHandles = super.joinHandles();
        joinHandles.put("companyStatus", "companyStatus" + BaseQuery.separator + "status");
        joinHandles.put("companyStatusValidity.GTE", "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        joinHandles.put("companyStatusValidity.LTE", "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        joinHandles.put("companyStartDate.GTE", "lifecycle" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        joinHandles.put("companyStartDate.LTE", "lifecycle" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        joinHandles.put("companyEndDate.GTE", "lifecycle" + BaseQuery.separator + "validity" + BaseQuery.separator + "validTo");
        joinHandles.put("companyEndDate.LTE", "lifecycle" + BaseQuery.separator + "validity" + BaseQuery.separator + "validTo");
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("companyStatus", this.companyStatus);
        if (this.companyrecordValidityTimeGTE != null) {
            this.addCondition("companyStatusValidity.GTE", Condition.Operator.GTE, this.companyrecordValidityTimeGTE, LocalDate.class, false);
        }
        if (this.companyrecordValidityTimeLTE != null) {
            this.addCondition("companyStatusValidity.LTE", Condition.Operator.LTE, this.companyrecordValidityTimeLTE, LocalDate.class, false);
        }

        if (this.companyStartDateGTE != null) {
            this.addCondition("companyStartDate.GTE", Condition.Operator.GTE, this.companyStartDateGTE, LocalDate.class, false);
        }
        if (this.companyStartDateLTE != null) {
            this.addCondition("companyStartDate.LTE", Condition.Operator.LTE, this.companyStartDateLTE, LocalDate.class, false);
        }
        if (this.companyEndDateGTE != null) {
            this.addCondition("companyEndDate.GTE", Condition.Operator.GTE, this.companyEndDateGTE, LocalDate.class, false);
        }
        if (this.companyEndDateLTE != null) {
            this.addCondition("companyEndDate.LTE", Condition.Operator.LTE, this.companyEndDateLTE, LocalDate.class, false);
        }
    }
}
