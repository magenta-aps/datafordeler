package dk.magenta.datafordeler.eskat.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.*;
import dk.magenta.datafordeler.cvr.DirectLookup;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.CompanyForm;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Container for a query for Companies, defining fields and database lookup
 */
public class EskatCompanyRecordQuery extends CompanyRecordQuery {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
    private OffsetDateTime companyrecordValidityTimeGTE;

    public void setCompanyStatusValidityFromGTE(OffsetDateTime companyrecordValidityTimeGTE) {
        this.companyrecordValidityTimeGTE = companyrecordValidityTimeGTE;
        this.updatedParameters();
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = COMPANYDATAEVENTTIME)
    private OffsetDateTime companyrecordValidityTimeLTE;

    public void setCompanyStatusValidityFromLTE(OffsetDateTime companyrecordValidityTimeLTE) {
        this.companyrecordValidityTimeLTE = companyrecordValidityTimeLTE;
        this.updatedParameters();
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        Map<String, Object> map = super.getSearchParameters();
        map.put("companyStatus", this.companyStatus);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        super.setFromParameters(parameters);
        this.setCompanyStatus(parameters.getI("companyStatus"));
        this.setDataEventTimeAfter(BaseQuery.parseDateTime(parameters.getFirst("companyrecordeventTime.GTE")));
        this.setDataEventTimeBefore(BaseQuery.parseDateTime(parameters.getFirst("companyrecordeventTime.LTE")));
    }

    @Override
    protected boolean isEmpty() {
        return super.isEmpty() && this.companyStatus.isEmpty() &&
                this.companyrecordValidityTimeGTE != null && this.companyrecordValidityTimeLTE != null;
    }


    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {

        joinHandles.put("companyrecordeventTime.GTE", CompanyRecord.DB_FIELD_DATAEVENT + BaseQuery.separator + CompanyDataEventRecord.DB_FIELD_TIMESTAMP);
        joinHandles.put("companyrecordeventTime.LTE", CompanyRecord.DB_FIELD_DATAEVENT + BaseQuery.separator + CompanyDataEventRecord.DB_FIELD_TIMESTAMP);
    }

    @Override
    protected Map<String, String> joinHandles() {

        Map<String, String> joinHandles = super.joinHandles();

        joinHandles.put("companyStatus", "companyStatus" + BaseQuery.separator + "status");
        joinHandles.put("companyStatusValidityFrom.GTE", "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        joinHandles.put("companyStatusValidityFrom.LTE", "companyStatus" + BaseQuery.separator + "validity" + BaseQuery.separator + "validFrom");
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {

        super.setupConditions();

        this.addCondition("companyStatus", this.companyStatus);
        this.addCondition("companyStatusValidityFrom", this.companyStatus);

        if (this.companyrecordValidityTimeGTE != null) {
            this.addCondition("companyStatusValidityFrom.GTE", Condition.Operator.GTE, this.companyrecordValidityTimeGTE, OffsetDateTime.class, false);
        }
        if (this.companyrecordValidityTimeLTE != null) {
            this.addCondition("companyStatusValidityFrom.LTE", Condition.Operator.LTE, this.companyrecordValidityTimeLTE, OffsetDateTime.class, false);
        }
    }

}
