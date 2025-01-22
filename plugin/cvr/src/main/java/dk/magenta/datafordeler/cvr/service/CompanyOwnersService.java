package dk.magenta.datafordeler.cvr.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.output.ParticipantRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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

    @PostConstruct
    public void addObjectSerializer() {
        SimpleModule module = new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanSerializerModifier(new BeanSerializerModifier() {
                    // Serialize only objects with current effect
                    @Override
                    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription desc, JsonSerializer<?> serializer) {
                        if (CvrRecord.class.isAssignableFrom(desc.getBeanClass())) {
                            return new FilterSerializer((JsonSerializer<Object>) serializer, cvrRecord -> {
                                if (cvrRecord instanceof CvrBitemporalRecord) {
                                    CvrBitemporalRecord bitemporalRecord = (CvrBitemporalRecord) cvrRecord;
                                    OffsetDateTime now = OffsetDateTime.now();
                                    return bitemporalRecord.hasEffectAt(now);
                                }
                                return true;
                            });
                        }
                        return serializer;
                    }
                });
            }
        };
        // Serialize only objects in sets that hold the latest registration among objects that are effetive now
        module.addSerializer(Set.class, new StdSerializer<>(Set.class) {
            @Override
            public void serialize(Set value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                OffsetDateTime now = OffsetDateTime.now();
                jsonGenerator.writeStartArray();
                if (!value.isEmpty()) {
                    HashSet<CvrBitemporalRecord> filterSet = new HashSet<>();
                    for (Object item : value) {
                        if (item instanceof CvrBitemporalRecord) {
                            CvrBitemporalRecord cvrBitemporalRecord = (CvrBitemporalRecord) item;
                            if (cvrBitemporalRecord.hasEffectAt(now)) {
                                filterSet.add(cvrBitemporalRecord);
                            }
                        }
                    }
                    if (filterSet.isEmpty()) {
                        for (Object item : value) {
                            jsonGenerator.writeObject(item);
                        }
                    } else {
                        OffsetDateTime last = filterSet.stream().max(
                                Comparator.comparing(CvrBitemporalRecord::getRegistrationFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                        ).get().getRegistrationFrom();
                        List<CvrBitemporalRecord> filtered = filterSet.stream().filter(
                                i -> Equality.equal(i.getRegistrationFrom(), last)
                        ).sorted(
                                Comparator.comparing(CvrBitemporalRecord::getEffectFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                        ).collect(Collectors.toList());
                        for (Object item : filtered) {
                            jsonGenerator.writeObject(item);
                        }
                    }
                }
                jsonGenerator.writeEndArray();
            }
        });
        this.objectMapper.registerModule(module);
    }

    @RequestMapping(
            path = {"/{cvr}"},
            produces = {"application/json"}
    )
    public String getRest(@PathVariable("cvr") String cvr, HttpServletRequest request) throws DataFordelerException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        loggerHelper.info("Incoming request for owners of cvr " + cvr);
        this.checkAndLogAccess(loggerHelper);
        try (Session lookupSession = sessionManager.getSessionFactory().openSession()) {
            try (Session participantSession = sessionManager.getSessionFactory().openSession()) {

                OffsetDateTime now = OffsetDateTime.now();
                CompanyRecordQuery companyRecordQuery = new CompanyRecordQuery();
                companyRecordQuery.setParameter(CompanyRecordQuery.CVRNUMMER, cvr);

                // Get items that are registered now, but with all effects
                // We need to find the latest registration and then filter effects
                // but registrationTo is not set by source data, so we can't filter on that yet
                // so the logic is:
                //     first exclude attributes that are older than the latest registration
                //     then exclude attributes that are not effective now
                companyRecordQuery.setRegistrationAt(now);
                companyRecordQuery.applyFilters(lookupSession);
                companyRecordQuery.setPage(1);
                companyRecordQuery.setPageSize(1);
                ObjectNode root = this.objectMapper.createObjectNode();



                List<CompanyRecord> companyRecords = QueryManager.getAllEntities(lookupSession, companyRecordQuery, CompanyRecord.class);
                if (companyRecords != null && !companyRecords.isEmpty()) {
                    CompanyRecord companyRecord = companyRecords.get(0);

                    ArrayNode legaleEjere = objectMapper.createArrayNode();
                    ArrayNode reelleEjere = objectMapper.createArrayNode();

                    root.set("legale_ejere", legaleEjere);
                    root.set("reelle_ejere", reelleEjere);

                    HashMap<Long, ParticipantRecord> participantCache = new HashMap<>();

                    companyRecord.getParticipants().currentStream().forEach(relationRecord -> {
                        for (OrganizationRecord organizationRecord : relationRecord.getOrganizations()) {
                            if (organizationRecord.getMainType().equals("REGISTER")) {

                                Map<Bitemporality, String> ejerandel = new HashMap<>();
                                Map<Bitemporality, String> stemmeret = new HashMap<>();
                                Map<Bitemporality, String> særligeEjerforhold = new HashMap<>();
                                Set<String> organizationNames = organizationRecord.getNames().stream().map(n -> n.getName()).collect(Collectors.toSet());
                                Set<String> organizationFunctions = organizationRecord.getAttributes().stream()
                                        .filter(attributeRecord -> attributeRecord.getType().equals("FUNKTION") && attributeRecord.getValueType().equals("string"))
                                        .flatMap(attributeRecord -> attributeRecord.getValues().stream())
                                        .map(String::valueOf).collect(Collectors.toSet());
                                Map<Bitemporality, String> memberFunctions = new HashMap<>();

                                for (OrganizationMemberdataRecord memberdataRecord : organizationRecord.getMemberData()) {

                                    ejerandel.putAll(memberdataRecord.getAttributes().stream()
                                            .filter(attributeRecord -> attributeRecord.getType().equals("EJERANDEL_PROCENT") && attributeRecord.getValueType().equals("decimal"))
                                            .flatMap(attributeRecord -> attributeRecord.getValues().stream())
                                            .collect(Collectors.toMap(CvrBitemporalRecord::getBitemporality, BaseAttributeValueRecord::getValue))
                                    );
                                    stemmeret.putAll(memberdataRecord.getAttributes().stream()
                                            .filter(attributeRecord -> attributeRecord.getType().equals("EJERANDEL_STEMMERET_PROCENT") && attributeRecord.getValueType().equals("decimal"))
                                            .flatMap(attributeRecord -> attributeRecord.getValues().stream())
                                            .collect(Collectors.toMap(CvrBitemporalRecord::getBitemporality, BaseAttributeValueRecord::getValue))
                                    );
                                    særligeEjerforhold.putAll(memberdataRecord.getAttributes().stream()
                                            .filter(attributeRecord -> attributeRecord.getType().equals("SÆRLIGE_EJERFORHOLD") && attributeRecord.getValueType().equals("string"))
                                            .flatMap(attributeRecord -> attributeRecord.getValues().stream())
                                            .collect(Collectors.toMap(CvrBitemporalRecord::getBitemporality, BaseAttributeValueRecord::getValue))
                                    );
                                    memberFunctions.putAll(
                                            memberdataRecord.getAttributes().stream()
                                            .filter(attributeRecord -> attributeRecord.getType().equals("FUNKTION") && attributeRecord.getValueType().equals("string"))
                                            .flatMap(attributeRecord -> attributeRecord.getValues().stream())
                                            .collect(Collectors.toMap(CvrBitemporalRecord::getBitemporality, BaseAttributeValueRecord::getValue))
                                    );
                                }
                                ejerandel = this.filterLastRegistration(ejerandel);
                                ejerandel = this.filterEmptyEffect(ejerandel);
                                stemmeret = this.filterLastRegistration(stemmeret);
                                stemmeret = this.filterEmptyEffect(stemmeret);
                                særligeEjerforhold = this.filterLastRegistration(særligeEjerforhold);
                                særligeEjerforhold = this.filterEmptyEffect(særligeEjerforhold);
                                memberFunctions = this.filterLastRegistration(memberFunctions);
                                memberFunctions = this.filterEmptyEffect(memberFunctions);

                                RelationParticipantRecord relationParticipantRecord = relationRecord.getRelationParticipantRecord();

                                /*
                                long unitNumber = relationParticipantRecord.getUnitNumber();
                                ParticipantRecord participantRecord = participantCache.get(unitNumber);
                                Object participantObject = relationParticipantRecord;
                                if (participantRecord == null) {
                                    ParticipantRecordQuery participantRecordQuery = new ParticipantRecordQuery();
                                    participantRecordQuery.setRegistrationAt(now);
                                    participantRecordQuery.setEffectAt(now);
                                    participantRecordQuery.setUuid(
                                            Collections.singletonList(
                                                    ParticipantRecord.generateUUID(relationParticipantRecord.getUnitType(), relationParticipantRecord.getUnitNumber())
                                            )
                                    );
                                    List<ParticipantRecord> participantRecords = QueryManager.getAllEntities(participantSession, participantRecordQuery, ParticipantRecord.class);
                                    if (participantRecords != null && !participantRecords.isEmpty()) {
                                        participantRecord = participantRecords.get(0);
                                    }


//                                    participantRecord = QueryManager.getEntity(participantSession, ParticipantRecord.generateUUID(relationParticipantRecord.getUnitType(), relationParticipantRecord.getUnitNumber()), ParticipantRecord.class);
                                    if (participantRecord != null) {
                                        participantCache.put(unitNumber, participantRecord);
                                    }
                                }
                                if (participantRecord != null) {
                                    participantObject = participantRecord;
                                }*/

                                // Legale ejere
                                if (organizationNames.contains("EJERREGISTER") || organizationFunctions.contains("EJERREGISTER")) {
                                    if (
                                            (!ejerandel.isEmpty() && ejerandel.values().stream().anyMatch(s -> Float.parseFloat(s) >= 0.05)) ||
                                            (!stemmeret.isEmpty() && stemmeret.values().stream().anyMatch(s -> Float.parseFloat(s) >= 0.05))
                                    ) {
                                        ObjectNode legalEjer = objectMapper.createObjectNode();
                                        legalEjer.put(
                                                "deltager",
                                                objectMapper.setFilterProvider(CompanyOwnersService.this.getFilterProvider()).valueToTree(relationParticipantRecord)
                                        );
                                        legalEjer.put("ejerandel", this.bitemporalValueList(ejerandel, "ejerandel", true));
                                        legalEjer.put("stemmeret", this.bitemporalValueList(stemmeret, "stemmeret", false));
                                        legalEjer.put("særlige_ejerforhold", this.bitemporalValueList(særligeEjerforhold, "ejerforhold", false));

                                        ArrayList<Bitemporality> ejerPeriods = new ArrayList<>();
                                        ejerPeriods.addAll(
                                                ejerandel.entrySet().stream()
                                                        .filter(bitemporalityStringEntry -> Float.parseFloat(bitemporalityStringEntry.getValue()) >= 0.05)
                                                        .map(Map.Entry::getKey)
                                                        .collect(Collectors.toSet())
                                        );
                                        ejerPeriods.addAll(
                                                stemmeret.entrySet().stream()
                                                        .filter(bitemporalityStringEntry -> Float.parseFloat(bitemporalityStringEntry.getValue()) >= 0.05)
                                                        .map(Map.Entry::getKey)
                                                        .collect(Collectors.toSet())
                                        );

                                        legalEjer.put("periode", this.virkning(Bitemporality.unionOnEffect(ejerPeriods)));
                                        legaleEjere.add(legalEjer);
                                    }
                                }

                                // Reelle ejere
                                if ((organizationNames.contains("Reelle ejere") || organizationFunctions.contains("Reelle ejere")) && memberFunctions.containsValue("Reel ejer")) {
                                    ObjectNode reelEjer = objectMapper.createObjectNode();
                                    reelEjer.put(
                                            "deltager",
                                            objectMapper.setFilterProvider(CompanyOwnersService.this.getFilterProvider()).valueToTree(relationParticipantRecord)
                                    );
                                    reelEjer.put("ejerandel", this.bitemporalValueList(ejerandel, "ejerandel", false));
                                    reelEjer.put("særlige_ejerforhold", this.bitemporalValueList(særligeEjerforhold, "ejerforhold", false));

                                    ArrayList<Bitemporality> ejerPeriods = new ArrayList<>();
                                    ejerPeriods.addAll(
                                            memberFunctions.entrySet().stream()
                                                    .filter(bitemporalityStringEntry -> Objects.equals(bitemporalityStringEntry.getValue(), "Reel ejer"))
                                                    .map(Map.Entry::getKey)
                                                    .collect(Collectors.toSet())
                                    );
                                    reelEjer.put("periode", this.virkning(Bitemporality.unionOnEffect(ejerPeriods)));
                                    reelleEjere.add(reelEjer);
                                }
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
        }
    }

    private ObjectNode virkning(Bitemporality bitemporality) {
        ObjectNode virkning = objectMapper.createObjectNode();
        virkning.put("gyldigFra", bitemporality.effectFrom != null ? bitemporality.effectFrom.toLocalDate().toString() : null);
        virkning.put("gyldigTil", bitemporality.effectTo != null ? bitemporality.effectTo.toLocalDate().toString() : null);
        return virkning;
    }
    private ArrayNode virkning(Collection<Bitemporality> bitemporalities) {
        ArrayNode perioder = objectMapper.createArrayNode();
        for (Bitemporality bitemporality : bitemporalities) {
            if (!bitemporality.emptyEffect()) {
                perioder.add(this.virkning(bitemporality));
            }
        }
        return perioder;
    }

    private ArrayNode bitemporalValueList(Map<Bitemporality, String> items, String key, boolean isAndelRange) {
        ArrayNode ejerandelOut = objectMapper.createArrayNode();
        items.forEach((bitemporality, o) ->  {
            if (!bitemporality.emptyEffect()) {
                ObjectNode ejerandelItem = objectMapper.createObjectNode();
                ejerandelItem.put("periode", this.virkning(bitemporality));
                if (isAndelRange) {
                    ejerandelItem.put(key, this.ejerandelRange(o));
                } else {
                    ejerandelItem.put(key, o);
                }
                ejerandelOut.add(ejerandelItem);
            }
        });
        return ejerandelOut;
    }

    private Map<Bitemporality, String> filterLastRegistration(Map<Bitemporality, String> items) {
        if (!items.isEmpty()) {
            OffsetDateTime last = items.keySet().stream().max(BitemporalityComparator.REGISTRATION_FROM).get().registrationFrom;
            return items.entrySet().stream().filter(bitemporalityStringEntry -> bitemporalityStringEntry.getKey().registrationFrom.isEqual(last)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return Collections.emptyMap();
    }
    private Map<Bitemporality, String> filterEmptyEffect(Map<Bitemporality, String> items) {
        if (!items.isEmpty()) {
            return items.entrySet().stream().filter(bitemporalityStringEntry -> !bitemporalityStringEntry.getKey().emptyEffect()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return Collections.emptyMap();
    }
    private ArrayNode stringList(Collection<String> strings) {
            ArrayNode array = objectMapper.createArrayNode();
            for (String e : strings) {
                array.add(e);
            }
            return array;
    }

    private ObjectNode ejerandelRange(String rangeStart) {
        if (rangeStart != null) {
            Pair<String, String> ejerandelRange = intervalMap.get(rangeStart);
            if (ejerandelRange != null) {
                ObjectNode ejerandelObject = objectMapper.createObjectNode();
                ejerandelObject.put("fra", ejerandelRange.getFirst());
                ejerandelObject.put("til", ejerandelRange.getSecond());
                return ejerandelObject;
            }
        }
        return null;
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    protected FilterProvider getFilterProvider() {
        return new SimpleFilterProvider().addFilter(
                "ParticipantRecordFilter",
                SimpleBeanPropertyFilter.serializeAllExcept(ParticipantRecord.IO_FIELD_BUSINESS_KEY)
        );
    }
}
