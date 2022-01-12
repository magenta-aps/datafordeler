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

    public static final String LASTUPDATED = CvrBitemporalRecord.IO_FIELD_LAST_UPDATED;
    public static final String COMPANYDATAEVENT = CompanyDataEventRecord.DB_FIELD_FIELD;
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


    @Override
    public Map<String, Object> getSearchParameters() {
        Map<String, Object> map = super.getSearchParameters();
        map.put("companyStatus", this.companyStatus);
        map.put("companyStatusValidity.GTE", this.companyrecordValidityTimeGTE);
        map.put("companyStatusValidity.LTE", this.companyrecordValidityTimeLTE);
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
                this.companyrecordValidityTimeGTE != null && this.companyrecordValidityTimeLTE != null;
    }

    @Override
    protected Map<String, String> joinHandles() {

        Map<String, String> joinHandles = super.joinHandles();

        joinHandles.put("companyStatus", "companyStatus" + BaseQuery.separator + "status");
        joinHandles.put("companyStatusValidity.GTE", "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        joinHandles.put("companyStatusValidity.LTE", "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
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
    }
}
