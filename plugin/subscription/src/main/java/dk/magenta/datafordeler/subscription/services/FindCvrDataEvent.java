package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import dk.magenta.datafordeler.subscription.queries.GeneralQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/subscription/1/findCvrDataEvent")
public class FindCvrDataEvent {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private RecordMetadataWrapper personRecordOutputWrapperStuff;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(FindCvrDataEvent.class.getCanonicalName());


    @PostConstruct
    public void init() {

    }


    /**
     * Get a list of all subscribtions
     * @return
     */
    @GetMapping("/fetchEvents")
    public ResponseEntity<Envelope> findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String dataEventId = requestParams.getFirst("subscription");
        String timestampGTE = requestParams.getFirst("timestamp.GTE");
        String timestampLTE = requestParams.getFirst("timestamp.LTE");
        Boolean includeMeta = Boolean.parseBoolean(requestParams.getFirst("includeMeta"));
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("fetchEvents");

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            this.checkAndLogAccess(loggerHelper);

            String hql = "SELECT max(event.timestamp) FROM "+ CompanyDataEventRecord.class.getCanonicalName()+" event ";
            Query timestampQuery = session.createQuery(hql);
            OffsetDateTime newestEventTimestamp = (OffsetDateTime)timestampQuery.getResultList().get(0);

            Query eventQuery = session.createQuery(" from "+ DataEventSubscription.class.getName() +" where dataEventId = :dataEventId", DataEventSubscription.class);
            eventQuery.setParameter("dataEventId", dataEventId);
            if(eventQuery.getResultList().size()==0) {
                String errorMessage = "Subscription not found";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                DataEventSubscription subscribtion = (DataEventSubscription) eventQuery.getResultList().get(0);
                if(!subscribtion.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                    String errorMessage = "No access";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.warn(errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
                }
                OffsetDateTime offsetTimestampGTE;
                if(timestampGTE==null) {
                    offsetTimestampGTE = OffsetDateTime.of(0,1,1,1,1,1,1, ZoneOffset.ofHours(0));
                } else {
                    offsetTimestampGTE = dk.magenta.datafordeler.core.fapi.Query.parseDateTime(timestampGTE);
                }

                OffsetDateTime offsetTimestampLTE=null;
                if(timestampLTE!=null) {
                    offsetTimestampLTE = dk.magenta.datafordeler.core.fapi.Query.parseDateTime(timestampLTE);
                }

                //TODO: dette skal oprettes med opsplitning i forskellige attributter med betydning
                String[] subscribtionKodeId = subscribtion.getKodeId().split("[.]");
                if(!"cvr".equals(subscribtionKodeId[0]) && !"dataevent".equals(subscribtionKodeId[1])) {
                    String errorMessage = "No access";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.warn(errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
                }

                CompanyRecordQuery query = GeneralQuery.getCompanyQuery(subscribtionKodeId[2], offsetTimestampGTE, offsetTimestampLTE);
                CvrList cvrList = subscribtion.getCvrList();
                if(cvrList!=null) {
                    Collection<SubscribedCvrNumber> theList = cvrList.getCvr();
                    List<String> pnrFilterList = theList.stream().map(x -> x.getCvrNumber()).collect(Collectors.toList());
                    query.setCvrNumre(pnrFilterList);
                }

                query.setPageSize(pageSize);
                if(query.getPageSize()>1000) {
                    String errorMessage = "No access";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.warn(errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
                }
                query.setPage(page);
                List<ResultSet<CompanyRecord>> entities = QueryManager.getAllEntitySets(session, query, CompanyRecord.class);
                Envelope envelope = new Envelope();

                List otherList = new ArrayList<ObjectNode>();
                if(includeMeta) {
                    for(ResultSet<CompanyRecord> entity : entities) {
                        CvrBitemporalDataRecord oldValues = null;
                        CvrBitemporalDataRecord newValues = getActualValueRecord(subscribtionKodeId[2], entity.getPrimaryEntity());
                        Set<CompanyDataEventRecord> events = entity.getPrimaryEntity().getDataevent();
                        if(events.size()>0) {
                            CompanyDataEventRecord eventRecord = events.iterator().next();
                            if(eventRecord.getOldItem() != null) {
                                String queryPreviousItem = GeneralQuery.getQueryCompanyValueObjectFromIdInEvent(subscribtionKodeId[2]);
                                if(eventRecord.getOldItem()!=null) {
                                    oldValues = (CvrBitemporalDataMetaRecord)session.createQuery(queryPreviousItem).setParameter("id", eventRecord.getOldItem()).getResultList().get(0);
                                }
                            }
                        }

                        if(subscribtionKodeId.length>=5) {
                            if(subscribtionKodeId[3].equals("before")) {
                                if(this.validateIt(subscribtionKodeId[2], subscribtionKodeId[4], oldValues)) {
                                    ObjectNode node = personRecordOutputWrapperStuff.fillContainer(entity.getPrimaryEntity().getCvrNumberString(), subscribtionKodeId[2], oldValues, newValues);
                                    otherList.add(node);
                                }
                            } else if(subscribtionKodeId[3].equals("after")) {
                                if(this.validateIt(subscribtionKodeId[2], subscribtionKodeId[4], newValues)) {
                                    ObjectNode node = personRecordOutputWrapperStuff.fillContainer(entity.getPrimaryEntity().getCvrNumberString(), subscribtionKodeId[2], oldValues, newValues);
                                    otherList.add(node);
                                }
                            }
                        } else {
                            ObjectNode node = personRecordOutputWrapperStuff.fillContainer(entity.getPrimaryEntity().getCvrNumberString(),subscribtionKodeId[2], oldValues, newValues);
                            otherList.add(node);
                        }
                    }
                } else {
                    otherList = entities.stream().map(x -> x.getPrimaryEntity().getCvrNumberString()).collect(Collectors.toList());
                }

                envelope.setResults(otherList);
                envelope.setNewestResultTimestamp(newestEventTimestamp);
                loggerHelper.urlInvokePersistablelogs("fetchEvents done");
                return ResponseEntity.ok(envelope);
            }
        } catch (AccessRequiredException e) {
            String errorMessage = "No access to this information";
            ObjectNode obj = this.objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.warn(errorMessage);
            return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
        } catch(Exception e) {
            log.error("Failed pulling events from subscribtion", e);
        }
        return ResponseEntity.status(500).build();
    }


    private boolean validateIt(String fieldname, String logic, CvrBitemporalDataRecord companyEntity) {

        if(AddressRecord.TABLE_NAME.equals(fieldname)) {
            String[] splitLogic = logic.split("=");
            if("kommunekode".equals(splitLogic[0])) {
                return ((AddressRecord)companyEntity).getMunicipality().getMunicipalityCode() == Integer.parseInt(splitLogic[1]);
            }
        }
        return false;
    }


    private CvrBitemporalDataRecord getActualValueRecord(String fieldname, CompanyRecord companyEntity) {

        switch(fieldname) {
            case BaseNameRecord.TABLE_NAME:
                return companyEntity.getNames().stream().reduce((first, second) -> second).orElse(null);
            case AddressRecord.TABLE_NAME:
                return companyEntity.getPostalAddress().stream().reduce((first, second) -> second).orElse(null);
            case StatusRecord.TABLE_NAME:
                return companyEntity.getStatus().stream().reduce((first, second) -> second).orElse(null);
            default:
                return null;
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }
}