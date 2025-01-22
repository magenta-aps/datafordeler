package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;

import jakarta.persistence.Entity;
/**
 * Identifier for Entity objects, easing cross-referencing. A reference need not
 * locate the referenced Entity in the database (it may not even exist yet), but only
 * locate/generate the relevant Identification based on data that it should know.
 * Each UUID used here should be generated from identifying data, such as Person
 * Number, Company Number etc, so that a given one of these always results in the
 * same UUID.
 * That way, we can always locate/create an Identification when we know this seed
 * data.
 */
@Entity
@Table(name = "identification", indexes = {@Index(name = "uuid", columnList = "uuid"), @Index(name = "id", columnList = "uuid, domain")})
public final class Identification extends DatabaseEntry implements Comparable<Identification> {

    public Identification() {
    }

    public Identification(UUID uuid, String domain) {
        this.uuid = uuid;
        this.domain = domain;
    }

    public static final String DB_FIELD_UUID = "uuid";
    public static final String IO_FIELD_UUID = "uuid";

    @JsonProperty(value = IO_FIELD_UUID)
    @Column(unique = true, nullable = false, insertable = true, updatable = false, name = DB_FIELD_UUID)
    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public static final String DB_FIELD_DOMAIN = "domain";
    public static final String IO_FIELD_DOMAIN = "domæne";

    @Column(nullable = false, insertable = true, updatable = false, name = DB_FIELD_DOMAIN)
    private String domain;

    @JsonProperty(value = IO_FIELD_DOMAIN)
    public String getDomain() {
        return domain;
    }

    @JsonProperty(value = "domaene")
    public void setDomain(String domaene) {
        this.domain = domaene;
    }


    public static String getTableName() {
        return Identification.class.getAnnotation(Table.class).name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identification that = (Identification) o;

        if (!uuid.equals(that.uuid)) return false;
        return domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + domain.hashCode();
        return result;
    }

    @Override
    public int compareTo(Identification o) {
        if (o == null) return 1;
        return this.uuid.compareTo(o.getUuid());
    }
}
