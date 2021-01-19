package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.service.ParticipantRecordService;
import org.hibernate.Session;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Record for Company and CompanyUnit relationParticipantRecord relations.
 */

@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyParticipantRelationRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyParticipantRelationRecord.TABLE_NAME + "__company", columnList = CompanyParticipantRelationRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyParticipantRelationRecord.TABLE_NAME + "__unit", columnList = CompanyParticipantRelationRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyParticipantRelationRecord.TABLE_NAME + "__participant", columnList = CompanyParticipantRelationRecord.DB_FIELD_PARTICIPANT + DatabaseEntry.REF),

        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyParticipantRelationRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyParticipantRelationRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyParticipantRelationRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyParticipantRelationRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_company_participant_relation";

    public static final String DB_FIELD_PARTICIPANT_RELATION = "relationParticipantRecord";
    public static final String IO_FIELD_PARTICIPANT_RELATION = "deltager";

    public String getFieldName() {
        return TABLE_NAME;
    }

    @OneToOne(targetEntity = RelationParticipantRecord.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonProperty(value = IO_FIELD_PARTICIPANT_RELATION)
    private RelationParticipantRecord relationParticipantRecord;

    public void setRelationParticipantRecord(RelationParticipantRecord relationParticipantRecord) {
        this.relationParticipantRecord = relationParticipantRecord;
        if (relationParticipantRecord != null) {
            relationParticipantRecord.setCompanyParticipantRelationRecord(this);
        }
    }

    public RelationParticipantRecord getRelationParticipantRecord() {
        return this.relationParticipantRecord;
    }

    @JsonIgnore
    public Long getParticipantUnitNumber() {
        return this.relationParticipantRecord != null ? this.relationParticipantRecord.getUnitNumber() : null;
    }




    public static final String DB_FIELD_COMPANY_RELATION = "relationCompanyRecord";
    public static final String IO_FIELD_COMPANY_RELATION = "virksomhed";

    @OneToOne(targetEntity = RelationCompanyRecord.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonProperty(value = IO_FIELD_COMPANY_RELATION)
    private RelationCompanyRecord relationCompanyRecord;

    public void setRelationCompanyRecord(RelationCompanyRecord relationCompanyRecord) {
        this.relationCompanyRecord = relationCompanyRecord;
        relationCompanyRecord.setCompanyParticipantRelationRecord(this);
    }

    public RelationCompanyRecord getRelationCompanyRecord() {
        return this.relationCompanyRecord;
    }

    @JsonIgnore
    public Long getCompanyUnitNumber() {
        return this.relationCompanyRecord != null ? this.relationCompanyRecord.getUnitNumber() : null;
    }


    public static final String DB_FIELD_OFFICES = "offices";
    public static final String IO_FIELD_OFFICES = "kontorsteder";

    @OneToMany(targetEntity = OfficeRelationRecord.class, mappedBy = OfficeRelationRecord.DB_FIELD_COMPANY_RELATION, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(value = IO_FIELD_OFFICES)
    private Set<OfficeRelationRecord> offices = new HashSet<>();

    public void setOffices(Set<OfficeRelationRecord> offices) {
        this.offices = offices;
        for (OfficeRelationRecord office : offices) {
            office.setCompanyParticipantRelationRecord(this);
        }
    }

    public Set<OfficeRelationRecord> getOffices() {
        return this.offices;
    }



    public static final String DB_FIELD_ORGANIZATIONS = "organizations";
    public static final String IO_FIELD_ORGANIZATIONS = "organisationer";

    @OneToMany(mappedBy = OrganizationRecord.DB_FIELD_PARTICIPANT_RELATION, targetEntity = OrganizationRecord.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(value = IO_FIELD_ORGANIZATIONS)
    private Set<OrganizationRecord> organizations;

    public void setOrganizations(Set<OrganizationRecord> organizations) {
        this.organizations = organizations;
        for (OrganizationRecord organizationRecord : organizations) {
            organizationRecord.setCompanyParticipantRelationRecord(this);
        }
    }

    public void addOrganization(OrganizationRecord organization) {
        if (organization != null) {
            organization.setCompanyParticipantRelationRecord(this);
            this.organizations.add(organization);
        }
    }

    public Set<OrganizationRecord> getOrganizations() {
        return this.organizations;
    }



    // Our source omits temporality on this object, so we must gather it elsewhere
    public OffsetDateTime getRegistrationFrom() {
        OffsetDateTime registrationFrom = super.getRegistrationFrom();
        if (registrationFrom == null && this.relationParticipantRecord != null) {
            registrationFrom = this.relationParticipantRecord.getRegistrationFrom();
        }
        if (registrationFrom == null) {
            registrationFrom = this.getLastUpdated();
        }
        return registrationFrom;
    }

    private Identification getParticipantIdentification(Session session) {
        if (this.relationParticipantRecord != null) {
            UUID participantUUID = this.relationParticipantRecord.generateUUID();
            Identification participantIdentification = QueryManager.getOrCreateIdentification(session, participantUUID, ParticipantRecordService.getDomain());
            return participantIdentification;
        } else {
            return null;
        }
    }

    public void save(Session session) {
        super.save(session);
        if (this.relationParticipantRecord != null) {
            this.relationParticipantRecord.save(session);
        }
        /*for (OrganizationRecord organizationRecord : this.organizations) {
            organizationRecord.save(session);
        }*/
    }

    public void wire(Session session) {
        if (this.relationParticipantRecord != null) {
            this.relationParticipantRecord.wire(session);
        }
//        if (this.relationCompanyRecord != null) {
//            this.relationCompanyRecord.wire(session);
//        }
        for (OfficeRelationRecord officeRelationRecord : this.offices) {
            officeRelationRecord.wire(session);
        }
    }

    /*private Set<Identification> getOrganizationIdentifications(Session session) {
        HashSet<Identification> organizationIdentifications = new HashSet<>();
        for (OrganizationRecord organizationRecord : this.organizations) {
            UUID organizationUUID = organizationRecord.generateUUID();
            Identification organizationIdentification = QueryManager.getOrCreateIdentification(session, organizationUUID, CvrPlugin.getDomain());
            organizationIdentifications.add(organizationIdentification);
        }
        return organizationIdentifications;
    }*/
/*
    @Override
    public void populateBaseData(CompanyBaseData baseData, Session session, Bitemporality bitemporality) {
        baseData.addParticipantRelation(
                this.getParticipantIdentification(session),
                this.getOrganizationIdentifications(session)
        );
    }

    @Override
    public void populateBaseData(CompanyUnitBaseData baseData, Session session, Bitemporality bitemporality) {
        baseData.addParticipantRelation(
                this.getParticipantIdentification(session),
                this.getOrganizationIdentifications(session)
        );
    }*/


    public void merge(CompanyParticipantRelationRecord other) {

        if (other.getRelationParticipantRecord() != null) {
            if (this.getRelationParticipantRecord() == null) {
                this.setRelationParticipantRecord(other.getRelationParticipantRecord());
            } else {
                this.relationParticipantRecord.merge(other.getRelationParticipantRecord());
            }
        }

        if (other.getRelationCompanyRecord() != null) {
            if (this.getRelationCompanyRecord() == null) {
                this.setRelationCompanyRecord(other.getRelationCompanyRecord());
            } else {
                this.getRelationCompanyRecord().merge(other.getRelationCompanyRecord());
            }
        }

        HashMap<Long, OfficeRelationRecord> ourOffices = new HashMap<>();
        for (OfficeRelationRecord ourOffice : this.offices) {
            ourOffices.put(ourOffice.getOfficeUnitNumber(), ourOffice);
        }

        for (OfficeRelationRecord otherOffice : other.getOffices()) {
            Long otherUnitNumber = otherOffice.getOfficeUnitNumber();

            boolean found = false;
            if (otherUnitNumber != null) {
                OfficeRelationRecord ourOffice = ourOffices.get(otherUnitNumber);
                if (ourOffice != null) {
                    ourOffice.merge(otherOffice);
                    found = true;
                }
            }
            if (!found) {
                otherOffice.setCompanyParticipantRelationRecord(this);
                this.offices.add(otherOffice);
            }
        }

        for (OrganizationRecord otherOrganization : other.getOrganizations()) {
            long otherUnitNumber = otherOrganization.getUnitNumber();
            boolean found = false;
            for (OrganizationRecord ourOrganization : this.organizations) {
                if (ourOrganization.getUnitNumber() == otherUnitNumber) {
                    ourOrganization.merge(otherOrganization);
                    found = true;
                    break;
                }
            }
            if (!found) {
                this.addOrganization(otherOrganization);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompanyParticipantRelationRecord that = (CompanyParticipantRelationRecord) o;
        return Objects.equals(relationParticipantRecord, that.relationParticipantRecord) &&
                Objects.equals(relationCompanyRecord, that.relationCompanyRecord) &&
                Objects.equals(offices, that.offices) &&
                Objects.equals(organizations, that.organizations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), relationParticipantRecord, relationCompanyRecord, offices, organizations);
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        if (this.relationParticipantRecord != null) {
            subs.add(this.relationParticipantRecord);
        }
        if (this.relationCompanyRecord != null) {
            subs.add(this.relationCompanyRecord);
        }
        subs.addAll(this.offices);
        subs.addAll(this.organizations);
        return subs;
    }

    /*@Override
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        CompanyParticipantRelationRecord that = (CompanyParticipantRelationRecord) o;
        return Objects.equals(relationParticipantRecord, that.relationParticipantRecord) &&
                Objects.equals(relationCompanyRecord, that.relationCompanyRecord) &&
                Objects.equals(offices, that.offices) &&
                Objects.equals(organizations, that.organizations);
    }*/
}
