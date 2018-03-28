package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.*;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.database.Registration;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = ParticipantMetadataRecord.TABLE_NAME, indexes = {
        @Index(name = ParticipantMetadataRecord.TABLE_NAME + "__participant", columnList = ParticipantMetadataRecord.DB_FIELD_PARTICIPANT + DatabaseEntry.REF),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParticipantMetadataRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_participant_metadata";

    public static final String DB_FIELD_NEWEST_LOCATION = "newestLocation";
    public static final String IO_FIELD_NEWEST_LOCATION = "nyesteBeliggenhedsadresse";

    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_PARTICIPANT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    @Filters({
            @Filter(name = Registration.FILTER_REGISTRATION_TO, condition="("+CvrBitemporalRecord.DB_FIELD_LAST_UPDATED+" < :"+Registration.FILTERPARAM_REGISTRATION_TO+")"),
            @Filter(name = Effect.FILTER_EFFECT_FROM, condition = "("+CvrRecordPeriod.DB_FIELD_VALID_TO+" >= :" + Effect.FILTERPARAM_EFFECT_FROM + " OR "+CvrRecordPeriod.DB_FIELD_VALID_TO+" is null)"),
            @Filter(name = Effect.FILTER_EFFECT_TO, condition = "("+CvrRecordPeriod.DB_FIELD_VALID_FROM+" < :" + Effect.FILTERPARAM_EFFECT_TO + " OR "+CvrRecordPeriod.DB_FIELD_VALID_FROM+" is null)")
    })
    @JsonProperty(value = IO_FIELD_NEWEST_LOCATION)
    private Set<AddressRecord> newestLocation = new HashSet<>();

    public void setNewestLocation(Set<AddressRecord> newestLocation) {
        this.newestLocation = newestLocation;
    }

    @JsonSetter(IO_FIELD_NEWEST_LOCATION)
    public void addNewestLocation(AddressRecord newestLocation) {
        if (newestLocation != null && !this.newestLocation.contains(newestLocation)) {
            newestLocation.setParticipantMetadataRecord(this);
            this.newestLocation.add(newestLocation);
        }
    }

    @JsonIgnore
    public Set<AddressRecord> getNewestLocation() {
        return this.newestLocation;
    }

    @JsonGetter(IO_FIELD_NEWEST_LOCATION)
    public AddressRecord getLatestNewestLocation() {
        AddressRecord latest = null;
        for (AddressRecord locationRecord : this.newestLocation) {
            if (latest == null || locationRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = locationRecord;
            }
        }
        return latest;
    }



    public static final String DB_FIELD_NEWEST_CONTACT_DATA = "newestContactData";
    public static final String IO_FIELD_NEWEST_CONTACT_DATA = "nyesteKontaktoplysninger";

    @OneToMany(targetEntity = MetadataContactRecord.class, mappedBy = MetadataContactRecord.DB_FIELD_PARTICIPANT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    @Filters({
            @Filter(name = Registration.FILTER_REGISTRATION_TO, condition="("+CvrBitemporalRecord.DB_FIELD_LAST_UPDATED+" < :"+Registration.FILTERPARAM_REGISTRATION_TO+")"),
            @Filter(name = Effect.FILTER_EFFECT_FROM, condition = "("+CvrRecordPeriod.DB_FIELD_VALID_TO+" >= :" + Effect.FILTERPARAM_EFFECT_FROM + " OR "+CvrRecordPeriod.DB_FIELD_VALID_TO+" is null)"),
            @Filter(name = Effect.FILTER_EFFECT_TO, condition = "("+CvrRecordPeriod.DB_FIELD_VALID_FROM+" < :" + Effect.FILTERPARAM_EFFECT_TO + " OR "+CvrRecordPeriod.DB_FIELD_VALID_FROM+" is null)")
    })
    private Set<MetadataContactRecord> metadataContactRecords = new HashSet<>();

    @JsonProperty(IO_FIELD_NEWEST_CONTACT_DATA)
    public void setMetadataContactData(Set<String> contactData) {
        HashSet<String> contacts = new HashSet<>(contactData);
        HashSet<MetadataContactRecord> remove = new HashSet<>();
        for (MetadataContactRecord contactRecord : this.metadataContactRecords) {
            String data = contactRecord.getData();
            if (contacts.contains(data)) {
                contacts.remove(data);
            } else {
                remove.add(contactRecord);
            }
        }
        this.metadataContactRecords.removeAll(remove);
        for (String data : contacts) {
            MetadataContactRecord newContactRecord = new MetadataContactRecord();
            newContactRecord.setData(data);
            newContactRecord.setParticipantMetadataRecord(this);
            this.metadataContactRecords.add(newContactRecord);
        }
    }

    public void addMetadataContactData(MetadataContactRecord contactRecord) {
        if (contactRecord != null && !this.metadataContactRecords.contains(contactRecord)) {
            contactRecord.setParticipantMetadataRecord(this);
            this.metadataContactRecords.add(contactRecord);
        }
    }

    @JsonProperty(IO_FIELD_NEWEST_CONTACT_DATA)
    public Set<String> getMetadataContactData() {
        HashSet<String> contacts = new HashSet<>();
        for (MetadataContactRecord metadataContactRecord : this.metadataContactRecords) {
            contacts.add(metadataContactRecord.getData());
        }
        return contacts;
    }

    public Set<MetadataContactRecord> getMetadataContactRecords() {
        return this.metadataContactRecords;
    }



    public void wire(Session session) {
        for (AddressRecord addressRecord : this.newestLocation) {
            addressRecord.wire(session);
        }
    }

    public boolean merge(ParticipantMetadataRecord other) {
        if (other != null && !Objects.equals(this.getId(), other.getId())) {
            for (AddressRecord addressRecord : other.getNewestLocation()) {
                this.addNewestLocation(addressRecord);
            }
            for (MetadataContactRecord contactRecord : other.getMetadataContactRecords()) {
                this.addMetadataContactData(contactRecord);
            }
            return true;
        }
        return false;
    }

}
