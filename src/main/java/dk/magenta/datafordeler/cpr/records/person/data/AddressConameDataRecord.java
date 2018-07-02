package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;

@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressConameDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressConameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressConameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressConameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressConameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
})
public class AddressConameDataRecord extends CprBitemporalPersonRecord {

    public static final String TABLE_NAME = "cpr_person_address_coname_record";

    public AddressConameDataRecord() {
    }

    public AddressConameDataRecord(String coname) {
        this.coname = coname;
    }

    public static final String DB_FIELD_CONAME = "coname";
    public static final String IO_FIELD_CONAME = "conavn";
    @Column(name = DB_FIELD_CONAME)
    @JsonProperty(value = IO_FIELD_CONAME)
    @XmlElement(name = IO_FIELD_CONAME)
    private String coname;

    public String getConame() {
        return this.coname;
    }

    public void setConame(String coname) {
        this.coname = coname;
    }

    @Override
    protected AddressConameDataRecord clone() {
        AddressConameDataRecord clone = new AddressConameDataRecord();
        clone.coname = this.coname;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
