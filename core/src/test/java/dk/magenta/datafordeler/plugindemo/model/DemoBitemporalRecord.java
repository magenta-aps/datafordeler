package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.util.Bitemporality;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class DemoBitemporalRecord extends DatabaseEntry implements Monotemporal, Bitemporal {


    public static final String DB_FIELD_ENTITY = "entity";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = DB_FIELD_ENTITY + DatabaseEntry.REF)
    @JsonIgnore
    private DemoEntityRecord entity;

    public DemoEntityRecord getEntity() {
        return this.entity;
    }

    public void setEntity(DemoEntityRecord entity) {
        this.entity = entity;
    }

    public void setEntity(IdentifiedEntity entity) {
        this.entity = (DemoEntityRecord) entity;
    }



    @JsonProperty(value = "id")
    public Long getId() {
        return super.getId();
    }





    public static final Class<?> FILTERPARAMTYPE_REGISTRATIONFROM = OffsetDateTime.class;
    public static final Class<?> FILTERPARAMTYPE_REGISTRATIONTO = OffsetDateTime.class;

    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_FROM = Monotemporal.DB_FIELD_REGISTRATION_FROM;
    public static final String IO_FIELD_REGISTRATION_FROM = Monotemporal.IO_FIELD_REGISTRATION_FROM;


    @Column(name = DB_FIELD_REGISTRATION_FROM, columnDefinition = "datetime2")
    private OffsetDateTime registrationFrom;

    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    public OffsetDateTime getRegistrationFrom() {
        return Bitemporal.fixOffsetOut(this.registrationFrom);
    }

    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = Bitemporal.fixOffsetIn(registrationFrom);
    }


    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_TO = Monotemporal.DB_FIELD_REGISTRATION_TO;
    public static final String IO_FIELD_REGISTRATION_TO = Monotemporal.IO_FIELD_REGISTRATION_TO;
    @Column(name = DB_FIELD_REGISTRATION_TO, columnDefinition = "datetime2")
    private OffsetDateTime registrationTo;

    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    public OffsetDateTime getRegistrationTo() {
        return Bitemporal.fixOffsetOut(this.registrationTo);
    }

    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = Bitemporal.fixOffsetIn(registrationTo);
    }


    public static final Class<?> FILTERPARAMTYPE_EFFECTFROM = OffsetDateTime.class;
    public static final Class<?> FILTERPARAMTYPE_EFFECTTO = OffsetDateTime.class;


    public static final String DB_FIELD_EFFECT_FROM = Bitemporal.DB_FIELD_EFFECT_FROM;
    public static final String IO_FIELD_EFFECT_FROM = Bitemporal.IO_FIELD_EFFECT_FROM;
    @Column(name = DB_FIELD_EFFECT_FROM, columnDefinition = "datetime2")
    private OffsetDateTime effectFrom;

    @JsonProperty(value = IO_FIELD_EFFECT_FROM)
    public OffsetDateTime getEffectFrom() {
        return Bitemporal.fixOffsetOut(this.effectFrom);
    }

    @JsonProperty(value = IO_FIELD_EFFECT_FROM)
    public void setEffectFrom(OffsetDateTime effectFrom) {
        this.effectFrom = Bitemporal.fixOffsetIn(effectFrom);
    }


    public static final String DB_FIELD_EFFECT_TO = Bitemporal.DB_FIELD_EFFECT_TO;
    public static final String IO_FIELD_EFFECT_TO = Bitemporal.IO_FIELD_EFFECT_TO;
    @Column(name = DB_FIELD_EFFECT_TO, columnDefinition = "datetime2")
    private OffsetDateTime effectTo;

    @JsonProperty(value = IO_FIELD_EFFECT_TO)
    public OffsetDateTime getEffectTo() {
        return Bitemporal.fixOffsetOut(this.effectTo);
    }

    @JsonProperty(value = IO_FIELD_EFFECT_TO)
    public void setEffectTo(OffsetDateTime effectTo) {
        this.effectTo = Bitemporal.fixOffsetIn(effectTo);
    }

    @Override
    @JsonIgnore
    public Bitemporality getBitemporality() {
        return new Bitemporality(this.getRegistrationFrom(), this.getRegistrationTo(), this.getEffectFrom(), this.getEffectTo());
    }

    public static final Class<?> FILTERPARAMTYPE_LASTUPDATED = OffsetDateTime.class;

    public static final String DB_FIELD_UPDATED = Nontemporal.DB_FIELD_UPDATED;
    public static final String IO_FIELD_UPDATED = Nontemporal.IO_FIELD_UPDATED;
    @Column(name = DB_FIELD_UPDATED, columnDefinition = "datetime2")
    public OffsetDateTime dafoUpdated;

    @JsonProperty(value = IO_FIELD_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return Bitemporal.fixOffsetOut(this.dafoUpdated);
    }

    @Override
    @JsonProperty(value = IO_FIELD_UPDATED)
    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = Bitemporal.fixOffsetIn(dafoUpdated);
    }



}
