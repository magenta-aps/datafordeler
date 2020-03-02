package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

import static dk.magenta.datafordeler.cpr.data.person.data.PersonNameData.DB_FIELD_FIRST_NAMES;
import static dk.magenta.datafordeler.cpr.data.person.data.PersonNameData.DB_FIELD_LAST_NAME;

/**
 * Storage for data on a Person's name,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_name", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_name_firstname", columnList = DB_FIELD_FIRST_NAMES),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_name_lastname", columnList = DB_FIELD_LAST_NAME),
})
public class PersonNameData extends AuthorityDetailData {


    public static final String DB_FIELD_ADDRESSING_NAME = "addressingName";
    public static final String IO_FIELD_ADDRESSING_NAME = "adresseringsnavn";
    @Column(name = DB_FIELD_ADDRESSING_NAME)
    @JsonProperty(value = IO_FIELD_ADDRESSING_NAME)
    @XmlElement(name = IO_FIELD_ADDRESSING_NAME)
    private String addressingName;

    public String getAddressingName() {
        return this.addressingName;
    }

    public void setAddressingName(String addressingName) {
        this.addressingName = addressingName;
    }



    public static final String DB_FIELD_FIRST_NAMES = "firstNames";
    public static final String IO_FIELD_FIRST_NAMES = "fornavn";
    @Column(name = DB_FIELD_FIRST_NAMES)
    @JsonProperty(value = IO_FIELD_FIRST_NAMES)
    @XmlElement(name = IO_FIELD_FIRST_NAMES)
    private String firstNames;

    public String getFirstNames() {
        return this.firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }



    public static final String DB_FIELD_FIRST_NAMES_MARKING = "firstNamesMarking";
    public static final String IO_FIELD_FIRST_NAMES_MARKING = "fornavneMarkering";
    @Column(name = DB_FIELD_FIRST_NAMES_MARKING)
    @JsonProperty(value = IO_FIELD_FIRST_NAMES_MARKING)
    @XmlElement(name = IO_FIELD_FIRST_NAMES_MARKING)
    private boolean firstNamesMarking;

    public boolean isFirstNamesMarking() {
        return this.firstNamesMarking;
    }

    public void setFirstNamesMarking(boolean firstNamesMarking) {
        this.firstNamesMarking = firstNamesMarking;
    }



    public static final String DB_FIELD_MIDDLE_NAME = "middleName";
    public static final String IO_FIELD_MIDDLE_NAME = "mellemnavn";
    @Column(name = DB_FIELD_MIDDLE_NAME)
    @JsonProperty(value = IO_FIELD_MIDDLE_NAME)
    @XmlElement(name = IO_FIELD_MIDDLE_NAME)
    private String middleName;

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }



    public static final String DB_FIELD_MIDDLE_NAME_MARKING = "middleNameMarking";
    public static final String IO_FIELD_MIDDLE_NAME_MARKING = "mellemnavnMarkering";
    @Column(name = DB_FIELD_MIDDLE_NAME_MARKING)
    @JsonProperty(value = IO_FIELD_MIDDLE_NAME_MARKING)
    @XmlElement(name = IO_FIELD_MIDDLE_NAME_MARKING)
    private boolean middleNameMarking;

    public boolean isMiddleNameMarking() {
        return this.middleNameMarking;
    }

    public void setMiddleNameMarking(boolean middleNameMarking) {
        this.middleNameMarking = middleNameMarking;
    }



    public static final String DB_FIELD_LAST_NAME = "lastName";
    public static final String IO_FIELD_LAST_NAME = "efternavn";
    @Column(name = DB_FIELD_LAST_NAME)
    @JsonProperty(value = IO_FIELD_LAST_NAME)
    @XmlElement(name = IO_FIELD_LAST_NAME)
    private String lastName;

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



    public static final String DB_FIELD_LAST_NAME_MARKING = "lastNameMarking";
    public static final String IO_FIELD_LAST_NAME_MARKING = "efternavnMarkering";
    @Column(name = DB_FIELD_LAST_NAME_MARKING)
    @JsonProperty(value = IO_FIELD_LAST_NAME_MARKING)
    @XmlElement(name = IO_FIELD_LAST_NAME_MARKING)
    private boolean lastNameMarking;

    public boolean isLastNameMarking() {
        return this.lastNameMarking;
    }

    public void setLastNameMarking(boolean lastNameMarking) {
        this.lastNameMarking = lastNameMarking;
    }


    //Ikke i grunddatamodellen

    @Transient
    private String egetEfternavn;

    public String getEgetEfternavn() {
        return this.egetEfternavn;
    }

    public void setEgetEfternavn(String egetEfternavn) {
        this.egetEfternavn = egetEfternavn;
    }


    @Transient
    private boolean egetEfternavnMarkering;

    public boolean isEgetEfternavnMarkering() {
        return this.egetEfternavnMarkering;
    }

    public void setEgetEfternavnMarkering(boolean egetEfternavnMarkering) {
        this.egetEfternavnMarkering = egetEfternavnMarkering;
    }


    @Column
    @JsonProperty
    @XmlElement
    private boolean rapportnavne;

    public boolean isRapportnavne() {
        return this.rapportnavne;
    }

    public void setRapportnavne(boolean rapportnavne) {
        this.rapportnavne = rapportnavne;
    }


    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_ADDRESSING_NAME, this.addressingName);
        map.put(DB_FIELD_FIRST_NAMES, this.firstNames);
        map.put(DB_FIELD_FIRST_NAMES_MARKING, this.firstNamesMarking);
        map.put(DB_FIELD_MIDDLE_NAME, this.middleName);
        map.put(DB_FIELD_MIDDLE_NAME_MARKING, this.middleNameMarking);
        map.put(DB_FIELD_LAST_NAME, this.lastName);
        map.put(DB_FIELD_LAST_NAME_MARKING, this.lastNameMarking);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        //Navn
        map.put("addressingName", this.addressingName);
        map.put("lastName", this.lastName);
        map.put("firstNames", this.firstNames);
        map.put("middleName", this.middleName);

        //NavneMarkering
        map.put("lastNameMarking", this.lastNameMarking);
        map.put("firstNamesMarking", this.firstNamesMarking);
        map.put("middleNameMarking", this.middleNameMarking);

        //Ikke i grunddatamodellen
        map.put("rapportnavne", this.rapportnavne);
        //map.put("egetEfternavnMarkering", this.egetEfternavnMarkering);
        //map.put("egetEfternavn", this.egetEfternavn);

        //OBS: Virkning fra og til mangler i forhold til grunddatamodellen
        return map;
    }

    @Override
    protected PersonNameData clone() {
        PersonNameData clone = new PersonNameData();
        clone.addressingName = this.addressingName;
        clone.firstNames = this.firstNames;
        clone.firstNamesMarking = this.firstNamesMarking;
        clone.middleName = this.middleName;
        clone.middleNameMarking = this.middleNameMarking;
        clone.lastName = this.lastName;
        clone.lastNameMarking = this.lastNameMarking;
        clone.egetEfternavn = this.egetEfternavn;
        clone.egetEfternavnMarkering = this.egetEfternavnMarkering;
        clone.rapportnavne = this.rapportnavne;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
