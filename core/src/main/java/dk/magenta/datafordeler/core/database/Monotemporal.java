package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.util.Monotemporality;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;


public interface Monotemporal extends Nontemporal {

    String FILTER_REGISTRATIONFROM_AFTER = "registrationFromAfterFilter";
    String FILTER_REGISTRATIONFROM_BEFORE = "registrationFromBeforeFilter";
    String FILTER_REGISTRATIONTO_AFTER = "registrationToAfterFilter";
    String FILTER_REGISTRATIONTO_BEFORE = "registrationToBeforeFilter";

    String FILTERPARAM_REGISTRATIONFROM_AFTER = "registrationFromAfterDate";
    String FILTERPARAM_REGISTRATIONFROM_BEFORE = "registrationFromBeforeDate";
    String FILTERPARAM_REGISTRATIONTO_AFTER = "registrationToAfterDate";
    String FILTERPARAM_REGISTRATIONTO_BEFORE = "registrationToBeforeDate";

    // True if the sought registration begins after our query
    String FILTERLOGIC_REGISTRATIONFROM_AFTER = "(" + Monotemporal.DB_FIELD_REGISTRATION_FROM + " >= :" + Monotemporal.FILTERPARAM_REGISTRATIONFROM_AFTER + ")";
    // True if the sought registration begins before our query
    String FILTERLOGIC_REGISTRATIONFROM_BEFORE = "(" + Monotemporal.DB_FIELD_REGISTRATION_FROM + " < :" + Monotemporal.FILTERPARAM_REGISTRATIONFROM_BEFORE + " OR " + Monotemporal.DB_FIELD_REGISTRATION_FROM + " is null)";

    // True if the sought registration ends after our query
    String FILTERLOGIC_REGISTRATIONTO_AFTER = "(" + Monotemporal.DB_FIELD_REGISTRATION_TO + " >= :" + Monotemporal.FILTERPARAM_REGISTRATIONTO_AFTER + " OR " + Monotemporal.DB_FIELD_REGISTRATION_TO + " is null)";
    // True if the sought registration ends before our query
    String FILTERLOGIC_REGISTRATIONTO_BEFORE = "(" + Monotemporal.DB_FIELD_REGISTRATION_TO + " < :" + Monotemporal.FILTERPARAM_REGISTRATIONTO_BEFORE + ")";


    String DB_FIELD_REGISTRATION_FROM = "registrationFrom";
    String IO_FIELD_REGISTRATION_FROM = "registreringFra";

    OffsetDateTime getRegistrationFrom();

    void setRegistrationFrom(OffsetDateTime registrationFrom);


    String DB_FIELD_REGISTRATION_TO = "registrationTo";
    String IO_FIELD_REGISTRATION_TO = "registreringTil";

    OffsetDateTime getRegistrationTo();

    void setRegistrationTo(OffsetDateTime registrationTo);


    static <T extends Monotemporal> T newestRecord(Collection<T> set) {
        if (set.isEmpty()) {
            return null;
        } else {
            ArrayList<T> list = new ArrayList<>(set);
            list.sort(Comparator.comparing(Monotemporal::getRegistrationFrom));
            return list.get(list.size() - 1);
        }
    }

    static <T extends Monotemporal> void updateRegistrationTo(Collection<T> set) {
        ArrayList<T> list = new ArrayList<>(set);
        list.sort(Comparator.comparing(Monotemporal::getRegistrationFrom));
        T previous = null;
        for (T record : list) {
            if (previous != null && previous.getRegistrationTo() == null) {
                previous.setRegistrationTo(record.getRegistrationFrom());
            }
            previous = record;
        }
    }


    /**
     * For sorting purposes; we implement the Comparable interface, so we should
     * provide a comparison method. Here, we sort CvrRecord objects by registrationFromBefore, with nulls first
     */
    default int compareTo(Monotemporal o) {
        OffsetDateTime oUpdated = o == null ? null : o.getRegistrationFrom();
        if (this.getRegistrationFrom() == null && oUpdated == null) return 0;
        if (this.getRegistrationFrom() == null) return -1;
        return this.getRegistrationFrom().compareTo(oUpdated);
    }

    static void copy(Monotemporal from, Monotemporal to) {
        Nontemporal.copy(from, to);
        to.setRegistrationFrom(from.getRegistrationFrom());
        to.setRegistrationTo(from.getRegistrationTo());
    }

    @JsonIgnore
    default Monotemporality getMonotemporality() {
        return new Monotemporality(this.getRegistrationFrom(), this.getRegistrationTo());
    }
}
