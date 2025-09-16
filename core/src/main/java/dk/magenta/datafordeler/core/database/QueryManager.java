package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.util.DoubleHashMap;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.NoResultException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection of static methods for accessing the database
 */
public class QueryManager {

    private static final Logger log = LogManager.getLogger(QueryManager.class.getCanonicalName());

    public static final String ENTITY = "e";


    /**
     * We keep a local cache of Identification references, the idea being to quickly determine if an Identification objects exists in the database.
     * If initializeCache has been run for the domain, this cache should always be able to say whether an identification *exists* in he DB, even if
     * we need to do a DB lookup to get it.
     * Looking in the database to see if an Identification exists is an expensive workload when it happens thousands of times per minute during an import.
     * This cache holds the object ID of all known identifications in a domain, findable by domain string and UUID.
     */
    private static final DoubleHashMap<String, UUID, Long> identifications = new DoubleHashMap<>();


    /**
     * Populate the Identification cache from the database, if the given domain is not already fetched
     *
     * @param session
     * @param domain
     */
    private static void initializeCache(Session session, String domain) {
        /*if (!identifications.containsKey(domain)) {
            log.info("Loading identifications for domain "+domain);
            Query<Identification> databaseQuery = session.createQuery("select i from Identification i where i.domain = :domain", Identification.class);
            databaseQuery.setParameter("domain", domain);
            databaseQuery.setFlushMode(FlushModeType.COMMIT);
            for (Identification identification : databaseQuery.getResultList()) {
                identifications.put(domain, identification.getUuid(), identification.getId());
            }
            log.info("Identifications loaded");
        }*/
    }

    /**
     * Obtain the object id from the cache, and find the corresponding Identification from Hibernate L1 cache
     * If the id is not found in the cache, or the Identification is not in L1 cache, return null
     *
     * @param session
     * @param uuid
     * @param domain
     * @return
     */
    private static Identification getIdentificationFromCache(Session session, UUID uuid, String domain) {
        initializeCache(session, domain);
        Long id = identifications.get(domain, uuid);
        if (id != null) {
            return session.get(Identification.class, id);
        }
        return null;
    }


    /**
     * Get one Identification object based on a UUID
     *
     * @param session Database session to work from
     * @param uuid    UUID to search for
     * @return
     */
    public static Identification getIdentification(Session session, UUID uuid) {
        //log.info("Get Identification from UUID " + uuid);
        Query<Identification> databaseQuery = session.createQuery("select i from Identification i where i.uuid = :uuid", Identification.class);
        databaseQuery.setParameter("uuid", uuid);
        databaseQuery.setCacheable(true);
        try {
            return databaseQuery.getSingleResult();
        } catch (NoResultException e) {
            //log.info("not found");
            return null;
        }
    }

    public static Identification getIdentification(Session session, UUID uuid, String domain) {
        Identification identification = getIdentificationFromCache(session, uuid, domain);
        if (identification == null/* && hasIdentification(session, uuid, domain)*/) {
            identification = getIdentification(session, uuid);
            /*if (identification != null) {
                identifications.put(domain, uuid, identification.getId());
            }*/
        }
        return identification;
    }

    /**
     * Determine whether an Identification exists.
     *
     * @param session
     * @param uuid
     * @param domain
     * @return
     */
    public static boolean hasIdentification(Session session, UUID uuid, String domain) {
        initializeCache(session, domain);
        return identifications.get(domain, uuid) != null;
    }

