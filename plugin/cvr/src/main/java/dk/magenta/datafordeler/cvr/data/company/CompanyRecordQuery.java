package dk.magenta.datafordeler.cvr.data.company;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.cvr.CvrRecordLookupDefinition;
import dk.magenta.datafordeler.cvr.data.unversioned.CompanyForm;
import dk.magenta.datafordeler.cvr.data.unversioned.Municipality;
import dk.magenta.datafordeler.cvr.records.*;

import java.util.StringJoiner;

/**
 * Container for a query for Companies, defining fields and database lookup
 */
public class CompanyRecordQuery extends CompanyQuery {

    @Override
    public LookupDefinition getLookupDefinition() {
        CvrRecordLookupDefinition lookupDefinition = new CvrRecordLookupDefinition(this, null);

        if (this.getCvrNumre() != null && !this.getCvrNumre().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_CVR_NUMBER, this.getCvrNumre(), Integer.class);
        }
        if (this.getVirksomhedsform() != null && !this.getVirksomhedsform().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_FORM + LookupDefinition.separator + FormRecord.DB_FIELD_FORM + LookupDefinition.separator + CompanyForm.DB_FIELD_CODE, this.getVirksomhedsform(), String.class);
        }
        if (this.getReklamebeskyttelse() != null) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_ADVERTPROTECTION, this.getReklamebeskyttelse(), Boolean.class);
        }
        if (this.getVirksomhedsnavn() != null && !this.getVirksomhedsnavn().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_NAMES + LookupDefinition.separator + SecNameRecord.DB_FIELD_NAME, this.getVirksomhedsnavn(), String.class);
        }
        if (this.getTelefonnummer() != null && !this.getTelefonnummer().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_PHONE + LookupDefinition.separator + ContactRecord.DB_FIELD_DATA, this.getTelefonnummer(), String.class);
        }
        if (this.getTelefaxnummer() != null && !this.getTelefaxnummer().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_FAX + LookupDefinition.separator +ContactRecord.DB_FIELD_DATA, this.getTelefaxnummer(), String.class);
        }
        if (this.getEmailadresse() != null && !this.getEmailadresse().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + CompanyRecord.DB_FIELD_EMAIL + LookupDefinition.separator + ContactRecord.DB_FIELD_DATA, this.getEmailadresse(), String.class);
        }
        if (this.getKommuneKode() != null && !this.getKommuneKode().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(LookupDefinition.entityref);
            sj.add(CompanyRecord.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressRecord.DB_FIELD_MUNICIPALITY);
            sj.add(AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommuneKode(), Integer.class);
        }
        if (this.getKommunekodeRestriction() != null && !this.getKommunekodeRestriction().isEmpty()) {
            StringJoiner sj = new StringJoiner(LookupDefinition.separator);
            sj.add(LookupDefinition.entityref);
            sj.add(CompanyRecord.DB_FIELD_LOCATION_ADDRESS);
            sj.add(AddressRecord.DB_FIELD_MUNICIPALITY);
            sj.add(AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY);
            sj.add(Municipality.DB_FIELD_CODE);
            lookupDefinition.put(sj.toString(), this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }
}
