package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.*;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.Session;
import org.hibernate.annotations.*;

import java.util.*;
import java.util.function.Consumer;

@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyUnitMetadataRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyUnitMetadataRecord.TABLE_NAME + "__unit", columnList = MetadataRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF, unique = true),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyUnitMetadataRecord extends MetadataRecord {

    public static final String TABLE_NAME = "cvr_record_unit_metadata";

    public static final String DB_FIELD_NEWEST_CVR_RELATION = "newestCvrRelation";
    public static final String IO_FIELD_NEWEST_CVR_RELATION = "nyesteCvrNummerRelation";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column
    @JsonProperty(value = IO_FIELD_NEWEST_CVR_RELATION)
    private int newestCvrRelation;

    public int getNewestCvrRelation() {
        return this.newestCvrRelation;
    }

    public void setNewestCvrRelation(int newestCvrRelation) {
        this.newestCvrRelation = newestCvrRelation;
    }


    @OneToMany(targetEntity = MetadataContactRecord.class, mappedBy = MetadataContactRecord.DB_FIELD_UNIT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MetadataContactRecord> metadataContactRecords = new HashSet<>();

    public RecordSet<MetadataContactRecord, CompanyUnitMetadataRecord> getMetadataContactRecords() {
        return new RecordSet<>(this.metadataContactRecords, MetadataContactRecord.class, this, MetadataContactRecord.DB_FIELD_UNIT_METADATA);
    }

    public void setMetadataContactRecords(Set<MetadataContactRecord> metadataContactRecords) {
        this.metadataContactRecords = (metadataContactRecords == null) ? new HashSet<>() : new HashSet<>(metadataContactRecords);
        for (MetadataContactRecord metadataContactRecord : this.metadataContactRecords) {
            metadataContactRecord.setUnitMetadataRecord(this);
        }
    }

    public void addMetadataContactRecord(MetadataContactRecord metadataContactRecord) {
        if (metadataContactRecord != null && !this.metadataContactRecords.contains(metadataContactRecord)) {
            metadataContactRecord.setUnitMetadataRecord(this);
            this.metadataContactRecords.add(metadataContactRecord);
        }
    }


    public static final String DB_FIELD_NEWEST_NAME = "newestName";
    public static final String IO_FIELD_NEWEST_NAME = "nyesteNavn";

    @OneToMany(targetEntity = BaseNameRecord.class, mappedBy = BaseNameRecord.DB_FIELD_UNIT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonIgnore
    private Set<BaseNameRecord> newestName = new HashSet<>();

    public void setNewestName(Set<BaseNameRecord> newestName) {
        this.newestName = (newestName == null) ? new HashSet<>() : new HashSet<>(newestName);
        for (BaseNameRecord nameRecord : this.newestName) {
            nameRecord.setUnitMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_NAME)
    public void addNewestName(BaseNameRecord newestName) {
        if (newestName != null && !this.newestName.contains(newestName)) {
            newestName.setMetadataRecord(this);
            this.newestName.add(newestName);
        }
    }

    @JsonIgnore
    public RecordSet<BaseNameRecord, CompanyUnitMetadataRecord> getNewestName() {
        return new RecordSet<>(this.newestName, BaseNameRecord.class, this, BaseNameRecord.DB_FIELD_UNIT_METADATA);
    }

    @JsonGetter(IO_FIELD_NEWEST_NAME)
    public BaseNameRecord getLatestNewestName() {
        BaseNameRecord latest = null;
        for (BaseNameRecord nameRecord : this.newestName) {
            if (latest == null || nameRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = nameRecord;
            }
        }
        return latest;
    }


    public static final String DB_FIELD_NEWEST_LOCATION = "newestLocation";
    public static final String IO_FIELD_NEWEST_LOCATION = "nyesteBeliggenhedsadresse";

    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_UNIT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonIgnore
    private Set<AddressRecord> newestLocation = new HashSet<>();

    public void setNewestLocation(Set<AddressRecord> newestLocation) {
        this.newestLocation = (newestLocation == null) ? new HashSet<>() : new HashSet<>(newestLocation);
        for (AddressRecord addressRecord : this.newestLocation) {
            addressRecord.setUnitMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_LOCATION)
    public void addNewestLocation(AddressRecord newestLocation) {
        if (newestLocation != null && !this.newestLocation.contains(newestLocation)) {
            newestLocation.setMetadataRecord(this);
            this.newestLocation.add(newestLocation);
        }
    }

    @JsonIgnore
    public RecordSet<AddressRecord, CompanyUnitMetadataRecord> getNewestLocation() {
        return new RecordSet<>(this.newestLocation, AddressRecord.class, this, AddressRecord.DB_FIELD_UNIT_METADATA);
    }

    @JsonGetter(IO_FIELD_NEWEST_LOCATION)
    public AddressRecord getLatestNewestLocation() {
        AddressRecord latest = null;
        for (AddressRecord nameRecord : this.newestLocation) {
            if (latest == null || nameRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = nameRecord;
            }
        }
        return latest;
    }


    public static final String DB_FIELD_NEWEST_PRIMARY_INDUSTRY = "newestPrimaryIndustry";
    public static final String IO_FIELD_NEWEST_PRIMARY_INDUSTRY = "nyesteHovedbranche";
    public static final String CLAUSE_PRIMARY_INDUSTRY = CompanyIndustryRecord.DB_FIELD_INDEX + "=0";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_UNIT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction(CLAUSE_PRIMARY_INDUSTRY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonIgnore
    private Set<CompanyIndustryRecord> newestPrimaryIndustry = new HashSet<>();

    public void setNewestPrimaryIndustry(Set<CompanyIndustryRecord> newestPrimaryIndustry) {
        this.newestPrimaryIndustry = (newestPrimaryIndustry == null) ? new HashSet<>() : new HashSet<>(newestPrimaryIndustry);
        for (CompanyIndustryRecord industryRecord : this.newestPrimaryIndustry) {
            industryRecord.setUnitMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_PRIMARY_INDUSTRY)
    public void addNewestPrimaryIndustry(CompanyIndustryRecord newestPrimaryIndustry) {
        if (newestPrimaryIndustry != null && !this.newestPrimaryIndustry.contains(newestPrimaryIndustry)) {
            newestPrimaryIndustry.setMetadataRecord(this);
            newestPrimaryIndustry.setIndex(0);
            this.newestPrimaryIndustry.add(newestPrimaryIndustry);
        }
    }

    public BitemporalSet<CompanyIndustryRecord, CompanyUnitMetadataRecord> getNewestPrimaryIndustry() {
        return new BitemporalSet<>(this.newestPrimaryIndustry, CompanyIndustryRecord.class, this, CompanyIndustryRecord.DB_FIELD_UNIT_METADATA, "index=0");
    }

    @JsonGetter(IO_FIELD_NEWEST_PRIMARY_INDUSTRY)
    public CompanyIndustryRecord getLatestNewestPrimaryIndustry() {
        CompanyIndustryRecord latest = null;
        for (CompanyIndustryRecord industryRecord : this.newestPrimaryIndustry) {
            if (latest == null || industryRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = industryRecord;
            }
        }
        return latest;
    }


    public static final String DB_FIELD_NEWEST_SECONDARY_INDUSTRY1 = "newestSecondaryIndustry1";
    public static final String IO_FIELD_NEWEST_SECONDARY_INDUSTRY1 = "nyesteBibranche1";
    public static final String CLAUSE_SECONDARY_INDUSTRY1 = CompanyIndustryRecord.DB_FIELD_INDEX + "=1";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_UNIT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction(CompanyIndustryRecord.DB_FIELD_INDEX + "=1")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonIgnore
    private Set<CompanyIndustryRecord> newestSecondaryIndustry1 = new HashSet<>();

    public void addNewestSecondaryIndustry1(Set<CompanyIndustryRecord> newestSecondaryIndustry1) {
        this.newestSecondaryIndustry1 = (newestSecondaryIndustry1 == null) ? new HashSet<>() : new HashSet<>(newestSecondaryIndustry1);
        for (CompanyIndustryRecord industryRecord : this.newestSecondaryIndustry1) {
            industryRecord.setUnitMetadataRecord(this);
        }
    }

    @JsonProperty(IO_FIELD_NEWEST_SECONDARY_INDUSTRY1)
    public void addNewestSecondaryIndustry1(CompanyIndustryRecord newestSecondaryIndustry1) {
        if (newestSecondaryIndustry1 != null && !this.newestSecondaryIndustry1.contains(newestSecondaryIndustry1)) {
            newestSecondaryIndustry1.setMetadataRecord(this);
            newestSecondaryIndustry1.setIndex(1);
            this.newestSecondaryIndustry1.add(newestSecondaryIndustry1);
        }
    }

    @JsonIgnore
    public RecordSet<CompanyIndustryRecord, CompanyUnitMetadataRecord> getNewestSecondaryIndustry1() {
        return new RecordSet<>(this.newestSecondaryIndustry1, CompanyIndustryRecord.class, this, CompanyIndustryRecord.DB_FIELD_UNIT_METADATA, "index=1");
    }

    @JsonProperty(IO_FIELD_NEWEST_SECONDARY_INDUSTRY1)
    public CompanyIndustryRecord getLatestNewestSecondaryIndustry1() {
        CompanyIndustryRecord latest = null;
        for (CompanyIndustryRecord industryRecord : this.newestSecondaryIndustry1) {
            if (latest == null || industryRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = industryRecord;
            }
        }
        return latest;
    }


    public static final String DB_FIELD_NEWEST_SECONDARY_INDUSTRY2 = "newestSecondaryIndustry2";
    public static final String IO_FIELD_NEWEST_SECONDARY_INDUSTRY2 = "nyesteBibranche2";
    public static final String CLAUSE_SECONDARY_INDUSTRY2 = CompanyIndustryRecord.DB_FIELD_INDEX + "=2";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_UNIT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction(CLAUSE_SECONDARY_INDUSTRY2)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonIgnore
    private Set<CompanyIndustryRecord> newestSecondaryIndustry2 = new HashSet<>();

    public void setNewestSecondaryIndustry2(Set<CompanyIndustryRecord> newestSecondaryIndustry2) {
        this.newestSecondaryIndustry2 = (newestSecondaryIndustry2 == null) ? new HashSet<>() : new HashSet<>(newestSecondaryIndustry2);
        for (CompanyIndustryRecord industryRecord : this.newestSecondaryIndustry2) {
            industryRecord.setUnitMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_SECONDARY_INDUSTRY2)
    public void addNewestSecondaryIndustry2(CompanyIndustryRecord newestSecondaryIndustry2) {
        if (newestSecondaryIndustry2 != null && !this.newestSecondaryIndustry2.contains(newestSecondaryIndustry2)) {
            newestSecondaryIndustry2.setMetadataRecord(this);
            newestSecondaryIndustry2.setIndex(2);
            this.newestSecondaryIndustry2.add(newestSecondaryIndustry2);
        }
    }

    @JsonIgnore
    public RecordSet<CompanyIndustryRecord, CompanyUnitMetadataRecord> getNewestSecondaryIndustry2() {
        return new RecordSet<>(this.newestSecondaryIndustry2, CompanyIndustryRecord.class, this, CompanyIndustryRecord.DB_FIELD_UNIT_METADATA, "index=2");
    }

    @JsonGetter(IO_FIELD_NEWEST_SECONDARY_INDUSTRY2)
    public CompanyIndustryRecord getLatestNewestSecondaryIndustry2() {
        CompanyIndustryRecord latest = null;
        for (CompanyIndustryRecord industryRecord : this.newestSecondaryIndustry2) {
            if (latest == null || industryRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = industryRecord;
            }
        }
        return latest;
    }


    public static final String DB_FIELD_NEWEST_SECONDARY_INDUSTRY3 = "newestSecondaryIndustry3";
    public static final String IO_FIELD_NEWEST_SECONDARY_INDUSTRY3 = "nyesteBibranche3";
    public static final String CLAUSE_SECONDARY_INDUSTRY3 = CompanyIndustryRecord.DB_FIELD_INDEX + "=3";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_UNIT_METADATA, cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction(CLAUSE_SECONDARY_INDUSTRY3)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonIgnore
    private Set<CompanyIndustryRecord> newestSecondaryIndustry3 = new HashSet<>();

    public void setNewestSecondaryIndustry3(Set<CompanyIndustryRecord> newestSecondaryIndustry3) {
        this.newestSecondaryIndustry3 = (newestSecondaryIndustry3 == null) ? new HashSet<>() : new HashSet<>(newestSecondaryIndustry3);
        for (CompanyIndustryRecord industryRecord : this.newestSecondaryIndustry3) {
            industryRecord.setUnitMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_SECONDARY_INDUSTRY3)
    public void addNewestSecondaryIndustry3(CompanyIndustryRecord newestSecondaryIndustry3) {
        if (newestSecondaryIndustry3 != null && !this.newestSecondaryIndustry3.contains(newestSecondaryIndustry3)) {
            newestSecondaryIndustry3.setMetadataRecord(this);
            newestSecondaryIndustry3.setIndex(3);
            this.newestSecondaryIndustry3.add(newestSecondaryIndustry3);
        }
    }

    @JsonIgnore
    public RecordSet<CompanyIndustryRecord, CompanyUnitMetadataRecord> getNewestSecondaryIndustry3() {
        return new RecordSet<>(this.newestSecondaryIndustry3, CompanyIndustryRecord.class, this, CompanyIndustryRecord.DB_FIELD_UNIT_METADATA, "index=3");
    }

    @JsonGetter(IO_FIELD_NEWEST_SECONDARY_INDUSTRY3)
    public CompanyIndustryRecord getLatestNewestSecondaryIndustry3() {
        CompanyIndustryRecord latest = null;
        for (CompanyIndustryRecord industryRecord : this.newestSecondaryIndustry3) {
            if (latest == null || industryRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = industryRecord;
            }
        }
        return latest;
    }

    @Override
    public void wire(Session session) {
        for (AddressRecord addressRecord : this.newestLocation) {
            addressRecord.wire(session);
        }
    }


    @Override
    public boolean merge(MetadataRecord other) {
        if (other != null && !Objects.equals(this.getId(), other.getId()) && other instanceof CompanyUnitMetadataRecord) {
            CompanyUnitMetadataRecord otherRecord = (CompanyUnitMetadataRecord) other;
            for (BaseNameRecord nameRecord : otherRecord.getNewestName()) {
                this.addNewestName(nameRecord);
            }
            for (AddressRecord addressRecord : otherRecord.getNewestLocation()) {
                this.addNewestLocation(addressRecord);
            }
            for (CompanyIndustryRecord industryRecord : otherRecord.getNewestPrimaryIndustry()) {
                this.addNewestPrimaryIndustry(industryRecord);
            }
            for (CompanyIndustryRecord industryRecord : otherRecord.getNewestSecondaryIndustry1()) {
                this.addNewestSecondaryIndustry1(industryRecord);
            }
            for (CompanyIndustryRecord industryRecord : otherRecord.getNewestSecondaryIndustry2()) {
                this.addNewestSecondaryIndustry2(industryRecord);
            }
            for (CompanyIndustryRecord industryRecord : otherRecord.getNewestSecondaryIndustry3()) {
                this.addNewestSecondaryIndustry3(industryRecord);
            }
            for (MetadataContactRecord metadataContactRecord : otherRecord.getMetadataContactRecords()) {
                this.addMetadataContactRecord(metadataContactRecord);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.newestName);
        subs.addAll(this.newestLocation);
        subs.addAll(this.newestPrimaryIndustry);
        subs.addAll(this.newestSecondaryIndustry1);
        subs.addAll(this.newestSecondaryIndustry2);
        subs.addAll(this.newestSecondaryIndustry3);
        subs.addAll(this.metadataContactRecords);
        return subs;
    }

    @Override
    public void traverse(Consumer<RecordSet<? extends CvrRecord, ? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        super.traverse(setCallback, itemCallback);
        this.getNewestName().traverse(setCallback, itemCallback);
        this.getNewestLocation().traverse(setCallback, itemCallback);
        this.getNewestPrimaryIndustry().traverse(setCallback, itemCallback);
        this.getNewestSecondaryIndustry1().traverse(setCallback, itemCallback);
        this.getNewestSecondaryIndustry2().traverse(setCallback, itemCallback);
        this.getNewestSecondaryIndustry3().traverse(setCallback, itemCallback);
        CompanyYearlyNumbersRecord yearlyNumbersRecord = this.getNewestYearlyNumbers();
        if (yearlyNumbersRecord != null) {
            yearlyNumbersRecord.traverse(setCallback, itemCallback);
        }
        CompanyQuarterlyNumbersRecord quarterlyNumbersRecord = this.getNewestQuarterlyNumbers();
        if (quarterlyNumbersRecord != null) {
            quarterlyNumbersRecord.traverse(setCallback, itemCallback);
        }
        this.getMetadataContactRecords().traverse(setCallback, itemCallback);
    }

    public ArrayList<CvrBitemporalRecord> closeRegistrations() {
        ArrayList<CvrBitemporalRecord> updated = new ArrayList<>();
        updated.addAll(CvrBitemporalRecord.closeRegistrations(this.newestName));
        updated.addAll(CvrBitemporalRecord.closeRegistrations(this.newestLocation));
        updated.addAll(CvrBitemporalRecord.closeRegistrations(this.newestPrimaryIndustry));
        updated.addAll(CvrBitemporalRecord.closeRegistrations(this.newestSecondaryIndustry1));
        updated.addAll(CvrBitemporalRecord.closeRegistrations(this.newestSecondaryIndustry2));
        updated.addAll(CvrBitemporalRecord.closeRegistrations(this.newestSecondaryIndustry3));
        return updated;
    }
}
