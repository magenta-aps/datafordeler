package dk.magenta.datafordeler.cvr.data.companyunit;

import dk.magenta.datafordeler.cvr.data.CvrEffect;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Representation of effects in the bitemporal model for company units.
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name="cvr_companyunit_effect", indexes = {
        @Index(name = "cvr_companyunit_effect_registration", columnList = "registration_id"),
        @Index(name = "cvr_companyunit_effect_from", columnList = "effectFrom"),
        @Index(name = "cvr_companyunit_effect_to", columnList = "effectTo")
})
public class CompanyUnitEffect extends CvrEffect<CompanyUnitRegistration, CompanyUnitEffect, CompanyUnitBaseData> {
    public CompanyUnitEffect() {
        super();
    }

    public CompanyUnitEffect(CompanyUnitRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public CompanyUnitEffect(CompanyUnitRegistration registration, LocalDate effectFrom, LocalDate effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public CompanyUnitEffect(CompanyUnitRegistration registration, String effectFrom, String effectTo) {
        super(registration, effectFrom, effectTo);
    }
}
