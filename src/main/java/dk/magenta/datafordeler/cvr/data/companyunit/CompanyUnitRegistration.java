package dk.magenta.datafordeler.cvr.data.companyunit;

import dk.magenta.datafordeler.cvr.data.CvrRegistration;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Representation of registrations in the bitemporal model for company units.
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name="cvr_companyunit_registration", indexes = {
        @Index(name = "cvr_companyunit_entity", columnList = "entity_id"),
        @Index(name = "cvr_companyunit_registration_from", columnList = "registrationFrom"),
        @Index(name = "cvr_companyunit_registration_to", columnList = "registrationTo")
})
public class CompanyUnitRegistration extends CvrRegistration<CompanyUnitEntity, CompanyUnitRegistration, CompanyUnitEffect> {
    public CompanyUnitRegistration() {
        super();
    }

    public CompanyUnitRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public CompanyUnitRegistration(TemporalAccessor registrationFrom, TemporalAccessor registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public CompanyUnitRegistration(String registrationFrom, String registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    @Override
    protected CompanyUnitEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new CompanyUnitEffect(this, effectFrom, effectTo);
    }
}
