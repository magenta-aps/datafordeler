package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.util.Equality;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * The object representation of a “periode” JSON object from the source
 */
@Embeddable
public class CvrRecordPeriod {

    public static final String DB_FIELD_VALID_FROM = "validFrom";
    public static final String IO_FIELD_VALID_FROM = "gyldigFra";

    @Column(name = DB_FIELD_VALID_FROM)
    @JsonProperty(value = IO_FIELD_VALID_FROM)
    private LocalDate validFrom;

    public LocalDate getValidFrom() {
        return this.validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public static final String DB_FIELD_VALID_TO = "validTo";
    public static final String IO_FIELD_VALID_TO = "gyldigTil";

    @Column(name = DB_FIELD_VALID_TO)
    @JsonProperty(value = IO_FIELD_VALID_TO)
    private LocalDate validTo;

    public LocalDate getValidTo() {
        return this.validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CvrRecordPeriod that = (CvrRecordPeriod) o;
        return Equality.equal(validFrom, that.validFrom) &&
                Equality.equal(validTo, that.validTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(validFrom, validTo);
    }

    @JsonIgnore
    public boolean isValidAt(LocalDate date) {
        return (this.validFrom == null || date.isAfter(this.validFrom)) && (this.validTo == null || this.validTo.isAfter(date));
    }
}