    /**
     * Quickly get an Identification object, or create one if not found.
     * First look in the Hibernate L1 cache (fast).
     * If that fails, see if our local cache tells us whether the object even exists in the database (also fast), and if so do a DB lookup (slow).
     * If no object is found, we know that the DB doesn't hold it for us, so create the objects and save it, then put it in the cache.
     *
     * @param session
     * @param uuid
     * @param domain
     * @return
     */
    public static Identification getOrCreateIdentification(Session session, UUID uuid, String domain) {
        Identification identification;
        identification = getIdentificationFromCache(session, uuid, domain);
        if (identification == null) {
            log.debug("Didn't find identification for " + domain + "/" + uuid + " in cache");
            //if (hasIdentification(session, uuid, domain)) {
            //    log.debug("Cache for "+domain+"/"+uuid+" had a broken DB link");
            identification = getIdentification(session, uuid);
            //}
            if (identification == null) {
                log.debug("Creating new for " + domain + "/" + uuid);
                identification = new Identification(uuid, domain);
                session.persist(identification);
                identifications.put(domain, uuid, identification.getId());
            } else {
                log.debug("Identification for " + domain + "/" + uuid + " found in database: " + identification.getId());
                identifications.put(domain, uuid, identification.getId());
            }
        } else {
            log.debug("Identification for " + domain + "/" + uuid + " found in cache: " + identification.getId());
        }
        return identification;
    }

    /**
     * On transaction rollback, we must clear a number of optimization caches to avoid invalid references.
     * Each cache that needs clearing on rollback should be added here.
     */
    public static void clearCaches() {
        identifications.clear();
        for (HashMap map : caches) {
            map.clear();
        }
    }

    private static final List<HashMap> caches = new ArrayList<>();

    public static void addCache(HashMap map) {
        caches.add(map);
    }

    /**
     * Get all Entities of a specific class
     *
     * @param session Database session to work from
     * @param eClass  Entity subclass
     * @return
     */
    public static <E extends DatabaseEntry> List<E> getAllEntities(Session session, Class<E> eClass) {
        return getAllEntities(session, eClass, true);
    }

    public static <E extends DatabaseEntry> List<E> getAllEntities(Session session, Class<E> eClass, boolean joinIdentity) {
        log.debug("Get all Entities of class " + eClass.getCanonicalName());
        Query<E> databaseQuery;
        if (joinIdentity) {
            databaseQuery = session.createQuery("select " + ENTITY + " from " + eClass.getCanonicalName() + " " + ENTITY + " join " + ENTITY + ".identification i where i.uuid is not null", eClass);
        } else {
            databaseQuery = session.createQuery("select " + ENTITY + " from " + eClass.getCanonicalName() + " " + ENTITY, eClass);
        }
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        long start = Instant.now().toEpochMilli();
        List<E> results = databaseQuery.getResultList();
        log.debug("Query time: " + (Instant.now().toEpochMilli() - start) + " ms");
        return results;
    }

    private static final boolean logQuery = false;

    public static Query<Object> getQuery(Session session, BaseQuery query) {
        query.applyFilters(session);

        String queryString = query.toHql();

        StringJoiner stringJoiner = null;
        if (logQuery) {
            stringJoiner = new StringJoiner("\n");
            stringJoiner.add(queryString);
        }

        // Build query
        Query<Object> databaseQuery = session.createQuery(queryString, Object.class);

        // Insert parameters, casting as necessary
        Map<String, Object> extraParameters = query.getConditionParameters();

        for (String key : extraParameters.keySet()) {
            Object value = extraParameters.get(key);
            if (logQuery) {
                stringJoiner.add(key + " = " + value);
            }
            if (value instanceof Collection) {
                databaseQuery.setParameterList(key, (Collection) value);
            } else {
                databaseQuery.setParameter(key, value);
            }
        }

        if (logQuery) {
            log.info(stringJoiner.toString());
        }

        // Offset & limit
        if (query.getOffset() > 0) {
            databaseQuery.setFirstResult(query.getOffset());
        }
        if (query.getCount() < Integer.MAX_VALUE) {
            databaseQuery.setMaxResults(query.getCount());
        }
        return databaseQuery;
    }

    /**
     * Get all Entities of a specific class, that match the given parameters
     * This method does not support parameters that is or'ed like supplying a list of parameters and fetching all that matches one of them
     *
     * @param session Database session to work from
     * @param query   Query object defining search parameters
     * @return
     */
    public static <E extends IdentifiedEntity> List<ResultSet<E>> getAllEntitySets(Session session, BaseQuery query, Class<E> eClass) {
        HashMap<BaseQuery, List<ResultSet>> cache = new HashMap<>();
        List<ResultSet<E>> results = getAllEntitySets(session, query, eClass, cache);
        return results;
    }

