package dk.magenta.datafordeler.cpr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.migration.MigrateModel;
import dk.magenta.datafordeler.cpr.data.CprRecordEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@MappedSuperclass
public abstract class CprBitemporalRecord<E extends CprRecordEntity, S extends CprBitemporalRecord<E, S>> extends CprMonotemporalRecord<E, S> implements Comparable<CprBitemporalRecord>, Bitemporal {


    public static final Class<?> FILTERPARAMTYPE_EFFECTFROM = OffsetDateTime.class;
    public static final Class<?> FILTERPARAMTYPE_EFFECTTO = OffsetDateTime.class;


    public static final String DB_FIELD_ENTITY = CprMonotemporalRecord.DB_FIELD_ENTITY;

    public static final String DB_FIELD_REGISTRATION_FROM = CprMonotemporalRecord.DB_FIELD_REGISTRATION_FROM;
    public static final String IO_FIELD_REGISTRATION_FROM = CprMonotemporalRecord.IO_FIELD_REGISTRATION_FROM;
    public static final String DB_FIELD_REGISTRATION_TO = CprMonotemporalRecord.DB_FIELD_REGISTRATION_TO;
    public static final String IO_FIELD_REGISTRATION_TO = CprMonotemporalRecord.IO_FIELD_REGISTRATION_TO;

    public static final String DB_FIELD_EFFECT_FROM = Bitemporal.DB_FIELD_EFFECT_FROM;
    public static final String IO_FIELD_EFFECT_FROM = Bitemporal.IO_FIELD_EFFECT_FROM;
    @Column(name = DB_FIELD_EFFECT_FROM, columnDefinition = "datetime2")
    private OffsetDateTime effectFrom;

    @JsonIgnore
    @Column(name = DB_FIELD_EFFECT_FROM+"_new")
    private OffsetDateTime effectFromNew;

    @JsonProperty(value = IO_FIELD_EFFECT_FROM)
    public OffsetDateTime getEffectFrom() {
        return this.effectFromNew;
    }

    public void setEffectFrom(OffsetDateTime effectFrom) {
        this.effectFrom = Bitemporal.fixOffsetIn(effectFrom);
        this.effectFromNew = effectFrom;
    }

    public static final String DB_FIELD_EFFECT_FROM_UNCERTAIN = "effectFromUncertain";
    public static final String IO_FIELD_EFFECT_FROM_UNCERTAIN = "virkningFraUsikker";
    @Column(name = DB_FIELD_EFFECT_FROM_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_EFFECT_FROM_UNCERTAIN)
    private boolean effectFromUncertain;

    public boolean isEffectFromUncertain() {
        return this.effectFromUncertain;
    }

    public void setEffectFromUncertain(boolean effectFromUncertain) {
        this.effectFromUncertain = effectFromUncertain;
    }

    public static final String DB_FIELD_EFFECT_TO = Bitemporal.DB_FIELD_EFFECT_TO;
    public static final String IO_FIELD_EFFECT_TO = Bitemporal.IO_FIELD_EFFECT_TO;
    @Column(name = DB_FIELD_EFFECT_TO, columnDefinition = "datetime2")
    @JsonProperty(value = IO_FIELD_EFFECT_TO)
    private OffsetDateTime effectTo;

    @JsonIgnore
    @Column(name = DB_FIELD_EFFECT_TO+"_new")
    private OffsetDateTime effectToNew;

    public OffsetDateTime getEffectTo() {
        return this.effectToNew;
    }

    public void setEffectTo(OffsetDateTime effectTo) {
        this.effectTo = Bitemporal.fixOffsetIn(effectTo);
        this.effectToNew = effectTo;
    }

    public static final String DB_FIELD_EFFECT_TO_UNCERTAIN = "effectToUncertain";
    public static final String IO_FIELD_EFFECT_TO_UNCERTAIN = "virkningTilUsikker";
    @Column(name = DB_FIELD_EFFECT_TO_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_EFFECT_TO_UNCERTAIN)
    private boolean effectToUncertain;

    public boolean isEffectToUncertain() {
        return this.effectToUncertain;
    }

    public void setEffectToUncertain(boolean effectToUncertain) {
        this.effectToUncertain = effectToUncertain;
    }


    // Whether this record should replace any equal records? (equal except effectTo)
    @JsonIgnore
    private boolean historic = false;

    public CprBitemporalRecord setHistoric() {
        this.historic = true;
        return this;
    }

