package dk.magenta.datafordeler.cpr.records.road.data;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class RoadSegmentDataRecord<S extends RoadSegmentDataRecord<S>> extends CprBitemporalRoadRecord<S> {

    public abstract boolean getEven();

    public abstract String getFromHousenumber();

    public abstract String getToHousenumber();

    public boolean matches(String houseNumber) {
        if (houseNumber == null || houseNumber.isEmpty()) {
            return true;
        }
        int subjectNumberPart = getNumberPart(houseNumber);
        if ((subjectNumberPart % 2 == 0) != this.getEven()) {
            return false;
        }
        String ourFromHouseNumber = this.getFromHousenumber();
        if (ourFromHouseNumber == null || ourFromHouseNumber.isEmpty()) {
            ourFromHouseNumber = "0";
        }
        String ourToHouseNumber = this.getToHousenumber();
        if (ourToHouseNumber == null || ourToHouseNumber.isEmpty()) {
            ourToHouseNumber = "999Z";
        }
        int fromNumberPart = getNumberPart(ourFromHouseNumber);
        int toNumberPart = getNumberPart(ourToHouseNumber);
        if (fromNumberPart < subjectNumberPart && toNumberPart > subjectNumberPart) {
            return true;
        } else if (fromNumberPart == subjectNumberPart) {
            return getLetterPart(houseNumber).compareTo(getLetterPart(ourFromHouseNumber)) >= 0;
        } else if (toNumberPart == subjectNumberPart) {
            return getLetterPart(houseNumber).compareTo(getLetterPart(ourToHouseNumber)) <= 0;
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
