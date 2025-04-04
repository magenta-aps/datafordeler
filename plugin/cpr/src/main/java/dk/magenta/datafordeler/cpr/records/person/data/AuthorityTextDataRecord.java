package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;


@MappedSuperclass
public abstract class AuthorityTextDataRecord<S extends AuthorityTextDataRecord<S>> extends CprBitemporalPersonRecord<S> {

    public AuthorityTextDataRecord(String text, String correctionMarking) {
        this.text = text;
        this.correctionMarking = correctionMarking;
    }

    public static final String DB_FIELD_TEXT = "text";
    public static final String IO_FIELD_TEXT = "tekst";
    @Column(name = DB_FIELD_TEXT)
    @JsonProperty(value = IO_FIELD_TEXT)
    @XmlElement(name = IO_FIELD_TEXT)
    private String text;

    protected AuthorityTextDataRecord() {
    }


    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public static final String DB_FIELD_CORRECTION_MARKING = "correctionMarking";
    public static final String IO_FIELD_CORRECTION_MARKING = "retFortrydMarkering";
    @Column(name = DB_FIELD_CORRECTION_MARKING, length = 1)
    @JsonProperty(value = IO_FIELD_CORRECTION_MARKING)
    @XmlElement(name = IO_FIELD_CORRECTION_MARKING)
    private String correctionMarking;

    public String getCorrectionMarking() {
        return this.correctionMarking;
    }

    public void setCorrectionMarking(String correctionMarking) {
        this.correctionMarking = correctionMarking;
    }

    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        AuthorityTextDataRecord that = (AuthorityTextDataRecord) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(correctionMarking, that.correctionMarking);
    }

    @Override
    public boolean hasData() {
        return stringNonEmpty(this.correctionMarking) || stringNonEmpty(this.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), text, correctionMarking);
    }

    protected static void copy(AuthorityTextDataRecord from, AuthorityTextDataRecord to) {
        CprBitemporalRecord.copy(from, to);
        to.text = from.text;
        to.correctionMarking = from.correctionMarking;
    }

}
