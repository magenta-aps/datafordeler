package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import dk.magenta.datafordeler.core.database.DataItem;
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
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service container to be subclassed for each Entity class, serving REST and SOAP
 */
@RequestMapping("/fapi_service_with_no_requestmapping")
public abstract class FapiBaseService<E extends IdentifiedEntity, Q extends BaseQuery> {

    @Autowired
    protected ObjectMapper objectMapper;





    @Autowired
    protected SessionManager sessionManager;

    /**
     * Obtains the autowired SessionManager
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




    @Resource(name="wsContext")
    WebServiceContext context;

    @Autowired
    protected CsvMapper csvMapper;





    private OutputWrapper<E> outputWrapper;

    /**
     * Obtains the version number of the service. This will be used in the path that requests may interface with
     * @return service version, e.g. 1
     */
    @WebMethod(exclude = true)
    public abstract int getVersion();


    /**
     * Obtains the name of the service. This will be used in the path that requests may interface with
     * @return service name, e.g. "postnummer"
     */
    @WebMethod(exclude = true)
    public abstract String getServiceName();


    /**
     * @return Entity subclass
     */
    @WebMethod(exclude = true)
    protected abstract Class<E> getEntityClass();



    public abstract Plugin getPlugin();



    protected OutputWrapper<E> getOutputWrapper() {
        return outputWrapper;
    }

    protected void setOutputWrapper(OutputWrapper<E> outputWrapper) {
        this.outputWrapper = outputWrapper;
    }


    private Logger log = LogManager.getLogger(FapiBaseService.class.getCanonicalName());

    protected OutputWrapper.Mode getDefaultMode() {
        return OutputWrapper.Mode.RVD;
    }


