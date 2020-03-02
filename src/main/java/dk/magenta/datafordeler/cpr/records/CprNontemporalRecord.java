package dk.magenta.datafordeler.cpr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.cpr.data.CprEntity;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.ParamDef;

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
@FilterDefs({
        @FilterDef(name = Nontemporal.FILTER_LASTUPDATED_AFTER, parameters = @ParamDef(name = Nontemporal.FILTERPARAM_LASTUPDATED_AFTER, type = "java.time.OffsetDateTime")),
        @FilterDef(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, parameters = @ParamDef(name = Nontemporal.FILTERPARAM_LASTUPDATED_BEFORE, type = "java.time.OffsetDateTime"))
})
public abstract class CprNontemporalRecord<E extends CprEntity> extends DatabaseEntry implements Nontemporal<E> {

    public static final String DB_FIELD_ENTITY = Nontemporal.DB_FIELD_ENTITY;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
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

    public CprNontemporalRecord setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = dafoUpdated;
        return this;
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
        return Objects.equals(this.authority, that.authority) && Objects.equals(this.origin, that.origin);
    }
}
