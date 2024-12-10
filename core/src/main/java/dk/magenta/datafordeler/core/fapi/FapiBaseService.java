package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service container to be subclassed for each Entity class, serving REST
 */
@RequestMapping("/fapi_service_with_no_requestmapping")
public abstract class FapiBaseService<E extends IdentifiedEntity, Q extends BaseQuery> {

    @Autowired
    protected ObjectMapper objectMapper;


    @Autowired
    protected SessionManager sessionManager;

    /**
     * Obtains the autowired SessionManager
     *
     * @return SessionManager instance
     */
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }


    @Autowired
    private DafoUserManager dafoUserManager;

    protected DafoUserManager getDafoUserManager() {
        return this.dafoUserManager;
    }


    @Autowired
    protected CsvMapper csvMapper;


    private OutputWrapper<E> outputWrapper;

    /**
     * Obtains the version number of the service. This will be used in the path that requests may interface with
     *
     * @return service version, e.g. 1
     */
    public abstract int getVersion();


    /**
     * Obtains the name of the service. This will be used in the path that requests may interface with
     *
     * @return service name, e.g. "postnummer"
     */
    public abstract String getServiceName();


    /**
     * @return Entity subclass
     */
    protected abstract Class<E> getEntityClass();


    public abstract Plugin getPlugin();


    public OutputWrapper<E> getOutputWrapper() {
        return outputWrapper;
    }

    protected void setOutputWrapper(OutputWrapper<E> outputWrapper) {
        this.outputWrapper = outputWrapper;
    }


    private final Logger log = LogManager.getLogger(FapiBaseService.class.getCanonicalName());

    protected OutputWrapper.Mode getDefaultMode() {
        return OutputWrapper.Mode.DATAONLY;
    }


    public String[] getServicePaths() {
        RequestMapping requestMapping = this.getClass().getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            return requestMapping.value();
        }
        return null;
    }

    @RequestMapping(path = "", produces = "application/json")
    public String index(HttpServletRequest request) throws JsonProcessingException {
        String servletPath = request.getServletPath();
        return this.objectMapper.writeValueAsString(this.getServiceDescriptor(servletPath));
    }

    public ServiceDescriptor getServiceDescriptor(String servletPath) {
        return new RestServiceDescriptor(this.getPlugin(), this.getServiceName(), servletPath, this.getEmptyQuery().getClass());
    }

    /**
     * Checks that the user has access to the service
     *
     * @param user DafoUserDetails object representing the user provided from a SAML token.
     * @throws AccessDeniedException Implementing this method as a noop will make the service publicly accessible.
     */
    protected abstract void checkAccess(DafoUserDetails user)
            throws AccessDeniedException, AccessRequiredException;

    protected void checkAndLogAccess(LoggerHelper loggerHelper)
            throws AccessDeniedException, AccessRequiredException {
        try {
            this.checkAccess(loggerHelper.getUser());
        } catch (AccessDeniedException | AccessRequiredException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }


    /**
     * Handle a lookup-by-UUID request in REST. This method is called by the Servlet
     *
     * @param uuid          Identifier coming from the client
     * @param requestParams url parameters
     * @return Found Entity, or null if none found.
     */
    @RequestMapping(path = "/{uuid}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Envelope getRest(@PathVariable("uuid") String uuid, @RequestParam MultiValueMap<String, String> requestParams, HttpServletRequest request)
            throws DataFordelerException {
        Envelope envelope = new Envelope();
        Session session = this.getSessionManager().getSessionFactory().openSession();
        try {
            DafoUserDetails user = this.getDafoUserManager().getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.info(
                    "Incoming REST request for " + this.getServiceName() + " with uuid " + uuid
            );
            this.checkAndLogAccess(loggerHelper);
            Q query = this.getQuery(requestParams, true);
            query.addUUID(uuid);

            this.applyAreaRestrictionsToQuery(query, user);
            envelope.addQueryData(query);
            envelope.addUserData(user);
            envelope.addRequestData(request);
            try {
                List<ResultSet<E>> results = this.searchByQuery(query, session);
                if (this.getOutputWrapper() != null) {
                    envelope.setResults(this.getOutputWrapper().wrapResultSets(results, query, this.getDefaultMode()));
                } else {
                    ArrayNode jacksonConverted = objectMapper.valueToTree(results);
                    ArrayList<Object> wrapper = new ArrayList<>();
                    for (JsonNode node : jacksonConverted) {
                        wrapper.add(node);
                    }
                    envelope.setResults(wrapper);
                }
                if (results.isEmpty()) {
                    this.log.debug("Item not found, returning");
                } else {
                    this.log.debug("Item found, returning");
                }
                envelope.close();
                loggerHelper.logResult(envelope);

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new InvalidClientInputException(e.getMessage());
            }
        } catch (AccessDeniedException | AccessRequiredException | InvalidClientInputException | InvalidTokenException | InvalidCertificateException e) {
            this.log.warn("Error in REST getById (" + request.getRequestURI() + "): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error("Error in REST getById", e);
            throw e;
        } finally {
            session.close();
        }

        return envelope;
    }

    /**
     * Handle a lookup-by-UUID request in REST, returning CSV text.
     *
     * @see #getRest(String, MultiValueMap, HttpServletRequest)
     */
    @RequestMapping(path = "/{id}", produces = {
            "text/csv",
            "text/tsv",
    })
    public void getRestCSV(@PathVariable("id") String id,
                           @RequestParam MultiValueMap<String, String> requestParams,
                           HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        try {
            DafoUserDetails user = this.getDafoUserManager().getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.info(
                    "Incoming CSV REST request for " + this.getServiceName() +
                            " with id " + id
            );
            this.checkAndLogAccess(loggerHelper);
            Q query = this.getQuery(requestParams, true);
            this.applyAreaRestrictionsToQuery(query, user);

            E entity = this.searchById(id, query, session);

            sendAsCSV(Stream.of(entity), request, response);
        } catch (AccessDeniedException | AccessRequiredException | InvalidClientInputException | InvalidTokenException | HttpNotFoundException e) {
            this.log.warn("Error in REST getRestCsv (" + request.getRequestURI() + "): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error("Error in REST getRestCsv (" + request.getRequestURI() + ")", e);
            throw e;
        } finally {
            session.close();
        }
    }


    /**
     * Parse a registration boundary into a Query object of the correct subclass
     *
     * @param registrationFrom Low boundary for registration inclusion
     * @param registrationTo   High boundary for registration inclusion
     * @return Query subclass instance
     */
    protected Q getQuery(String registrationFrom, String registrationTo) {
        Q query = this.getEmptyQuery();
        OffsetDateTime now = OffsetDateTime.now();
        query.setRegistrationFrom(registrationFrom, now);
        query.setRegistrationTo(registrationTo, now);
        return query;
    }


    /**
     * Handle a lookup-by-parameters request in REST. This method is called by the Servlet
     *
     * @param requestParams Request Parameters from spring boot
     * @return Found Entities
     */
    @RequestMapping(path = "/search", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Envelope searchRest(@RequestParam MultiValueMap<String, String> requestParams, HttpServletRequest request) throws DataFordelerException {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Envelope envelope = new Envelope();
        try {
            DafoUserDetails user = this.getDafoUserManager().getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.info(
                    "Incoming REST request for " + this.getServiceName() + " with query " + requestParams.toString()
            );
            this.checkAndLogAccess(loggerHelper);
            System.out.println("client: "+request.getHeader("uxp-client"));

            Q query = this.getQuery(requestParams, false);
            this.applyAreaRestrictionsToQuery(query, user);
            envelope.addQueryData(query);
            envelope.addUserData(user);
            envelope.addRequestData(request);
            List<ResultSet<E>> results = this.searchByQuery(query, session);
            if (this.getOutputWrapper() != null) {
                this.log.info("Wrapping resultset with " + this.getOutputWrapper().getClass().getCanonicalName());
                envelope.setResults(this.getOutputWrapper().wrapResultSets(results, query, query.getMode(this.getDefaultMode())));
            } else {
                this.log.info("No outputwrapper defined for " + this.getClass().getCanonicalName() + ", not wrapping output");
                ArrayNode jacksonConverted = objectMapper.valueToTree(results.stream().map(resultset -> resultset.getPrimaryEntity()).collect(Collectors.toList()));
                ArrayList<Object> wrapper = new ArrayList<>();
                for (JsonNode node : jacksonConverted) {
                    wrapper.add(node);
                }
                envelope.setResults(wrapper);
            }
            envelope.close();
            loggerHelper.logResult(envelope, requestParams.toString());
        } catch (AccessDeniedException | AccessRequiredException | InvalidClientInputException | InvalidTokenException e) {
            this.log.warn("Error in REST search (" + request.getRequestURI() + "): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            this.log.error("Error in REST search (" + request.getRequestURI() + ")", e);
            throw e;
        } finally {
            session.close();
        }
        return envelope;
    }

    /**
     * Handle a lookup-by-parameters request in REST, outputting CSV text.
     *
     * @see #searchRest(MultiValueMap, HttpServletRequest)
     */
    @RequestMapping(path = "/search", produces = {
            "text/csv",
            "text/tsv",
    })
    public void searchRestCSV(@RequestParam MultiValueMap<String, String>
                                      requestParams, HttpServletRequest request,
                              HttpServletResponse response) throws DataFordelerException, IOException {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        try {
            DafoUserDetails user = this.getDafoUserManager().getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.info(
                    "Incoming CSV REST request for " + this.getServiceName() +
                            " with query " + requestParams.toString()
            );

            this.checkAndLogAccess(loggerHelper);
            Q query = this.getQuery(requestParams, false);
            this.applyAreaRestrictionsToQuery(query, user);

            sendAsCSV(this.searchByQueryAsStream(query, session),
                    request, response);
        } catch (AccessDeniedException | AccessRequiredException | InvalidClientInputException | HttpNotFoundException | InvalidTokenException e) {
            this.log.warn("Error in REST CSV search (" + request.getRequestURI() + "): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.log.error("Error in REST CSV search (" + request.getRequestURI() + ")", e);
            throw e;
        } finally {
            session.close();
        }
    }


    /**
     * Obtain an empty Query instance of the correct subclass
     *
     * @return Query subclass instance
     */
    protected abstract Q getEmptyQuery();

    /**
     * Parse a map of URL parameters into a Query object of the correct subclass
     *
     * @param parameters URL parameters received in a request
     * @return Query subclass instance
     */
    protected Q getQuery(MultiValueMap<String, String> parameters, boolean limitsOnly) throws InvalidClientInputException {
        Q query = this.getEmptyQuery();
        ParameterMap parameterMap = new ParameterMap(parameters, false);
        query.fillFromParameters(parameterMap, limitsOnly);
        return query;
    }

    protected void applyAreaRestrictionsToQuery(Q query, DafoUserDetails user) throws InvalidClientInputException {
        return;
    }


    /**
     * Perform a search for Entities by a Query object
     *
     * @param query Query objects to search by
     * @return Found Entities
     */
    //protected abstract Set<E> searchByQuery(Q query);
    public List<ResultSet<E>> searchByQuery(Q query, Session session) {
        return QueryManager.getAllEntitySets(session, query, this.getEntityClass());
    }

    /**
     * Perform a search for Entities by a Query object
     *
     * @param query Query objects to search by
     * @return Found Entities
     */
    //protected abstract Set<E> searchByQuery(Q query);
    protected Stream<E> searchByQueryAsStream(Q query, Session session) {
        return QueryManager.getAllEntitiesAsStream(
                session, query,
                this.getEntityClass()
        );
    }

    /**
     * Perform a search for Entities by id
     *
     * @param id    Identifier to search by. Must be parseable as a UUID
     * @param query Query object modifying the output (such as a bitemporal range)
     * @return Found Entities
     */
    protected E searchById(String id, Q query, Session session) {
        return this.searchById(UUID.fromString(id), query, session);
    }


    /**
     * Perform a search for Entities by id
     *
     * @param uuid  Identifier to search by
     * @param query Query object modifying the output (such as a bitemporal range)
     * @return Found Entities
     */
    protected E searchById(UUID uuid, Q query, Session session) {
        query.applyFilters(session);
        E entity = QueryManager.getEntity(session, uuid, this.getEntityClass());
        if (entity != null) {
            entity.forceLoad(session);
        }
        return entity;
    }

    //protected abstract void sendAsCSV(Stream<E> entities, HttpServletRequest request, HttpServletResponse response) throws IOException, HttpNotFoundException;


    private static Map<String, String> objectNodeToFlatMap(ObjectNode node, Set<String> omitKeys) {
        Map<String, String> output = new HashMap<>();
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            if (omitKeys == null || !omitKeys.contains(key)) {
                JsonNode value = node.get(key);
                if (value.isValueNode()) {
                    output.put(key, value.isNull() ? null : value.asText());
                }
            }
        }
        return output;
    }

    protected void sendAsCSV(Stream<E> entities, HttpServletRequest request,
                             HttpServletResponse response)
            throws IOException, HttpNotFoundException {

        List<MediaType> acceptedTypes = MediaType.parseMediaTypes(request.getHeader("Accept"));

        Set<String> omitEntityKeys = new HashSet<>();
        omitEntityKeys.add("domain");
        omitEntityKeys.add("domæne");
        BaseQuery baseQuery = this.getEmptyQuery();
        Iterator<Map<String, String>> dataIter = entities.map(e -> {
            List<Map<String, String>> rows = new ArrayList<>();
            Object wrapped = FapiBaseService.this.getOutputWrapper().wrapResult(e, baseQuery, OutputWrapper.Mode.RVD);
            ObjectNode entityNode = (ObjectNode) wrapped;
            ArrayNode registrations = (ArrayNode) entityNode.get("registreringer");
            if (registrations != null) {
                for (int i = 0; i < registrations.size(); i++) {
                    ObjectNode registrationNode = (ObjectNode) registrations.get(i);
                    if (registrationNode != null) {
                        ArrayNode effects = (ArrayNode) registrationNode.get("virkninger");
                        if (effects != null) {
                            for (int j = 0; j < effects.size(); j++) {
                                ObjectNode effectNode = (ObjectNode) effects.get(j);
                                if (effectNode != null) {
                                    Map<String, String> output = new HashMap<>();
                                    output.putAll(objectNodeToFlatMap(effectNode, null));
                                    output.putAll(objectNodeToFlatMap(registrationNode, null));
                                    output.putAll(objectNodeToFlatMap(entityNode, omitEntityKeys));
                                    rows.add(output);
                                }
                            }
                        }
                    }
                }
            }
            return rows;
        }).flatMap((Function<List<Map<String, String>>, Stream<Map<String, String>>>) Collection::stream).iterator();

/*
        Iterator<Map<String, Object>> dataIter =
                entities.map(Entity::getRegistrations).flatMap(
                        List::stream
                ).flatMap(
                        r -> ((Registration) r).getEffects().stream()
                ).map(
                        obj -> {
                            Effect e = (Effect) obj;
                            Registration r = e.getRegistration();
                            Map<String, Object> data = e.getData();

                            data.put("effectFrom",
                                    e.getEffectFrom());
                            data.put("effectTo",
                                    e.getEffectTo());
                            data.put("registrationFrom",
                                    r.getRegistrationFrom());
                            data.put("registrationTo",
                                    r.getRegistrationFrom());
                            data.put("sequenceNumber",
                                    r.getSequenceNumber());
                            data.put("uuid", r.getEntity().getUUID());

                            return data;
                        }
                ).iterator();
*/
        if (!dataIter.hasNext()) {
            response.sendError(HttpStatus.NO_CONTENT.value());
            return;
        }

        CsvSchema.Builder builder = new CsvSchema.Builder();

        Map<String, String> first = dataIter.next();
        ArrayList<String> keys = new ArrayList<>(first.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            builder.addColumn(new CsvSchema.Column(
                    i, keys.get(i),
                    CsvSchema.ColumnType.NUMBER_OR_STRING
            ));
        }

        CsvSchema schema = builder.build().withHeader();

        if (acceptedTypes.contains(new MediaType("text", "tsv"))) {
            schema = schema.withColumnSeparator('\t');
            response.setContentType("text/tsv");
        } else {
            response.setContentType("text/csv");
        }

        SequenceWriter writer = csvMapper.writer(schema).writeValues(response.getOutputStream());

        writer.write(first);

        while (dataIter.hasNext()) {
            Map<String, String> data = dataIter.next();
            writer.write(data);
        }
    }
}
