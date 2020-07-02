package dk.magenta.datafordeler.core.database;

import java.time.OffsetDateTime;

public interface Nontemporal {

    String FILTER_LASTUPDATED_AFTER = "lastupdatedAfterFilter";
    String FILTER_LASTUPDATED_BEFORE = "lastupdatedBeforeFilter";
    String FILTERPARAM_LASTUPDATED_AFTER = "lastupdatedAfterDate";
    String FILTERPARAM_LASTUPDATED_BEFORE = "lastupdatedBeforeDate";
    String FILTERLOGIC_LASTUPDATED_AFTER = "(" + Nontemporal.DB_FIELD_UPDATED + " >= :" + Monotemporal.FILTERPARAM_LASTUPDATED_AFTER + ")";
    String FILTERLOGIC_LASTUPDATED_BEFORE = "(" + Nontemporal.DB_FIELD_UPDATED + " < :" + Monotemporal.FILTERPARAM_LASTUPDATED_BEFORE + ")";

    String DB_FIELD_UPDATED = "dafoUpdated";
    String IO_FIELD_UPDATED = "sidstOpdateret";
    OffsetDateTime getDafoUpdated();
    void setDafoUpdated(OffsetDateTime dafoUpdated);
    //boolean equalData(Object o);

    static void copy(Nontemporal from, Nontemporal to) {
        to.setDafoUpdated(from.getDafoUpdated());
    }
/*
    static boolean equalData(Nontemporal a, Nontemporal b) {
        if (a != null) {
            if (b == null) return false;
            if (!a.equalData(b)) return false;
        } else {
            if (b != null) return false;
        }
        return true;
    }

    static boolean equalData(Set<Nontemporal> a, Set<Nontemporal> b) {
        if (a.size() != b.size()) return false;
        for (Nontemporal aa : a) {
            boolean found = false;
            for (Nontemporal bb : b) {
                if (aa.equalData(bb)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
*/
}
