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
 * Container for a query for Company units, defining fields and database lookup
 */
public class CompanyUnitRecordQuery extends BaseQuery {


    public static final String P_NUMBER = CompanyUnitRecord.IO_FIELD_P_NUMBER;
    public static final String ASSOCIATED_COMPANY_CVR = CompanyRecord.IO_FIELD_CVR_NUMBER;
    public static final String PRIMARYINDUSTRY = CompanyUnitRecord.IO_FIELD_PRIMARY_INDUSTRY;
    public static final String KOMMUNEKODE = Municipality.IO_FIELD_CODE;
    public static final String VEJKODE = AddressRecord.IO_FIELD_ROADCODE;
    public static final String LASTUPDATED = CvrBitemporalRecord.IO_FIELD_LAST_UPDATED;


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : new String[]{
                P_NUMBER, ASSOCIATED_COMPANY_CVR, PRIMARYINDUSTRY, KOMMUNEKODE, VEJKODE, LASTUPDATED
        }) {
            map.put(key, this.getParameter(key));
        }
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        for (String key : new String[]{P_NUMBER}) {
            ensureNumeric(key, parameters.getI(key), true);
        }
        for (String key : new String[]{KOMMUNEKODE, VEJKODE}) {
            ensureNumeric(key, parameters.getI(key));
        }
        for (String key : new String[]{
                P_NUMBER, ASSOCIATED_COMPANY_CVR, PRIMARYINDUSTRY, KOMMUNEKODE, VEJKODE,
        }) {
            this.setParameter(key, parameters.getI(key));
        }
        for (String key : new String[]{
                LASTUPDATED
        }) {
            String value = parameters.getFirstI(key);
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
        return CompanyUnitRecord.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "cvr_companyunit";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("pnr", CompanyUnitRecord.DB_FIELD_P_NUMBER);
        joinHandles.put("cvr", CompanyUnitRecord.DB_FIELD_COMPANY_LINK + BaseQuery.separator + CompanyLinkRecord.DB_FIELD_CVRNUMBER);
        joinHandles.put("primaryindustrycode", CompanyUnitRecord.DB_FIELD_PRIMARY_INDUSTRY + BaseQuery.separator + CompanyIndustryRecord.DB_FIELD_CODE);
        joinHandles.put("municipalitycode", CompanyUnitRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + Municipality.DB_FIELD_CODE);
        joinHandles.put("roadcode", CompanyUnitRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_ROADCODE);
        joinHandles.put("lastUpdated", CvrBitemporalRecord.DB_FIELD_LAST_UPDATED);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("pnr", P_NUMBER, Integer.class);
        this.addCondition("cvr", ASSOCIATED_COMPANY_CVR, Integer.class);
        this.addCondition("primaryindustrycode", PRIMARYINDUSTRY, String.class);
        this.addCondition("municipalitycode", KOMMUNEKODE, Integer.class);
        this.addCondition("roadcode", VEJKODE, Integer.class);
        this.addCondition("municipalitycode", this.getKommunekodeRestriction(), Integer.class);
        this.addCondition("lastUpdated", Condition.Operator.GT, this.getParameter(LASTUPDATED).asOffsetDateTime(), OffsetDateTime.class, false);
    }
}
