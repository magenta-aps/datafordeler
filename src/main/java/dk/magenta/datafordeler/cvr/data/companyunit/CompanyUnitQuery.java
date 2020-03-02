package dk.magenta.datafordeler.cvr.data.companyunit;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cvr.data.CvrQuery;
import dk.magenta.datafordeler.cvr.data.shared.AddressData;
import dk.magenta.datafordeler.cvr.data.shared.IndustryData;
import dk.magenta.datafordeler.cvr.data.shared.IntegerData;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;
import dk.magenta.datafordeler.cvr.data.unversioned.Municipality;

import java.util.*;

/**
 * Container for a query for Company units, defining fields and database lookup
 */
public class CompanyUnitQuery extends CvrQuery<CompanyUnitEntity> {

    public static final String P_NUMBER = CompanyUnitEntity.IO_FIELD_PNUMBER;
    public static final String ASSOCIATED_COMPANY_CVR = CompanyUnitBaseData.IO_FIELD_CVR_NUMBER;
    public static final String PRIMARYINDUSTRY = CompanyUnitBaseData.IO_FIELD_PRIMARY_INDUSTRY;
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
    public Class<CompanyUnitEntity> getEntityClass() {
        return CompanyUnitEntity.class;
    }

    @Override
    public Class getDataClass() {
        return CompanyUnitBaseData.class;
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new LookupDefinition(this, CompanyUnitBaseData.class);

        if (this.pNummer != null && !this.pNummer.isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyUnitEntity.DB_FIELD_PNUMBER, this.pNummer, Integer.class);
        }

        if (this.associatedCompanyCvrNumber != null && !this.associatedCompanyCvrNumber.isEmpty()) {
            lookupDefinition.put(CompanyUnitBaseData.DB_FIELD_CVR_NUMBER + LookupDefinition.separator + IntegerData.DB_FIELD_VALUE, this.associatedCompanyCvrNumber, Long.class);
        }
        if (this.primaryIndustry != null && !this.primaryIndustry.isEmpty()) {
            lookupDefinition.put(CompanyUnitBaseData.DB_FIELD_PRIMARY_INDUSTRY + LookupDefinition.separator + IndustryData.DB_FIELD_INDUSTRY + LookupDefinition.separator + Industry.DB_FIELD_CODE, this.primaryIndustry, String.class);
            lookupDefinition.put(CompanyUnitBaseData.DB_FIELD_PRIMARY_INDUSTRY + LookupDefinition.separator + IndustryData.DB_FIELD_PRIMARY, true, Boolean.class);
        }
        if (this.kommunekode != null && !this.kommunekode.isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(CompanyUnitBaseData.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressData.DB_FIELD_ADDRESS);
            sj.add(Address.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.kommunekode, Integer.class);
        }
        if (this.getKommunekodeRestriction() != null && !this.getKommunekodeRestriction().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(CompanyUnitBaseData.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressData.DB_FIELD_ADDRESS);
            sj.add(Address.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }

}
