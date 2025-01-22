package dk.magenta.datafordeler.cpr.records.road.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;

import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@javax.persistence.Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadMemoBitemporalRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadMemoBitemporalRecord.TABLE_NAME + RoadMemoBitemporalRecord.DB_FIELD_ENTITY, columnList = CprBitemporalRoadRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadMemoBitemporalRecord.TABLE_NAME + RoadMemoBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRoadRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF),
})
public class RoadMemoBitemporalRecord extends CprBitemporalRoadRecord<RoadMemoBitemporalRecord> {

    public static final String TABLE_NAME = "cpr_road_memo_record";

    public RoadMemoBitemporalRecord() {
    }

    public RoadMemoBitemporalRecord(int noteNumber, String noteLine) {
        this.noteNumber = noteNumber;
        this.noteLine = noteLine;
    }


    // Notatnummer
    public static final String DB_FIELD_NOTE_NUMBER = "noteNumber";
    public static final String IO_FIELD_NOTE_NUMBER = "notatNummer";
    @Column(name = DB_FIELD_NOTE_NUMBER)
    @JsonProperty(value = IO_FIELD_NOTE_NUMBER)
    @XmlElement(name = IO_FIELD_NOTE_NUMBER)
    private int noteNumber;

    public int getNoteNumber() {
        return noteNumber;
    }

    public void setNoteNumber(int noteNumber) {
        this.noteNumber = noteNumber;
    }

    // Notatlinie
    public static final String DB_FIELD_NOTE_LINE = "noteLine";
    public static final String IO_FIELD_NOTE_LINE = "notatLinie";
    @Column(name = DB_FIELD_NOTE_LINE)
    @JsonProperty(value = IO_FIELD_NOTE_LINE)
    @XmlElement(name = IO_FIELD_NOTE_LINE)
    private String noteLine;

    public String getNoteLine() {
        return noteLine;
    }

    public void setNoteLine(String noteLine) {
        this.noteLine = noteLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoadMemoBitemporalRecord)) return false;
        if (!super.equals(o)) return false;
        RoadMemoBitemporalRecord that = (RoadMemoBitemporalRecord) o;
        return Objects.equals(noteNumber, that.noteNumber) &&
                Objects.equals(noteLine, that.noteLine);
    }

    @Override
    public boolean hasData() {
        return this.noteNumber != 0 || stringNonEmpty(this.noteLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), noteNumber, noteLine);
    }

    @Override
    public RoadMemoBitemporalRecord clone() {
        RoadMemoBitemporalRecord clone = new RoadMemoBitemporalRecord();
        clone.noteNumber = this.noteNumber;
        clone.noteLine = this.noteLine;
        RoadMemoBitemporalRecord.copy(this, clone);
        return clone;
    }
}
