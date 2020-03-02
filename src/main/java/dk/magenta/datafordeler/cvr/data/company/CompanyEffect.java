package dk.magenta.datafordeler.cvr.data.company;

import dk.magenta.datafordeler.cvr.data.CvrEffect;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Representation of effects in the bitemporal model for companies.
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@Entity
@Table(name="cvr_company_effect", indexes = {
        @Index(name = "cvr_company_effect_registration", columnList = "registration_id"),
        @Index(name = "cvr_company_effect_from", columnList = "effectFrom"),
        @Index(name = "cvr_company_effect_to", columnList = "effectTo")
})
public class CompanyEffect extends CvrEffect<CompanyRegistration, CompanyEffect, CompanyBaseData> {
    public CompanyEffect() {
    }

    public CompanyEffect(CompanyRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public CompanyEffect(CompanyRegistration registration, LocalDate effectFrom, LocalDate effectTo) {
        super(registration, effectFrom, effectTo);
    }
/*
    public CompanyEffect(CompanyRegistration registrering, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        super(registrering, effectFrom, effectTo);
    }
*/

    public CompanyEffect(CompanyRegistration registration, String effectFrom, String effectTo) {
        super(registration, effectFrom, effectTo);
    }
}
