package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.ParamDef;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Created by lars on 20-02-17.
 * An Effect defines the time range in which a piece of data has effect.
 * An Effect points to exactly one Registration, but may have any number of DataItems
 * associated.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@FilterDefs({
        @FilterDef(name = Effect.FILTER_EFFECT_FROM, parameters = @ParamDef(name = Effect.FILTERPARAM_EFFECT_FROM, type = "java.time.OffsetDateTime")),
        @FilterDef(name = Effect.FILTER_EFFECT_TO, parameters = @ParamDef(name = Effect.FILTERPARAM_EFFECT_TO, type = "java.time.OffsetDateTime")),
        @FilterDef(name = DataItem.FILTER_RECORD_AFTER, parameters = @ParamDef(name = DataItem.FILTERPARAM_RECORD_AFTER, type = "java.time.OffsetDateTime"))
})
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
    @Filter(name = DataItem.FILTER_RECORD_AFTER, condition="(lastUpdated > :"+DataItem.FILTERPARAM_RECORD_AFTER+")")
    @JoinTable(
        joinColumns = @JoinColumn(name = "effects_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "dataitems_id", referencedColumnName = "id"),
        indexes = {
            @Index(columnList = "effects_id"),
            @Index(columnList = "dataitems_id")
        }
    )
    protected Set<D> dataItems;

    @Column(nullable = true, insertable = true, updatable = false)
    private OffsetDateTime effectFrom;

    @Column(nullable = true, insertable = true, updatable = false)
    private OffsetDateTime effectTo;

    public Effect() {
        this.dataItems = new HashSet<D>();
    }

    public Effect(R registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        this();
        this.setRegistration(registration);
        this.effectFrom = effectFrom;
        this.effectTo = effectTo;
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
     * @param effectTo A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Effect(R registration, String effectFrom, String effectTo) {
        this(
                registration,
                effectFrom != null ? OffsetDateTime.parse(effectFrom) : null,
                effectTo != null ? OffsetDateTime.parse(effectTo) : null
        );
    }

    @JsonIgnore
    @XmlTransient
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

    @JsonProperty(value = "virkningFra")
    @XmlElement(name = "virkningFra")
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value=OffsetDateTimeAdapter.class)
    public OffsetDateTime getEffectFrom() {
        return this.effectFrom;
    }

    @JsonProperty(value = "virkningFra")
    @XmlElement(name = "virkningFra")
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value=OffsetDateTimeAdapter.class)
    public void setEffectFrom(OffsetDateTime effectFrom) {
        this.effectFrom = effectFrom;
    }



    @JsonProperty(value = "virkningTil")
    @XmlElement(name = "virkningTil")
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value=OffsetDateTimeAdapter.class)
    public OffsetDateTime getEffectTo() {
        return this.effectTo;
    }

    @JsonProperty(value = "virkningTil")
    @XmlElement(name = "virkningTil")
    @XmlJavaTypeAdapter(type=OffsetDateTime.class, value=OffsetDateTimeAdapter.class)
    public void setEffectTo(OffsetDateTime effectTo) {
        this.effectTo = effectTo;
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
    @XmlElementWrapper(name = "data")
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
                    outList.addAll((Collection)value);
                } else {
                    outMap.put(key, value);
                }
            }
        }
        return outMap;
    }

    @JsonProperty(value = "data"/*, access = JsonProperty.Access.READ_ONLY*/)
    @JacksonXmlProperty(localName = "data")
    @JacksonXmlElementWrapper(useWrapping = false)
    public void setDataItems(Collection<D> items) {
        this.dataItems.addAll(items);
    }

    public boolean equalData(V other) {
        return (
                (this.effectFrom == null ? other.getEffectFrom() == null : this.effectFrom
                    .equals(other.getEffectFrom())) &&
                (this.effectTo == null ? other.getEffectTo() == null : this.effectTo
                    .equals(other.getEffectTo()))
        );
    }

    public static String getTableName(Class<? extends Effect> cls) {
        return cls.getAnnotation(Table.class).name();
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
        s.add(indentString + this.getClass().getSimpleName() + "["+this.hashCode()+"] {");
        s.add(subIndentString + "from: "+this.effectFrom);
        s.add(subIndentString + "to: "+this.effectTo);
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
        OffsetDateTime otherFrom = o == null ? null : o.effectFrom;
        int comparison = Equality.compare(this.effectFrom, otherFrom, OffsetDateTime.class, false);
        if (comparison != 0) {
            return comparison;
        }
        OffsetDateTime otherTo = o == null ? null : o.effectTo;
        return Equality.compare(this.effectTo, otherTo, OffsetDateTime.class, true);
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
        return registration.registrationFrom + "|" + registration.registrationTo + "|" + this.effectFrom + "|" + this.effectTo;
    }

}
