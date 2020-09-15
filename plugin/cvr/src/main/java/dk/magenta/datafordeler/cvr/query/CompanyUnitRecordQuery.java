package dk.magenta.datafordeler.cvr.query;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;

import java.util.*;

/**
 * Container for a query for Company units, defining fields and database lookup
 */
public class CompanyUnitRecordQuery extends BaseQuery {


    public static final String P_NUMBER = CompanyUnitRecord.IO_FIELD_P_NUMBER;
    public static final String ASSOCIATED_COMPANY_CVR = CompanyRecord.IO_FIELD_CVR_NUMBER;
    public static final String PRIMARYINDUSTRY = CompanyUnitRecord.IO_FIELD_PRIMARY_INDUSTRY;
    public static final String KOMMUNEKODE = Municipality.IO_FIELD_CODE;
    public static final String VEJKODE = AddressRecord.IO_FIELD_ROADCODE;

    @QueryField(type = QueryField.FieldType.INT, queryName = P_NUMBER)
    private List<String> pNummer = new ArrayList<>();

    public Collection<String> getPNummer() {
        return pNummer;
    }

    public void addPNummer(String pnummer) {
        if (pnummer != null) {
            this.pNummer.add(pnummer);
            this.updatedParameters();
        }
    }

    public void setPNummer(String pNumre) {
        this.clearPNummer();
        this.addPNummer(pNumre);
    }

    public void setPNummer(Collection<String> pNumre) {
        this.clearPNummer();
        if (pNumre != null) {
            for (String pNummer : pNumre) {
                this.addPNummer(pNummer);
            }
        }
    }

    public void clearPNummer() {
        this.pNummer.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.INT, queryName = ASSOCIATED_COMPANY_CVR)
    private List<String> associatedCompanyCvrNumber = new ArrayList<>();

    public Collection<String> getAssociatedCompanyCvrNummer() {
        return associatedCompanyCvrNumber;
    }

    public void addAssociatedCompanyCvrNummer(String cvrNummer) {
        if (cvrNummer != null) {
            this.associatedCompanyCvrNumber.add(cvrNummer);
            this.updatedParameters();
        }
    }

    public void setAssociatedCompanyCvrNummer(String cvrNumre) {
        this.clearAssociatedCompanyCvrNummer();
        this.addAssociatedCompanyCvrNummer(cvrNumre);
    }

    public void setAssociatedCompanyCvrNummer(Collection<String> cvrNumre) {
        this.clearAssociatedCompanyCvrNummer();
        if (cvrNumre != null) {
            for (String cvrNummer : cvrNumre) {
                this.addAssociatedCompanyCvrNummer(cvrNummer);
            }
        }
    }

    public void clearAssociatedCompanyCvrNummer() {
        this.associatedCompanyCvrNumber.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = PRIMARYINDUSTRY)
    private List<String> primaryIndustry = new ArrayList<>();

    public Collection<String> getPrimaryIndustry() {
        return primaryIndustry;
    }

    public void addPrimaryIndustry(String primaryIndustry) {
        if (primaryIndustry != null) {
            this.primaryIndustry.add(primaryIndustry);
            this.updatedParameters();
        }
    }

    public void setPrimaryIndustry(String primaryIndustry) {
        this.clearPrimaryIndustry();
        this.addPrimaryIndustry(primaryIndustry);
    }

    public void setPrimaryIndustry(Collection<String> primaryIndustries) {
        this.clearPrimaryIndustry();
        if (primaryIndustries != null) {
            for (String primaryIndustry : primaryIndustries) {
                this.addPrimaryIndustry(primaryIndustry);
            }
        }
    }

