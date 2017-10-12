package dk.magenta.datafordeler.cvr.data.companyunit;

import dk.magenta.datafordeler.cvr.data.CvrEffect;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name="cvr_companyunit_effect", indexes = {
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
