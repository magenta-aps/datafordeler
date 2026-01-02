package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.magenta.datafordeler.core.util.Equality;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;

/**
 * A Registration defines the time range in which a piece of data is "registered",
 * that is, when did it enter into the records of our data source, and when was it
 * supplanted by more recent data.
 * A Registration points to exactly one Entity, and may have any number of Effects
 * associated. Generally, there should not be stored other data in the object.
 */
@MappedSuperclass
@JsonPropertyOrder({"sequenceNumber", "registrationFromBefore", "registrationToBefore", "checksum", "effects"})
public abstract class Registration<E extends Entity, R extends Registration, V extends Effect> extends DatabaseEntry implements Comparable<Registration> {

    public static final String FILTER_REGISTRATION_FROM = "registrationFromFilter";
    public static final String FILTERPARAM_REGISTRATION_FROM = "registrationFromDate";
    public static final String FILTER_REGISTRATION_TO = "registrationToFilter";
    public static final String FILTERPARAM_REGISTRATION_TO = "registrationToDate";

    public Registration() {
        this.effects = new ArrayList<V>();
    }

    public Registration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, int sequenceNumber) {
        this();
        this.setRegistrationFrom(registrationFrom);
        this.setRegistrationTo(registrationTo);
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
     * @param registrationFrom A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * @param registrationTo   A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     *                         If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Registration(String registrationFrom, String registrationTo, int sequenceNumber) {
        this(
                registrationFrom != null ? OffsetDateTime.parse(registrationFrom) : null,
                registrationTo != null ? OffsetDateTime.parse(registrationTo) : null,
                sequenceNumber
        );
    }


    @ManyToOne(cascade = CascadeType.ALL)
    protected E entity;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
            //@Filter(name = Effect.FILTER_EFFECT_FROM, condition="(effectTo >= :"+Effect.FILTERPARAM_EFFECT_FROM+" OR effectTo is null)"),
            @Filter(name = Effect.FILTER_EFFECT_TO, condition = "(effectFrom < :" + Effect.FILTERPARAM_EFFECT_TO + " or effectFrom is null)")
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


    public static final String DB_FIELD_REGISTRATION_FROM = "registrationFrom";
    public static final String IO_FIELD_REGISTRATION_FROM = "registreringFra";

    @Column(name = DB_FIELD_REGISTRATION_FROM, nullable = true, insertable = true, updatable = false, columnDefinition = "datetime2")
    protected OffsetDateTime registrationFrom;

    @JsonIgnore
    @Column(name = DB_FIELD_REGISTRATION_FROM+"_new", nullable = true, insertable = true, updatable = false)
    protected OffsetDateTime registrationFromNew;


    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFromNew;
    }

    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = fixOffsetIn(registrationFrom);
        this.registrationFromNew = registrationFrom;
    }


    public static final String DB_FIELD_REGISTRATION_TO = "registrationTo";
    public static final String IO_FIELD_REGISTRATION_TO = "registreringTil";

    @Column(name = DB_FIELD_REGISTRATION_TO, nullable = true, insertable = true, updatable = false, columnDefinition = "datetime2")
    protected OffsetDateTime registrationTo;

    @JsonIgnore
    @Column(name = DB_FIELD_REGISTRATION_TO+"_new", nullable = true, insertable = true, updatable = false)
    protected OffsetDateTime registrationToNew;

    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    public OffsetDateTime getRegistrationTo() {
        return this.registrationToNew;
    }

    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = fixOffsetIn(registrationTo);
        this.registrationToNew = registrationTo;
    }


    public static final String IO_FIELD_SEQUENCE_NUMBER = "sekvensnummer";

    @Column(nullable = false, insertable = true, updatable = false)
    protected int sequenceNumber;

    @JsonProperty(value = IO_FIELD_SEQUENCE_NUMBER)
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    @JsonProperty(value = IO_FIELD_SEQUENCE_NUMBER)
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }


    public static final String IO_FIELD_CHECKSUM = "checksum";

    // The checksum as reported by the register
    protected String registerChecksum;

    @JsonProperty(value = IO_FIELD_CHECKSUM)
    public String getRegisterChecksum() {
        return this.registerChecksum;
    }


    @JsonProperty(value = IO_FIELD_CHECKSUM)
    public void setRegisterChecksum(String registerChecksum) {
        this.registerChecksum = registerChecksum;
    }


    @Column(nullable = true, insertable = true, updatable = true, columnDefinition = "datetime2")
    protected OffsetDateTime lastImportTime;

    @JsonIgnore
    @Column(name="lastImportTime"+"_new", nullable = true, insertable = true, updatable = true)
    protected OffsetDateTime lastImportTimeNew;

    @JsonProperty("sidstImporteret")
    public OffsetDateTime getLastImportTime() {
        return this.lastImportTimeNew;
    }

    public void setLastImportTime(OffsetDateTime lastImportTime) {
        this.lastImportTime = fixOffsetIn(lastImportTime);
        this.lastImportTimeNew = lastImportTime;
    }

    public void setLastImportTime() {
        this.setLastImportTime(OffsetDateTime.now());
    }

    /**
     * Pretty-print contained data
     *
     * @return Compiled string output
     */
    public String toString() {
        return this.toString(0);
    }

    /**
     * Pretty-print contained data
     *
     * @param indent Number of spaces to indent the output with
     * @return Compiled string output
     */
    public String toString(int indent) {
        String indentString = new String(new char[4 * (indent)]).replace("\0", " ");
        String subIndentString = new String(new char[4 * (indent + 1)]).replace("\0", " ");
        StringJoiner s = new StringJoiner("\n");
        s.add(indentString + this.getClass().getSimpleName() + "[" + this.hashCode() + "] {");
        if (this.entity != null) {
            s.add(subIndentString + "entity: " + entity.getUUID() + " @ " + entity.getDomain());
        } else {
            s.add(subIndentString + "entity: NULL");
        }
        s.add(subIndentString + "checksum: " + this.registerChecksum);
        s.add(subIndentString + "from: " + this.getRegistrationFrom());
        s.add(subIndentString + "to: " + this.getRegistrationTo());
        s.add(subIndentString + "effects: [");
        for (V effect : this.effects) {
            s.add(effect.toString(indent + 2));
        }
        s.add(subIndentString + "]");
        s.add(indentString + "}");
        return s.toString();
    }

    public void forceLoad(Session session) {
        for (V effect : this.effects) {
            effect.forceLoad(session);
        }
    }

    /**
     * Comparison method for the Comparable interface; results in Registrations being sorted by registrationFromBefore date, nulls first?
     */
    @Override
    public int compareTo(Registration o) {
        OffsetDateTime oDateTime = o == null ? null : o.getRegistrationFrom();
        if (this.registrationFrom == null && oDateTime == null) return 0;
        if (oDateTime == null) return 1;
        if (this.registrationFrom == null) return -1;
        return this.getRegistrationFrom().toInstant().compareTo(oDateTime.toInstant());
    }

    public boolean equals(Registration o) {
        return o != null &&
                compareTo(o) == 0 &&
                getSequenceNumber() == o.getSequenceNumber() &&
                (getRegisterChecksum() == o.getRegisterChecksum() ||
                        getRegisterChecksum()
                                .equalsIgnoreCase(o.getRegisterChecksum()));
    }

    public boolean equalTime(Registration o) {
        if (o == null) return false;
        if (this.compareTo(o) != 0) return false;

        OffsetDateTime oDateTime = o == null ? null : o.getRegistrationTo();
        if (this.registrationTo == null) {
            return (oDateTime == null);
        }
        return this.getRegistrationTo().toInstant().compareTo(oDateTime.toInstant()) == 0;
    }

    public R split(OffsetDateTime splitTime) {
        R newReg = (R) this.entity.createRegistration();
        newReg.setRegistrationFrom(splitTime);
        newReg.setRegistrationTo(this.getRegistrationTo());
        this.setRegistrationTo(splitTime);
        for (V effect : new ArrayList<V>(this.effects)) {
            V newEffect = (V) effect.createClone();
            newEffect.setRegistration(newReg);
        }
        return newReg;
    }

    public void mergeInto(R otherRegistration) {
        for (V effect : new ArrayList<V>(this.getEffects())) {
            effect.setRegistration(otherRegistration);
        }
    }

    public void updateTimestamp() {
        this.lastImportTimeNew = Bitemporal.fixOffsetOut(this.lastImportTime);
        this.registrationFromNew = Bitemporal.fixOffsetOut(this.registrationFrom);
        this.registrationToNew = Bitemporal.fixOffsetOut(this.registrationTo);
        for (V effect : this.effects) {
            effect.updateTimestamp();
        }
    }

    public static List<String> updateFields() {
        return Arrays.asList(DB_FIELD_REGISTRATION_FROM, DB_FIELD_REGISTRATION_TO, "lastImportTime");
    }
}