    public void clearPrimaryIndustry() {
        this.primaryIndustry.clear();
        this.updatedParameters();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekode = new ArrayList<>();

    public Collection<String> getKommuneKode() {
        return kommunekode;
    }

    public void addKommuneKode(String kommunekode) {
        if (kommunekode != null) {
            this.kommunekode.add(kommunekode);
            this.updatedParameters();
        }
    }

    public void addKommuneKode(int kommunekode) {
        this.addKommuneKode(String.format("%03d", kommunekode));
    }

    public void setKommuneKode(String kommunekode) {
        this.clearKommuneKode();
        this.addKommuneKode(kommunekode);
    }

    public void setKommuneKode(Collection<String> kommunekoder) {
        this.clearKommuneKode();
        if (kommunekoder != null) {
            for (String kommunekode : kommunekoder) {
                this.addKommuneKode(kommunekode);
            }
        }
    }

    public void clearKommuneKode() {
        this.kommunekode.clear();
        this.updatedParameters();
    }





    @QueryField(type = QueryField.FieldType.STRING, queryName = VEJKODE)
    private List<String> vejkode = new ArrayList<>();

    public Collection<String> getVejkode() {
        return vejkode;
    }

    public void addVejkode(String vejkode) {
        if (vejkode != null) {
            this.vejkode.add(vejkode);
            this.updatedParameters();
        }
    }

    public void addVejkode(int vejkode) {
        this.addVejkode(String.format("%03d", vejkode));
    }

    public void setVejkode(String vejkode) {
        this.clearVejkode();
        this.addVejkode(vejkode);
    }

    public void setVejkode(Collection<String> vejkoder) {
        this.clearVejkode();
        if (vejkoder != null) {
            for (String vejkode : vejkoder) {
                this.addVejkode(vejkode);
            }
        }
    }

    public void clearVejkode() {
        this.vejkode.clear();
        this.updatedParameters();
    }





    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(P_NUMBER, this.pNummer);
        map.put(ASSOCIATED_COMPANY_CVR, this.associatedCompanyCvrNumber);
        map.put(PRIMARYINDUSTRY, this.primaryIndustry);
        map.put(KOMMUNEKODE, this.kommunekode);
        map.put(VEJKODE, this.vejkode);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setPNummer(parameters.getI(P_NUMBER));
        this.setAssociatedCompanyCvrNummer(parameters.getI(ASSOCIATED_COMPANY_CVR));
        this.setPrimaryIndustry(parameters.getFirst(PRIMARYINDUSTRY));
        this.setKommuneKode(parameters.getI(KOMMUNEKODE));
        this.setVejkode(parameters.getI(VEJKODE));
    }

    @Override
    protected boolean isEmpty() {
        return this.pNummer.isEmpty() && this.associatedCompanyCvrNumber.isEmpty() && this.primaryIndustry.isEmpty() && this.kommunekode.isEmpty() && this.vejkode.isEmpty();
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

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("pnr", CompanyUnitRecord.DB_FIELD_P_NUMBER);
        joinHandles.put("cvr", CompanyUnitRecord.DB_FIELD_COMPANY_LINK + BaseQuery.separator + CompanyLinkRecord.DB_FIELD_CVRNUMBER);
        joinHandles.put("primaryindustrycode", CompanyUnitRecord.DB_FIELD_PRIMARY_INDUSTRY + BaseQuery.separator + CompanyIndustryRecord.DB_FIELD_CODE);
        joinHandles.put("municipalitycode", CompanyUnitRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + BaseQuery.separator + Municipality.DB_FIELD_CODE);
        joinHandles.put("roadcode", CompanyUnitRecord.DB_FIELD_LOCATION_ADDRESS + BaseQuery.separator + AddressRecord.DB_FIELD_ROADCODE);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        this.addCondition("pnr", this.pNummer, Integer.class);
        this.addCondition("cvr", this.associatedCompanyCvrNumber, Integer.class);
        this.addCondition("primaryindustrycode", this.primaryIndustry);
        this.addCondition("municipalitycode", this.kommunekode, Integer.class);
        this.addCondition("roadcode", this.vejkode, Integer.class);
        this.addCondition("municipalitycode", this.getKommunekodeRestriction(), Integer.class);
    }
}
