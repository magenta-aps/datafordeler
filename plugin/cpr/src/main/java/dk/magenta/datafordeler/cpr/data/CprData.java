package dk.magenta.datafordeler.cpr.data;

import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Effect;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class CprData<V extends Effect, D extends DataItem> extends DataItem<V, D> {

    public abstract D clone();

}
