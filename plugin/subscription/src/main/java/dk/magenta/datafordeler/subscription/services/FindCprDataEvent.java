package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CprList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.DataEventSubscription;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCprNumber;
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

import static java.util.Comparator.naturalOrder;


@RestController
@RequestMapping("/subscription/1/findCprDataEvent")
public class FindCprDataEvent {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private RecordMetadataWrapper personRecordOutputWrapper;


    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(FindCprDataEvent.class.getCanonicalName());


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

            String hql = "SELECT max(event.timestamp) FROM "+ PersonDataEventDataRecord.class.getCanonicalName()+" event ";
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
                if(!"cpr".equals(subscribtionKodeId[0]) && !"dataevent".equals(subscribtionKodeId[1])) {
                    String errorMessage = "No access";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.warn(errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
                }

                PersonRecordQuery query = GeneralQuery.getPersonQuery(subscribtionKodeId[2], offsetTimestampGTE, offsetTimestampLTE);
                CprList cprList = subscribtion.getCprList();
                if(cprList!=null) {
                    Collection<SubscribedCprNumber> theList = cprList.getCpr();
                    List<String> pnrFilterList = theList.stream().map(x -> x.getCprNumber()).collect(Collectors.toList());
                    query.setPersonnumre(pnrFilterList);
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
                List<ResultSet<PersonEntity>> entities = QueryManager.getAllEntitySets(session, query, PersonEntity.class);
                List otherList = new ArrayList<ObjectNode>();

                if(includeMeta) {
                    for(ResultSet<PersonEntity> entity : entities) {
                        CprBitemporalPersonRecord oldValues = null;
                        CprBitemporalPersonRecord newValues = getActualValueRecord(subscribtionKodeId[2], entity.getPrimaryEntity());
                        PersonDataEventDataRecord eventRecord = entity.getPrimaryEntity().getDataEvent(subscribtionKodeId[2]);
                        if(eventRecord != null) {
                            if(eventRecord.getOldItem() != null) {
                                String queryPreviousItem = GeneralQuery.getQueryPersonValueObjectFromIdInEvent(subscribtionKodeId[2]);
                                oldValues = (CprBitemporalPersonRecord)session.createQuery(queryPreviousItem).setParameter("id", eventRecord.getOldItem().longValue()).getResultList().get(0);
                            }
                        }

                        if(subscribtionKodeId.length>=5) {
                            if(subscribtionKodeId[3].equals("before")) {
                                if(this.validateIt(subscribtionKodeId[2], subscribtionKodeId[4], oldValues)) {
                                    ObjectNode node = personRecordOutputWrapper.fillContainer(entity.getPrimaryEntity().getPersonnummer(), subscribtionKodeId[2], oldValues, newValues);
                                    otherList.add(node);
                                }
                            } else if(subscribtionKodeId[3].equals("after")) {
                                if(this.validateIt(subscribtionKodeId[2], subscribtionKodeId[4], newValues)) {
                                    ObjectNode node = personRecordOutputWrapper.fillContainer(entity.getPrimaryEntity().getPersonnummer(), subscribtionKodeId[2], oldValues, newValues);
                                    otherList.add(node);
                                }
                            }
                        } else {
                            ObjectNode node = personRecordOutputWrapper.fillContainer(entity.getPrimaryEntity().getPersonnummer(), subscribtionKodeId[2], oldValues, newValues);
                            otherList.add(node);
                        }
                    }
                } else {
                    otherList = entities.stream().map(x -> x.getPrimaryEntity().getPersonnummer()).collect(Collectors.toList());
                }
                Envelope envelope = new Envelope();

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

    private CprBitemporalPersonRecord getActualValueRecord(String fieldname, PersonEntity personEntity) {

        switch(fieldname) {
            case NameDataRecord.TABLE_NAME:
                return personEntity.getName().current().get(0);
            case AddressDataRecord.TABLE_NAME:
                return personEntity.getAddress().current().get(0);
            case AddressConameDataRecord.TABLE_NAME:
                return personEntity.getConame().current().get(0);
            case AddressNameDataRecord.TABLE_NAME:
                return personEntity.getAddressName().current().get(0);
            case CitizenshipDataRecord.TABLE_NAME:
                return personEntity.getCitizenship().current().get(0);
            case CivilStatusDataRecord.TABLE_NAME:
                return personEntity.getCivilstatus().current().get(0);
            default:
                return null;
        }
    }

    private boolean validateIt(String fieldname, String logic, CprBitemporalPersonRecord personEntity) {

        if(AddressDataRecord.TABLE_NAME.equals(fieldname)) {
            String[] splitLogic = logic.split("=");
            if(personEntity!=null && "kommunekode".equals(splitLogic[0])) {
                return ((AddressDataRecord)personEntity).getMunicipalityCode() == Integer.parseInt(splitLogic[1]);
            }
        }
        return false;
    }

    private static Comparator bitemporalComparator = Comparator.comparing(FindCprDataEvent::getBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprNontemporalRecord::getOriginDate, Comparator.nullsLast(naturalOrder()))
            .thenComparing(CprNontemporalRecord::getDafoUpdated)
            .thenComparing(DatabaseEntry::getId);


    public static CprBitemporality getBitemporality(CprBitemporalRecord record) {
        return record.getBitemporality();
    }

    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestUnclosedOnRegistartionAndEffect(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null &&
                r.getBitemporality().effectTo == null).max(bitemporalComparator).orElse(null);
    }
}