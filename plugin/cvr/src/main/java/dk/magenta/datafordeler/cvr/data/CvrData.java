package dk.magenta.datafordeler.cvr.data;

import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Effect;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

//@Entity
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class CvrData<V extends Effect, D extends DataItem> extends DataItem<V, D> {
}
