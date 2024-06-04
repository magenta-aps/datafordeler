package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.CvrPlugin;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Record for one participant on a Company or CompanyUnit
 */
@Entity
@Table(
        name = CvrPlugin.DEBUG_TABLE_PREFIX + RelationCompanyRecord.TABLE_NAME,
        indexes = {
                @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + RelationCompanyRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
                @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + RelationCompanyRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
                @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + RelationCompanyRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
        }
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationCompanyRecord extends CvrBitemporalRecord {

    public static final String TABLE_NAME = CompanyParticipantRelationRecord.TABLE_NAME + "_company";

    public static final String DB_FIELD_UNITNUMBER = "unitNumber";
    public static final String IO_FIELD_UNITNUMBER = "enhedsNummer";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column(name = DB_FIELD_UNITNUMBER)
    @JsonProperty(value = IO_FIELD_UNITNUMBER)
    private long unitNumber;

    public long getUnitNumber() {
        return this.unitNumber;
    }


    public static final String DB_FIELD_UNITTYPE = "unitType";
    public static final String IO_FIELD_UNITTYPE = "enhedstype";

    @Column(name = DB_FIELD_UNITTYPE)
    @JsonProperty(value = IO_FIELD_UNITTYPE)
    private String unitType;

    public String getUnitType() {
        return this.unitType;
    }


    public static final String DB_FIELD_CVRNUMBER = "cvrNumber";
    public static final String IO_FIELD_CVRNUMBER = "cvrNummer";

    @Column(name = DB_FIELD_CVRNUMBER)
    @JsonProperty(value = IO_FIELD_CVRNUMBER)
    private long cvrNumber;

    public long getCvrNumber() {
        return this.cvrNumber;
    }


    public static final String DB_FIELD_REGISTER_ERROR = "registerError";
    public static final String IO_FIELD_REGISTER_ERROR = "fejlRegistreret";

    @Column(name = DB_FIELD_REGISTER_ERROR)
    @JsonProperty(value = IO_FIELD_REGISTER_ERROR)
    private boolean registerError;

    public boolean getRegisterError() {
        return this.registerError;
    }


    public static final String DB_FIELD_REG_NUMBER = "regNumber";
    public static final String IO_FIELD_REG_NUMBER = "regNummer";

    @JsonProperty(value = IO_FIELD_REG_NUMBER)
    @OneToMany(targetEntity = CompanyRegNumberRecord.class, mappedBy = CompanyRegNumberRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION, cascade = CascadeType.ALL)
    private Set<CompanyRegNumberRecord> regNumber;

    public void setRegNumber(Set<CompanyRegNumberRecord> regNumber) {
        this.regNumber = regNumber;
        for (CompanyRegNumberRecord regNumberRecord : regNumber) {
            regNumberRecord.setParticipantRelationCompanyRecord(this);
        }
    }

    public void addRegNumber(CompanyRegNumberRecord record) {
        if (!this.regNumber.contains(record)) {
            record.setParticipantRelationCompanyRecord(this);
            this.regNumber.add(record);
        }
    }

    public BitemporalSet<CompanyRegNumberRecord> getRegNumber() {
        return new BitemporalSet<>(this.regNumber);
    }


    public static final String DB_FIELD_NAMES = "names";
    public static final String IO_FIELD_NAMES = "navne";

    @JsonProperty(value = IO_FIELD_NAMES)
    @OneToMany(targetEntity = BaseNameRecord.class, mappedBy = BaseNameRecord.DB_FIELD_COMPANY_RELATION, cascade = CascadeType.ALL)
    private Set<BaseNameRecord> names = new HashSet<>();

    public void setNames(Set<BaseNameRecord> names) {
        this.names = names;
        for (BaseNameRecord record : names) {
            record.setRelationCompanyRecord(this);
        }
    }

    public void addName(BaseNameRecord record) {
        if (!this.names.contains(record)) {
            record.setRelationCompanyRecord(this);
            this.names.add(record);
        }
    }

    public BitemporalSet<BaseNameRecord> getNames() {
        return new BitemporalSet<>(this.names);
    }


    public static final String DB_FIELD_LIFECYCLE = "lifecycle";
    public static final String IO_FIELD_LIFECYCLE = "livsforloeb";

    @JsonProperty(value = IO_FIELD_LIFECYCLE)
    @OneToMany(targetEntity = LifecycleRecord.class, mappedBy = LifecycleRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION, cascade = CascadeType.ALL)
    private Set<LifecycleRecord> lifecycle = new HashSet<>();

    public void setLifecycle(Set<LifecycleRecord> lifecycle) {
        this.lifecycle = lifecycle;
        for (LifecycleRecord lifecycleRecord : lifecycle) {
            lifecycleRecord.setRelationCompanyRecord(this);
        }
    }

    public void addLifecycle(LifecycleRecord record) {
        if (!this.lifecycle.contains(record)) {
            record.setRelationCompanyRecord(this);
            this.lifecycle.add(record);
        }
    }

    public BitemporalSet<LifecycleRecord> getLifecycle() {
        return new BitemporalSet<>(this.lifecycle);
    }


    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";

    @JsonProperty(value = IO_FIELD_STATUS)
    @OneToMany(targetEntity = StatusRecord.class, mappedBy = CompanyStatusRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION, cascade = CascadeType.ALL)
    private Set<StatusRecord> status = new HashSet<>();

    public void setStatus(Set<StatusRecord> status) {
        this.status = status;
        for (StatusRecord statusRecord : status) {
            statusRecord.setRelationCompanyRecord(this);
        }
    }

    public void addStatus(StatusRecord record) {
        if (!this.status.contains(record)) {
            record.setRelationCompanyRecord(this);
            this.status.add(record);
        }
    }

    public BitemporalSet<StatusRecord> getStatus() {
        return new BitemporalSet<>(this.status);
    }


    public static final String DB_FIELD_COMPANYSTATUS = "companyStatus";
    public static final String IO_FIELD_COMPANYSTATUS = "virksomhedsstatus";

    @JsonProperty(value = IO_FIELD_COMPANYSTATUS)
    @OneToMany(targetEntity = CompanyStatusRecord.class, mappedBy = CompanyStatusRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION, cascade = CascadeType.ALL)
    private Set<CompanyStatusRecord> companyStatus = new HashSet<>();


    public void setCompanyStatus(Set<CompanyStatusRecord> companyStatus) {
        this.companyStatus = companyStatus;
        for (CompanyStatusRecord statusRecord : companyStatus) {
            statusRecord.setRelationCompanyRecord(this);
        }
    }

    public void addCompanyStatus(CompanyStatusRecord record) {
        if (!this.companyStatus.contains(record)) {
            record.setRelationCompanyRecord(this);
            this.companyStatus.add(record);
        }
    }

    public BitemporalSet<CompanyStatusRecord> getCompanyStatus() {
        return new BitemporalSet<>(this.companyStatus);
    }


    public static final String DB_FIELD_FORM = "form";
    public static final String IO_FIELD_FORM = "virksomhedsform";

    @JsonProperty(value = IO_FIELD_FORM)
    @OneToMany(targetEntity = FormRecord.class, mappedBy = FormRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION, cascade = CascadeType.ALL)
    private Set<FormRecord> form = new HashSet<>();

    public void setForm(Set<FormRecord> form) {
        this.form = form;
        for (FormRecord formRecord : form) {
            formRecord.setRelationCompanyRecord(this);
        }
    }

    public void addForm(FormRecord record) {
        if (!this.form.contains(record)) {
            record.setRelationCompanyRecord(this);
            this.form.add(record);
        }
    }

    public BitemporalSet<FormRecord> getForm() {
        return new BitemporalSet<>(this.form);
    }


    @OneToOne(targetEntity = CompanyParticipantRelationRecord.class, mappedBy = CompanyParticipantRelationRecord.DB_FIELD_COMPANY_RELATION)
    @JsonIgnore
    private CompanyParticipantRelationRecord companyParticipantRelationRecord;

    public void setCompanyParticipantRelationRecord(CompanyParticipantRelationRecord companyParticipantRelationRecord) {
        this.companyParticipantRelationRecord = companyParticipantRelationRecord;
    }


    public UUID generateUUID() {
        String uuidInput = "participant:" + this.unitType + "/" + this.unitNumber;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }

    @Override
    public OffsetDateTime getRegistrationFrom() {
        OffsetDateTime registrationFrom = super.getRegistrationFrom();
        if (registrationFrom == null) {
            registrationFrom = this.getLastUpdated();
        }
        return registrationFrom;
    }

    public void merge(RelationCompanyRecord other) {
        if (other != null) {
            for (CompanyRegNumberRecord regNumber : other.getRegNumber()) {
                this.addRegNumber(regNumber);
            }
            for (BaseNameRecord name : other.getNames()) {
                this.addName(name);
            }
            for (LifecycleRecord lifecycle : other.getLifecycle()) {
                this.addLifecycle(lifecycle);
            }
            for (StatusRecord status : other.getStatus()) {
                this.addStatus(status);
            }
            for (CompanyStatusRecord status : other.getCompanyStatus()) {
                this.addCompanyStatus(status);
            }
            for (FormRecord form : other.getForm()) {
                this.addForm(form);
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RelationCompanyRecord that = (RelationCompanyRecord) o;
        return unitNumber == that.unitNumber &&
                cvrNumber == that.cvrNumber &&
                Objects.equals(unitType, that.unitType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), unitNumber, unitType, cvrNumber);
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.regNumber);
        subs.addAll(this.names);
        subs.addAll(this.lifecycle);
        subs.addAll(this.status);
        subs.addAll(this.companyStatus);
        subs.addAll(this.form);
        return subs;
    }
}
