package dk.magenta.datafordeler.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Debugging {

    private static Logger log = LogManager.getLogger("Debugging");

//    public static void dumpHibernateSession(Session s) {
//        try {
//            SessionImplementor sessionImpl = (SessionImplementor) s;
//            PersistenceContext persistenceContext = sessionImpl.getPersistenceContext();
//            Field entityEntriesField = StatefulPersistenceContext.class.getDeclaredField("entitiesByKey");
//            entityEntriesField.setAccessible(true);
//            HashMap<EntityKey, EntityHolder> map = (HashMap<EntityKey, EntityHolder>) entityEntriesField.get(persistenceContext);
//            ArrayList<EntityKey> keys = new ArrayList<>(map.keySet());
//            keys.sort(Comparator.comparing(EntityKey::getEntityName).thenComparing(e -> e.getIdentifier().toString()));
//            for (EntityKey key : keys) {
//                EntityHolder holder = map.get(key);
//                EntityEntry entry = holder.getEntityEntry();
//                System.out.println(key.getEntityName()+"#"+key.getIdentifier() + ": " + holder.getEntity().getClass().getSimpleName()+" (initialized: "+holder.isInitialized()+", detached: "+holder.isDetached()+", isDeletedOrGone: "+entry.getStatus().isDeletedOrGone()+")");
//            }
//        } catch (Exception e) {
//            log.error(e);
//        }
//
//    }
}
