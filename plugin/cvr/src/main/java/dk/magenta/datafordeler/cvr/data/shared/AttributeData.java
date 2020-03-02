package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.DetailData;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantBaseData;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Class holding attributes for companies and company units.
 */
@Entity
@Table(name = "cvr_company_attributes", indexes = {
        @Index(name = "cvr_attribute_company", columnList = AttributeData.DB_FIELD_COMPANYBASE + DatabaseEntry.REF),
        @Index(name = "cvr_attribute_companyunit", columnList = AttributeData.DB_FIELD_COMPANYUNITBASE + DatabaseEntry.REF),
        @Index(name = "cvr_attribute_participant", columnList = AttributeData.DB_FIELD_PARTICIPANTBASE + DatabaseEntry.REF)
})
public class AttributeData extends DetailData implements Comparable<AttributeData> {

    public static final String DB_FIELD_COMPANYBASE = "companyBaseData";

    @ManyToOne(targetEntity = CompanyBaseData.class, optional = true)
    @JoinColumn(name = DB_FIELD_COMPANYBASE + DatabaseEntry.REF)
    private CompanyBaseData companyBaseData;

    public void setCompanyBaseData(CompanyBaseData companyBaseData) {
        this.companyBaseData = companyBaseData;
    }



    public static final String DB_FIELD_COMPANYUNITBASE = "companyUnitBaseData";

    @ManyToOne(targetEntity = CompanyUnitBaseData.class, optional = true)
    @JoinColumn(name = DB_FIELD_COMPANYUNITBASE + DatabaseEntry.REF)
    private CompanyUnitBaseData companyUnitBaseData;

    public void setCompanyUnitBaseData(CompanyUnitBaseData companyUnitBaseData) {
        this.companyUnitBaseData = companyUnitBaseData;
    }



    public static final String DB_FIELD_PARTICIPANTBASE = "participantBaseData";

    @ManyToOne(targetEntity = ParticipantBaseData.class, optional = true)
    @JoinColumn(name = DB_FIELD_PARTICIPANTBASE + DatabaseEntry.REF)
    private ParticipantBaseData participantBaseData;

    public void setParticipantBaseData(ParticipantBaseData participantBaseData) {
        this.participantBaseData = participantBaseData;
    }



    public static final String DB_FIELD_SEQNO = "sequenceNumber";
    public static final String IO_FIELD_SEQNO = "sekvensNummer";

    @JsonProperty(value = IO_FIELD_SEQNO)
    @XmlElement(name = IO_FIELD_SEQNO)
    @Column(name = DB_FIELD_SEQNO)
    private int sequenceNumber;

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    //------------------------------------------------

    public static final String DB_FIELD_TYPE = "type";
    public static final String IO_FIELD_TYPE = "type";

    @JsonProperty(value = IO_FIELD_TYPE)
    @XmlElement(name = IO_FIELD_TYPE)
    @Column(name = DB_FIELD_TYPE, length = 50)
    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    //------------------------------------------------

    public static final String DB_FIELD_VALUETYPE = "valueType";
    public static final String IO_FIELD_VALUETYPE = "værditype";

    @JsonProperty(value = IO_FIELD_VALUETYPE)
    @XmlElement(name = IO_FIELD_VALUETYPE)
    @Column(name = DB_FIELD_VALUETYPE)
    private String valueType;

    public String getValueType() {
        return this.valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    //------------------------------------------------

    public static final String DB_FIELD_VALUE = "value";
    public static final String IO_FIELD_VALUE = "værdi";
    public static final String IO_FIELD_VALUES = "værdier";

    @JsonProperty(value = IO_FIELD_VALUE)
    @XmlElement(name = IO_FIELD_VALUE)
    @Column(name = DB_FIELD_VALUE, length = 8000)
    private String value;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        if (value.length() > 8000) {
            value = value.substring(0, 7999);
        }
        this.value = value;
    }

    //------------------------------------------------

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_SEQNO, this.sequenceNumber);
        map.put(DB_FIELD_TYPE, this.type);
        map.put(DB_FIELD_VALUETYPE, this.valueType);
        map.put(DB_FIELD_VALUE, this.value);
        return map;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("sekvensnummer", this.sequenceNumber);
        map.put("type", this.type);
        map.put("valueType", this.valueType);
        map.put("value", this.value);
        return map;
    }

    @Override
    public int compareTo(AttributeData o) {
        String oType = o == null ? null : o.type;
        if (this.type == null && oType == null) return 0;
        if (this.type == null) return -1;
        if (oType == null) return 1;
        return this.type.compareTo(o.type);
    }
}
