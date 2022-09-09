package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.DirectLookup;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.CvrPostCode;
import dk.magenta.datafordeler.geo.GeoLookupDTO;
import dk.magenta.datafordeler.geo.GeoLookupService;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prisme/cvr/1")
public class CvrRecordService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private CvrPlugin cvrPlugin;

    @Autowired
    protected MonitorService monitorService;

    @Autowired
    private DirectLookup directLookup;

    private Logger log = LogManager.getLogger(CvrRecordService.class.getCanonicalName());

    @Autowired
    private GerCompanyLookup gerCompanyLookup;

    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/prisme/cvr/1/1234");
        this.monitorService.addAccessCheckPoint("POST", "/prisme/cvr/1/", "{}");
    }

    public static final String PARAM_UPDATED_SINCE = "updatedSince";
    public static final String PARAM_CVR_NUMBER = "cvrNumber";
    public static final String PARAM_RETURN_PARTICIPANT_DETAILS = "returnParticipantDetails";

    private boolean enableDirectLookup = true;

    public boolean isEnableDirectLookup() {
        return this.enableDirectLookup;
    }

    public void setEnableDirectLookup(boolean enableDirectLookup) {
        this.enableDirectLookup = enableDirectLookup;
    }


    private boolean enableGerLookup = true;

    public boolean isEnableGerLookup() {
        return this.enableGerLookup;
    }

    public void setEnableGerLookup(boolean enableGerLookup) {
        this.enableGerLookup = enableGerLookup;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{cvrNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cvrNummer") String cvrNummer, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {

        boolean returnParticipantDetails = "1".equals(request.getParameter(PARAM_RETURN_PARTICIPANT_DETAILS));

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCvrService with cvrNummer " + cvrNummer + " and " +
                        PARAM_RETURN_PARTICIPANT_DETAILS + " = " + returnParticipantDetails
        );
        this.checkAndLogAccess(loggerHelper, returnParticipantDetails);
        loggerHelper.urlInvokePersistablelogs("CvrRecordService");

        HashSet<String> cvrNumbers = new HashSet<>();
        cvrNumbers.add(cvrNummer);

        Session session = sessionManager.getSessionFactory().openSession();
        GeoLookupService service = new GeoLookupService(sessionManager);
        try {
            ObjectNode formattedRecord = null;

            if (this.enableDirectLookup) {
                Collection<CompanyRecord> records = this.getCompanies(session, cvrNumbers, user);
                if (!records.isEmpty()) {
                    CompanyRecord companyRecord = records.iterator().next();
                    formattedRecord = this.wrapRecord(companyRecord, service, returnParticipantDetails);
                }
            }

            if (this.enableGerLookup && formattedRecord == null) {
                Collection<CompanyEntity> companyEntities = gerCompanyLookup.lookup(session, cvrNumbers);
                if (!companyEntities.isEmpty()) {
                    CompanyEntity companyEntity = companyEntities.iterator().next();
                    formattedRecord = this.wrapGerCompany(companyEntity, service, returnParticipantDetails);
                }
            }

            if (formattedRecord != null) {
                loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CprService done");
                return objectMapper.writeValueAsString(formattedRecord);
            }
        } finally {
            session.close();
        }
        loggerHelper.urlResponsePersistablelogs(HttpStatus.NOT_FOUND.value(), "CprService done");
        throw new HttpNotFoundException("No entity with CVR number " + cvrNummer + " was found");
    }

    protected Collection<CompanyRecord> getCompanies(Session session, Collection<String> cvrNumbers, DafoUserDetails user) throws DataFordelerException {
        CompanyRecordQuery query = new CompanyRecordQuery();
        query.setParameter(CompanyRecordQuery.CVRNUMMER, cvrNumbers);
        this.applyAreaRestrictionsToQuery(query, user);
        return QueryManager.getAllEntities(session, query, CompanyRecord.class);
    }

    protected static final byte[] START_OBJECT = "{".getBytes();
    protected static final byte[] END_OBJECT = "}".getBytes();
    protected static final byte[] OBJECT_SEPARATOR = ",\n".getBytes();

    @RequestMapping(method = RequestMethod.POST, path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getBulk(HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, IOException, HttpNotFoundException, InvalidCertificateException {
        JsonNode requestBody;
        try {
            requestBody = objectMapper.readTree(request.getInputStream());
        } catch (IOException e) {
            throw new InvalidClientInputException(e.getMessage());
        }
        if (!requestBody.isObject()) {
            throw new InvalidClientInputException("Input is not a JSON object");
        }
        ObjectNode requestObject = (ObjectNode) requestBody;

        final OffsetDateTime updatedSince = requestObject.has(PARAM_UPDATED_SINCE) ? Query.parseDateTime(requestObject.get(PARAM_UPDATED_SINCE).asText(), false) : null;

        final List<String> cvrNumbers = (requestObject.has(PARAM_CVR_NUMBER)) ? this.getCvrNumber(requestObject.get(PARAM_CVR_NUMBER)) : null;

        boolean returnParticipantDetails = "1".equals(request.getParameter(PARAM_RETURN_PARTICIPANT_DETAILS));

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCvrRecordService with " +
                        PARAM_UPDATED_SINCE + " = " + updatedSince + ", " +
                        PARAM_CVR_NUMBER + " = " + (cvrNumbers != null && cvrNumbers.size() > 10 ? (cvrNumbers.size() + " cpr numbers") : cvrNumbers) + " and " +
                        PARAM_RETURN_PARTICIPANT_DETAILS + " = " + returnParticipantDetails
        );
        this.checkAndLogAccess(loggerHelper, returnParticipantDetails);
        loggerHelper.urlInvokePersistablelogs("CvrRecordService");

        HashSet<String> cvr = new HashSet<>();

        if (cvrNumbers != null) {
            for (String cvrNumber : cvrNumbers) {
                try {
                    cvr.add(Integer.toString(Integer.parseInt(cvrNumber, 10)));
                } catch (NumberFormatException e) {
                }
            }
        }
        if (cvr.isEmpty()) {
            throw new InvalidClientInputException("Please specify at least one CVR number");
        }

        CompanyRecordQuery query = new CompanyRecordQuery();
        query.setParameter(CompanyRecordQuery.CVRNUMMER, cvrNumbers);
        query.setRecordAfter(updatedSince);
        this.applyAreaRestrictionsToQuery(query, user);

        loggerHelper.urlResponsePersistablelogs("CvrRecordService");
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                Session session = sessionManager.getSessionFactory().openSession();
                List<CompanyRecord> records = QueryManager.getAllEntitiesAsStream(session, query, CompanyRecord.class).collect(Collectors.toList());
                GeoLookupService lookupService = new GeoLookupService(sessionManager);
                try {
                    boolean first = true;
                    outputStream.write(START_OBJECT);
                    for (CompanyRecord record : records) {
                        if (!first) {
                            outputStream.flush();
                            outputStream.write(OBJECT_SEPARATOR);
                        } else {
                            first = false;
                        }

                        outputStream.write(("\"" + record.getCvrNumber() + "\":").getBytes());
                        outputStream.write(
                                objectMapper.writeValueAsString(
                                        CvrRecordService.this.wrapRecord(record, lookupService, returnParticipantDetails)
                                ).getBytes("UTF-8")
                        );
                    }
                    outputStream.write(END_OBJECT);
                    outputStream.flush();
                } finally {
                    session.close();
                }
            }
        };
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper, boolean includeCpr) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
            if (includeCpr) {
                loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
            }
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }

    private static Pattern nonDigits = Pattern.compile("[^\\d]");
    protected List<String> getCvrNumber(JsonNode node) {
        ArrayList<String> cvrNumbers = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : (ArrayNode) node) {
                cvrNumbers.addAll(this.getCvrNumber(item));
            }
        } else if (node.isTextual()) {
            cvrNumbers.add(nonDigits.matcher(node.asText()).replaceAll(""));
        } else if (node.isNumber()) {
            cvrNumbers.add(String.format("%08d", node.asInt()));
        }
        return cvrNumbers;
    }

    protected ObjectNode wrapRecord(CompanyRecord record, GeoLookupService lookupService, boolean returnParticipantDetails) {
        ObjectNode root = objectMapper.createObjectNode();

        root.put("source", "CVR");
        root.put("cvrNummer", record.getCvrNumber());

        SecNameRecord nameRecord = this.getLastUpdated(record.getNames());
        if (nameRecord != null) {
            root.put("navn", nameRecord.getName());
        }

        CompanyIndustryRecord industryRecord = this.getLastUpdated(record.getPrimaryIndustry());
        if (industryRecord != null) {
            root.put("forretningsområde", industryRecord.getIndustryText());
        }

        ArrayList<CompanyStatusRecord> statusRecords = new ArrayList<>(record.getCompanyStatus());
        if (record.getMetadata() != null) {
            CompanyStatusRecord metaStatusRecord = record.getMetadata().getCompanyStatusRecord(record);
            if (metaStatusRecord != null) {
                statusRecords.add(metaStatusRecord);
            }
        }

        CompanyStatusRecord statusRecord = this.getLastUpdated(statusRecords);
        if (statusRecord != null) {
            root.put("statuskode", statusRecord.getStatus());
            root.put("statuskodedato", statusRecord.getValidFrom() != null ? statusRecord.getValidFrom().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
        }

        AddressRecord addressRecord = this.getLastUpdated(record.getPostalAddress());

        if (addressRecord == null) {
            addressRecord = this.getLastUpdated(record.getLocationAddress());
        }
        if (addressRecord != null) {
            AddressMunicipalityRecord municipality = addressRecord.getMunicipality();
            int municipalityCode = 0;
            if (municipality != null) {
                municipalityCode = municipality.getMunicipalityCode();
                root.put("myndighedskode", municipalityCode);
                root.put("kommune", municipality.getMunicipalityName());
            }

            int roadCode = addressRecord.getRoadCode();
            if (roadCode > 0) {
                root.put("vejkode", roadCode);
                if (municipalityCode > 0 && lookupService != null) {
                    GeoLookupDTO lookup = lookupService.doLookup(municipalityCode, roadCode);
                    if (lookup.getLocalityCodeNumber() != 0) {
                        root.put("stedkode", lookup.getLocalityCodeNumber());
                    }
                }
            }

            StringBuilder addressFormatted = new StringBuilder();
            if (addressRecord.getRoadName() != null) {
                addressFormatted.append(addressRecord.getRoadName());
            }
            if (addressRecord.getHouseNumberFrom() != 0) {
                addressFormatted.append(" " + addressRecord.getHouseNumberFrom() + emptyIfNull(addressRecord.getLetterFrom()));
                if (addressRecord.getHouseNumberTo() != 0) {
                    addressFormatted.append("-");
                    if (addressRecord.getHouseNumberTo()==addressRecord.getHouseNumberFrom()) {
                        addressFormatted.append(emptyIfNull(addressRecord.getLetterTo()));
                    } else {
                        addressFormatted.append(addressRecord.getHouseNumberTo() + emptyIfNull(addressRecord.getLetterTo()));
                    }
                }
                if (addressRecord.getFloor() != null) {
                    addressFormatted.append(", " + addressRecord.getFloor() + ".");
                }
                if (addressRecord.getDoor() != null) {
                    addressFormatted.append(" " + addressRecord.getDoor());
                }
            }


            String addressFormattedString = addressFormatted.toString();

            if (!addressFormattedString.isEmpty()) {
                root.put("adresse", addressFormattedString);
            }
            if (addressRecord.getPostBox() != null && addressRecord.getPostBox() != "") {
                root.put("postboks", Integer.parseInt(addressRecord.getPostBox()));
            }

            CvrPostCode postCode = addressRecord.getPost();
            if (addressRecord.getPostnummer() != 0) {
                root.put("postnummer", addressRecord.getPostnummer());
            }
            if (addressRecord.getPostdistrikt() != null) {
                root.put("bynavn", addressRecord.getPostdistrikt());
            }
            root.put("landekode", addressRecord.getCountryCode());

            String coName = addressRecord.getCoName();
            if (coName != null) {
                root.put("co", coName);
            }
        }

        ContactRecord emailAddress = this.getLastUpdated(record.getEmailAddress());
        if (emailAddress != null) {
            root.put("email", emailAddress.getContactInformation());
        }
        ContactRecord phoneNumber = this.getLastUpdated(record.getPhoneNumber());
        if (phoneNumber != null) {
            root.put("telefon", phoneNumber.getContactInformation());
        }
        ContactRecord faxNumber = this.getLastUpdated(record.getFaxNumber());
        if (faxNumber != null) {
            root.put("telefax", faxNumber.getContactInformation());
        }

        if (returnParticipantDetails) {
            root.set("deltagere", this.getParticipants(record));
        }

        return root;
    }

    protected ArrayNode getParticipants(CompanyRecord record) {
        ArrayNode participantsOutput = objectMapper.createArrayNode();
        OffsetDateTime current = OffsetDateTime.now();
        Bitemporality now = new Bitemporality(current, current, current, current);
        for (CompanyParticipantRelationRecord participant : record.getParticipants()) {
            RelationParticipantRecord relationParticipantRecord = participant.getRelationParticipantRecord();
            HashSet<MembershipDescription> membershipDescriptions = new HashSet<>();
            if (relationParticipantRecord != null && ("PERSON".equals(relationParticipantRecord.unitType) || "ANDEN_DELTAGER".equals(relationParticipantRecord.unitType))) {
                boolean hasEligibleParticipant = false;
                ObjectNode participantOutput = objectMapper.createObjectNode();
                ArrayNode organizationsOutput = objectMapper.createArrayNode();
                for (OrganizationRecord organization : participant.getOrganizations()) {
                    ArrayNode memberNodes = objectMapper.createArrayNode();
                    boolean found = false;
                    for (OrganizationMemberdataRecord memberdataRecord : organization.getMemberData()) {
                        for (AttributeRecord memberAttribute : memberdataRecord.getAttributes()) {
                            if ("FUNKTION".equals(memberAttribute.getType())) {
                                AttributeValueRecord memberAttributeValue = getLastUpdated(memberAttribute.getValues());
                                if (memberAttributeValue != null && memberAttributeValue.getBitemporality().contains(now)) {
                                    ObjectNode orgMemberNode = objectMapper.createObjectNode();
                                    orgMemberNode.put("funktion", memberAttributeValue.getValue());
                                    memberNodes.add(orgMemberNode);
                                    found = true;
                                    for (BaseNameRecord organizationName : organization.getNames()) {
                                        membershipDescriptions.add(
                                                new MembershipDescription(organization.getMainType(), organizationName.getName(), memberAttributeValue.getValue())
                                        );
                                    }
                                }
                            }
                        }
                    }
                    if (found) {
                        hasEligibleParticipant = true;
                        ObjectNode organizationOutput = objectMapper.createObjectNode();
                        organizationsOutput.add(organizationOutput);
                        organizationOutput.put("type", organization.getMainType());
                        for (BaseNameRecord organizationName : organization.getNames()) {
                            String name = organizationName.getName();
                            organizationOutput.put("navn", name);
                        }
                        organizationOutput.set("medlemskaber", memberNodes);
                    }
                }
                if (hasEligibleParticipant) {
                    long unitNumber = relationParticipantRecord.getUnitNumber();
                    participantOutput.put("enhedsNummer", unitNumber);
                    try {
                        ParticipantRecord participantRecord = directLookup.participantLookup(Long.toString(unitNumber, 10));
                        if (participantRecord != null) {
                            Long businessKey = participantRecord.getBusinessKey();
                            if (Objects.equals(businessKey, unitNumber)) {
                                // Foreigner
                            } else {
                                participantOutput.put("deltagerPnr", String.format("%010d", businessKey));
                            }
                        }
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
                    boolean ownerMatch = false;
                    for (MembershipDescription d : membershipDescriptions) {
                        if (d.isOwner()) {
                            ownerMatch = true;
                        }
                    }
                    participantOutput.put("ownerMatch", ownerMatch);
                    participantOutput.set("organisationer", organizationsOutput);
                    participantsOutput.add(participantOutput);
                }
            }
        }
        return participantsOutput;
    }

    private <T extends CvrBitemporalRecord> T getLastUpdated(Collection<T> records) {
        ArrayList<T> list = new ArrayList<>();
        for (T record : records) {
            if (record != null) {
                list.add(record);
            }
        }
        if (list.size() > 1) {
            list.sort(
                    Comparator.comparing(
                            CvrBitemporalRecord::getValidFrom, Comparator.nullsFirst(Comparator.naturalOrder())
                    ).thenComparing(
                            CvrBitemporalRecord::getLastUpdated, Comparator.nullsFirst(Comparator.naturalOrder())
                    )
            );
        }
        return list.isEmpty() ? null : list.get(list.size()-1);
    }

    Pattern postcodePattern = Pattern.compile("(\\d{4}) (.*)");

    private static HashMap<Integer, String> municipalityMap = new HashMap<>();
    static {
        municipalityMap.put(955, "Kommune Kujalleq");
        municipalityMap.put(956, "Kommuneqarfik Sermersooq");
        municipalityMap.put(957, "Qeqqata Kommunia");
        municipalityMap.put(958, "Qaasuitsup Kommunia");
        municipalityMap.put(959, "Kommune Qeqertalik");
        municipalityMap.put(960, "Avannaata Kommunia");
    }


    protected ObjectNode wrapGerCompany(CompanyEntity entity, GeoLookupService lookupService, boolean returnParticipantDetails) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("source", "GER");
        root.put("cvrNummer", entity.getGerNr());
        root.put(
                "navn",
                (entity.getEndDate() != null ? "historisk ":"") +
                entity.getName()
        );
        root.put("forretningsområde", entity.getBusinessText());

        String statusCode = gerCompanyLookup.getStatus(entity.getStatusGuid());
        root.put("statuskode", statusCode);
        root.put("statuskodedato", entity.getStatusChange() != null ? entity.getStatusChange().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);

        Integer municipalityCode = entity.getMunicipalityCode();
        root.put("myndighedskode", municipalityCode);
        if (municipalityCode != null) {
            if (municipalityMap.containsKey(municipalityCode)) {
                root.put("kommune", municipalityMap.get(municipalityCode));
            }
        }

        //root.put("vejkode", roadCode);
        root.put("stedkode", entity.getLocalityCode());

        Integer countryCode = entity.getCountryCode();
        if (countryCode != null && countryCode != 0) {
            root.put("landekode", countryCode);
        }

        if (countryCode == 8 || countryCode == 406) { // Denmark or Greenland

            String address = entity.getAddress1();
            if (address == null || address.isEmpty()) {
                String address2 = entity.getAddress2();
                if (address2 != null && !address2.isEmpty() && !address2.contains("Postboks")) {
                    address = address2;
                }
            }
            root.put("adresse", address);

            String boxNr = entity.getBoxNr();
            if (boxNr != null && !boxNr.trim().isEmpty()) {
                root.put("postboks", boxNr.trim());
            }

            Matcher addressMatcher = null;
            String postcodeField = entity.getAddress3();
            if (postcodeField != null && !postcodeField.isEmpty()) {
                addressMatcher = postcodePattern.matcher(postcodeField);
            }
            if (addressMatcher != null && addressMatcher.find()) {
                root.put("postnummer", addressMatcher.group(1));
                root.put("bynavn", addressMatcher.group(2));
            } else {
                Integer postCode = entity.getPostNr();
                if (postCode != null && postCode != 0) {
                    root.put("postnummer", postCode);
                    String district = lookupService.getPostalCodeDistrict(postCode);
                    if (district != null) {
                        root.put("bynavn", district);
                    }
                }
            }
        } else {
            StringJoiner address = new StringJoiner("\n");
            String[] parts = new String[]{
                entity.getAddress1(), entity.getAddress2(), entity.getAddress3()
            };
            for (String part : parts) {
                if (part != null && !part.isEmpty()) {
                    address.add(part);
                }
            }
            root.put("adresse", address.toString());
        }


        String coName = entity.getCoName();
        if (coName != null) {
            root.put("co", coName);
        }


        String emailAddress = entity.getEmail();
        if (emailAddress != null) {
            root.put("email", emailAddress);
        }

        String phoneNumber = entity.getPhone();
        if (phoneNumber != null) {
            root.put("telefon", phoneNumber);
        }

        String faxNumber = entity.getFax();
        if (faxNumber != null) {
            root.put("telefax", faxNumber);
        }

        if (returnParticipantDetails) {
            ResponsibleQuery responsibleQuery = new ResponsibleQuery();
            responsibleQuery.setGerNr(entity.getGerNr());
            try(Session session = sessionManager.getSessionFactory().openSession();) {
                List<ResponsibleEntity> responsibleEntities = QueryManager.getAllEntities(session, responsibleQuery, ResponsibleEntity.class);
                if (!responsibleEntities.isEmpty()) {
                    ArrayNode participantsNode = objectMapper.createArrayNode();
                    for (ResponsibleEntity responsibleEntity : responsibleEntities) {
                        ObjectNode responsibleNode = objectMapper.createObjectNode();
                        if (responsibleEntity.getCprNumber() != null) {
                            responsibleNode.put("deltagerPnr", responsibleEntity.getCprNumberString());
                        }
                        if (responsibleEntity.getCvrNumber() != null) {
                            responsibleNode.put("deltagerCvrNr", responsibleEntity.getCvrNumber().toString());
                        }
                        if (responsibleEntity.getName() != null) {
                            responsibleNode.put("deltagerNavn", responsibleEntity.getName());
                        }
                        participantsNode.add(responsibleNode);
                    }
                    root.set("deltagere", participantsNode);
                }
            }
        }

        return root;
    }

    protected void applyAreaRestrictionsToQuery(CompanyRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CvrRolesDefinition.READ_CVR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cvrPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }

    private static String emptyIfNull(String text) {
        if (text == null) return "";
        return text;
    }

}
