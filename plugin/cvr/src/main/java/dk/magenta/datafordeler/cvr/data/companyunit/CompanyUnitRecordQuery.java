package dk.magenta.datafordeler.cvr.data.companyunit;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.cvr.CvrRecordLookupDefinition;
import dk.magenta.datafordeler.cvr.data.unversioned.Municipality;
import dk.magenta.datafordeler.cvr.records.*;

import java.util.StringJoiner;

/**
 * Container for a query for Company units, defining fields and database lookup
 */
public class CompanyUnitRecordQuery extends CompanyUnitQuery {

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new CvrRecordLookupDefinition(this, null);

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

}
