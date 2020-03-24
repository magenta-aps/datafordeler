package dk.magenta.datafordeler.cpr.records.road.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;

import javax.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class RoadSegmentDataRecord<S extends RoadSegmentDataRecord<S>> extends CprBitemporalRoadRecord<S> {

    public abstract boolean getEven();

    public abstract String getFromHousenumber();

    public abstract String getToHousenumber();

    public boolean matches(String houseNumber) {
        int subjectNumberPart = getNumberPart(houseNumber);
        if ((subjectNumberPart % 2 == 0) != this.getEven()) {
            return false;
        }
        int fromNumberPart = getNumberPart(this.getFromHousenumber());
        int toNumberPart = getNumberPart(this.getToHousenumber());
        if (fromNumberPart < subjectNumberPart && toNumberPart > subjectNumberPart) {
            return true;
        } else if (fromNumberPart == subjectNumberPart) {
            return getLetterPart(houseNumber).compareTo(getLetterPart(this.getFromHousenumber())) >= 0;
        } else if (toNumberPart == subjectNumberPart) {
            return getLetterPart(houseNumber).compareTo(getLetterPart(this.getToHousenumber())) <= 0;
        } else {
            return false;
        }
    }

    private static int getNumberPart(String houseNumber) {
        return Integer.parseInt(houseNumber.replaceAll("[^0-9]", ""));
    }
    private static String getLetterPart(String houseNumber) {
        return houseNumber.replaceAll("[^a-zA-Z]", "");
    }
}