    public String[] getServicePaths() {
        RequestMapping requestMapping = this.getClass().getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            return requestMapping.value();
        }
        return null;
    }

    @RequestMapping(path="", produces="application/json")
    public String index(HttpServletRequest request) throws JsonProcessingException {
        String servletPath = request.getServletPath();
        return this.objectMapper.writeValueAsString(this.getServiceDescriptor(servletPath, false));
    }

    public ServiceDescriptor getServiceDescriptor(String servletPath, boolean isSoap) {
        if (isSoap) {
            return new SoapServiceDescriptor(this.getPlugin(), this.getServiceName(), servletPath, this.getEmptyQuery().getClass());
        } else {
            return new RestServiceDescriptor(this.getPlugin(), this.getServiceName(), servletPath, this.getEmptyQuery().getClass());
        }
    }

    /**
     * Checks that the user has access to the service
     * @param user DafoUserDetails object representing the user provided from a SAML token.
     * @throws AccessDeniedException
     *
     * Implementing this method as a noop will make the service publicly accessible.
     */
    protected abstract void checkAccess(DafoUserDetails user)
            throws AccessDeniedException, AccessRequiredException;

    protected void checkAndLogAccess(LoggerHelper loggerHelper)
            throws AccessDeniedException, AccessRequiredException {
        try {
            this.checkAccess(loggerHelper.getUser());
        }
        catch (AccessDeniedException|AccessRequiredException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }




    /**
     * Handle a lookup-by-UUID request in REST. This method is called by the Servlet
     * @param uuid Identifier coming from the client
     * @param requestParams url parameters
     * @return Found Entity, or null if none found.
     */
    @WebMethod(exclude = true)
    @RequestMapping(path="/{uuid}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
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
                List<E> results = this.searchByQuery(query, session);
                if (this.getOutputWrapper() != null) {
                    envelope.setResults(this.getOutputWrapper().wrapResults(results, query, this.getDefaultMode()));
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
        } catch (AccessDeniedException|AccessRequiredException|InvalidClientInputException|InvalidTokenException e) {
            this.log.warn("Error in REST getById ("+request.getRequestURI()+")", e);
            throw e;
        } catch (DataFordelerException e) {
            e.printStackTrace();
            this.log.error("Error in REST getById", e);
            throw e;
        } finally {
            session.close();
        }

        return envelope;
    }

    /**
     * Handle a lookup-by-UUID request in REST, returning CSV text.
     * @see #getRest(String, MultiValueMap, HttpServletRequest)
     */
    @WebMethod(exclude = true)
    @RequestMapping(path="/{id}", produces = {
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
        } catch (AccessDeniedException|AccessRequiredException|InvalidClientInputException|InvalidTokenException|HttpNotFoundException e) {
            this.log.warn("Error in REST getRestCsv ("+request.getRequestURI()+")", e);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            this.log.error("Error in REST getRestCsv ("+request.getRequestURI()+")", e);
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * Handle a lookup-by-UUID request in SOAP. This method is called by the Servlet
     * @param id Identifier coming from the client
     * @param registeringFra Low boundary for registration inclusion
     * @param registeringTil High boundary for registration inclusion
     * @return Found Entity, or null if none found.
     */
    // TODO: How to use DafoUserDetails with SOAP requests?
    @WebMethod(operationName = "get")
    public Envelope getSoap(@WebParam(name="id") @XmlElement(required=true) String id,
                     @WebParam(name="registeringFra") @XmlElement(required = false) String registeringFra,
                     @WebParam(name="registeringTil") @XmlElement(required = false) String registeringTil)
            throws DataFordelerException {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Envelope envelope = new Envelope();
        try {
            MessageContext messageContext = context.getMessageContext();
            HttpServletRequest request = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
            DafoUserDetails user = this.getDafoUserManager().getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.info(
                    "Incoming SOAP request for " + this.getServiceName() + " with id " + id
            );
            this.checkAndLogAccess(loggerHelper);
            Q query = this.getQuery(registeringFra, registeringTil);
            query.addUUID(id);
            envelope.addQueryData(query);
            envelope.addUserData(user);
            envelope.addRequestData(request);
            try {
                List<E> results = this.searchByQuery(query, session);
                if (this.getOutputWrapper() != null) {
                    envelope.setResults(this.getOutputWrapper().wrapResults(results, query, this.getDefaultMode()));
                } else {
                    ArrayNode jacksonConverted = objectMapper.valueToTree(results);
                    ArrayList<Object> wrapper = new ArrayList<>();
                    for (JsonNode node : jacksonConverted) {
                        wrapper.add(node);
                    }
                    envelope.setResults(wrapper);
                }
                envelope.close();
                loggerHelper.logResult(envelope);
            } catch (IllegalArgumentException e) {
                throw new InvalidClientInputException(e.getMessage());
            }
        } catch (AccessDeniedException|AccessRequiredException|InvalidClientInputException|InvalidTokenException e) {
            this.log.warn("Error in SOAP getById (id: "+id+", registeringFra: "+registeringFra+", registeringTil: "+registeringTil+")", e);
            throw e;
        } catch (DataFordelerException e) {
            e.printStackTrace();
            this.log.error("Error in SOAP getById (id: "+id+", registeringFra: "+registeringFra+", registeringTil: "+registeringTil+")", e);
            throw e;
        } finally {
            session.close();
        }
        return envelope;
    }


    /**
     * Parse a registration boundary into a Query object of the correct subclass
     * @param registrationFrom Low boundary for registration inclusion
     * @param registrationTo High boundary for registration inclusion
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
     * @param requestParams Request Parameters from spring boot
     * @return Found Entities
     */
    @WebMethod(exclude = true)
    @RequestMapping(path="/search", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
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
            Q query = this.getQuery(requestParams, false);
            this.applyAreaRestrictionsToQuery(query, user);
            envelope.addQueryData(query);
            envelope.addUserData(user);
            envelope.addRequestData(request);
            List<E> results = this.searchByQuery(query, session);
            if (this.getOutputWrapper() != null) {
                envelope.setResults(this.getOutputWrapper().wrapResults(results, query, query.getMode(this.getDefaultMode())));
            } else {
                ArrayNode jacksonConverted = objectMapper.valueToTree(results);
                ArrayList<Object> wrapper = new ArrayList<>();
                for (JsonNode node : jacksonConverted) {
                    wrapper.add(node);
                }
                envelope.setResults(wrapper);
            }
            envelope.close();
            loggerHelper.logResult(envelope, requestParams.toString());
        } catch (AccessDeniedException|AccessRequiredException|InvalidClientInputException|InvalidTokenException|HttpNotFoundException e) {
            this.log.warn("Error in REST search ("+request.getRequestURI()+")", e);
            e.printStackTrace();
            throw e;
        } catch (DataFordelerException e) {
            e.printStackTrace();
            this.log.error("Error in REST search ("+request.getRequestURI()+")", e);
            throw e;
        } finally {
            session.close();
        }
        return envelope;
    }

    /**
     * Handle a lookup-by-parameters request in REST, outputting CSV text.
     * @see #searchRest(MultiValueMap, HttpServletRequest)
     */
    @WebMethod(exclude = true)
    @RequestMapping(path="/search", produces = {
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
        } catch (AccessDeniedException|AccessRequiredException|InvalidClientInputException|HttpNotFoundException|InvalidTokenException e) {
            this.log.warn("Error in REST CSV search ("+request.getRequestURI()+")", e);
            throw e;
        } catch (DataFordelerException e) {
            e.printStackTrace();
            this.log.error("Error in REST CSV search ("+request.getRequestURI()+")", e);
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * Handle a lookup-by-parameters request in SOAP. This method is called by the Servlet
     * @param query Query object specifying search parameters
     * @return Found Entities
     */
    // TODO: How to use DafoUserDetails with SOAP requests?
    @WebMethod(operationName = "search")
    public Envelope searchSoap(@WebParam(name="query") @XmlElement(required = true) Q query) throws DataFordelerException {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Envelope envelope = new Envelope();
        try {
            MessageContext messageContext = context.getMessageContext();
            HttpServletRequest request = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
            DafoUserDetails user = this.getDafoUserManager().getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.info(
                    "Incoming SOAP request for " + this.getServiceName() + " with query " + query.toString()
            );
            this.checkAndLogAccess(loggerHelper);
            envelope.addQueryData(query);
            envelope.addUserData(user);
            envelope.addRequestData(request);
            List<E> results = this.searchByQuery(query, session);
            if (this.getOutputWrapper() != null) {
                envelope.setResult(this.getOutputWrapper().wrapResults(results, query, query.getMode(this.getDefaultMode())));
            } else {
                envelope.setResults(results);
            }
            envelope.close();
            loggerHelper.logResult(envelope, query.toString());
        } catch (AccessDeniedException|AccessRequiredException|InvalidClientInputException|HttpNotFoundException|InvalidTokenException e) {
            this.log.warn("Error in SOAP search", e);
            throw e;
        } catch (DataFordelerException e) {
            e.printStackTrace();
            this.log.error("Error in SOAP search", e);
            throw e;
        } finally {
            session.close();
        }
        return envelope;
    }


    /**
     * Obtain an empty Query instance of the correct subclass
     * @return Query subclass instance
     */
    protected abstract Q getEmptyQuery();

    /**
     * Parse a map of URL parameters into a Query object of the correct subclass
     * @param parameters URL parameters received in a request
     * @return Query subclass instance
     */
    protected Q getQuery(MultiValueMap<String, String> parameters, boolean limitsOnly) throws InvalidClientInputException {
        Q query = this.getEmptyQuery();
        ParameterMap parameterMap = new ParameterMap(parameters);
        query.fillFromParameters(parameterMap, limitsOnly);
        return query;
    }

    protected void applyAreaRestrictionsToQuery(Q query, DafoUserDetails user) throws InvalidClientInputException {
        return;
    }


    /**
     * Perform a search for Entities by a Query object
     * @param query Query objects to search by
     * @return Found Entities
     */
    //@WebMethod(exclude = true)
    //protected abstract Set<E> searchByQuery(Q query);
    @WebMethod(exclude = true) // Non-soap methods must have this
    protected List<E> searchByQuery(Q query, Session session) throws DataFordelerException {
        query.applyFilters(session);
        return QueryManager.getAllEntities(
                session, query,
                this.getEntityClass()
        );
    }

    /**
     * Perform a search for Entities by a Query object
     * @param query Query objects to search by
     * @return Found Entities
     */
    //@WebMethod(exclude = true)
    //protected abstract Set<E> searchByQuery(Q query);
    @WebMethod(exclude = true) // Non-soap methods must have this
    protected Stream<E> searchByQueryAsStream(Q query, Session session) throws
        DataFordelerException {
        query.applyFilters(session);
        return QueryManager.getAllEntitiesAsStream(
                session, query,
                this.getEntityClass()
        );
    }

    /**
     * Perform a search for Entities by id
     * @param id Identifier to search by. Must be parseable as a UUID
     * @param query Query object modifying the output (such as a bitemporal range)
     * @return Found Entities
     */
    @WebMethod(exclude = true)
    protected E searchById(String id, Q query, Session session) {
        return this.searchById(UUID.fromString(id), query, session);
    }


    /**
     * Perform a search for Entities by id
     * @param uuid Identifier to search by
     * @param query Query object modifying the output (such as a bitemporal range)
     * @return Found Entities
     */
    @WebMethod(exclude = true)
    protected E searchById(UUID uuid, Q query, Session session) {
        query.applyFilters(session);
        E entity = QueryManager.getEntity(session, uuid, this.getEntityClass());
        if (entity != null) {
            entity.forceLoad(session);
        }
        return entity;
    }

    protected abstract void sendAsCSV(Stream<E> entities, HttpServletRequest request, HttpServletResponse response) throws IOException, HttpNotFoundException;
}
