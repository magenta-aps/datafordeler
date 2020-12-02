package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import org.hibernate.Session;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface IdentifiedEntity {

    Identification getIdentification();

    void forceLoad(Session session);

    IdentifiedEntity getNewest(Collection<IdentifiedEntity> set);

    static Iterator<Map<String, Object>> itemIterator(Stream<IdentifiedEntity> entities) {return null;}

    List<BaseQuery> getAssoc();

}