    public boolean isHistoric() {
        return this.historic;
    }


    public CprBitemporalRecord setAnnKor(Character annkor) {
        if (annkor != null) {
            if (annkor == 'A') {
                this.setUndo(true);
            } else if (annkor == 'K') {
                this.setCorrection(true);
            } else if (annkor == 'Æ') {
                this.setTechnicalCorrection(true);
            }
        }
        return this;
    }


    //@Column(name = DB_FIELD_CORRECTED)
    @Transient
    private boolean correction = false;

    public CprBitemporalRecord setCorrection(boolean correction) {
        this.correction = correction;
        return this;
    }

    @JsonIgnore
    public boolean isCorrection() {
        return this.correction;
    }


    @Transient
    private boolean technicalCorrection = false;

    public CprBitemporalRecord setTechnicalCorrection(boolean technicalCorrection) {
        this.technicalCorrection = technicalCorrection;
        return this;
    }

    public boolean isTechnicalCorrection() {
        return this.technicalCorrection;
    }


    public static final String DB_FIELD_UNDO = "undo";

    //@Column(name = DB_FIELD_UNDO)
    @Transient
    private boolean undo = false;

    public boolean isUndo() {
        return this.undo;
    }

    public CprBitemporalRecord setUndo(boolean undo) {
        this.undo = undo;
        return this;
    }


    public CprBitemporalRecord setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        super.setBitemporality(registrationFrom, registrationTo);
        this.setEffectFrom(effectFrom);
        this.setEffectFromUncertain(effectFromUncertain);
        this.setEffectTo(effectTo);
        this.setEffectToUncertain(effectToUncertain);
        return this;
    }

    public CprBitemporalRecord setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, TemporalAccessor effectFrom, boolean effectFromUncertain, TemporalAccessor effectTo, boolean effectToUncertain) {
        return this.setBitemporality(
                registrationFrom,
                registrationTo,
                CprBitemporality.convertTime(effectFrom),
                effectFromUncertain,
                CprBitemporality.convertTime(effectTo),
                effectToUncertain
        );
    }

    public CprBitemporalRecord setBitemporality(CprBitemporality bitemporality) {
        if (bitemporality != null) {
            return this.setBitemporality(
                    bitemporality.registrationFrom,
                    bitemporality.registrationTo,
                    bitemporality.effectFrom,
                    bitemporality.effectFromUncertain,
                    bitemporality.effectTo,
                    bitemporality.effectToUncertain
            );
        }
        return this;
    }

    public CprBitemporalRecord setAuthority(int authority) {
        super.setAuthority(authority);
        return this;
    }

    /**
     * For sorting purposes; we implement the Comparable interface, so we should
     * provide a comparison method. Here, we sort CvrRecord objects by registrationFrom, with nulls first
     */
    @Override
    public int compareTo(CprBitemporalRecord o) {
        return super.compareTo(o);
    }

    @JsonIgnore
    public CprBitemporality getBitemporality() {
        return new CprBitemporality(this.getRegistrationFrom(), this.getRegistrationTo(), this.getEffectFrom(), this.effectFromUncertain, this.getEffectTo(), this.effectToUncertain);
    }

    protected static void copy(CprBitemporalRecord from, CprBitemporalRecord to) {
        CprMonotemporalRecord.copy(from, to);
        to.effectFrom = from.effectFrom;
        to.effectTo = from.effectTo;
        to.effectFromNew = from.effectFromNew;
        to.effectToNew = from.effectToNew;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        if (!this.equalData(o)) return false;
        CprBitemporalRecord that = (CprBitemporalRecord) o;
        return effectFromUncertain == that.effectFromUncertain &&
                effectToUncertain == that.effectToUncertain &&
                historic == that.historic &&
                Objects.equals(effectFrom, that.effectFrom) &&
                Objects.equals(effectTo, that.effectTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), effectFrom, effectFromUncertain, effectTo, effectToUncertain);
    }

    public void updateTimestamp() {
        super.updateTimestamp();
        this.effectFromNew = Bitemporal.fixOffsetOut(this.effectFrom);
        this.effectToNew = Bitemporal.fixOffsetOut(this.effectTo);
    }

    public static List<String> updateFields() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(CprMonotemporalRecord.updateFields());
        list.addAll(Arrays.asList(DB_FIELD_EFFECT_FROM, DB_FIELD_EFFECT_TO));
        return list;
    }

}