    /**
     * Get all Entities of a specific class, that match the given parameters
     * This method does not support parameters that is or'ed like supplying a list of parameters and fetching all that matches one of them
     *
     * @param session
     * @param query
     * @param eClass
     * @param cache
     * @param <E>
     * @return
     */
    private static <E extends IdentifiedEntity> List<ResultSet<E>> getAllEntitySets(Session session, BaseQuery query, Class<E> eClass, HashMap<BaseQuery, List<ResultSet>> cache) {
        if (cache.containsKey(query)) {
            return cache.get(query).stream().map(r -> (ResultSet<E>) r).collect(Collectors.toList());
        }
        LinkedHashMap<UUID, ResultSet<E>> identitySetList = new LinkedHashMap<>();
        log.debug("Get all Entities of class " + query.getEntityClassname() + " matching parameters " + query.getSearchParameters() + " [offset: " + query.getOffset() + ", limit: " + query.getCount() + "]");
        Query<Object> databaseQuery = QueryManager.getQuery(session, query);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        long start = Instant.now().toEpochMilli();

        List<Object> databaseResults = databaseQuery.list();
        for (Object r : databaseResults) {
            try {
                ResultSet<E> resultSet = new ResultSet<E>(r, query.getEntityClassnames());
                LinkedList<BaseQuery> subQueries = new LinkedList<>();
                for (IdentifiedEntity entity : resultSet.all()) {
                    subQueries.addAll(entity.getAssoc());
                }
                for (BaseQuery subQuery : subQueries) {
                    List<ResultSet<IdentifiedEntity>> subResults = getAllEntitySets(session, subQuery, (Class<IdentifiedEntity>) Class.forName(subQuery.getEntityClassname()), cache);
                    for (ResultSet<IdentifiedEntity> subResult : subResults) {
                        resultSet.addAssociatedEntities(subResult.all());
                    }
                }
                E primaryEntity = resultSet.getPrimaryEntity();
                if (primaryEntity.getIdentification() != null) {
                    identitySetList.put(primaryEntity.getIdentification().getUuid(), resultSet);
                } else {
                    log.error("No identification for " + primaryEntity.getClass().getCanonicalName() + " " + session.getIdentifier(primaryEntity));
                }
            } catch (ClassNotFoundException e) {
                log.error(e);
            }
        }
        List<ResultSet<E>> results = new ArrayList<>(identitySetList.values());
        cache.put(query, new ArrayList<>(results));
        log.debug("Query time: " + (Instant.now().toEpochMilli() - start) + " ms");
        return results;
    }

    /**
     * Get all Entities of a specific class, that match the given parameters
     * This method does not support parameters that is or'ed like supplying a list of parameters and fetching all that matches one of them
     *
     * @param session
     * @param query
     * @param eClass
     * @param <E>
     * @return
     */
    public static <E extends IdentifiedEntity> List<E> getAllEntities(Session session, BaseQuery query, Class<E> eClass) {
        return getAllEntitySets(session, query, eClass).stream().map(s -> s.getPrimaryEntity()).collect(Collectors.toList());
    }

    /**
     * Get all Entities of a specific class, that match the given parameters
     * This method DOES support parameters that is or'ed like suppliing a list of parameters and fetching all that matches one of them
     *
     * @param session Database session to work from
     * @param query   Query object defining search parameters
     * @param eClass  Entity subclass
     * @return
     */
    public static <E extends IdentifiedEntity> Stream<E> getAllEntitiesAsStream(Session session, BaseQuery query, Class<E> eClass) {
        log.debug("Get all Entities of class " + eClass.getCanonicalName() + " matching parameters " + query.getSearchParameters() + " [offset: " + query.getOffset() + ", limit: " + query.getCount() + "]");
        Query databaseQuery = QueryManager.getQuery(session, query);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        databaseQuery.setFetchSize(1000);
        List<String> classNames = query.getEntityClassnames();
        return databaseQuery.stream().map(object -> {
            try {
                return new ResultSet<E>(object, classNames).getPrimaryEntity();
            } catch (ClassNotFoundException e) {
                log.error("Failed casting for query classes " + classNames, e);
            }
            return null;
        });
    }

