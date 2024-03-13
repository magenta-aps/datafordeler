package dk.magenta.datafordeler.cvr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.fapi.RecordOutputWrapper;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.output.ParticipantRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cvr/owners/")
public class CompanyOwnersService {

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    private final Logger log = LogManager.getLogger(CompanyOwnersService.class.getCanonicalName());

    @Autowired
    ParticipantRecordOutputWrapper participantRecordOutputWrapper;

    private static final Map<String, Pair<String, String>> intervalMap;
    static {
        HashMap<String, Pair<String, String>> map = new HashMap<>();
        map.put("0.05", Pair.of("0.05", "0.0999"));
        map.put("0.10", Pair.of("0.1", "0.1499"));
        map.put("0.15", Pair.of("0.15", "0.1999"));
        map.put("0.2", Pair.of("0.20", "0.2499"));
        map.put("0.25", Pair.of("0.25", "0.3333"));
        map.put("0.3333", Pair.of("0.3334", "0.4999"));
        map.put("0.5", Pair.of("0.5", "0.6665"));
        map.put("0.6667", Pair.of("0.6666", "0.8999"));
        map.put("0.9", Pair.of("0.9", "0.9999"));
        map.put("1.0", Pair.of("1", "1"));
        intervalMap = Collections.unmodifiableMap(map);
    }



    @RequestMapping(
            path = {"/{cvr}"},
            produces = {"application/json"}
    )
    public String getRest(@PathVariable("cvr") String cvr, HttpServletRequest request) throws DataFordelerException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        loggerHelper.info("Incoming request for owners of cvr " + cvr);
        //this.checkAndLogAccess(loggerHelper);
        Session session = sessionManager.getSessionFactory().openSession();

        OffsetDateTime now = OffsetDateTime.now();
        CompanyRecordQuery companyRecordQuery = new CompanyRecordQuery();
        companyRecordQuery.setParameter(CompanyRecordQuery.CVRNUMMER, cvr);

        companyRecordQuery.setRegistrationAt(now);
        companyRecordQuery.setEffectAt(now);
        companyRecordQuery.applyFilters(session);
        companyRecordQuery.setPage(1);
        companyRecordQuery.setPageSize(1);
        ObjectNode root = this.objectMapper.createObjectNode();


        List<CompanyRecord> companyRecords = QueryManager.getAllEntities(session, companyRecordQuery, CompanyRecord.class);
        if (companyRecords != null && !companyRecords.isEmpty()) {
            CompanyRecord companyRecord = companyRecords.get(0);

            ArrayNode legaleEjere = objectMapper.createArrayNode();
            ArrayNode reelleEjere = objectMapper.createArrayNode();

            root.set("legale_ejere", legaleEjere);
            root.set("reelle_ejere", reelleEjere);

            System.out.println("Der er "+companyRecord.getParticipants().current().size()+" deltagere");
            companyRecord.getParticipants().currentStream().forEach(relationRecord -> {

                boolean erLegalEjer = false;
                boolean erReelEjer = false;

                    // Ifølge Ejerforhold_Doc.png
                    String ejerandel = null;
                    String stemmeret = null;
                    HashSet<String> særligeEjerforhold = new HashSet<>();
                    for (OrganizationRecord organizationRecord : relationRecord.getOrganizations()) {
                        if (organizationRecord.getMainType().equals("REGISTER")) {
                            Set<String> currentOrganizationNames = organizationRecord.getNames().current().stream().map(n -> n.getName()).collect(Collectors.toSet());
                            Set<String> currentOrganizationFunctions = organizationRecord.getAttributes().getCurrentAttributeValues("FUNKTION", "string").stream().map(String::valueOf).collect(Collectors.toSet());

                            for (OrganizationMemberdataRecord memberdataRecord : organizationRecord.getMemberData()) {
                                String memberEjerandel = memberdataRecord.getAttributes().getCurrentAttributeValues("EJERANDEL_PROCENT", "decimal", false).stream().map(String::valueOf).findFirst().orElse(null);
                                if (memberEjerandel != null) {
                                    ejerandel = memberEjerandel;
                                }
                                String memberStemmeret = memberdataRecord.getAttributes().getCurrentAttributeValues("EJERANDEL_STEMMERET_PROCENT", "decimal", false).stream().map(String::valueOf).findFirst().orElse(null);
                                if (memberStemmeret != null) {
                                    stemmeret = memberStemmeret;
                                }
                                særligeEjerforhold.addAll(memberdataRecord.getAttributes().getCurrentAttributeValues("SÆRLIGE_EJERFORHOLD", "string").stream().map(String::valueOf).collect(Collectors.toSet()));

                            }


                            // Legale ejere
                            if (currentOrganizationNames.contains("EJERREGISTER") || currentOrganizationFunctions.contains("EJERREGISTER")) {
                                if ((ejerandel != null && Float.parseFloat(ejerandel) >= 0.05) || (stemmeret != null && Float.parseFloat(stemmeret) >= 0.05)) {
                                    erLegalEjer = true;
                                }
                            }

                            // Reelle ejere
                            if (currentOrganizationNames.contains("Reelle ejere") || currentOrganizationFunctions.contains("Reelle ejere")) {
                                erReelEjer = true;
                            }
                        }
                    }


                    if (erLegalEjer || erReelEjer) {
                        ObjectNode ejer = objectMapper.createObjectNode();

                        RelationParticipantRecord participantRecord = relationRecord.getRelationParticipantRecord();
                        ejer.put(
                                "deltager",
                                objectMapper.setFilterProvider(this.getFilterProvider()).valueToTree(participantRecord)
                        );

                        ejer.put("ejerandel", ejerandel);
                        if (erLegalEjer && ejerandel != null) {
                            System.out.println("ejerandel: "+ejerandel);
                            Pair<String, String> ejerandelRange = intervalMap.get(ejerandel);
                            if (ejerandelRange != null) {
                                ObjectNode ejerandelObject = objectMapper.createObjectNode();
                                ejerandelObject.put("fra", ejerandelRange.getFirst());
                                ejerandelObject.put("til", ejerandelRange.getSecond());
                                ejer.set("ejerandel", ejerandelObject);
                            }
                        }

                        if (!særligeEjerforhold.isEmpty()) {
                            ArrayNode ejerforhold = objectMapper.createArrayNode();
                            for (String e : særligeEjerforhold) {
                                ejerforhold.add(e);
                            }
                            ejer.put("særlige_ejerforhold", ejerforhold);
                        }

                        if (erLegalEjer) {
                            legaleEjere.add(ejer);
                        }
                        if (erReelEjer) {
                            reelleEjere.add(ejer);
                        }
                    }
            });
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new DataStreamException(e);
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    private void applyFilter(Session session, String filterName, String parameterName, Object parameterValue) {
        if (session.getSessionFactory().getDefinedFilterNames().contains(filterName)) {
            session.enableFilter(filterName).setParameter(
                    parameterName,
                    parameterValue
            );
        }
    }

    protected FilterProvider getFilterProvider() {
        return new SimpleFilterProvider().addFilter(
                "ParticipantRecordFilter",
                SimpleBeanPropertyFilter.serializeAllExcept(ParticipantRecord.IO_FIELD_BUSINESS_KEY)
        );
    }
}
