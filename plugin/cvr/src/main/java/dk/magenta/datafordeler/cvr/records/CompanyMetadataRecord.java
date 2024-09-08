package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.*;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import org.hibernate.Session;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMetadataRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMetadataRecord.TABLE_NAME + "__company", columnList = MetadataRecord.DB_FIELD_COMPANY + DatabaseEntry.REF, unique = true),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyMetadataRecord extends MetadataRecord {

    public static final String TABLE_NAME = "cvr_record_company_metadata";

    public static final String DB_FIELD_NEWEST_FORM = "newestForm";
    public static final String IO_FIELD_NEWEST_FORM = "nyesteVirksomhedsform";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @OneToMany(mappedBy = FormRecord.DB_FIELD_COMPANY_METADATA, targetEntity = FormRecord.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    private Set<FormRecord> newestForm = new HashSet<>();

    public void setNewestForm(Set<FormRecord> newestForm) {
        this.newestForm = (newestForm == null) ? new HashSet<>() : new HashSet<>(newestForm);
        for (FormRecord form : this.newestForm) {
            form.setCompanyMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_FORM)
    public void addNewestForm(FormRecord newestForm) {
        if (newestForm != null) {
            newestForm.setCompanyMetadataRecord(this);
            this.newestForm.add(newestForm);
        }
    }

    @JsonIgnore
    public Set<FormRecord> getNewestForm() {
        return this.newestForm;
    }

    @JsonGetter(IO_FIELD_NEWEST_FORM)
    public FormRecord getLatestNewestForm() {
        FormRecord latest = null;
        for (FormRecord formRecord : this.newestForm) {
            if (latest == null || formRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = formRecord;
            }
        }
        return latest;
    }


    public static final String DB_FIELD_NEWEST_NAME = "newestName";
    public static final String IO_FIELD_NEWEST_NAME = "nyesteNavn";

    @OneToMany(targetEntity = BaseNameRecord.class, mappedBy = BaseNameRecord.DB_FIELD_COMPANY_METADATA, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_NEWEST_NAME)
    private Set<BaseNameRecord> newestName = new HashSet<>();

    public void setNewestName(Set<BaseNameRecord> newestName) {
        this.newestName = (newestName == null) ? new HashSet<>() : new HashSet<>(newestName);
        for (BaseNameRecord name : this.newestName) {
            name.setCompanyMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_NAME)
    public void addNewestName(BaseNameRecord newestName) {
        if (newestName != null) {
            newestName.setMetadataRecord(this);
            this.newestName.add(newestName);
        }
    }

    @JsonIgnore
    public Set<BaseNameRecord> getNewestName() {
        return this.newestName;
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

    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_COMPANY_METADATA, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_NEWEST_LOCATION)
    private Set<AddressRecord> newestLocation = new HashSet<>();

    public void setNewestLocation(Set<AddressRecord> newestLocation) {
        this.newestLocation = (newestLocation == null) ? new HashSet<>() : new HashSet<>(newestLocation);
        for (AddressRecord location : this.newestLocation) {
            location.setCompanyMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_LOCATION)
    public void addNewestLocation(AddressRecord newestLocation) {
        if (newestLocation != null) {
            newestLocation.setMetadataRecord(this);
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
        for (AddressRecord nameRecord : this.newestLocation) {
            if (latest == null || nameRecord.getLastUpdated().isAfter(latest.getLastUpdated())) {
                latest = nameRecord;
            }
        }
        return latest;
    }


    public static final String DB_FIELD_NEWEST_PRIMARY_INDUSTRY = "newestPrimaryIndustry";
    public static final String IO_FIELD_NEWEST_PRIMARY_INDUSTRY = "nyesteHovedbranche";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY_METADATA, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Where(clause = CompanyIndustryRecord.DB_FIELD_INDEX + "=0")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_NEWEST_PRIMARY_INDUSTRY)
    private Set<CompanyIndustryRecord> newestPrimaryIndustry = new HashSet<>();

    public void setNewestPrimaryIndustry(Set<CompanyIndustryRecord> newestPrimaryIndustry) {
        this.newestPrimaryIndustry = (newestPrimaryIndustry == null) ? new HashSet<>() : new HashSet<>(newestPrimaryIndustry);
        for (CompanyIndustryRecord industry : this.newestPrimaryIndustry) {
            industry.setCompanyMetadataRecord(this);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_PRIMARY_INDUSTRY)
    public void addNewestPrimaryIndustry(CompanyIndustryRecord newestPrimaryIndustry) {
        if (newestPrimaryIndustry != null) {
            newestPrimaryIndustry.setMetadataRecord(this);
            newestPrimaryIndustry.setIndex(0);
            this.newestPrimaryIndustry.add(newestPrimaryIndustry);
        }
    }

    @JsonIgnore
    public Set<CompanyIndustryRecord> getNewestPrimaryIndustry() {
        return this.newestPrimaryIndustry;
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

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY_METADATA, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Where(clause = CompanyIndustryRecord.DB_FIELD_INDEX + "=1")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_NEWEST_SECONDARY_INDUSTRY1)
    private Set<CompanyIndustryRecord> newestSecondaryIndustry1 = new HashSet<>();

    public void setNewestSecondaryIndustry1(Set<CompanyIndustryRecord> newestSecondaryIndustry1) {
        this.newestSecondaryIndustry1 = (newestSecondaryIndustry1 == null) ? new HashSet<>() : new HashSet<>(newestSecondaryIndustry1);
        for (CompanyIndustryRecord industry : this.newestSecondaryIndustry1) {
            industry.setCompanyMetadataRecord(this);
            industry.setIndex(1);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_SECONDARY_INDUSTRY1)
    public void addNewestSecondaryIndustry1(CompanyIndustryRecord newestSecondaryIndustry1) {
        if (newestSecondaryIndustry1 != null) {
            newestSecondaryIndustry1.setMetadataRecord(this);
            newestSecondaryIndustry1.setIndex(1);
            this.newestSecondaryIndustry1.add(newestSecondaryIndustry1);
        }
    }

    @JsonIgnore
    public Set<CompanyIndustryRecord> getNewestSecondaryIndustry1() {
        return this.newestSecondaryIndustry1;
    }

    @JsonGetter(IO_FIELD_NEWEST_SECONDARY_INDUSTRY1)
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

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY_METADATA, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Where(clause = CompanyIndustryRecord.DB_FIELD_INDEX + "=2")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_NEWEST_SECONDARY_INDUSTRY2)
    private Set<CompanyIndustryRecord> newestSecondaryIndustry2 = new HashSet<>();

    public void setNewestSecondaryIndustry2(Set<CompanyIndustryRecord> newestSecondaryIndustry2) {
        this.newestSecondaryIndustry2 = (newestSecondaryIndustry2 == null) ? new HashSet<>() : new HashSet<>(newestSecondaryIndustry2);
        for (CompanyIndustryRecord industry : this.newestSecondaryIndustry2) {
            industry.setCompanyMetadataRecord(this);
            industry.setIndex(2);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_SECONDARY_INDUSTRY2)
    public void addNewestSecondaryIndustry2(CompanyIndustryRecord newestSecondaryIndustry2) {
        if (newestSecondaryIndustry2 != null) {
            newestSecondaryIndustry2.setMetadataRecord(this);
            newestSecondaryIndustry2.setIndex(2);
            this.newestSecondaryIndustry2.add(newestSecondaryIndustry2);
        }
    }

    @JsonIgnore
    public Set<CompanyIndustryRecord> getNewestSecondaryIndustry2() {
        return this.newestSecondaryIndustry2;
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

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY_METADATA, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Where(clause = CompanyIndustryRecord.DB_FIELD_INDEX + "=3")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_NEWEST_SECONDARY_INDUSTRY3)
    private Set<CompanyIndustryRecord> newestSecondaryIndustry3 = new HashSet<>();

    public void setNewestSecondaryIndustry3(Set<CompanyIndustryRecord> newestSecondaryIndustry3) {
        this.newestSecondaryIndustry3 = (newestSecondaryIndustry3 == null) ? new HashSet<>() : new HashSet<>(newestSecondaryIndustry3);
        for (CompanyIndustryRecord industryRecord : this.newestSecondaryIndustry3) {
            industryRecord.setMetadataRecord(this);
            industryRecord.setIndex(3);
        }
    }

    @JsonSetter(IO_FIELD_NEWEST_SECONDARY_INDUSTRY3)
    public void addNewestSecondaryIndustry3(CompanyIndustryRecord newestSecondaryIndustry3) {
        if (newestSecondaryIndustry3 != null) {
            newestSecondaryIndustry3.setMetadataRecord(this);
            newestSecondaryIndustry3.setIndex(3);
            this.newestSecondaryIndustry3.add(newestSecondaryIndustry3);
        }
    }

    @JsonIgnore
    public Set<CompanyIndustryRecord> getNewestSecondaryIndustry3() {
        return this.newestSecondaryIndustry3;
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


    // Apparently a recent addition to metadata, but it's an empty array in all entries
    public static final String DB_FIELD_NEWEST_FAD_CPR = "newestFadCpr";
    public static final String IO_FIELD_NEWEST_FAD_CPR = "nyesteFadCprnumre";

    @Transient
    @JsonProperty(value = IO_FIELD_NEWEST_FAD_CPR)
    private Set<String> newestFadCpr = new HashSet<>();

    public Set<String> getNewestFadCpr() {
        return this.newestFadCpr;
    }


    public static final String DB_FIELD_NEWEST_STATUS = "newestStatus";
    public static final String IO_FIELD_NEWEST_STATUS = "nyesteStatus";

    @OneToOne(targetEntity = StatusRecord.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty(value = IO_FIELD_NEWEST_STATUS)
    private StatusRecord newestStatus;

    @Override
    public StatusRecord getNewestStatus() {
        return this.newestStatus;
    }


    public static final String DB_FIELD_UNIT_COUNT = "unitCount";
    public static final String IO_FIELD_UNIT_COUNT = "antalPenheder";

    @Column(name = DB_FIELD_UNIT_COUNT)
    @JsonProperty(value = IO_FIELD_UNIT_COUNT)
    private int unitCount;

    @Override
    public int getUnitCount() {
        return this.unitCount;
    }


    public static final String DB_FIELD_FOUNDING_DATE = "foundingDate";
    public static final String IO_FIELD_FOUNDING_DATE = "stiftelsesDato";

    @Column(name = DB_FIELD_FOUNDING_DATE)
    @JsonProperty(value = IO_FIELD_FOUNDING_DATE)
    private LocalDate foundingDate;

    @Override
    public LocalDate getFoundingDate() {
        return this.foundingDate;
    }


    public static final String DB_FIELD_EFFECT_DATE = "effectDate";
    public static final String IO_FIELD_EFFECT_DATE = "virkningsDato";

    @Column(name = DB_FIELD_EFFECT_DATE)
    @JsonProperty(value = IO_FIELD_EFFECT_DATE)
    private LocalDate effectDate;

    @Override
    public LocalDate getEffectDate() {
        return this.effectDate;
    }


    @OneToMany(targetEntity = MetadataContactRecord.class, mappedBy = MetadataContactRecord.DB_FIELD_COMPANY_METADATA, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MetadataContactRecord> metadataContactRecords = new HashSet<>();

    public Set<MetadataContactRecord> getMetadataContactRecords() {
        return this.metadataContactRecords;
    }

    public void setMetadataContactRecords(Set<MetadataContactRecord> metadataContactRecords) {
        this.metadataContactRecords = (metadataContactRecords == null) ? new HashSet<>() : new HashSet<>(metadataContactRecords);
        for (MetadataContactRecord metadataContactRecord : this.metadataContactRecords) {
            metadataContactRecord.setCompanyMetadataRecord(this);
        }
    }

    public void addMetadataContactRecord(MetadataContactRecord metadataContactRecord) {
        if (metadataContactRecord != null) {
            metadataContactRecord.setCompanyMetadataRecord(this);
            this.metadataContactRecords.add(metadataContactRecord);
        }
    }


    @Override
    public void wire(Session session) {
        super.wire(session);
        for (FormRecord formRecord : this.newestForm) {
            formRecord.wire(session);
        }
        for (AddressRecord addressRecord : this.newestLocation) {
            addressRecord.wire(session);
        }
    }

    @Override
    public boolean merge(MetadataRecord other) {
        if (other != null && !Objects.equals(this.getId(), other.getId()) && other instanceof CompanyMetadataRecord) {
            CompanyMetadataRecord otherRecord = (CompanyMetadataRecord) other;
            for (FormRecord formRecord : otherRecord.getNewestForm()) {
                this.addNewestForm(formRecord);
            }
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
//            CvrBitemporalRecord.updateRegistrations(this.newestForm);
//            CvrBitemporalRecord.updateRegistrations(this.newestName);
//            CvrBitemporalRecord.updateRegistrations(this.newestLocation);
//            CvrBitemporalRecord.updateRegistrations(this.newestPrimaryIndustry);
//            CvrBitemporalRecord.updateRegistrations(this.newestSecondaryIndustry1);
//            CvrBitemporalRecord.updateRegistrations(this.newestSecondaryIndustry2);
//            CvrBitemporalRecord.updateRegistrations(this.newestSecondaryIndustry3);
            return true;
        }
        return false;
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.newestForm);
        subs.addAll(this.newestName);
        subs.addAll(this.newestLocation);
        subs.addAll(this.newestPrimaryIndustry);
        subs.addAll(this.newestSecondaryIndustry1);
        subs.addAll(this.newestSecondaryIndustry2);
        subs.addAll(this.newestSecondaryIndustry3);
        return subs;
    }

}