    /**
     * Get one Entity of a specific class, by uuid
     *
     * @param session Database session to work from
     * @param uuid    UUID to search for
     * @param eClass  Entity subclass
     * @return
     */
    public static <E extends IdentifiedEntity> E getEntity(Session session, UUID uuid, Class<E> eClass) {
        Identification identification = getIdentification(session, uuid);
        if (identification != null) {
            return getEntity(session, identification, eClass);
        }
        return null;
    }

    /**
     * Get an entity from an identification
     *
     * @param session
     * @param identification
     * @param eClass
     * @param <E>
     * @return
     */
    public static <E extends IdentifiedEntity> E getEntity(Session session, Identification identification, Class<E> eClass) {
        log.debug("Get Entity of class " + eClass.getCanonicalName() + " by identification " + identification.getUuid());
        Query<E> databaseQuery = session.createQuery("select " + ENTITY + " from " + eClass.getCanonicalName() + " " + ENTITY + " where " + ENTITY + ".identification = :identification", eClass);
        databaseQuery.setParameter("identification", identification);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        databaseQuery.setCacheable(true);
        try {
            long start = Instant.now().toEpochMilli();
            E entity = databaseQuery.getSingleResult();
            log.debug("Query time: " + (Instant.now().toEpochMilli() - start) + " ms");
            return entity;
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            List<E> entities = databaseQuery.getResultList();
            return (E) entities.get(0).getNewest(new ArrayList<>(entities));
        }
    }

    public static <T extends DatabaseEntry> List<T> getAllItems(
            Session session, Class<T> tClass
    ) {
        Query<T> databaseQuery = session
                .createQuery(
                        String.format("SELECT t FROM %s t", tClass.getCanonicalName()),
                        tClass);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        return databaseQuery.getResultList();
    }

    public static <T extends DatabaseEntry> Stream<T> getAllItemsAsStream(
            Session session, Class<T> tClass
    ) {
        Query<T> databaseQuery = session
                .createQuery(
                        String.format("SELECT t FROM %s t", tClass.getCanonicalName()),
                        tClass);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        return databaseQuery.stream();
    }

    public static <T extends DatabaseEntry> List<T> getItems(Session session, Class<T> tClass, Map<String, Object> filter) {
        StringJoiner whereJoiner = new StringJoiner(" and ");
        for (String key : filter.keySet()) {
            whereJoiner.add("t." + key + " = :" + key);
        }
        Query<T> databaseQuery = session.createQuery("select t from " + tClass.getCanonicalName() + " t where " + whereJoiner, tClass);
        for (String key : filter.keySet()) {
            databaseQuery.setParameter(key, filter.get(key));
        }
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        return databaseQuery.getResultList();
    }

    public static <T extends DatabaseEntry> T getItem(Session session, Class<T> tClass, Map<String, Object> filter) {
        List<T> items = getItems(session, tClass, filter);
        if (!items.isEmpty()) {
            return items.get(0);
        } else {
            return null;
        }
    }

    public static <T extends DatabaseEntry> long count(Session session, Class<T> tClass, Map<String, Object> filter) {
        String where = "";
        if (filter != null && !filter.isEmpty()) {
            StringJoiner whereJoiner = new StringJoiner(" and ");
            for (String key : filter.keySet()) {
                whereJoiner.add("t." + key + " = :" + key);
            }
            where = " where " + whereJoiner;
        }
        Query databaseQuery = session.createQuery("select count(t) from " + tClass.getCanonicalName() + " t " + where);
        if (filter != null && !filter.isEmpty()) {
            for (String key : filter.keySet()) {
                databaseQuery.setParameter(key, filter.get(key));
            }
        }
        return (long) databaseQuery.uniqueResult();
    }

}
