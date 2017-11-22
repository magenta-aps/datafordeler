package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import org.hibernate.Session;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import dk.magenta.datafordeler.core.database.Entity;

/**
 * Created by lars on 20-02-17.
 * A Registration defines the time range in which a piece of data is “registered”,
 * that is, when did it enter into the records of our data source, and when was it 
 * supplanted by more recent data.
 * A Registration points to exactly one Entity, and may have any number of Effects
 * associated. Generally, there should not be stored other data in the object.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@FilterDefs({
        @FilterDef(name = Registration.FILTER_REGISTRATION_FROM, parameters = @ParamDef(name = Registration.FILTERPARAM_REGISTRATION_FROM, type = "java.time.OffsetDateTime")),
        @FilterDef(name = Registration.FILTER_REGISTRATION_TO, parameters = @ParamDef(name = Registration.FILTERPARAM_REGISTRATION_TO, type = "java.time.OffsetDateTime"))
})
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonPropertyOrder({"sequenceNumber", "registrationFrom", "registrationTo", "checksum", "effects"})
public abstract class Registration<E extends Entity, R extends Registration, V extends Effect> extends DatabaseEntry implements Comparable<Registration> {

    public static final String FILTER_REGISTRATION_FROM = "registrationFromFilter";
    public static final String FILTERPARAM_REGISTRATION_FROM = "registrationFromDate";
    public static final String FILTER_REGISTRATION_TO = "registrationToFilter";
    public static final String FILTERPARAM_REGISTRATION_TO = "registrationToDate";

    public Registration() {
        this.effects = new ArrayList<V>();
    }

    public Registration(OffsetDateTime registreringFra, OffsetDateTime registrationTo, int sequenceNumber) {
        this();
        this.registrationFrom = registreringFra;
        this.registrationTo = registrationTo;
        this.sequenceNumber = sequenceNumber;
        this.setLastImportTime();
    }

    public Registration(LocalDate registreringFra, LocalDate registrationTo, int sequenceNumber) {
        this(
                registreringFra != null ? OffsetDateTime.of(registreringFra, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null,
                registrationTo != null ? OffsetDateTime.of(registrationTo, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null,
                sequenceNumber
        );
    }

    public Registration(TemporalAccessor registreringFra, TemporalAccessor registrationTo, int sequenceNumber) {
        this(
                registreringFra != null ? OffsetDateTime.from(registreringFra) : null,
                registrationTo != null ? OffsetDateTime.from(registrationTo) : null,
                sequenceNumber
        );
    }

    /**
     * @param registreringFra A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * @param registrationTo A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Registration(String registreringFra, String registrationTo, int sequenceNumber) {
        this(
                registreringFra != null ? OffsetDateTime.parse(registreringFra) : null,
                registrationTo != null ? OffsetDateTime.parse(registrationTo) : null,
                sequenceNumber
        );
    }




    @ManyToOne(cascade = CascadeType.ALL)
    protected E entity;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @XmlTransient
    public E getEntity() {
        return this.entity;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setEntity(E entity) {
        this.entity = entity;
        this.entity.addRegistration(this);
    }





    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "registration")
    @Filters({
            @Filter(name = Effect.FILTER_EFFECT_FROM, condition="(effectTo >= :"+Effect.FILTERPARAM_EFFECT_FROM+" OR effectTo is null)"),
            @Filter(name = Effect.FILTER_EFFECT_TO, condition="(effectFrom < :"+Effect.FILTERPARAM_EFFECT_TO+" or effectFrom is null)")
    })
    protected List<V> effects;

    @JsonIgnore
    public List<V> getEffects() {
        return this.effects;
    }


    /**
     * Get the Effects of the Registration, sorted by the comparison method of the
     * Effect class (usually by startDate)
     */
    @JsonProperty(value = "virkninger", access = JsonProperty.Access.READ_ONLY)
    @XmlElement(name = "virkning")
    @JacksonXmlProperty(localName = "virkning")
    @JacksonXmlElementWrapper(useWrapping = false)
    public ArrayList<V> getSortedEffects() {
        ArrayList<V> sortedEffects = new ArrayList<V>(this.effects);
        Collections.sort(sortedEffects);
        return sortedEffects;
    }

    /**
     * Looks for an effect on this Registration, that matches the given range 
     * exactly.
     */
    public V getEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        for (V effect : this.effects) {
            if (Equality.equal(effect.getEffectFrom(), effectFrom) &&
                    Equality.equal(effect.getEffectTo(), effectTo)) {
                return effect;
            }
        }
        return null;
    }

    public List<V> getEffectsAt(OffsetDateTime time) {
        List<V> effects = new ArrayList<>();
        for (V effect : this.effects) {
            OffsetDateTime from = effect.getEffectFrom();
            OffsetDateTime to = effect.getEffectTo();
            if ((from == null || from.isBefore(time) || from.isEqual(time)) && (to == null || to.isAfter(time) || to.isEqual(time))) {
                effects.add(effect);
            }
        }
        return effects;
    }

    public V getEffect(LocalDateTime effectFrom, LocalDateTime effectTo) {
        return this.getEffect(
                effectFrom != null ? OffsetDateTime.of(effectFrom, ZoneOffset.UTC) : null,
                effectTo != null ? OffsetDateTime.of(effectTo, ZoneOffset.UTC) : null
        );
    }

    public V getEffect(LocalDate effectFrom, LocalDate effectTo) {
        return this.getEffect(
                effectFrom != null ? OffsetDateTime.of(effectFrom, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null,
                effectTo != null ? OffsetDateTime.of(effectTo, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null
        );
    }

    /**
     * Add an effect to this registration
     */
    public void addEffect(V effect) {
        if (!this.effects.contains(effect)) {
            effect.setRegistration(this);
            this.effects.add(effect);
        }
    }

    /**
     * Removed an Effect from this Registration
     */
    public void removeEffect(V effect) {
        // Be sure to also delete the effect yourself, since it still points to the Registration
        this.effects.remove(effect);
    }

    @JsonProperty(value = "virkninger", access = JsonProperty.Access.WRITE_ONLY)
    public void setEffects(Collection<V> effects) {
        this.effects = new ArrayList<V>(effects);
        this.wireEffects();
    }

    protected abstract V createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo);

    public final V createEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        V effect = this.createEmptyEffect(effectFrom, effectTo);
        effect.setRegistration(this);
        return effect;
    }
    public final V createEffect(TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        return this.createEffect(Effect.convertTime(effectFrom), Effect.convertTime(effectTo));
    }
    public final V createEffect(LocalDate effectFrom, LocalDate effectTo) {
        return this.createEffect(Effect.convertTime(effectFrom), Effect.convertTime(effectTo));
    }

    public void wireEffects() {
        for (V effect : this.effects) {
            effect.setRegistration(this);
        }
    }




    @Column(nullable = true, insertable = true, updatable = false)
    protected OffsetDateTime registrationFrom;


    @JsonProperty(value = "registreringFra")
    @XmlElement
    @XmlJavaTypeAdapter(type = OffsetDateTime.class, value = OffsetDateTimeAdapter.class)
    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFrom;
    }

    @JsonProperty(value = "registreringFra")
    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = registrationFrom;
    }




    @Column(nullable = true, insertable = true, updatable = false)
    protected OffsetDateTime registrationTo;

    @JsonProperty(value = "registreringTil")
    @XmlElement
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value= OffsetDateTimeAdapter.class)
    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    @JsonProperty(value = "registreringTil")
    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = registrationTo;
    }



    @Column(nullable = false, insertable = true, updatable = false)
    protected int sequenceNumber;

    @JsonProperty(value = "sekvensnummer")
    @XmlElement
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    @JsonProperty(value = "sekvensnummer")
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }


    // The checksum as reported by the register
    protected String registerChecksum;

    @JsonProperty("checksum")
    public String getRegisterChecksum() {
        return this.registerChecksum;
    }


    @JsonProperty("checksum")
    public void setRegisterChecksum(String registerChecksum) {
        this.registerChecksum = registerChecksum;
    }


    @Column(nullable = true, insertable = true, updatable = true)
    protected OffsetDateTime lastImportTime;

    @JsonProperty("sidstImporteret")
    public OffsetDateTime getLastImportTime() {
        return this.lastImportTime;
    }

    public void setLastImportTime(OffsetDateTime lastImportTime) {
        this.lastImportTime = lastImportTime;
    }

    public void setLastImportTime() {
        this.setLastImportTime(OffsetDateTime.now());
    }

    /**
     * Pretty-print contained data
     * @return Compiled string output
     */
    public String toString() {
        return this.toString(0);
    }

    /**
     * Pretty-print contained data
     * @param indent Number of spaces to indent the output with
     * @return Compiled string output
     */
    public String toString(int indent) {
        String indentString = new String(new char[4 * (indent)]).replace("\0", " ");
        String subIndentString = new String(new char[4 * (indent + 1)]).replace("\0", " ");
        StringJoiner s = new StringJoiner("\n");
        s.add(indentString + this.getClass().getSimpleName()+"["+this.hashCode()+"] {");
        if (this.entity != null) {
            s.add(subIndentString + "entity: " + entity.getUUID()+" @ "+entity.getDomain());
        } else {
            s.add(subIndentString + "entity: NULL");
        }
        s.add(subIndentString + "checksum: "+this.registerChecksum);
        s.add(subIndentString + "from: "+this.registrationFrom);
        s.add(subIndentString + "to: "+this.registrationTo);
        s.add(subIndentString + "effects: [");
        for (V effect : this.effects) {
            s.add(effect.toString(indent + 2));
        }
        s.add(subIndentString + "]");
        s.add(indentString+"}");
        return s.toString();
    }

    public void forceLoad(Session session) {
        for (V effect : this.effects) {
            effect.forceLoad(session);
        }
    }

    /**
     * Comparison method for the Comparable interface; results in Registrations being sorted by registrationFrom date, nulls first?
     */
    @Override
    public int compareTo(Registration o) {
        OffsetDateTime oDateTime = o == null ? null : o.registrationFrom;
        if (this.registrationFrom == null && oDateTime == null) return 0;
        if (oDateTime == null) return 1;
        if (this.registrationFrom == null) return -1;
        return this.registrationFrom.toInstant().compareTo(oDateTime.toInstant());
    }

    public boolean equals(Registration o) {
        return o != null &&
            compareTo(o) == 0 &&
            getSequenceNumber() == o.getSequenceNumber() &&
            (getRegisterChecksum() == o.getRegisterChecksum() ||
                getRegisterChecksum()
                    .equalsIgnoreCase(o.getRegisterChecksum()));
    }

    public R split(OffsetDateTime splitTime) {
        //Registration newReg = this.entity.createRegistration();
        R newReg = (R) this.entity.createRegistration();
        newReg.setRegistrationFrom(splitTime);
        newReg.setRegistrationTo(this.registrationTo);
        this.registrationTo = splitTime;
        for (V effect : new ArrayList<V>(this.effects)) {
            V newEffect = (V) effect.createClone();
            newEffect.setRegistration(newReg);
        }
        return newReg;
    }
}
