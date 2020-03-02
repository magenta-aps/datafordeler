package dk.magenta.datafordeler.cvr.query;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
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

    @QueryField(type = QueryField.FieldType.INT, queryName = P_NUMBER)
    private List<String> pNummer = new ArrayList<>();

    public Collection<String> getPNummer() {
        return pNummer;
    }

    public void addPNummer(String pnummer) {
        if (pnummer != null) {
            this.pNummer.add(pnummer);
            this.increaseDataParamCount();
        }
    }

    public void setPNummer(String pNumre) {
        this.pNummer.clear();
        this.addPNummer(pNumre);
    }

    public void setPNummer(Collection<String> pNumre) {
        this.pNummer.clear();
        if (pNumre != null) {
            for (String pNummer : pNumre) {
                this.addPNummer(pNummer);
            }
        }
    }

    public void clearPNummer() {
        this.pNummer.clear();
    }



    @QueryField(type = QueryField.FieldType.INT, queryName = ASSOCIATED_COMPANY_CVR)
    private List<String> associatedCompanyCvrNumber = new ArrayList<>();

    public Collection<String> getAssociatedCompanyCvrNummer() {
        return associatedCompanyCvrNumber;
    }

    public void addAssociatedCompanyCvrNummer(String cvrNummer) {
        if (cvrNummer != null) {
            this.associatedCompanyCvrNumber.add(cvrNummer);
            this.increaseDataParamCount();
        }
    }

    public void setAssociatedCompanyCvrNummer(String cvrNumre) {
        this.associatedCompanyCvrNumber.clear();
        this.addAssociatedCompanyCvrNummer(cvrNumre);
    }

    public void setAssociatedCompanyCvrNummer(Collection<String> cvrNumre) {
        this.associatedCompanyCvrNumber.clear();
        if (cvrNumre != null) {
            for (String cvrNummer : cvrNumre) {
                this.addAssociatedCompanyCvrNummer(cvrNummer);
            }
        }
    }

    public void clearAssociatedCompanyCvrNummer() {
        this.associatedCompanyCvrNumber.clear();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = PRIMARYINDUSTRY)
    private List<String> primaryIndustry = new ArrayList<>();

    public Collection<String> getPrimaryIndustry() {
        return primaryIndustry;
    }

    public void addPrimaryIndustry(String primaryIndustry) {
        if (primaryIndustry != null) {
            this.primaryIndustry.add(primaryIndustry);
            this.increaseDataParamCount();
        }
    }

    public void setPrimaryIndustry(String primaryIndustry) {
        this.primaryIndustry.clear();
        this.addPrimaryIndustry(primaryIndustry);
    }

    public void setPrimaryIndustry(Collection<String> primaryIndustries) {
        this.primaryIndustry.clear();
        if (primaryIndustries != null) {
            for (String primaryIndustry : primaryIndustries) {
                this.addPrimaryIndustry(primaryIndustry);
            }
        }
    }

    public void clearPrimaryIndustry() {
        this.primaryIndustry.clear();
    }



    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekode = new ArrayList<>();

    public Collection<String> getKommuneKode() {
        return kommunekode;
    }

    public void addKommuneKode(String kommunekode) {
        if (kommunekode != null) {
            this.kommunekode.add(kommunekode);
            this.increaseDataParamCount();
        }
    }

    public void addKommuneKode(int kommunekode) {
        this.addKommuneKode(String.format("%03d", kommunekode));
    }

    public void setKommuneKode(String kommunekode) {
        this.kommunekode.clear();
        this.addKommuneKode(kommunekode);
    }

    public void setKommuneKode(Collection<String> kommunekoder) {
        this.kommunekode.clear();
        if (kommunekoder != null) {
            for (String kommunekode : kommunekoder) {
                this.addKommuneKode(kommunekode);
            }
        }
    }

    public void clearKommuneKode() {
        this.kommunekode.clear();
    }



    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(P_NUMBER, this.pNummer);
        map.put(ASSOCIATED_COMPANY_CVR, this.associatedCompanyCvrNumber);
        map.put(PRIMARYINDUSTRY, this.primaryIndustry);
        map.put(KOMMUNEKODE, this.kommunekode);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setPNummer(parameters.getI(P_NUMBER));
        this.setAssociatedCompanyCvrNummer(parameters.getI(ASSOCIATED_COMPANY_CVR));
        this.setPrimaryIndustry(parameters.getFirst(PRIMARYINDUSTRY));
        this.setKommuneKode(parameters.getI(KOMMUNEKODE));
    }


    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = new CvrRecordLookupDefinition(this);

        if (this.getPNummer() != null && !this.getPNummer().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyUnitRecord.DB_FIELD_P_NUMBER, this.getPNummer(), Integer.class);
        }
        if (this.getAssociatedCompanyCvrNummer() != null && !this.getAssociatedCompanyCvrNummer().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyUnitRecord.DB_FIELD_COMPANY_LINK + LookupDefinition.separator + CompanyLinkRecord.DB_FIELD_CVRNUMBER, this.getAssociatedCompanyCvrNummer(), Integer.class);
        }
        if (this.getPrimaryIndustry() != null && !this.getPrimaryIndustry().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyUnitRecord.DB_FIELD_PRIMARY_INDUSTRY + LookupDefinition.separator + CompanyIndustryRecord.DB_FIELD_CODE, this.getPrimaryIndustry(), String.class);
        }
        if (this.getKommuneKode() != null && !this.getKommuneKode().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(LookupDefinition.entityref);
            sj.add(CompanyUnitRecord.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressRecord.DB_FIELD_MUNICIPALITY);
            sj.add(AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommuneKode(), Integer.class);
        }
        if (this.getKommunekodeRestriction() != null && !this.getKommunekodeRestriction().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(LookupDefinition.entityref);
            sj.add(CompanyUnitRecord.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressRecord.DB_FIELD_MUNICIPALITY);
            sj.add(AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }

    @Override
    protected Object castFilterParam(Object input, String filter) {
        return super.castFilterParam(input, filter);
    }
}
