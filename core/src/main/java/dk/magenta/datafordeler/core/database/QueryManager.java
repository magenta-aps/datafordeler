package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.util.DoubleHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;

import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection of static methods for accessing the database
 */
public class QueryManager {

    private static Logger log = LogManager.getLogger(QueryManager.class.getCanonicalName());

    public static final String ENTITY = "e";


    /**
     * We keep a local cache of Identification references, the idea being to quickly determine if an Identification objects exists in the database.
     * If initializeCache has been run for the domain, this cache should always be able to say whether an identification *exists* in he DB, even if
     * we need to do a DB lookup to get it.
     * Looking in the database to see if an Identification exists is an expensive workload when it happens thousands of times per minute during an import.
     * This cache holds the object ID of all known identifications in a domain, findable by domain string and UUID.
     */
    private static DoubleHashMap<String, UUID, Long> identifications = new DoubleHashMap<>();


    /**
     * Populate the Identification cache from the database, if the given domain is not already fetched
     * @param session
     * @param domain
     */
    private static void initializeCache(Session session, String domain) {
        /*if (!identifications.containsKey(domain)) {
            log.info("Loading identifications for domain "+domain);
            org.hibernate.query.Query<Identification> databaseQuery = session.createQuery("select i from Identification i where i.domain = :domain", Identification.class);
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
     * @param session Database session to work from
     * @param uuid UUID to search for
     * @return
     */
    public static Identification getIdentification(Session session, UUID uuid) {
        //log.info("Get Identification from UUID " + uuid);
        org.hibernate.query.Query<Identification> databaseQuery = session.createQuery("select i from Identification i where i.uuid = :uuid", Identification.class);
        databaseQuery.setParameter("uuid", uuid);
        databaseQuery.setCacheable(true);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
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
     * @param session
     * @param uuid
     * @param domain
     * @return
     */
    public static Identification getOrCreateIdentification(Session session, UUID uuid, String domain) {
        Identification identification;
        identification = getIdentificationFromCache(session, uuid, domain);
        if (identification == null) {
            log.debug("Didn't find identification for "+domain+"/"+uuid+" in cache");
            //if (hasIdentification(session, uuid, domain)) {
            //    log.debug("Cache for "+domain+"/"+uuid+" had a broken DB link");
                identification = getIdentification(session, uuid);
            //}
            if (identification == null) {
                log.debug("Creating new for "+domain+"/"+uuid);
                identification = new Identification(uuid, domain);
                session.save(identification);
                identifications.put(domain, uuid, identification.getId());
            } else {
                log.debug("Identification for "+domain+"/"+uuid+" found in database: "+identification.getId());
                identifications.put(domain, uuid, identification.getId());
            }
        } else {
            log.debug("Identification for "+domain+"/"+uuid+" found in cache: "+identification.getId());
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

    private static List<HashMap> caches = new ArrayList<>();

    public static void addCache(HashMap map) {
        caches.add(map);
    }

    /**
     * Get all Entities of a specific class
     * @param session Database session to work from
     * @param eClass Entity subclass
     * @return
     */
    public static <E extends DatabaseEntry> List<E> getAllEntities(Session session, Class<E> eClass) {
        log.debug("Get all Entities of class " + eClass.getCanonicalName());
        org.hibernate.query.Query<E> databaseQuery = session.createQuery("select "+ENTITY+" from " + eClass.getCanonicalName() + " " + ENTITY + " join "+ENTITY+".identification i where i.uuid != null", eClass);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        long start = Instant.now().toEpochMilli();
        List<E> results = databaseQuery.getResultList();
        log.debug("Query time: "+(Instant.now().toEpochMilli() - start)+" ms");
        return results;
    }

    private static final boolean logQuery = true;

    public static org.hibernate.query.Query getFirstQuery(Session session, BaseQuery query) {
        String queryString = query.toFirstHql();

        StringJoiner stringJoiner = null;
        if (logQuery) {
            stringJoiner = new StringJoiner("\n");
            stringJoiner.add(queryString.toString());
        }

        // Build query
        org.hibernate.query.Query databaseQuery = session.createQuery(queryString);

        // Insert parameters, casting as necessary
        Map<String, Object> extraParameters = query.getFirstParameters();

        for (String key : extraParameters.keySet()) {
            Object value = extraParameters.get(key);
            if (logQuery) {
                stringJoiner.add(key+" = "+value);
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

        System.out.println(stringJoiner.toString());

        // Offset & limit
        if (query.getOffset() > 0) {
            databaseQuery.setFirstResult(query.getOffset());
        }
        if (query.getCount() < Integer.MAX_VALUE) {
            databaseQuery.setMaxResults(query.getCount());
        }
        return databaseQuery;
    }

    public static org.hibernate.query.Query getSecondQuery(Session session, BaseQuery query, Collection<DatabaseEntry> entries) {
        String queryString = query.toSecondHql();

        StringJoiner stringJoiner = null;
        if (logQuery) {
            stringJoiner = new StringJoiner("\n");
            stringJoiner.add(queryString.toString());
        }

        // Build query
        org.hibernate.query.Query databaseQuery = session.createQuery(queryString);

        // Insert parameters, casting as necessary
        Map<String, Object> extraParameters = query.getSecondParameters(entries);

        for (String key : extraParameters.keySet()) {
            Object value = extraParameters.get(key);
            if (logQuery) {
                stringJoiner.add(key+" = "+value);
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
        return databaseQuery;
    }


    private static <E extends IdentifiedEntity> org.hibernate.query.Query<E> getQuery(Session session, BaseQuery query, Class<E> eClass) {
        BaseLookupDefinition lookupDefinition = query.getLookupDefinition();
        String root = lookupDefinition.usingRVDModel() ? "d" : ENTITY;

        String extraWhere = lookupDefinition.getHqlWhereString(root, ENTITY);
        String extraJoin = "";
        if (!lookupDefinition.usingRVDModel()) {
            extraJoin = lookupDefinition.getHqlJoinString(root, ENTITY);
        }

        String queryString = "SELECT DISTINCT "+ENTITY+" from " + eClass.getCanonicalName() + " " + ENTITY +
                " " + extraJoin +
                " WHERE " + ENTITY + ".identification.uuid IS NOT null "+ extraWhere;

        StringJoiner stringJoiner = null;
        if (logQuery) {
            stringJoiner = new StringJoiner("\n");
            stringJoiner.add(queryString);
        }

        // Build query
        org.hibernate.query.Query<E> databaseQuery = session.createQuery(queryString, eClass);

        // Insert parameters, casting as necessary
        HashMap<String, Object> extraParameters = lookupDefinition.getHqlParameters(root, ENTITY);

        for (String key : extraParameters.keySet()) {
            Object value = extraParameters.get(key);
            if (logQuery) {
                stringJoiner.add(key+" = "+value);
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

        System.out.println(stringJoiner.toString());

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
     * @param session Database session to work from
     * @param query Query object defining search parameters
     * @return
     */

    public static <E extends IdentifiedEntity> List<ResultSet<E>> getAllEntitySets(Session session, BaseQuery query, Class<E> eClass) {
        HashMap<BaseQuery, List<ResultSet>> cache = new HashMap<>();
        List<ResultSet<E>> results = getAllEntitySets(session, query, eClass, cache);
        System.out.println(results);
        return results;
    }


    private static <E extends IdentifiedEntity> List<ResultSet<E>> getAllEntitySets(Session session, BaseQuery query, Class<E> eClass, HashMap<BaseQuery, List<ResultSet>> cache) {
        if (cache.containsKey(query)) {
            return cache.get(query).stream().map(r -> (ResultSet<E>) r).collect(Collectors.toList());
        }

        LinkedHashMap<E, ResultSet<E>> identitySetList = new LinkedHashMap<>();
        log.debug("Get all Entities of class " + query.getEntityClassname() + " matching parameters " + query.getSearchParameters() + " [offset: " + query.getOffset() + ", limit: " + query.getCount() + "]");
        org.hibernate.query.Query databaseQuery = QueryManager.getFirstQuery(session, query);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        long start = Instant.now().toEpochMilli();

        List<Object> results = databaseQuery.list();
        for (Object r : results) {
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
                identitySetList.put(resultSet.getPrimaryEntity(), resultSet);
            } catch (ClassNotFoundException e) {
                log.error(e);
            }
        }

        log.debug("Query time: "+(Instant.now().toEpochMilli() - start)+" ms");
        return new ArrayList<>(identitySetList.values());
    }
    public static <E extends IdentifiedEntity> List<E> getAllEntities(Session session, BaseQuery query, Class<E> eClass) {
        return getAllEntitySets(session, query, eClass).stream().map(s -> s.getPrimaryEntity()).collect(Collectors.toList());
    }

    /**
     * Get all Entities of a specific class, that match the given parameters
     * @param session Database session to work from
     * @param query Query object defining search parameters
     * @param eClass Entity subclass
     * @return
     */
    public static <E extends IdentifiedEntity> Stream<E> getAllEntitiesAsStream(Session session, BaseQuery query, Class<E> eClass) {
        log.debug("Get all Entities of class " + eClass.getCanonicalName() + " matching parameters " + query.getSearchParameters() + " [offset: " + query.getOffset() + ", limit: " + query.getCount() + "]");
        org.hibernate.query.Query databaseQuery = QueryManager.getFirstQuery(session, query);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        databaseQuery.setFetchSize(1000);
        List<String> classNames = query.getEntityClassnames();
        Stream<E> results = databaseQuery.stream().map(object -> {
            // org.hibernate.query.Query subQuery = QueryManager.getSecondQuery(session, query, Collections.singletonList(((DatabaseEntry) object)));
            try {
                return new ResultSet<E>(object, classNames).getPrimaryEntity();
            } catch (ClassNotFoundException e) {
                log.error("Failed casting for query classes "+classNames, e);
            }
            return null;
        });
        return results;
    }

    /**
     * Get one Entity of a specific class, by uuid
     * @param session Database session to work from
     * @param uuid UUID to search for
     * @param eClass Entity subclass
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
     * @param session
     * @param identification
     * @param eClass
     * @param <E>
     * @return
     */
    public static <E extends IdentifiedEntity> E getEntity(Session session, Identification identification, Class<E> eClass) {
        log.debug("Get Entity of class " + eClass.getCanonicalName() + " by identification "+identification.getUuid());
        org.hibernate.query.Query<E> databaseQuery = session.createQuery("select "+ENTITY+" from " + eClass.getCanonicalName() + " " + ENTITY + " where " + ENTITY + ".identification = :identification", eClass);
        databaseQuery.setParameter("identification", identification);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        databaseQuery.setCacheable(true);
        try {
            long start = Instant.now().toEpochMilli();
            E entity = databaseQuery.getSingleResult();
            log.debug("Query time: "+(Instant.now().toEpochMilli() - start)+" ms");
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
        org.hibernate.query.Query<T> databaseQuery = session
            .createQuery(
                String.format("SELECT t FROM %s t", tClass.getCanonicalName()),
                tClass);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        return databaseQuery.getResultList();
    }

    public static <T extends DatabaseEntry> Stream<T> getAllItemsAsStream(
        Session session, Class<T> tClass
    ) {
        org.hibernate.query.Query<T> databaseQuery = session
            .createQuery(
                String.format("SELECT t FROM %s t", tClass.getCanonicalName()),
                tClass);
        databaseQuery.setFlushMode(FlushModeType.COMMIT);
        return databaseQuery.stream();
    }

    public static <T extends DatabaseEntry> List<T> getItems(Session session, Class<T> tClass, Map<String, Object> filter) {
        StringJoiner whereJoiner = new StringJoiner(" and ");
        for (String key : filter.keySet()) {
            whereJoiner.add("t."+key+" = :"+key);
        }
        org.hibernate.query.Query<T> databaseQuery = session.createQuery("select t from " + tClass.getCanonicalName() + " t where " + whereJoiner.toString(), tClass);
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
            where = " where " + whereJoiner.toString();
        }
        org.hibernate.query.Query databaseQuery = session.createQuery("select count(t) from " + tClass.getCanonicalName() + " t " + where);
        if (filter != null && !filter.isEmpty()) {
            for (String key : filter.keySet()) {
                databaseQuery.setParameter(key, filter.get(key));
            }
        }
        return (long) databaseQuery.uniqueResult();
    }

    /**
     * Get Effects for a specific class, matching a start and end time
     * @param session Database session to work from
     * @param entity Entity to search under
     * @param effectFrom start time
     * @param effectTo end time
     * @param vClass Effect subclass
     * @return
     */
    public static <V extends Effect> List<V> getEffects(Session session, Entity entity, OffsetDateTime effectFrom, OffsetDateTime effectTo, Class<V> vClass) {
        // AFAIK, this method is only ever used for testing
        log.debug("Get Effects of class " + vClass.getCanonicalName() + " under Entity "+entity.getUUID() + " from "+effectFrom.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + " to " + effectTo.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        org.hibernate.query.Query<V> databaseQuery = session.createQuery("select v from " + entity.getClass().getCanonicalName() + " " + ENTITY +" join "+ENTITY+".registrations r join r.effects v where "+ENTITY+".id = :id and v.effectFrom = :from and v.effectTo = :to", vClass);
        databaseQuery.setParameter("id", entity.getId());
        databaseQuery.setParameter("from", effectFrom);
        databaseQuery.setParameter("to", effectTo);
        return databaseQuery.list();
    }

    /**
     * Get DataItems matching an existing DataItem (for locating duplicates)
     * @param session Database session to work from
     * @param entity Entity to search under
     * @param similar Another DataItem, whose properties will form the base of the search
     * @param dClass DataItem subclass
     * @return
     */
    public static <D extends DataItem> List<D> getDataItems(Session session, Entity entity, D similar, Class<D> dClass) throws PluginImplementationException {
        log.debug("Get DataItems of class " + dClass.getCanonicalName() + " under Entity "+entity.getUUID() + " with content matching DataItem "+similar.asMap());
        LookupDefinition lookupDefinition = similar.getLookupDefinition();
        String dataItemKey = "d";
        String extraJoin = lookupDefinition.getHqlJoinString(dataItemKey, ENTITY);
        StringJoiner extraWhere = new StringJoiner(" AND ");

        List<String> whereParts = lookupDefinition.getHqlWhereParts(dataItemKey, ENTITY, true);
        if (!whereParts.isEmpty()) {
            extraWhere.add("");
            for (String part : whereParts) {
                extraWhere.add(part);
            }
        }

        String entityIdKey = "E" + UUID.randomUUID().toString().replace("-", "");

        String queryString = "SELECT " + dataItemKey + " FROM " + dClass.getCanonicalName() + " " + dataItemKey +
                " JOIN " + dataItemKey+".effects v" +
                " JOIN v.registration r" +
                " JOIN r.entity "+ENTITY + " "+extraJoin +
                " WHERE " + ENTITY + ".id = :"+entityIdKey + " "+ extraWhere.toString();

        log.debug(queryString);
        org.hibernate.query.Query<D> query = session.createQuery(queryString, dClass);

        query.setParameter(entityIdKey, entity.getId());
        HashMap<String, Object> extraParameters = lookupDefinition.getHqlParameters(dataItemKey, ENTITY);
        for (String key : extraParameters.keySet()) {
            Object value = extraParameters.get(key);
            if (value instanceof Collection) {
                query.setParameterList(key, (Collection) value);
            } else {
                query.setParameter(key, value);
            }
        }
        List<D> results = query.list();
        return results;
    }

    /**
     * Identify and remove duplicate Effects in a Registration, so the Registration will only have unique Effects
     * @param session Database session to work from
     * @param registration Registration to dedup
     */
    public static <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void dedupEffects(Session session, R registration) {
        log.debug("Remove duplicate Effects in Registration " + registration.getId() + " ("+registration.getRegisterChecksum()+")");
        DoubleHashMap<OffsetDateTime, OffsetDateTime, V> authoritative = new DoubleHashMap<>();
        ListHashMap<V, V> duplicates = new ListHashMap<>();
        for (V effect : registration.getEffects()) {
            OffsetDateTime key1 = effect.getEffectFrom();
            OffsetDateTime key2 = effect.getEffectTo();
            if (!authoritative.containsKey(key1, key2)) {
                authoritative.put(key1, key2, effect);
            } else {
                duplicates.add(authoritative.get(key1, key2), effect);
            }
        }
        if (duplicates.isEmpty()) {
            log.debug("No duplicate effects found");
        } else {
            for (V master : duplicates.keySet()) {
                List<V> dups = duplicates.get(master);
                log.debug("There are " + dups.size() + " duplicates of Effect " + master.getEffectFrom() + " - " + master.getEffectTo());
                int i = 0;
                for (V dup : dups) {
                    log.debug("    Duplicate " + i + " contains " + dup.getDataItems().size() + " DataItems");
                    for (D dataItem : dup.getDataItems()) {
                        dataItem.addEffect(master);
                        dataItem.removeEffect(dup);
                    }
                    registration.removeEffect(dup);
                    session.delete(dup);
                    i++;
                }
            }
        }
    }

    /**
     * Save registration to database, re-pointing the entity reference to a persistent entity if one exists, merging effects with identical timestamps, and saving all associated effects and dataitems.
     * @param session A database session to work on
     * @param registration Registration to be saved
     */
    public static <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void saveRegistration(Session session, E entity, R registration) throws DataFordelerException {
        saveRegistration(session, entity, registration, true, true, true);
    }

    /**
     * Save registration to database, re-pointing the entity reference to a persistent entity if one exists, merging effects with identical timestamps, and saving all associated effects and dataitems.
     * @param session A database session to work on
     * @param registration Registration to be saved
     */
    public static <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void saveRegistration(Session session, E entity, R registration, boolean dedupEffects, boolean dedupItems, boolean validateRegistrationSequence) throws DataFordelerException {
        log.debug("Saving registration of type " + registration.getClass().getCanonicalName() + " with checksum " + registration.getRegisterChecksum() + " and sequence number " + registration.getSequenceNumber());
        if (entity == null && registration.entity != null) {
            E existingEntity = getEntity(session, registration.entity.getUUID(), (Class<E>) registration.entity.getClass());
            if (existingEntity != null) {
                entity = existingEntity;
                log.debug("There is an existing entity with uuid " + existingEntity.getUUID().toString() + ", using that");
            } else {
                entity = registration.entity;
                log.debug("No existing entity with uuid "+entity.getUUID().toString()+" "+registration.entity.getClass());
            }
        }
        if (entity == null) {
            throw new MissingEntityException(registration);
        }


        if (validateRegistrationSequence) {
            // Validate registration:
            // * No existing registration on the entity may have the same sequence number
            // * The registration must have a sequence number one higher than the highest existing one
            int highestSequenceNumber = -1;
            R lastExistingRegistration = null;
            if (registration.getId() == null) {
                // New registration
                for (R otherRegistration : entity.getRegistrations()) {
                    if (otherRegistration != registration) { // Consider only other registrations
                        if (otherRegistration.getId() != null || session.contains(otherRegistration)) { // Consider only saved registrations
                            if (registration.equals(otherRegistration)) {
                                // the registration exactly matches a pre-existing registration, so saving it is a no-op
                                return;
                            }
                            if (otherRegistration.getSequenceNumber() == registration.getSequenceNumber()) {
                                log.error("Duplicate sequence number");
                                for (R r : entity.getRegistrations()) {
                                    log.error((r == registration ? "* " : "  ") + r.getSequenceNumber() + ": " + r.getRegistrationFrom() + " => " + r.getRegistrationTo());
                                }
                                throw new DuplicateSequenceNumberException(registration, otherRegistration);
                            }
                            if (otherRegistration.getSequenceNumber() > highestSequenceNumber) {
                                highestSequenceNumber = otherRegistration.getSequenceNumber();
                                lastExistingRegistration = otherRegistration;
                            }
                        }
                    }
                }

                if (highestSequenceNumber > -1 && registration.getSequenceNumber() != highestSequenceNumber + 1) {
                    log.warn("Skipped sequence number");
                    for (R r : entity.getRegistrations()) {
                        log.warn((r == registration ? "* " : "  ") + r.getSequenceNumber() + ": " + r.getRegistrationFrom() + " => " + r.getRegistrationTo());
                    }
                    //throw new SkippedSequenceNumberException(registration, highestSequenceNumber);
                }
                if (lastExistingRegistration != null) {
                    if (lastExistingRegistration.getRegistrationTo() == null) {
                        lastExistingRegistration.setRegistrationTo(registration.getRegistrationFrom());
                    } else if (!lastExistingRegistration.getRegistrationTo().equals(registration.getRegistrationFrom())) {
                        log.error("Mismatching registration boundary");
                        for (R r : entity.getRegistrations()) {
                            log.error((r == registration ? "* " : "  ") + r.getSequenceNumber() + ": " + r.getRegistrationFrom() + " => " + r.getRegistrationTo());
                        }
                        throw new MismatchingRegistrationBoundaryException(registration, lastExistingRegistration);
                    }
                }
            }
            if (lastExistingRegistration != null) {
                session.update(lastExistingRegistration);
            }
        }

        registration.setEntity(entity);

        // Normalize references: setting them to existing Identification entries if possible
        // If no existing Identification exists, keep the one we have and save it to the session
        for (V effect : registration.getEffects()) {
            for (D dataItem : effect.getDataItems()) {
                HashMap<String, Identification> references = dataItem.getReferences();
                boolean changed = false;
                for (String key : references.keySet()) {
                    Identification reference = references.get(key);
                    if (reference != null) {
                        Identification otherReference = getIdentification(session, reference.getUuid());
                        if (otherReference != null && reference != otherReference) {
                            references.put(key, otherReference);
                            changed = true;
                        } else {
                            session.saveOrUpdate(reference);
                        }
                    }
                }
                if (changed) {
                    dataItem.updateReferences(references);
                }
            }
        }

        if (dedupItems) {
            dedupItems(session, entity, registration);
        }

        Identification existing;
        //if (entity.getIdentification() != null && entity.getIdentification().getId() != null) {
        //    existing = session.get(Identification.class, entity.getIdentification().getId());
        //} else {
            existing = getOrCreateIdentification(session, entity.getUUID(), entity.getDomain());
        //}

        if (existing != null && existing != entity.getIdentification()) {
            entity.setIdentifikation(existing);
            session.saveOrUpdate(existing);
            session.saveOrUpdate(entity);
        }/* else if (existing == null) {
            log.info("identification "+entity.getUUID()+" does not already exist or is already assigned");
            session.saveOrUpdate(entity.getIdentification());
            session.saveOrUpdate(entity);
        }*/



        if (dedupEffects) {
            dedupEffects(session, registration);
        }

        session.saveOrUpdate(registration);

        for (V effect : registration.getEffects()) {
            session.saveOrUpdate(effect);
            for (D dataItem : effect.getDataItems()) {
                session.saveOrUpdate(dataItem);
                /*for (Identification reference : dataItem.getReferences()) {
                    System.out.println("saving reference "+reference);
                    session.saveOrUpdate(reference);
                }*/
            }
        }

    }

    private static <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void dedupItems(Session session, E entity, R registration) throws PluginImplementationException {
        // Dedup dataitems
        // Find existing DataItems on the Entity that hold the same data
        for (V effect : registration.getEffects()) {
            HashSet<D> obsolete = new HashSet<D>();
            for (D dataItem : effect.getDataItems()) {
                List<D> existing = getDataItems(session, entity, dataItem, (Class<D>) dataItem.getClass());
                // If found, use that DataItem instead
                if (existing != null && !existing.isEmpty() && dataItem != existing.get(0)) {
                    obsolete.add(dataItem);
                    dataItem = existing.get(0);
                }
                // Couple it with the Effect
                dataItem.addEffect(effect);
            }

            log.info("Found " + obsolete.size() + " obsolete items!");
            for (D dataItem : obsolete) {
                Set<V> effects = dataItem.getEffects();
                for (V e : effects) {
                    e.dataItems.remove(dataItem);
                    session.saveOrUpdate(e);
                }
                session.delete(dataItem);
            }
        }
    }

}
