package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.util.Bitemporality;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

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


    @Column(name = DB_FIELD_REGISTRATION_FROM)
    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    @XmlElement(name = IO_FIELD_REGISTRATION_FROM)
    private OffsetDateTime registrationFrom;

    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFrom;
    }

    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = registrationFrom;
    }


    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_TO = Monotemporal.DB_FIELD_REGISTRATION_TO;
    public static final String IO_FIELD_REGISTRATION_TO = Monotemporal.IO_FIELD_REGISTRATION_TO;
    @Column(name = DB_FIELD_REGISTRATION_TO)
    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    @XmlElement(name = IO_FIELD_REGISTRATION_TO)
    private OffsetDateTime registrationTo;

    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = registrationTo;
    }


    public void setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
    }



    public static final Class<?> FILTERPARAMTYPE_EFFECTFROM = OffsetDateTime.class;
    public static final Class<?> FILTERPARAMTYPE_EFFECTTO = OffsetDateTime.class;


    public static final String DB_FIELD_EFFECT_FROM = Bitemporal.DB_FIELD_EFFECT_FROM;
    public static final String IO_FIELD_EFFECT_FROM = Bitemporal.IO_FIELD_EFFECT_FROM;
    @Column(name = DB_FIELD_EFFECT_FROM)
    @JsonProperty(value = IO_FIELD_EFFECT_FROM)
    @XmlElement(name = IO_FIELD_EFFECT_FROM)
    private OffsetDateTime effectFrom;

    public OffsetDateTime getEffectFrom() {
        return this.effectFrom;
    }

    public void setEffectFrom(OffsetDateTime effectFrom) {
        this.effectFrom = effectFrom;
    }


    public static final String DB_FIELD_EFFECT_TO = Bitemporal.DB_FIELD_EFFECT_TO;
    public static final String IO_FIELD_EFFECT_TO = Bitemporal.IO_FIELD_EFFECT_TO;
    @Column(name = DB_FIELD_EFFECT_TO)
    @JsonProperty(value = IO_FIELD_EFFECT_TO)
    @XmlElement(name = IO_FIELD_EFFECT_TO)
    private OffsetDateTime effectTo;

    public OffsetDateTime getEffectTo() {
        return this.effectTo;
    }

    public void setEffectTo(OffsetDateTime effectTo) {
        this.effectTo = effectTo;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public Bitemporality getBitemporality() {
        return new Bitemporality(this.registrationFrom, this.registrationTo, this.effectFrom, this.effectTo);
    }

    public static final Class<?> FILTERPARAMTYPE_LASTUPDATED = OffsetDateTime.class;

    public static final String DB_FIELD_UPDATED = Nontemporal.DB_FIELD_UPDATED;
    public static final String IO_FIELD_UPDATED = Nontemporal.IO_FIELD_UPDATED;
    @Column(name = DB_FIELD_UPDATED)
    @JsonProperty(value = IO_FIELD_UPDATED)
    @XmlElement(name = IO_FIELD_UPDATED)
    public OffsetDateTime dafoUpdated;

    public OffsetDateTime getDafoUpdated() {
        return this.dafoUpdated;
    }

    @Override
    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = dafoUpdated;
    }



}
