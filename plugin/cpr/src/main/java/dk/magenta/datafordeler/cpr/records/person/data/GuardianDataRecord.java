package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Storage for data on a Person's civil status,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + GuardianDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + GuardianDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + GuardianDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + GuardianDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + GuardianDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + GuardianDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
})
public class GuardianDataRecord extends CprBitemporalPersonRecord<GuardianDataRecord> {

    public static final String TABLE_NAME = "cpr_person_guardian_record";

    public GuardianDataRecord() {
    }

    public GuardianDataRecord(int guardianRelationType, int relationType1, int relationAuthority1, String relationPnr, LocalDate relationPnrStart, int relationType2, int relationAuthority2, String guardianName, LocalDate guardianAddressStartDate, String relationText1, String relationText2, String relationText3, String relationText4, String relationText5) {
        this.guardianRelationType = guardianRelationType;
        this.relationType1 = relationType1;
        this.relationAuthority1 = relationAuthority1;
        this.relationPnr = relationPnr;
        this.relationPnrStart = relationPnrStart;
        this.relationType2 = relationType2;
        this.relationAuthority2 = relationAuthority2;
        this.guardianName = guardianName;
        this.guardianAddressStartDate = guardianAddressStartDate;
        this.relationText1 = relationText1;
        this.relationText2 = relationText2;
        this.relationText3 = relationText3;
        this.relationText4 = relationText4;
        this.relationText5 = relationText5;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_GUARDIAN_RELATION_TYPE = "guardianRelationType";
    public static final String IO_FIELD_GUARDIAN_RELATION_TYPE = "værgeRelationsType";
    @Column(name = DB_FIELD_GUARDIAN_RELATION_TYPE)
    @JsonProperty(value = IO_FIELD_GUARDIAN_RELATION_TYPE)
    @XmlElement(name = IO_FIELD_GUARDIAN_RELATION_TYPE)
    private int guardianRelationType;

    public int getGuardianRelationType() {
        return this.guardianRelationType;
    }

    public void setGuardianRelationType(int guardianRelationType) {
        this.guardianRelationType = guardianRelationType;
    }


    public static final String DB_FIELD_RELATION_TYPE1 = "relationType1";
    public static final String IO_FIELD_RELATION_TYPE1 = "relationsType1";
    @Column(name = DB_FIELD_RELATION_TYPE1)
    @JsonProperty(value = IO_FIELD_RELATION_TYPE1)
    @XmlElement(name = IO_FIELD_RELATION_TYPE1)
    private int relationType1;

    public int getRelationType1() {
        return this.relationType1;
    }

    public void setRelationType1(int relationType1) {
        this.relationType1 = relationType1;
    }

    public static final String DB_FIELD_RELATION_AUTHORITY1 = "relationAuthority1";
    public static final String IO_FIELD_RELATION_AUTHORITY1 = "relationsMyndighed1";
    @Column(name = DB_FIELD_RELATION_AUTHORITY1)
    @JsonProperty(value = IO_FIELD_RELATION_AUTHORITY1)
    @XmlElement(name = IO_FIELD_RELATION_AUTHORITY1)
    private int relationAuthority1;

    public int getRelationAuthority1() {
        return this.relationAuthority1;
    }

    public void setRelationAuthority1(int relationAuthority1) {
        this.relationAuthority1 = relationAuthority1;
    }

    public static final String DB_FIELD_RELATION_PNR = "relationPnr";
    public static final String IO_FIELD_RELATION_PNR = "relationsPnr";
    @Column(name = DB_FIELD_RELATION_PNR)
    @JsonProperty(value = IO_FIELD_RELATION_PNR)
    @XmlElement(name = IO_FIELD_RELATION_PNR)
    private String relationPnr;


    public String getRelationPnr() {
        return this.relationPnr;
    }

    public void setRelationPnr(String relationPnr) {
        this.relationPnr = relationPnr;
    }

    public static final String DB_FIELD_RELATION_PNR_START = "relationPnrStart";
    public static final String IO_FIELD_RELATION_PNR_START = "relationsPnrStart";
    @Column(name = DB_FIELD_RELATION_PNR_START)
    @JsonProperty(value = IO_FIELD_RELATION_PNR_START)
    @XmlElement(name = IO_FIELD_RELATION_PNR_START)
    private LocalDate relationPnrStart;

    public LocalDate getRelationPnrStart() {
        return this.relationPnrStart;
    }

    public void setRelationPnrStart(LocalDate relationPnrStart) {
        this.relationPnrStart = relationPnrStart;
    }

    public static final String DB_FIELD_RELATION_TYPE2 = "relationType2";
    public static final String IO_FIELD_RELATION_TYPE2 = "relationsType2";
    @Column(name = DB_FIELD_RELATION_TYPE2)
    @JsonProperty(value = IO_FIELD_RELATION_TYPE2)
    @XmlElement(name = IO_FIELD_RELATION_TYPE2)
    private int relationType2;

    public int getRelationType2() {
        return this.relationType2;
    }

    public void setRelationType2(int relationType2) {
        this.relationType2 = relationType2;
    }

    public static final String DB_FIELD_RELATION_AUTHORITY2 = "relationAuthority2";
    public static final String IO_FIELD_RELATION_AUTHORITY2 = "relationsMyndighed2";
    @Column(name = DB_FIELD_RELATION_AUTHORITY2)
    @JsonProperty(value = IO_FIELD_RELATION_AUTHORITY2)
    @XmlElement(name = IO_FIELD_RELATION_AUTHORITY2)
    private int relationAuthority2;

    public int getRelationAuthority2() {
        return this.relationAuthority2;
    }

    public void setRelationAuthority2(int relationAuthority2) {
        this.relationAuthority2 = relationAuthority2;
    }

    public static final String DB_FIELD_GUARDIAN_NAME = "guardianName";
    public static final String IO_FIELD_GUARDIAN_NAME = "værgeNavn";
    @Column(name = DB_FIELD_GUARDIAN_NAME)
    @JsonProperty(value = IO_FIELD_GUARDIAN_NAME)
    @XmlElement(name = IO_FIELD_GUARDIAN_NAME)
    private String guardianName;

    public String getGuardianName() {
        return this.guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public static final String DB_FIELD_GUARDIAN_ADDRESS_DATE = "guardianAddressStartDate";
    public static final String IO_FIELD_GUARDIAN_ADDRESS_DATE = "værgeAdresseStartDato";
    @Column(name = DB_FIELD_GUARDIAN_ADDRESS_DATE)
    @JsonProperty(value = IO_FIELD_GUARDIAN_ADDRESS_DATE)
    @XmlElement(name = IO_FIELD_GUARDIAN_ADDRESS_DATE)
    private LocalDate guardianAddressStartDate;

    public LocalDate getGuardianAddressStartDate() {
        return this.guardianAddressStartDate;
    }

    public void setGuardianAddressStartDate(LocalDate guardianAddressStartDate) {
        this.guardianAddressStartDate = guardianAddressStartDate;
    }

    public static final String DB_FIELD_RELATION_TEXT_1 = "relationText1";
    public static final String IO_FIELD_RELATION_TEXT_1 = "relationsTekst1";
    @Column(name = DB_FIELD_RELATION_TEXT_1)
    @JsonProperty(value = IO_FIELD_RELATION_TEXT_1)
    @XmlElement(name = IO_FIELD_RELATION_TEXT_1)
    private String relationText1;

    public String getRelationText1() {
        return this.relationText1;
    }

    public void setRelationText1(String relationText1) {
        this.relationText1 = relationText1;
    }

    public static final String DB_FIELD_RELATION_TEXT_2 = "relationText2";
    public static final String IO_FIELD_RELATION_TEXT_2 = "relationsTekst2";
    @Column(name = DB_FIELD_RELATION_TEXT_2)
    @JsonProperty(value = IO_FIELD_RELATION_TEXT_2)
    @XmlElement(name = IO_FIELD_RELATION_TEXT_2)
    private String relationText2;

    public String getRelationText2() {
        return this.relationText2;
    }

    public void setRelationText2(String relationText2) {
        this.relationText2 = relationText2;
    }

    public static final String DB_FIELD_RELATION_TEXT_3 = "relationText3";
    public static final String IO_FIELD_RELATION_TEXT_3 = "relationsTekst3";
    @Column(name = DB_FIELD_RELATION_TEXT_3)
    @JsonProperty(value = IO_FIELD_RELATION_TEXT_3)
    @XmlElement(name = IO_FIELD_RELATION_TEXT_3)
    private String relationText3;

    public String getRelationText3() {
        return this.relationText3;
    }

    public void setRelationText3(String relationText3) {
        this.relationText3 = relationText3;
    }

    public static final String DB_FIELD_RELATION_TEXT_4 = "relationText4";
    public static final String IO_FIELD_RELATION_TEXT_4 = "relationsTekst4";
    @Column(name = DB_FIELD_RELATION_TEXT_4)
    @JsonProperty(value = IO_FIELD_RELATION_TEXT_4)
    @XmlElement(name = IO_FIELD_RELATION_TEXT_4)
    private String relationText4;

    public String getRelationText4() {
        return this.relationText4;
    }

    public void setRelationText4(String relationText4) {
        this.relationText4 = relationText4;
    }

    public static final String DB_FIELD_RELATION_TEXT_5 = "relationText5";
    public static final String IO_FIELD_RELATION_TEXT_5 = "relationsTekst5";
    @Column(name = DB_FIELD_RELATION_TEXT_5)
    @JsonProperty(value = IO_FIELD_RELATION_TEXT_5)
    @XmlElement(name = IO_FIELD_RELATION_TEXT_5)
    private String relationText5;

    public String getRelationText5() {
        return this.relationText5;
    }

    public void setRelationText5(String relationText5) {
        this.relationText5 = relationText5;
    }

    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        GuardianDataRecord that = (GuardianDataRecord) o;
        return Objects.equals(this.guardianRelationType, that.guardianRelationType) &&
                Objects.equals(this.relationType1, that.relationType1) &&
                Objects.equals(this.relationAuthority1, that.relationAuthority1) &&
                Objects.equals(this.relationPnr, that.relationPnr) &&
                Objects.equals(this.relationPnrStart, that.relationPnrStart) &&
                Objects.equals(this.relationType2, that.relationType2) &&
                Objects.equals(this.relationAuthority2, that.relationAuthority2) &&
                Objects.equals(this.guardianName, that.guardianName) &&
                Objects.equals(this.guardianAddressStartDate, that.guardianAddressStartDate) &&
                Objects.equals(this.relationText1, that.relationText1) &&
                Objects.equals(this.relationText2, that.relationText2) &&
                Objects.equals(this.relationText3, that.relationText3) &&
                Objects.equals(this.relationText4, that.relationText4) &&
                Objects.equals(this.relationText5, that.relationText5);
    }

    @Override
    public boolean hasData() {
        return this.guardianRelationType != 0
                || this.relationType1 != 0
                || this.relationAuthority1 != 0
                || this.relationType2 != 0
                || this.relationAuthority2 != 0
                || this.relationPnrStart != null
                || this.guardianAddressStartDate != null
                || stringNonEmpty(this.relationPnr)
                || stringNonEmpty(this.guardianName)
                || stringNonEmpty(this.relationText1)
                || stringNonEmpty(this.relationText2)
                || stringNonEmpty(this.relationText3)
                || stringNonEmpty(this.relationText4)
                || stringNonEmpty(this.relationText5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), guardianRelationType, relationType1, relationAuthority1, relationPnr, relationPnrStart, relationType2, relationAuthority2, guardianName, guardianAddressStartDate, relationText1, relationText2, relationText3, relationText4, relationText5);
    }

    @Override
    public GuardianDataRecord clone() {
        GuardianDataRecord clone = new GuardianDataRecord();
        clone.guardianRelationType = this.guardianRelationType;
        clone.relationType1 = this.relationType1;
        clone.relationAuthority1 = this.relationAuthority1;
        clone.relationPnr = this.relationPnr;
        clone.relationPnrStart = this.relationPnrStart;
        clone.relationType2 = this.relationType2;
        clone.relationAuthority2 = this.relationAuthority2;
        clone.guardianName = this.guardianName;
        clone.guardianAddressStartDate = this.guardianAddressStartDate;
        clone.relationText1 = this.relationText1;
        clone.relationText2 = this.relationText2;
        clone.relationText3 = this.relationText3;
        clone.relationText4 = this.relationText4;
        clone.relationText5 = this.relationText5;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
