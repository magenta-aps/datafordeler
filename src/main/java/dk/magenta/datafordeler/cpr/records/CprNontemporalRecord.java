package dk.magenta.datafordeler.cpr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.cpr.data.CprRecordEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MappedSuperclass
public abstract class CprNontemporalRecord<E extends CprRecordEntity, S extends CprNontemporalRecord<E, S>> extends DatabaseEntry implements Nontemporal {


    public static final String FILTERPARAMTYPE_LASTUPDATED = "java.time.OffsetDateTime";


    public static final String DB_FIELD_ENTITY = "entity";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = CprNontemporalRecord.DB_FIELD_ENTITY + DatabaseEntry.REF)
    @JsonIgnore
    @XmlTransient
    private E entity;

    public E getEntity() {
        return this.entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public void setEntity(IdentifiedEntity entity) {
        this.entity = (E) entity;
    }



    @JsonProperty(value = "id")
    public Long getId() {
        return super.getId();
    }


    @Column(length = 1024)
    //@Transient
    @JsonIgnore
    public String line;


    @Column
    public int cnt;

    public int getCnt() {
        return this.cnt;
    }



    public static final String DB_FIELD_CORRECTION_OF = "correctionof";

    @ManyToOne(fetch = FetchType.LAZY)
    private S correctionof;

    public S getCorrectionof() {
        return this.correctionof;
    }

    public void setCorrectionof(S correctionof) {
        /*if (correctionof != null && correctionof.getCorrector() != null && correctionof.getCorrector() != this) {
            System.out.println("Tried to point "+this.cnt+" to "+correctionof.cnt+", but "+correctionof.cnt+" already has "+correctionof.getCorrector().cnt+" pointing to it");
        }*/
        this.correctionof = correctionof;
        //correctionof.setCorrector((S) this);
    }

/*
    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    @JsonIgnore
    private S corrector;

    public S getCorrector() {
        return this.corrector;
    }

    public void setCorrector(S corrector) {
        this.corrector = corrector;
    }*/

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "replacedby")
    private S replaces;

    @JsonIgnore
    public S getReplaces() {
        return this.replaces;
    }

    public void setReplaces(S replaces) {
        if (replaces != null && replaces.getReplacedby() != null && replaces.getReplacedby() != this) {
            System.out.println("Tried to point "+this.cnt+" to "+replaces.cnt+", but "+replaces.cnt+" already has "+replaces.getReplacedby().cnt+" pointing to it");
        }
        this.replaces = replaces;
    }

    @JsonProperty
    public Long getReplacesId() {
        return (this.replaces != null) ? this.replaces.getId() : null;
    }



    public static final String DB_FIELD_REPLACED_BY = "replacedby";

    @OneToOne(fetch = FetchType.LAZY)
    private S replacedby;

    @JsonIgnore
    public S getReplacedby() {
        return this.replacedby;
    }

    public void setReplacedby(S replacedby) {
        if (replacedby != null && replacedby.getReplaces() != null && replacedby.getReplaces() != this) {
            System.out.println("Tried to point "+this.cnt+" to "+ replacedby.cnt+", but "+ replacedby.cnt+" already has "+ replacedby.getReplaces().cnt+" pointing to it");
        }
        this.replacedby = replacedby;
        if (replacedby != null) {
            replacedby.setReplaces((S) this);
        }
    }

    @JsonProperty
    public Long getReplacedById() {
        return (this.replacedby != null) ? this.replacedby.getId() : null;
    }



    public static final String DB_FIELD_UNDONE = "undone";
    public static final String IO_FIELD_UNDONE = "undone";
    @Column(name = DB_FIELD_UNDONE)
    private Boolean undone = false;

    public boolean isUndone() {
        return this.undone != null ? this.undone : false;
    }

    public void setUndone(Boolean undone) {
        this.undone = undone != null ? undone : false;
    }



    public static final String DB_FIELD_AUTHORITY = "authority";
    public static final String IO_FIELD_AUTHORITY = "myndighed";
    @Column(name = DB_FIELD_AUTHORITY)
    @JsonProperty(value = IO_FIELD_AUTHORITY)
    @XmlElement(name = IO_FIELD_AUTHORITY)
    private int authority;

    public int getAuthority() {
        return this.authority;
    }

    public CprNontemporalRecord setAuthority(int authority) {
        if (authority != 0 || this.authority == 0) {
            this.authority = authority;
        }
        return this;
    }





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

    protected static void copy(CprNontemporalRecord from, CprNontemporalRecord to) {
        to.authority = from.authority;
        to.dafoUpdated = from.dafoUpdated;
    }



    public static final String DB_FIELD_ORIGIN = "origin";
    @Column(name = DB_FIELD_ORIGIN)
    private String origin;

    public String getOrigin() {
        return this.origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
        this.originDate = parseOrigin(origin);
    }

    private static final Pattern originPattern = Pattern.compile("d(\\d\\d)(\\d\\d)(\\d\\d)\\.l(\\d\\d\\d\\d\\d\\d)");
    public static LocalDate parseOrigin(String origin) {
        if (origin != null) {
            Matcher matcher = originPattern.matcher(origin);
            if (matcher.find()) {
                try {
                    return LocalDate.of(
                            2000 + Integer.parseInt(matcher.group(1)),
                            Integer.parseInt(matcher.group(2)),
                            Integer.parseInt(matcher.group(3))
                    );
                } catch (NumberFormatException | IndexOutOfBoundsException | IllegalStateException | DateTimeException e) {
                }
            }
        }
        return null;
    }

    public static final String DB_FIELD_ORIGIN_DATE = "originDate";
    @Column(name = DB_FIELD_ORIGIN_DATE)
    private LocalDate originDate;

    public LocalDate getOriginDate() {
        return this.originDate;
    }



    public boolean equalData(Object o) {
        if (o==null || (getClass() != o.getClass())) return false;
        CprNontemporalRecord that = (CprNontemporalRecord) o;
        //System.out.println(authority+(authority == that.authority ? " == ":" != ")+that.authority);
        return Objects.equals(this.authority, that.authority)/* && Objects.equals(this.origin, that.origin)*/;
    }

    public abstract boolean hasData();

    protected static boolean stringNonEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    protected static boolean intNonZero(Integer i) {
        return i != null && i != 0;
    }

    protected static String trim(String text) {
        return text != null ? text.trim() : null;
    }
}
