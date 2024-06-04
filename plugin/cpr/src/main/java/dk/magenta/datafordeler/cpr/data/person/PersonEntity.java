package dk.magenta.datafordeler.cpr.data.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.util.FixedQueueMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprRecordEntity;
import dk.magenta.datafordeler.cpr.records.BitemporalSet;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprMonotemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An Entity representing a person. Bitemporal data is structured as
 * described in {@link dk.magenta.datafordeler.core.database.Entity}
 */
@javax.persistence.Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_entity", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_identification", columnList = PersonEntity.DB_FIELD_IDENTIFICATION, unique = true),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_personnummer", columnList = PersonEntity.DB_FIELD_CPR_NUMBER, unique = true),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonEntity.TABLE_NAME + PersonEntity.DB_FIELD_DAFO_UPDATED, columnList = PersonEntity.DB_FIELD_DAFO_UPDATED)
})
@FilterDefs({
        @FilterDef(name = Bitemporal.FILTER_EFFECTFROM_AFTER, parameters = @ParamDef(name = Bitemporal.FILTERPARAM_EFFECTFROM_AFTER, type = CprBitemporalRecord.FILTERPARAMTYPE_EFFECTFROM)),
        @FilterDef(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, parameters = @ParamDef(name = Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, type = CprBitemporalRecord.FILTERPARAMTYPE_EFFECTFROM)),
        @FilterDef(name = Bitemporal.FILTER_EFFECTTO_AFTER, parameters = @ParamDef(name = Bitemporal.FILTERPARAM_EFFECTTO_AFTER, type = CprBitemporalRecord.FILTERPARAMTYPE_EFFECTTO)),
        @FilterDef(name = Bitemporal.FILTER_EFFECTTO_BEFORE, parameters = @ParamDef(name = Bitemporal.FILTERPARAM_EFFECTTO_BEFORE, type = CprBitemporalRecord.FILTERPARAMTYPE_EFFECTTO)),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONFROM_AFTER, type = CprMonotemporalRecord.FILTERPARAMTYPE_REGISTRATIONFROM)),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONFROM_BEFORE, type = CprMonotemporalRecord.FILTERPARAMTYPE_REGISTRATIONFROM)),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONTO_AFTER, type = CprMonotemporalRecord.FILTERPARAMTYPE_REGISTRATIONTO)),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONTO_BEFORE, type = CprMonotemporalRecord.FILTERPARAMTYPE_REGISTRATIONTO)),
        @FilterDef(name = Nontemporal.FILTER_LASTUPDATED_AFTER, parameters = @ParamDef(name = Nontemporal.FILTERPARAM_LASTUPDATED_AFTER, type = CprNontemporalRecord.FILTERPARAMTYPE_LASTUPDATED)),
        @FilterDef(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, parameters = @ParamDef(name = Nontemporal.FILTERPARAM_LASTUPDATED_BEFORE, type = CprNontemporalRecord.FILTERPARAMTYPE_LASTUPDATED))
})
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonEntity extends CprRecordEntity {

    public static final String TABLE_NAME = "cpr_person_entity";

    public PersonEntity() {
    }

    public PersonEntity(Identification identification) {
        super(identification);
    }

    public PersonEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }


    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Person";

    public static final String DB_FIELD_CPR_NUMBER = "personnummer";
    public static final String IO_FIELD_CPR_NUMBER = "pnr";

    @Column(name = DB_FIELD_CPR_NUMBER)
    @JsonProperty(IO_FIELD_CPR_NUMBER)
    @XmlElement(name = (IO_FIELD_CPR_NUMBER))
    private String personnummer;

    public String getPersonnummer() {
        return this.personnummer;
    }

    public void setPersonnummer(String personnummer) {
        this.personnummer = personnummer;
    }

    public static UUID generateUUID(String cprNumber) {
        String uuidInput = "person:" + cprNumber;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }


    public static final String DB_FIELD_ADDRESS_CONAME = "coname";
    public static final String IO_FIELD_ADDRESS_CONAME = "conavn";
    @JsonProperty(IO_FIELD_ADDRESS_CONAME)
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)

    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    Set<AddressConameDataRecord> coname = new HashSet<>();

    public BitemporalSet<AddressConameDataRecord> getConame() {
        return new BitemporalSet<>(this.coname);
    }

    public static final String DB_FIELD_ADDRESS = "address";
    public static final String IO_FIELD_ADDRESS = "adresse";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ADDRESS)
    Set<AddressDataRecord> address = new HashSet<>();

    public BitemporalSet<AddressDataRecord> getAddress() {
        return new BitemporalSet<>(this.address);
    }

    public static final String DB_FIELD_ADDRESS_NAME = "addressName";
    public static final String IO_FIELD_ADDRESS_NAME = "adresseringsnavn";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ADDRESS_NAME)
    Set<AddressNameDataRecord> addressName = new HashSet<>();

    public BitemporalSet<AddressNameDataRecord> getAddressName() {
        return new BitemporalSet<>(this.addressName);
    }

    public static final String DB_FIELD_BIRTHPLACE = "birthPlace";
    public static final String IO_FIELD_BIRTHPLACE = "fødselsted";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BIRTHPLACE)
    Set<BirthPlaceDataRecord> birthPlace = new HashSet<>();

    public BitemporalSet<BirthPlaceDataRecord> getBirthPlace() {
        return new BitemporalSet<>(this.birthPlace);
    }

    public static final String DB_FIELD_BIRTHPLACE_VERIFICATION = "birthPlaceVerification";
    public static final String IO_FIELD_BIRTHPLACE_VERIFICATION = "fødselssted_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BIRTHPLACE_VERIFICATION)
    Set<BirthPlaceVerificationDataRecord> birthPlaceVerification = new HashSet<>();

    public BitemporalSet<BirthPlaceVerificationDataRecord> getBirthPlaceVerification() {
        return new BitemporalSet<>(this.birthPlaceVerification);
    }

    public static final String DB_FIELD_BIRTHTIME = "birthTime";
    public static final String IO_FIELD_BIRTHTIME = "fødselstidspunkt";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BIRTHTIME)
    Set<BirthTimeDataRecord> birthTime = new HashSet<>();

    public BitemporalSet<BirthTimeDataRecord> getBirthTime() {
        return new BitemporalSet<>(this.birthTime);
    }

    public static final String DB_FIELD_CHURCH = "churchRelation";
    public static final String IO_FIELD_CHURCH = "folkekirkeoplysning";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CHURCH)
    Set<ChurchDataRecord> churchRelation = new HashSet<>();

    public BitemporalSet<ChurchDataRecord> getChurchRelation() {
        return new BitemporalSet<>(this.churchRelation);
    }

    public static final String DB_FIELD_CHURCH_VERIFICATION = "churchRelationVerification";
    public static final String IO_FIELD_CHURCH_VERIFICATION = "folkekirkeoplysning_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CHURCH_VERIFICATION)
    Set<ChurchVerificationDataRecord> churchRelationVerification = new HashSet<>();

    public BitemporalSet<ChurchVerificationDataRecord> getChurchRelationVerification() {
        return new BitemporalSet<>(this.churchRelationVerification);
    }

    public static final String DB_FIELD_CHILDREN = "children";
    public static final String IO_FIELD_CHILDREN = "born";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CHILDREN)
    Set<ChildrenDataRecord> children = new HashSet<>();

    public BitemporalSet<ChildrenDataRecord> getChildren() {
        return new BitemporalSet<>(this.children);
    }

    public static final String DB_FIELD_CUSTODY = "custody";
    public static final String IO_FIELD_CUSTODY = "varge";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CUSTODY)
    Set<CustodyDataRecord> custody = new HashSet<>();

    public BitemporalSet<CustodyDataRecord> getCustody() {
        return new BitemporalSet<>(this.custody);
    }


    public static final String DB_FIELD_CITIZENSHIP = "citizenship";
    public static final String IO_FIELD_CITIZENSHIP = "statsborgerskab";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CITIZENSHIP)
    Set<CitizenshipDataRecord> citizenship = new HashSet<>();

    public BitemporalSet<CitizenshipDataRecord> getCitizenship() {
        return new BitemporalSet<>(this.citizenship);
    }

    public static final String DB_FIELD_CITIZENSHIP_VERIFICATION = "citizenshipVerification";
    public static final String IO_FIELD_CITIZENSHIP_VERIFICATION = "statsborgerskab_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CITIZENSHIP_VERIFICATION)
    Set<CitizenshipVerificationDataRecord> citizenshipVerification = new HashSet<>();

    public BitemporalSet<CitizenshipVerificationDataRecord> getCitizenshipVerification() {
        return new BitemporalSet<>(this.citizenshipVerification);
    }

    public static final String DB_FIELD_CIVILSTATUS = "civilstatus";
    public static final String IO_FIELD_CIVILSTATUS = "civilstatus";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CIVILSTATUS)
    Set<CivilStatusDataRecord> civilstatus = new HashSet<>();

    public BitemporalSet<CivilStatusDataRecord> getCivilstatus() {
        return new BitemporalSet<>(this.civilstatus);
    }

    public static final String DB_FIELD_CIVILSTATUS_AUTHORITYTEXT = "civilstatusAuthorityText";
    public static final String IO_FIELD_CIVILSTATUS_AUTHORITYTEXT = "civilstatus_autoritetstekst";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CIVILSTATUS_AUTHORITYTEXT)
    Set<CivilStatusAuthorityTextDataRecord> civilstatusAuthorityText = new HashSet<>();

    public BitemporalSet<CivilStatusAuthorityTextDataRecord> getCivilstatusAuthorityText() {
        return new BitemporalSet<>(this.civilstatusAuthorityText);
    }

    public static final String DB_FIELD_CIVILSTATUS_VERIFICATION = "civilstatusVerification";
    public static final String IO_FIELD_CIVILSTATUS_VERIFICATION = "civilstatus_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CIVILSTATUS_VERIFICATION)
    Set<CivilStatusVerificationDataRecord> civilstatusVerification = new HashSet<>();

    public BitemporalSet<CivilStatusVerificationDataRecord> getCivilstatusVerification() {
        return new BitemporalSet<>(this.civilstatusVerification);
    }

    public static final String DB_FIELD_FOREIGN_ADDRESS = "foreignAddress";
    public static final String IO_FIELD_FOREIGN_ADDRESS = "udlandsadresse";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FOREIGN_ADDRESS)
    Set<ForeignAddressDataRecord> foreignAddress = new HashSet<>();

    public BitemporalSet<ForeignAddressDataRecord> getForeignAddress() {
        return new BitemporalSet<>(this.foreignAddress);
    }

    public static final String DB_FIELD_FOREIGN_ADDRESS_EMIGRATION = "emigration";
    public static final String IO_FIELD_FOREIGN_ADDRESS_EMIGRATION = "udrejse";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FOREIGN_ADDRESS_EMIGRATION)
    Set<ForeignAddressEmigrationDataRecord> emigration = new HashSet<>();

    public BitemporalSet<ForeignAddressEmigrationDataRecord> getEmigration() {
        return new BitemporalSet<>(this.emigration);
    }

    public static final String DB_FIELD_MOVE_MUNICIPALITY = "municipalityMove";
    public static final String IO_FIELD_MOVE_MUNICIPALITY = "kommuneflytning";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MOVE_MUNICIPALITY)
    Set<MoveMunicipalityDataRecord> municipalityMove = new HashSet<>();

    public BitemporalSet<MoveMunicipalityDataRecord> getMunicipalityMove() {
        return new BitemporalSet<>(this.municipalityMove);
    }

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME)
    Set<NameDataRecord> name = new HashSet<>();

    public BitemporalSet<NameDataRecord> getName() {
        return new BitemporalSet<>(this.name);
    }

    public static final String DB_FIELD_NAME_AUTHORITY_TEXT = "nameAuthorityText";
    public static final String IO_FIELD_NAME_AUTHORITY_TEXT = "navn_autoritetstekst";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME_AUTHORITY_TEXT)
    Set<NameAuthorityTextDataRecord> nameAuthorityText = new HashSet<>();

    public BitemporalSet<NameAuthorityTextDataRecord> getNameAuthorityText() {
        return new BitemporalSet<>(this.nameAuthorityText);
    }

    public static final String DB_FIELD_NAME_VERIFICATION = "nameVerification";
    public static final String IO_FIELD_NAME_VERIFICATION = "navn_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
        @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
        @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
        @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME_VERIFICATION)
    Set<NameVerificationDataRecord> nameVerification = new HashSet<>();

    public BitemporalSet<NameVerificationDataRecord> getNameVerification() {
        return new BitemporalSet<>(this.nameVerification);
    }

    public static final String DB_FIELD_MOTHER = "mother";
    public static final String IO_FIELD_MOTHER = "mor";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
        @Where(clause = ParentDataRecord.DB_FIELD_IS_MOTHER + "=true")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MOTHER)
    Set<ParentDataRecord> mother = new HashSet<>();

    public BitemporalSet<ParentDataRecord> getMother() {
        return new BitemporalSet<>(this.mother);
    }

    public static final String DB_FIELD_MOTHER_VERIFICATION = "motherVerification";
    public static final String IO_FIELD_MOTHER_VERIFICATION = "mor_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Where(clause = ParentDataRecord.DB_FIELD_IS_MOTHER + "=true")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MOTHER_VERIFICATION)
    Set<ParentVerificationDataRecord> motherVerification = new HashSet<>();

    public BitemporalSet<ParentVerificationDataRecord> getMotherVerification() {
        return new BitemporalSet<>(this.motherVerification);
    }

    public static final String DB_FIELD_FATHER = "father";
    public static final String IO_FIELD_FATHER = "far";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Where(clause = ParentDataRecord.DB_FIELD_IS_MOTHER + "=false")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FATHER)
    Set<ParentDataRecord> father = new HashSet<>();

    public BitemporalSet<ParentDataRecord> getFather() {
        return new BitemporalSet<>(this.father);
    }

    public static final String DB_FIELD_FATHER_VERIFICATION = "fatherVerification";
    public static final String IO_FIELD_FATHER_VERIFICATION = "far_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Where(clause = ParentDataRecord.DB_FIELD_IS_MOTHER + "=false")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FATHER_VERIFICATION)
    Set<ParentVerificationDataRecord> fatherVerification = new HashSet<>();

    public BitemporalSet<ParentVerificationDataRecord> getFatherVerification() {
        return new BitemporalSet<>(this.fatherVerification);
    }

    public static final String DB_FIELD_CORE = "person";
    public static final String IO_FIELD_CORE = "køn";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CORE)
    Set<PersonCoreDataRecord> core = new HashSet<>();

    public BitemporalSet<PersonCoreDataRecord> getCore() {
        return new BitemporalSet<>(this.core);
    }

    public static final String DB_FIELD_PNR = "personNumber";
    public static final String IO_FIELD_PNR = "historiskPersonnummer";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_PNR)
    Set<PersonNumberDataRecord> personNumber = new HashSet<>();

    public BitemporalSet<PersonNumberDataRecord> getPersonNumber() {
        return new BitemporalSet<>(this.personNumber);
    }

    public static final String DB_FIELD_POSITION = "position";
    public static final String IO_FIELD_POSITION = "stilling";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_POSITION)
    Set<PersonPositionDataRecord> position = new HashSet<>();

    public BitemporalSet<PersonPositionDataRecord> getPosition() {
        return new BitemporalSet<>(this.position);
    }

    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_STATUS)
    Set<PersonStatusDataRecord> status = new HashSet<>();

    public BitemporalSet<PersonStatusDataRecord> getStatus() {
        return new BitemporalSet<>(this.status);
    }

    public static final String DB_FIELD_PROTECTION = "protection";
    public static final String IO_FIELD_PROTECTION = "beskyttelse";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_PROTECTION)
    Set<ProtectionDataRecord> protection = new HashSet<>();

    public BitemporalSet<ProtectionDataRecord> getProtection() {
        return new BitemporalSet<>(this.protection);
    }


    public static final String DB_FIELD_GUARDIAN = "guardian";
    public static final String IO_FIELD_GUARDIAN = "værgemål";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_GUARDIAN)
    Set<GuardianDataRecord> guardian = new HashSet<>();

    public BitemporalSet<GuardianDataRecord> getGuardian() {
        return new BitemporalSet<>(this.guardian);
    }

    public static final String DB_FIELD_EVENT = "event";
    public static final String IO_FIELD_EVENT = "event";
    @OneToMany(mappedBy = PersonEventDataRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @JsonProperty(IO_FIELD_EVENT)
    Set<PersonEventDataRecord> event = new HashSet<>();

    public Set<PersonEventDataRecord> getEvent() {
        return this.event;
    }

    public void addEvent(PersonEventDataRecord record, Session session) {
        this.event.add(record);
        record.setEntity(this);
    }

    public static final String DB_FIELD_DATAEVENT = "dataevent";
    public static final String IO_FIELD_DATAEVENT = "dataevent";
    @OneToMany(mappedBy = PersonDataEventDataRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @JsonProperty(IO_FIELD_DATAEVENT)
    Set<PersonDataEventDataRecord> dataevent = new HashSet<>();

    public Set<PersonDataEventDataRecord> getDataEvent() {
        return this.dataevent;
    }

    /**
     * @param fieldName
     * @return
     */
    public PersonDataEventDataRecord getDataEvent(String fieldName) {
        return this.dataevent.stream().filter(event -> event.getField().equals(fieldName)).max(Comparator.comparingLong(s -> s.getTimestamp().toEpochSecond())).orElse(null);

    }

    public void addDataEvent(PersonDataEventDataRecord record) {
        this.dataevent.add(record);
        record.setEntity(this);
    }


    public void addBitemporalRecord(CprBitemporalPersonRecord record, Session session) {
        this.addBitemporalRecord(record, session, true);
    }

    public void addBitemporalRecord(CprBitemporalPersonRecord record, Session session, boolean compareExisting) {
        boolean added = false;
        if (record instanceof AddressConameDataRecord) {
            added = addItem(this, this.coname, record, session, compareExisting);
        }
        if (record instanceof AddressDataRecord) {
            added = addItem(this, this.address, record, session, compareExisting);
        }
        if (record instanceof AddressNameDataRecord) {
            added = addItem(this, this.addressName, record, session, compareExisting);
        }
        if (record instanceof BirthPlaceDataRecord) {
            added = addItem(this, this.birthPlace, record, session, compareExisting);
        }
        if (record instanceof BirthPlaceVerificationDataRecord) {
            added = addItem(this, this.birthPlaceVerification, record, session, compareExisting);
        }
        if (record instanceof BirthTimeDataRecord) {
            added = addItem(this, this.birthTime, record, session, compareExisting);
        }
        if (record instanceof ChurchDataRecord) {
            added = addItem(this, this.churchRelation, record, session, compareExisting);
        }
        if (record instanceof ChurchVerificationDataRecord) {
            added = addItem(this, this.churchRelationVerification, record, session, compareExisting);
        }
        if (record instanceof CitizenshipDataRecord) {
            added = addItem(this, this.citizenship, record, session, compareExisting);
        }
        if (record instanceof ChildrenDataRecord) {
            added = addItem(this, this.children, record, session, compareExisting);
        }
        if (record instanceof CustodyDataRecord) {
            added = addItem(this, this.custody, record, session, compareExisting);
        }
        if (record instanceof CitizenshipVerificationDataRecord) {
            added = addItem(this, this.citizenshipVerification, record, session, compareExisting);
        }
        if (record instanceof CivilStatusDataRecord) {
            added = addItem(this, this.civilstatus, record, session, compareExisting);
        }
        if (record instanceof CivilStatusAuthorityTextDataRecord) {
            added = addItem(this, this.civilstatusAuthorityText, record, session, compareExisting);
        }
        if (record instanceof CivilStatusVerificationDataRecord) {
            added = addItem(this, this.civilstatusVerification, record, session, compareExisting);
        }
        if (record instanceof ForeignAddressDataRecord) {
            added = addItem(this, this.foreignAddress, record, session, compareExisting);
        }
        if (record instanceof ForeignAddressEmigrationDataRecord) {
            added = addItem(this, this.emigration, record, session, compareExisting);
        }
        if (record instanceof MoveMunicipalityDataRecord) {
            added = addItem(this, this.municipalityMove, record, session, compareExisting);
        }
        if (record instanceof NameAuthorityTextDataRecord) {
            added = addItem(this, this.nameAuthorityText, record, session, compareExisting);
        }
        if (record instanceof NameDataRecord) {
            added = addItem(this, this.name, record, session, compareExisting);
        }
        if (record instanceof NameVerificationDataRecord) {
            added = addItem(this, this.nameVerification, record, session, compareExisting);
        }
        if (record instanceof ParentDataRecord) {
            ParentDataRecord pRecord = (ParentDataRecord) record;
            if (pRecord.isMother()) {
                added = addItem(this, this.mother, pRecord, session, compareExisting);
            } else {
                added = addItem(this, this.father, pRecord, session, compareExisting);
            }
        }
        if (record instanceof ParentVerificationDataRecord) {
            ParentVerificationDataRecord pRecord = (ParentVerificationDataRecord) record;
            if (pRecord.isMother()) {
                added = addItem(this, this.motherVerification, pRecord, session, compareExisting);
            } else {
                added = addItem(this, this.fatherVerification, pRecord, session, compareExisting);
            }
        }
        if (record instanceof PersonCoreDataRecord) {
            added = addItem(this, this.core, record, session, compareExisting);
        }
        if (record instanceof PersonNumberDataRecord) {
            added = addItem(this, this.personNumber, record, session, compareExisting);
        }
        if (record instanceof PersonPositionDataRecord) {
            added = addItem(this, this.position, record, session, compareExisting);
        }
        if (record instanceof PersonStatusDataRecord) {
            added = addItem(this, this.status, record, session, compareExisting);
        }
        if (record instanceof ProtectionDataRecord) {
            added = addItem(this, this.protection, record, session, compareExisting);
        }
        if (record instanceof GuardianDataRecord) {
            added = addItem(this, this.guardian, record, session, compareExisting);
        }
        if (added) {
            record.setEntity(this);
            if (record.getDafoUpdated() != null && (this.getDafoUpdated() == null || record.getDafoUpdated().isAfter(this.getDafoUpdated()))) {
                this.setDafoUpdated(record.getDafoUpdated());
            }
        }

    }

    private static final FixedQueueMap<PersonEntity, ListHashMap<OffsetDateTime, CprBitemporalPersonRecord>> recentTechnicalCorrections = new FixedQueueMap<>(10);

    private static final Logger log = LogManager.getLogger(PersonEntity.class.getCanonicalName());

    private static <E extends CprBitemporalPersonRecord> boolean addItem(PersonEntity entity, Set<E> set, CprBitemporalPersonRecord newItem, Session session, boolean compareExisting) {

        /*
         * Technical
         * */
        ListHashMap<OffsetDateTime, CprBitemporalPersonRecord> recentTechnicalCorrectionRecords = recentTechnicalCorrections.get(entity);
        if (recentTechnicalCorrectionRecords == null) {
            recentTechnicalCorrectionRecords = new ListHashMap<>();
            recentTechnicalCorrections.put(entity, recentTechnicalCorrectionRecords);
        }
        if (newItem.isTechnicalCorrection()) {
            recentTechnicalCorrectionRecords.add(newItem.getEffectFrom(), newItem);
        }

        if (newItem != null) {

            if (!compareExisting) {
                //Special case for direct lookup
                return set.add((E) newItem);
            }

            if (newItem.line == null || set.stream().anyMatch(item -> StringUtils.equals(newItem.line, item.line))) {
                //If this specific line with excatcly the same information has allready been read it should be ignored
                return false;
            }

            E correctedRecord = null;
            E correctingRecord = null;
            ArrayList<E> items = new ArrayList<>(set);

            items.sort(Comparator.comparing(CprNontemporalRecord::getCnt));

            for (E oldItem : items) {
                /*
                 * Items with correction marking specify that an older record should be corrected with new data and/or effect time
                 * The incoming item itself is not the corrected data, but another record with the same origin will hold it
                 * It is not a replacement, but a correction. We keep both the corrected and corrector records, with a link between them,
                 * and set registrationTo on the corrected record
                 * */
                if (oldItem.isHistoric() && newItem.isCorrection() && newItem.hasData()) {
                    // Annkor: K
                    // Acording to responses from CPR-office in denmark corrections can not be made to records that is not historic
                    if (
                            Objects.equals(newItem.getOrigin(), oldItem.getOrigin()) &&
                                    Equality.equal(newItem.getRegistrationFrom(), oldItem.getRegistrationFrom()) &&
                                    oldItem.getCorrectionof() == null &&
                                    correctingRecord == null
                    ) {
                        // The new record with corrected data is the first record with the same origin that shares registration
                        correctingRecord = oldItem;
                    } else if (newItem.equalData(oldItem) && !Objects.equals(newItem.getOrigin(), oldItem.getOrigin())) {
                        // The old record that is being corrected has equal data with the correction marking and shares registration
                        correctedRecord = oldItem;
                    }
                }

                /*
                 * Item marking that a previous record should be undone by setting a flag on it, enabling lookups to ignore it
                 * */
                else if (oldItem.isHistoric() && newItem.isUndo() && Equality.cprDomainEqualDate(newItem.getEffectFrom(), oldItem.getEffectFrom()) && newItem.equalData(oldItem) && oldItem.getReplacedby() == null) {
                    // Annkor: A
                    // Acording to responses from CPR-office in denmark corrections can not be made to records that is not historic
                    oldItem.setUndone(true);
                    session.saveOrUpdate(oldItem);
                    if (oldItem.getClosesRecordId() != null) {
                        CprBitemporalRecord previousClosedRecord = items.stream().filter(item -> oldItem.getClosesRecordId().equals(item.getId())).findFirst().orElse(null);
                        if (previousClosedRecord != null) {
                            previousClosedRecord.setRegistrationTo(null);
                            session.saveOrUpdate(previousClosedRecord);
                        }
                    }
                    return false;
                }
                //If we get the same record again we need to figure out if we already got the information,
                // or the person is moving to the same record again
                else if (newItem.equalData(oldItem)) {
                    /*
                     * Historic item matching prior current item. This means we have the prior item ended, and should set registrationTo
                     * The new item is added without change
                     * */
                    if (
                            newItem.isHistoric() && !oldItem.isHistoric() &&
                                    //Equality.equal(newItem.getRegistrationFrom(), oldItem.getRegistrationFrom()) &&
                                    Equality.cprDomainEqualDate(newItem.getEffectFrom(), oldItem.getEffectFrom()) && oldItem.getEffectTo() == null &&
                                    !Equality.cprDomainEqualDate(newItem.getEffectFrom(), newItem.getEffectTo())
                                    && oldItem.getReplacedby() == null
                    ) {
                        // If we recieve a "historic" record which is equal to a previous "not historic" record,
                        // then the record is marked as replaced
                        oldItem.setReplacedby(newItem);
                        oldItem.setRegistrationTo(newItem.getRegistrationFrom());
                        newItem.setSameAs(oldItem);
                        session.saveOrUpdate(oldItem);
                        boolean success = set.add((E) newItem);
                        return success;
                    } else if (
                            newItem.getBitemporality().equals(oldItem.getBitemporality()) &&
                                    (newItem instanceof AddressDataRecord) &&
                                    !((AddressDataRecord) newItem).equalDataWithMunicipalityChange(oldItem, false)
                    ) {
                        // Special case for addresses: Municipality codes may have changed without us getting a change record (AnnKor: Æ)
                        oldItem.setReplacedby(newItem);
                        oldItem.setRegistrationTo(newItem.getRegistrationFrom());
                        boolean success = set.add((E) newItem);
                        return success;

                    } else if (Equality.cprDomainEqualDate(newItem.getRegistrationFrom(), oldItem.getRegistrationFrom()) &&
                            (Equality.cprDomainEqualDate(newItem.getRegistrationTo(), oldItem.getRegistrationTo()) || newItem.getRegistrationTo() == null) &&
                            Equality.cprDomainEqualDate(newItem.getEffectFrom(), oldItem.getEffectFrom())
                    ) {
                        /*
                         * We see a record that is a near-repeat of a prior record. No need to add it
                         * */
                        if (newItem.isHistoric() || oldItem.isActiveRecord()) {
                            //If the repeated record is historic it should not be added, since this record is allready added.
                            //If the repeated record is not closed by either undone or closed timestamp, it should not be added.
                            return false;
                        }
                    } else if (!newItem.isHistoric() && oldItem.isHistoric() &&
                            oldItem.getRegistrationTo() == null &&
                            oldItem.getEffectTo() != null &&
                            !oldItem.isUndone() &&
                            Equality.cprDomainBafterA(newItem.getEffectFrom(), oldItem.getEffectTo())) {
                        // If we strike a historic record, which is unclosed, and has a effectTo ending after a new active record of same type, it needs to be closed
                        oldItem.setRegistrationTo(newItem.getRegistrationFrom());
                        session.saveOrUpdate(oldItem);
                    }
                }
            }

            /*
             * Items with only registrationFrom, with no historical record, come in and replace older items of the same type
             * The new item is added, but there must be a third item to represent the older item with a limited effect
             * Consider:
             *   ItemA   reg: 2000 -> inf.  eff: 2000 -> inf
             *   ItemB   reg: 2005 -> inf.  eff: 2005 -> inf
             *
             * Result:
             *   Item1   reg: 2000 -> 2005  eff: 2000 -> inf
             *   Item2   reg: 2005 -> inf   eff: 2000 -> 2005
             *   Item3   reg: 2005 -> inf   eff: 2005 -> inf
             * */
            if (newItem.updateBitemporalityByCloning()) {
                E newestOlderItem = null;
                E oldestNewerItem = null;
                for (E oldItem : items) {
                    if ((newestOlderItem == null || oldItem.getRegistrationFrom().isAfter(newestOlderItem.getRegistrationFrom())) && oldItem.getRegistrationFrom().isBefore(newItem.getRegistrationFrom())) {
                        newestOlderItem = oldItem;
                    }
                    if ((oldestNewerItem == null || oldItem.getRegistrationFrom().isBefore(oldestNewerItem.getRegistrationFrom())) && oldItem.getRegistrationFrom().isAfter(newItem.getRegistrationFrom())) {
                        oldestNewerItem = oldItem;
                    }
                }

                if (newestOlderItem != null) {
                    // Copy newest older (A and B)
                    E clone = (E) newestOlderItem.clone();
                    clone.setRegistrationFrom(newItem.getRegistrationFrom());
                    newestOlderItem.setRegistrationTo(clone.getRegistrationFrom());
                    clone.setEffectTo(newItem.getEffectFrom());
                    clone.setEntity(newestOlderItem.getEntity());
                    newestOlderItem.setReplacedby(clone);
                    set.add(clone);
                    entity.addDataEvent(new PersonDataEventDataRecord(newItem.getRegistrationFrom(), newItem.getFieldName(), newestOlderItem.getId(), "newestOlderItem_replaced"));
                }
                if (oldestNewerItem != null) {
                    if (newItem.getRegistrationTo() == null) {
                        E clone = (E) newItem.clone();
                        clone.setRegistrationFrom(oldestNewerItem.getRegistrationFrom());
                        newItem.setRegistrationTo(clone.getRegistrationFrom());
                        clone.setEffectTo(oldestNewerItem.getEffectFrom());
                        clone.setEntity(oldestNewerItem.getEntity());
                        newItem.setReplacedby(clone);
                        set.add(clone);
                        entity.addDataEvent(new PersonDataEventDataRecord(newItem.getRegistrationFrom(), newItem.getFieldName(), clone.getId(), "oldestNewerItem_replaced"));
                    }
                }
            }


            if (newItem.getEffectFrom() != null && !newItem.isTechnicalCorrection()) {
                List<CprBitemporalPersonRecord> corrections = recentTechnicalCorrections.get(entity).get(newItem.getEffectFrom());
                if (corrections != null) {
                    for (CprBitemporalPersonRecord olderRecord : corrections) {
                        if (olderRecord.getClass() == newItem.getClass() && olderRecord.isTechnicalCorrection() && olderRecord.getOrigin().equals(newItem.getOrigin())) {
                            //if (olderRecord.getEffectFrom() != null && olderRecord.getEffectFrom().equals(newItem.getEffectFrom())) {
                            newItem.setCorrectionof(olderRecord);
                            olderRecord.setEntity(entity);
                            session.saveOrUpdate(olderRecord);
                            return set.add((E) newItem);
                            //}
                        }
                    }
                }
            }


            if (newItem.isCorrection()) {
                if (correctedRecord != null && correctingRecord != null && correctedRecord != correctingRecord) {
                    //if (correctedRecord.getCorrector() == null) {
                    correctedRecord.setRegistrationTo(newItem.getRegistrationFrom());
                    correctingRecord.setCorrectionof(correctedRecord);
                    session.saveOrUpdate(correctingRecord);
                    //}
                }
            } else {
                //log.info("nonmatching item, adding as new");
                boolean hasAnyUnclosed = items.stream().anyMatch(item -> item.getRegistrationTo() == null && item.getEffectTo() == null);

                //Does this person allready have a recordtype which is of same type, and which is unclosed, then close the old one
                if (hasAnyUnclosed && newItem.getRegistrationTo() == null && newItem.getEffectTo() == null &&
                        //Specifically for custodyRecord there can be more than one at the same time
                        //This might be correct to do for all recordtypes but is has not been tested good yet
                        !(newItem instanceof CustodyDataRecord) &&
                        !(newItem instanceof ProtectionDataRecord) &&
                        !(newItem instanceof ChildrenDataRecord)) {
                    correctedRecord = items.stream().filter(i -> i.getRegistrationTo() == null && i.getEffectTo() == null).findAny().get();
                    correctedRecord.setRegistrationTo(newItem.getRegistrationFrom());
                    if (correctedRecord.getId() != null) {
                        newItem.setClosesRecordId(correctedRecord.getId());
                    }
                    boolean success = set.add((E) newItem);
                    if (newItem.getRegistrationFrom() != null && correctedRecord.getId() != null) {
                        entity.addDataEvent(new PersonDataEventDataRecord(newItem.getRegistrationFrom(), newItem.getFieldName(), correctedRecord.getId(), "sametype_closed"));
                    }
                    return success;
                } else {
                    return set.add((E) newItem);
                }


            }
        }
        return false;
    }

    @JsonIgnore
    public Set<CprBitemporalPersonRecord> getBitemporalRecords() {
        HashSet<CprBitemporalPersonRecord> records = new HashSet<>();
        records.addAll(this.coname);
        records.addAll(this.address);
        records.addAll(this.addressName);
        records.addAll(this.birthPlace);
        records.addAll(this.birthPlaceVerification);
        records.addAll(this.birthTime);
        records.addAll(this.churchRelation);
        records.addAll(this.churchRelationVerification);
        records.addAll(this.children);
        records.addAll(this.citizenship);
        records.addAll(this.citizenshipVerification);
        records.addAll(this.civilstatus);
        records.addAll(this.civilstatusAuthorityText);
        records.addAll(this.civilstatusVerification);
        records.addAll(this.foreignAddress);
        records.addAll(this.emigration);
        records.addAll(this.municipalityMove);
        records.addAll(this.nameAuthorityText);
        records.addAll(this.nameVerification);
        records.addAll(this.mother);
        records.addAll(this.motherVerification);
        records.addAll(this.father);
        records.addAll(this.fatherVerification);
        records.addAll(this.core);
        records.addAll(this.personNumber);
        records.addAll(this.position);
        records.addAll(this.status);
        records.addAll(this.protection);
        return records;
    }

    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }

    public List<BaseQuery> getAssoc() {
        ArrayList<BaseQuery> queries = new ArrayList<>();
        queries.addAll(this.address.stream().map(a -> a.getAssoc()).flatMap(x -> x.stream()).collect(Collectors.toList()));
        return queries;
    }
}
