package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Identification;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Query object specifying a search, with basic filter parameters
 * Subclasses should specify further searchable parameters, annotated with @QueryField.
 * SOAP clients should pass a serialized instance of a Query class to the SOAP interface
 */
public abstract class Query<E extends Entity> extends BaseQuery {

    public Query() {
    }

    public Query(int page, int pageSize) {
        super(page, pageSize);
    }

    public Query(int page, int pageSize, OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        super(page, pageSize, registrationFrom, registrationTo);
    }

    public Query(String page, String pageSize) {
        super(page, pageSize);
    }

    public Query(String page, String pageSize, String registrationFrom, String registrationTo) {
        super(page, pageSize, registrationFrom, registrationTo);
    }

    /**
     * Subclasses should return the EntityClass that the Query class pertains to
     * @return
     */
    public abstract Class<E> getEntityClass();


    /**
     * Subclasses should return the base Data class that the Query class pertains to
     * @return
     */
    public abstract Class<? extends DataItem> getDataClass();

}
