package dk.magenta.datafordeler.cvr.data.company;

import dk.magenta.datafordeler.cvr.data.CvrRegistration;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Representation of registrations in the bitemporal model for companies.
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name="cvr_company_registration", indexes = {
        @Index(name = "cvr_company_entity", columnList = "entity_id"),
        @Index(name = "cvr_company_registration_from", columnList = "registrationFrom"),
        @Index(name = "cvr_company_registration_to", columnList = "registrationTo")
})
public class CompanyRegistration extends CvrRegistration<CompanyEntity, CompanyRegistration, CompanyEffect> {
    public CompanyRegistration() {
    }

    public CompanyRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public CompanyRegistration(TemporalAccessor registrationFrom, TemporalAccessor registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public CompanyRegistration(String registrationFrom, String registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    @Override
    protected CompanyEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new CompanyEffect(this, effectFrom, effectTo);
    }
    // A Registration has a registrationFrom field and a registrationTo field (with setters)
    // Both are of type java.time.OffsetDateTime, and describe the time range within which the contained data is considered authoritative
    // RegistrationTo may be null, indicating that the data is currently authoritative
    // The Registration also contains a list of Effects (here CompanyEffect, as seen in the erasure).
}
