package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.Equality;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;

/**
 * An Effect defines the time range in which a piece of data has effect.
 * An Effect points to exactly one Registration, but may have any number of DataItems
 * associated.
 */
@MappedSuperclass
@JsonPropertyOrder({"effectFrom", "effectTo", "dataItems"})
public abstract class Effect<R extends Registration, V extends Effect, D extends DataItem> extends DatabaseEntry implements Comparable<Effect> {

    public static final String FILTER_EFFECT_FROM = "effectFromFilter";
    public static final String FILTERPARAM_EFFECT_FROM = "effectFromDate";
    public static final String FILTER_EFFECT_TO = "effectToFilter";
    public static final String FILTERPARAM_EFFECT_TO = "effectToDate";

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonIgnore
    protected R registration;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filter(name = DataItem.FILTER_RECORD_AFTER, condition = "(lastUpdated > :" + DataItem.FILTERPARAM_RECORD_AFTER + ")")
    @JoinTable(
            joinColumns = @JoinColumn(name = "effects_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "dataitems_id", referencedColumnName = "id"),
            indexes = {
                    @Index(columnList = "effects_id"),
                    @Index(columnList = "dataitems_id")
            }
    )
    protected Set<D> dataItems;

    public Effect() {
        this.dataItems = new HashSet<D>();
    }

    public Effect(R registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        this();
        this.setRegistration(registration);
        this.setEffectFrom(effectFrom);
        this.setEffectTo(effectTo);
    }

    public Effect(R registration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        this(
                registration,
                convertTime(effectFrom),
                convertTime(effectTo)
        );
    }

    public Effect(R registration, LocalDate effectFrom, LocalDate effectTo) {
        this(
                registration,
                convertTime(effectFrom),
                convertTime(effectTo)
        );
    }

    public static OffsetDateTime convertTime(TemporalAccessor time) {
        return time != null ? OffsetDateTime.from(time) : null;
    }

    public static OffsetDateTime convertTime(LocalDate time) {
        return time != null ? OffsetDateTime.of(time, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null;
    }

    /**
     * @param effectFrom A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * @param effectTo   A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     *                   If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Effect(R registration, String effectFrom, String effectTo) {
        this(
                registration,
                effectFrom != null ? OffsetDateTime.parse(effectFrom) : null,
                effectTo != null ? OffsetDateTime.parse(effectTo) : null
        );
    }

    @JsonIgnore
    public R getRegistration() {
        return this.registration;
    }

    protected void setRegistration(R registration) {
        if (registration != null && registration != this.registration) {
            if (this.registration != null) {
                this.registration.removeEffect(this);
            }
            this.registration = registration;
            if (!registration.effects.contains(this)) {
                registration.addEffect(this);
            }
        }
    }

    public static final String DB_FIELD_EFFECT_FROM = "effectFrom";
    public static final String IO_FIELD_EFFECT_FROM = "virkningFra";

    @Column(name = DB_FIELD_EFFECT_FROM, nullable = true, insertable = true, updatable = false, columnDefinition = "datetime2")
    @JsonProperty(value = IO_FIELD_EFFECT_FROM)
    private OffsetDateTime effectFrom;

    @JsonIgnore
    @Column(name = DB_FIELD_EFFECT_FROM+"_new", nullable = true, insertable = true, updatable = false)
    private OffsetDateTime effectFromNew;

    public OffsetDateTime getEffectFrom() {
        return this.effectFromNew;
    }

    public void setEffectFrom(OffsetDateTime effectFrom) {
        this.effectFrom = fixOffsetIn(effectFrom);
        this.effectFromNew = effectFrom;
    }


    public static final String DB_FIELD_EFFECT_TO = "effectTo";
    public static final String IO_FIELD_EFFECT_TO = "virkningTil";

    @JsonProperty(value = IO_FIELD_EFFECT_TO)
    @Column(name = DB_FIELD_EFFECT_TO, nullable = true, insertable = true, updatable = false, columnDefinition = "datetime2")
    private OffsetDateTime effectTo;

    @JsonIgnore
    @Column(name = DB_FIELD_EFFECT_TO+"_new", nullable = true, insertable = true, updatable = false)
    private OffsetDateTime effectToNew;

    public OffsetDateTime getEffectTo() {
        return this.effectToNew;
    }

    public void setEffectTo(OffsetDateTime effectTo) {
        this.effectTo = fixOffsetIn(effectTo);
        this.effectToNew = effectTo;
    }


    @JsonIgnore
    public List<D> getDataItems() {
        return new ArrayList(this.dataItems);
    }

    /**
     * Merges the associated DataItems into one Map for output. Each DataItem may
     * point to several Effects, in order to avoid duplicate data, meaning that the
     * data that are effecting under this Effect is spread over several objects, and
     * needs merging.
     */
    @JsonProperty(value = "data")
    public Map<String, Object> getData() {
        HashMap<String, Object> outMap = new HashMap<>();
        for (D d : this.dataItems) {
            Map<String, Object> inMap = d.asMap();
            for (String key : inMap.keySet()) {
                Object value = inMap.get(key);
                if (value instanceof Collection) {
                    ArrayList<Object> outList = (ArrayList<Object>) outMap.get(key);
                    if (outList == null) {
                        outList = new ArrayList<>();
                        outMap.put(key, outList);
                    }
                    outList.addAll((Collection) value);
                } else {
                    outMap.put(key, value);
                }
            }
        }
        return outMap;
    }

    @JsonProperty(value = "data"/*, access = JsonProperty.Access.READ_ONLY*/)
    public void setDataItems(Collection<D> items) {
        this.dataItems.addAll(items);
    }

    public boolean equalData(V other) {
        return (
                (this.effectFrom == null ? other.getEffectFrom() == null : this.getEffectFrom()
                        .equals(other.getEffectFrom())) &&
                        (this.effectTo == null ? other.getEffectTo() == null : this.getEffectTo()
                                .equals(other.getEffectTo()))
        );
    }

    public static String getTableName(Class<? extends Effect> cls) {
        return cls.getAnnotation(Table.class).name();
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
        s.add(subIndentString + "from: " + this.getEffectFrom());
        s.add(subIndentString + "to: " + this.getEffectTo());
        s.add(subIndentString + "data: [");
        for (D dataItem : this.getDataItems()) {
            s.add(dataItem.toString(indent + 2));
        }
        s.add(subIndentString + "]");
        s.add(indentString + "}");
        return s.toString();
    }

    public void forceLoad(Session session) {
        for (D dataItem : this.dataItems) {
            dataItem.forceLoad(session);
        }
    }

    /**
     * Comparison method for the Comparable interface; results in Effects being sorted by effectFrom date, nulls first?
     */
    @Override
    public int compareTo(Effect o) {
        OffsetDateTime otherFrom = o == null ? null : o.getEffectFrom();
        int comparison = Equality.compare(this.getEffectFrom(), otherFrom, OffsetDateTime.class, false);
        if (comparison != 0) {
            return comparison;
        }
        OffsetDateTime otherTo = o == null ? null : o.getEffectTo();
        return Equality.compare(this.getEffectTo(), otherTo, OffsetDateTime.class, true);
    }


    public V createClone() {
        V other = (V) this.registration.createEffect(this.getEffectFrom(), this.getEffectTo());
        for (D data : this.dataItems) {
            data.addEffect(other);
        }
        return other;
    }

    public String toCompactString() {
        R registration = this.registration;
        return registration.registrationFrom + "|" + registration.registrationTo + "|" + this.getEffectFrom() + "|" + this.getEffectTo();
    }

    public boolean compareRange(V other) {
        return (other != null && this.compareRange(other.getEffectFrom(), other.getEffectTo()));
    }

    public boolean compareRange(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return (
                Equality.equal(this.getEffectFrom(), effectFrom) &&
                        Equality.equal(this.getEffectTo(), effectTo)
        );
    }

    public boolean compareRange(Bitemporality bitemporality) {
        return this.compareRange(bitemporality.effectFrom, bitemporality.effectTo);
    }

    public void updateTimestamp() {
        this.effectFromNew = Bitemporal.fixOffsetOut(this.effectFrom);
        this.effectToNew = Bitemporal.fixOffsetOut(this.effectTo);
    }

    public static List<String> updateFields() {
        return Arrays.asList(DB_FIELD_EFFECT_FROM, DB_FIELD_EFFECT_TO);
    }

}
