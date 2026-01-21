package dk.magenta.datafordeler.cvr.records;

import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class CvrNontemporalRecord extends CvrRecord implements Nontemporal {

    public static final String FILTERLOGIC_LASTUPDATED_AFTER = "(" + CvrRecord.DB_FIELD_DAFO_UPDATED + " >= :" + Monotemporal.FILTERPARAM_LASTUPDATED_AFTER + ")";
    public static final String FILTERLOGIC_LASTUPDATED_BEFORE = "(" + CvrRecord.DB_FIELD_DAFO_UPDATED + " < :" + Monotemporal.FILTERPARAM_LASTUPDATED_BEFORE + ")";

}
