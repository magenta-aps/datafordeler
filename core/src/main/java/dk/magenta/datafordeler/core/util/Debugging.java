package dk.magenta.datafordeler.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.internal.StatefulPersistenceContext;
import org.hibernate.engine.spi.*;
import org.hibernate.internal.util.collections.IdentityMap;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Debugging {

    private static Logger log = LogManager.getLogger("Debugging");

    public static void dumpHibernateSession(Session s) {
        try {
            SessionImplementor sessionImpl = (SessionImplementor) s;
            PersistenceContext persistenceContext = sessionImpl.getPersistenceContext();
            Field entityEntriesField = StatefulPersistenceContext.class.getDeclaredField("entitiesByKey");
            entityEntriesField.setAccessible(true);
            HashMap<EntityKey, EntityHolder> map = (HashMap<EntityKey, EntityHolder>) entityEntriesField.get(persistenceContext);
            for (EntityKey key : map.keySet()) {
                EntityHolder holder = map.get(key);
                EntityEntry entry = holder.getEntityEntry();
                System.out.println(key.getEntityName()+"#"+key.getIdentifier() + ": " + holder.getEntity().getClass().getSimpleName()+" (initialized: "+holder.isInitialized()+", detached: "+holder.isDetached()+", isDeletedOrGone: "+entry.getStatus().isDeletedOrGone()+")");
            }
        } catch (Exception e) {
            log.error(e);
        }

    }
}
