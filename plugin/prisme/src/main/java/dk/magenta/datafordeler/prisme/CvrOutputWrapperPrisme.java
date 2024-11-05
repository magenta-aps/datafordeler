package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cvr.CollectiveCvrLookup;
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
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CvrOutputWrapperPrisme extends OutputWrapper<CompanyRecord> {

    private final Logger log = LogManager.getLogger(CvrOutputWrapperPrisme.class.getCanonicalName());

    @Autowired
    private ObjectMapper objectMapper;

    private GeoLookupService lookupService;

    public void setLookupService(GeoLookupService lookupService) {
        this.lookupService = lookupService;
    }

    private CollectiveCvrLookup directLookup;

    public void setCollectiveLookup(CollectiveCvrLookup directLookup) {
        this.directLookup = directLookup;
    }

    @Autowired
    private GerCompanyLookup gerCompanyLookup;

    public void setGerCompanyLookup(GerCompanyLookup gerCompanyLookup) {
        this.gerCompanyLookup = gerCompanyLookup;
    }

    private <T extends CprBitemporalPersonRecord> T getLatest(Collection<T> records) {
        //OffsetDateTime latestEffect = OffsetDateTime.MIN;
        OffsetDateTime latestRegistration = OffsetDateTime.MIN;
        ArrayList<T> latest = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        Bitemporality currentBitemp = new Bitemporality(now, now, now, now);
        OffsetDateTime latestUpdated = OffsetDateTime.MIN;
        for (T record : records) {
            if (record.getBitemporality().contains(currentBitemp) && !record.getDafoUpdated().isBefore(latestUpdated) && !record.isUndone()/* && record.getCorrector() == null*/) {
                OffsetDateTime registrationFrom = record.getRegistrationFrom();
                if (registrationFrom == null) {
                    registrationFrom = OffsetDateTime.MIN;
                }
                if (!registrationFrom.isBefore(latestRegistration)) {
                    if (!registrationFrom.isEqual(latestRegistration)) {
                        latest.clear();
                    }
                    latest.add(record);
                    latestUpdated = record.getDafoUpdated();
                    latestRegistration = registrationFrom;
                }
            }
        }
        if (latest.size() > 1) {
            latest.sort(Comparator.comparing(CprBitemporalPersonRecord::getId));
        }
        return latest.isEmpty() ? null : latest.get(latest.size() - 1);
    }

    Pattern postcodePattern = Pattern.compile("(\\d{4}) (.*)");

    private static final HashMap<Integer, String> municipalityMap = new HashMap<>();

    static {
        municipalityMap.put(955, "Kommune Kujalleq");
        municipalityMap.put(956, "Kommuneqarfik Sermersooq");
        municipalityMap.put(957, "Qeqqata Kommunia");
        municipalityMap.put(958, "Qaasuitsup Kommunia");
        municipalityMap.put(959, "Kommune Qeqertalik");
        municipalityMap.put(960, "Avannaata Kommunia");
    }

    private static final Pattern nonDigits = Pattern.compile("[^\\d]");


    protected static List<String> getCvrNumber(JsonNode node) {
        ArrayList<String> cvrNumbers = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                cvrNumbers.addAll(getCvrNumber(item));
            }
        } else if (node.isTextual()) {
            cvrNumbers.add(nonDigits.matcher(node.asText()).replaceAll(""));
        } else if (node.isNumber()) {
            cvrNumbers.add(String.format("%08d", node.asInt()));
        }
        return cvrNumbers;
    }


    protected ObjectNode wrapGerCompany(CompanyEntity entity, GeoLookupService lookupService, boolean returnParticipantDetails) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("source", "GER");
        root.put("cvrNummer", entity.getGerNr());
        root.put(
                "navn",
                (entity.getEndDate() != null ? "historisk " : "") +
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
            root.put("landekode", HardcodedCountrycodeForGERCompany.countrycodes.get(countryCode));
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
            try (Session session = lookupService.getSessionManager().getSessionFactory().openSession()) {
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


    protected ObjectNode wrapRecord(CompanyRecord record, GeoLookupService lookupService, boolean returnParticipantDetails, boolean returnGlobalIds) {
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
                    GeoLookupDTO lookup = null;
                    try {
                        lookup = lookupService.doLookup(municipalityCode, roadCode);
                    } catch (InvalidClientInputException e) {
                        throw new RuntimeException(e);
                    }
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
                    if (addressRecord.getHouseNumberTo() == addressRecord.getHouseNumberFrom()) {
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

            if (returnGlobalIds) {
                try {
                    String houseNumber = addressRecord.getHouseNumberFrom() != 0 ? String.valueOf(addressRecord.getHouseNumberFrom()) : null;
                    GeoLookupDTO lookup = lookupService.doLookup(
                            municipalityCode,
                            roadCode,
                            houseNumber,
                            null,
                            addressRecord.getFloor(),
                            addressRecord.getDoor(),
                            true
                    );
                    root.put("husnummer", houseNumber);
                    root.put("etage", addressRecord.getFloor());
                    root.put("sidedør", addressRecord.getDoor());
                    root.put("adgangsadresseGlobalId", lookup.getAccessAddressGlobalId());
                    root.put("enhedsadresseGlobalId", lookup.getUnitAddressGlobalId());
                } catch (InvalidClientInputException e) {
                }
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
                        //It is expected to find only one participant
                        try (Session session = lookupService.getSessionManager().getSessionFactory().openSession()) {
                            ParticipantRecord participantRecord = directLookup.participantLookup(session, Arrays.asList(Long.toString(unitNumber, 10))).iterator().next();
                            if (participantRecord != null) {
                                Long businessKey = participantRecord.getBusinessKey();
                                if (Objects.equals(businessKey, unitNumber)) {
                                    // Foreigner
                                } else {
                                    participantOutput.put("deltagerPnr", String.format("%010d", businessKey));
                                }
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
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    private String emptyIfNull(String text) {
        if (text == null) return "";
        return text;
    }
}
