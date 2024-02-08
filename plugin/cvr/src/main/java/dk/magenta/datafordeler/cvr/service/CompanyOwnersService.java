package dk.magenta.datafordeler.cvr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private static final String LEGALE_EJERE = "LEGALE EJERE";
    private static final String REELLE_EJERE = "REELLE EJERE";


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
        this.applyFilter(session, Bitemporal.FILTER_EFFECTFROM_BEFORE, Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, now);
        this.applyFilter(session, Bitemporal.FILTER_EFFECTTO_AFTER, Bitemporal.FILTERPARAM_EFFECTTO_AFTER, now);

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
            System.out.println("Fandt en CompanyRecord");

            HashMap<Long, ParticipantRecord> participantMap = new HashMap<>();
            HashSet<CompanyParticipantRelationRecord> legaleEjere = new HashSet<>();
            HashSet<CompanyParticipantRelationRecord> reelleEjere = new HashSet<>();
            HashSet<CompanyParticipantRelationRecord> fuldtAnsvarligeDeltagere = new HashSet<>();


            boolean virksomhedErOphoert = companyRecord.getLifecycle().current().isEmpty();
            boolean erVirksomhedErhvervsdrivendeFonde = Objects.equals(companyRecord.getCompanyForm().getFirst(true, true).getCompanyFormCode(), "100");

            System.out.println("Der er "+companyRecord.getParticipants().current().size()+" deltagere");
            companyRecord.getParticipants().currentStream().forEach(relationRecord -> {
                System.out.println("Undersøger deltager "+relationRecord.getParticipantUnitNumber());

                boolean useGroovyCode = false;
                boolean erLegalEjer = false;
                boolean erReelEjer = false;


                if (useGroovyCode) {
                    boolean harVirksomhedReelEjerOrganisation = relationRecord.getOrganizations().stream().anyMatch(o -> Objects.equals(o.getMainType(), REELLE_EJERE));

                    boolean harVirksomhedTagetstillingTilReelleEjere = harVirksomhedReelEjerOrganisation;
                    boolean harVirksomhedAktiveReelleEjere = relationRecord.getOrganizations().stream().anyMatch(o -> /*o.active && */Objects.equals(o.getMainType(), REELLE_EJERE));
                    boolean erVirksomhedBoersnoteret = companyRecord.getAttributes().stream().anyMatch(
                            a -> Objects.equals(a.getType(), "BØRSNOTERET")
                                    && a.getValues().stream().anyMatch(v -> v.getBitemporality().isCurrent() && v.getValue().equalsIgnoreCase("true"))
                    );

                    boolean bestyrelseAnsesSomReelleEjere = harVirksomhedReelEjerOrganisation &&
                            erVirksomhedErhvervsdrivendeFonde &&
                            harVirksomhedTagetstillingTilReelleEjere &&
                            !virksomhedErOphoert;

                    boolean virksomhedHarIkkeReelleEjereOgLedelseErIndsat = harVirksomhedReelEjerOrganisation &&
                            !harVirksomhedAktiveReelleEjere &&
                            !erVirksomhedErhvervsdrivendeFonde &&
                            !erVirksomhedBoersnoteret &&
                            !relationRecord.getOrganizations().stream().anyMatch(o -> o.getAttributes().getCurrentAttributeValues("KAN_IKKE_IDENTIFICERE_REELLE_EJERE", "boolean").contains(Boolean.TRUE)) &&
                            !virksomhedErOphoert;

                    boolean virksomhedHarIkkeKunnetIdentificereReelleEjereLedelseErIndsat = harVirksomhedReelEjerOrganisation &&
                                !harVirksomhedAktiveReelleEjere &&
                                !erVirksomhedErhvervsdrivendeFonde &&
                                !erVirksomhedBoersnoteret &&
                                relationRecord.getOrganizations().stream().anyMatch(o -> o.getAttributes().getCurrentAttributeValues("KAN_IKKE_IDENTIFICERE_REELLE_EJERE", "boolean").contains(Boolean.TRUE)) &&
                                !virksomhedErOphoert;

                    erReelEjer = relationRecord.getOrganizations().stream().anyMatch(o -> Objects.equals(o.getMainType(), REELLE_EJERE));

                    for (OrganizationRecord organizationRecord : relationRecord.getOrganizations()) {
                        String type = organizationRecord.getMainType();
                        if (Objects.equals(type, LEGALE_EJERE)) {
                            System.out.println("Er i LEGALE_EJERE");
                            erLegalEjer = true;
                        }
                        if (Objects.equals(type, REELLE_EJERE)) {
                            erReelEjer = true;  // er i aktiveReelleEjere
                            System.out.println("Er i REELLE_EJERE");
                        }
                        if (organizationRecord.getAttributes().getCurrentAttributeValues("FUNKTION", "string").contains("Reelle ejere")) {
                            erReelEjer = true;
                            System.out.println("Er i en organisation er har FUNKTION='Reelle Ejere'");
                        }
                    }


                    if (!erReelEjer) {
                        // Se om andre data gør personen til reel ejer

                    }

                } else {

                    boolean person = Objects.equals(relationRecord.getRelationParticipantRecord().getUnitType(), "PERSON");
                    boolean virksomhed = Objects.equals(relationRecord.getRelationParticipantRecord().getUnitType(), "VIRKSOMHED");


                    // Ifølge Ejerforhold_Doc.png
                    for (OrganizationRecord organizationRecord : relationRecord.getOrganizations()) {
                        Set<String> currentOrganizationNames = organizationRecord.getNames().current().stream().map(n -> n.getName()).collect(Collectors.toSet());

                        // Tjek Ejerregister
                        if (currentOrganizationNames.contains("EJERREGISTER")) {
                            for (OrganizationMemberdataRecord organizationMemberdataRecord : organizationRecord.memberData) {
                                for (AttributeRecord attributeRecord : organizationMemberdataRecord.attributes) {
                                    if (Objects.equals(attributeRecord.getType(), "EJERANDEL_PROCENT")) {
                                        for (AttributeValueRecord value : attributeRecord.getValues()) {
                                            if (value.getBitemporality().isCurrent()) {
                                                float ejerandel = Float.parseFloat(value.getValue());
                                                if (ejerandel >= 0.05) {
                                                    System.out.println("Har ejerandel over 5%");
                                                    erLegalEjer = true;
                                                }
                                                if (person && ejerandel > 0.25) {
                                                    System.out.println("Har ejerandel over 25%");
                                                    erReelEjer = true;
                                                }
                                            }
                                        }
                                    }
                                    if (person && Objects.equals(attributeRecord.getType(), "EJERANDEL_STEMMERET_PROCENT")) {
                                        for (AttributeValueRecord value : attributeRecord.getValues()) {
                                            if (value.getBitemporality().isCurrent()) {
                                                float ejerandel = Float.parseFloat(value.getValue());
                                                if (ejerandel > 0.25) {
                                                    System.out.println("Har stemmeret over 25%");
                                                    erReelEjer = true;
                                                }
                                            }
                                        }
                                    }
                                    // TODO: Har anden afgørende kontrol, fx. vetoret, ret til at udpege bestyrelsesmedlemmer el. lign.
                                    // erReelEjer = true;
                                }
                            }
                        }

                        // Tjek Register
                        if (organizationRecord.getMainType().equalsIgnoreCase("REGISTER") && currentOrganizationNames.contains("Reelle ejere")) {
                            erReelEjer = true;
                        }
                    }

                }

                if (erReelEjer) {
                    reelleEjere.add(relationRecord);
                }
                if (erLegalEjer) {
                    legaleEjere.add(relationRecord);
                }


                long participantUnitNumber = relationRecord.getParticipantUnitNumber();
                participantMap.put(participantUnitNumber, null);
            });
            ParticipantRecordQuery participantRecordQuery = new ParticipantRecordQuery();
            participantRecordQuery.setParameter(
                    ParticipantRecordQuery.UNITNUMBER,
                    participantMap.keySet().stream().map(Object::toString).collect(Collectors.toList())
            );

            for (ParticipantRecord participantRecord : QueryManager.getAllEntities(session, participantRecordQuery, ParticipantRecord.class)) {
                participantMap.put(participantRecord.getUnitNumber(), participantRecord);
            }

            root.set(
                    "legaleEjere",
                    outputParticipantList(participantMap, legaleEjere, participantRecordQuery, participantRecordOutputWrapper)
            );
            root.set(
                    "reelleEjere",
                    outputParticipantList(participantMap, reelleEjere, participantRecordQuery, participantRecordOutputWrapper)
            );
            root.set(
                    "fuldtAnsvarligeDeltagere",
                    outputParticipantList(participantMap, fuldtAnsvarligeDeltagere, participantRecordQuery, participantRecordOutputWrapper)
            );
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new DataStreamException(e);
        }
    }

    private ArrayNode outputParticipantList(HashMap<Long, ParticipantRecord> participantMap, HashSet<CompanyParticipantRelationRecord> participantRelationRecords, ParticipantRecordQuery participantRecordQuery, ParticipantRecordOutputWrapper participantRecordOutputWrapper) {
        ArrayNode output = this.objectMapper.createArrayNode();
        for (CompanyParticipantRelationRecord relationRecord : participantRelationRecords) {
            ParticipantRecord participantRecord = participantMap.get(relationRecord.getParticipantUnitNumber());
            if (participantRecord == null) {
                ObjectNode node = this.objectMapper.createObjectNode();
                node.put(ParticipantRecord.IO_FIELD_UNIT_NUMBER, relationRecord.getParticipantUnitNumber());
                output.add(node);
            } else {
                /*output.add(
                        (ObjectNode) participantRecordOutputWrapper.wrapResult(
                                participantRecord,
                                participantRecordQuery,
                                OutputWrapper.Mode.DATAONLY
                        )
                );*/
                output.add(participantRecord.getBusinessKey());
            }
        }
        return output;
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
}
