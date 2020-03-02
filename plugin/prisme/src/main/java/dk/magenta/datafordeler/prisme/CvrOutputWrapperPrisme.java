package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cvr.CollectiveCvrLookup;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.PostCode;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CvrOutputWrapperPrisme extends OutputWrapper<CompanyRecord> {

    private Logger log = LogManager.getLogger(CvrOutputWrapperPrisme.class.getCanonicalName());

    @Autowired
    private ObjectMapper objectMapper;

    private LookupService lookupService;

    public void setLookupService(LookupService lookupService) {
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
        return latest.isEmpty() ? null : latest.get(latest.size()-1);
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

    private static Pattern nonDigits = Pattern.compile("[^\\d]");


    protected static List<String> getCvrNumber(JsonNode node) {
        ArrayList<String> cvrNumbers = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : (ArrayNode) node) {
                cvrNumbers.addAll(getCvrNumber(item));
            }
        } else if (node.isTextual()) {
            cvrNumbers.add(nonDigits.matcher(node.asText()).replaceAll(""));
        } else if (node.isNumber()) {
            cvrNumbers.add(String.format("%08d", node.asInt()));
        }
        return cvrNumbers;
    }



    protected ObjectNode wrapGerCompany(CompanyEntity entity, LookupService lookupService, boolean returnParticipantDetails) {
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
            List<ResponsibleEntity> responsibleEntities = QueryManager.getAllEntities(lookupService.getSession(), responsibleQuery, ResponsibleEntity.class);
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

        return root;
    }



    protected ObjectNode wrapRecord(CompanyRecord record, LookupService lookupService, boolean returnParticipantDetails) {
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
                    Lookup lookup = lookupService.doLookup(municipalityCode, roadCode);
                    if (lookup.localityCode != 0) {
                        root.put("stedkode", lookup.localityCode);
                    }
                }
            }

            StringBuilder addressFormatted = new StringBuilder();
            if (addressRecord.getRoadName() != null) {
                addressFormatted.append(addressRecord.getRoadName());
            }
            if (addressRecord.getHouseNumberFrom() != null) {
                addressFormatted.append(" " + addressRecord.getHouseNumberFrom() + emptyIfNull(addressRecord.getLetterFrom()));
                if (addressRecord.getHouseNumberTo() != null) {
                    addressFormatted.append("-");
                    if (addressRecord.getHouseNumberTo().equals(addressRecord.getHouseNumberFrom())) {
                        addressFormatted.append(emptyIfNull(addressRecord.getLetterTo()));
                    } else {
                        addressFormatted.append(addressRecord.getHouseNumberTo() + emptyIfNull(addressRecord.getLetterTo()));
                    }
                }
                if (addressRecord.getFloor() != null) {
                    addressFormatted.append(", " + addressRecord.getFloor() + ".");
                    if (addressRecord.getDoor() != null) {
                        addressFormatted.append(" " + addressRecord.getDoor());
                    }
                }
            }


            String addressFormattedString = addressFormatted.toString();

            if (!addressFormattedString.isEmpty()) {
                root.put("adresse", addressFormattedString);
            }
            if (addressRecord.getPostBox() != null && addressRecord.getPostBox() != "") {
                root.put("postboks", addressRecord.getPostBox());
            }

            PostCode postCode = addressRecord.getPost();
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
                        //It is expected to find only one participant
                        ParticipantRecord participantRecord = directLookup.participantLookup(lookupService.getSession(), Arrays.asList(Long.toString(unitNumber, 10))).iterator().next();
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

    private String emptyIfNull(String text) {
        if (text == null) return "";
        return text;
    }

    private static HashMap<Integer, String> countryCodeMap = new HashMap<>();
    static {
        countryCodeMap.put(5404, "AF");
        countryCodeMap.put(5299, "XX");
        countryCodeMap.put(5122, "AL");
        countryCodeMap.put(5202, "DZ");
        countryCodeMap.put(5124, "AD");
        countryCodeMap.put(5204, "AO");
        countryCodeMap.put(5309, "AG");
        countryCodeMap.put(5302, "AR");
        countryCodeMap.put(5708, "AM");
        countryCodeMap.put(5499, "XX");
        countryCodeMap.put(5502, "AU");
        countryCodeMap.put(5710, "AZ");
        countryCodeMap.put(5303, "BS");
        countryCodeMap.put(5406, "BH");
        countryCodeMap.put(5410, "BD");
        countryCodeMap.put(5305, "BB");
        countryCodeMap.put(5706, "BY");
        countryCodeMap.put(5126, "BE");
        countryCodeMap.put(5526, "BZ");
        countryCodeMap.put(5281, "BJ");
        countryCodeMap.put(5408, "BT");
        countryCodeMap.put(5304, "BO");
        countryCodeMap.put(5754, "BA");
        countryCodeMap.put(5207, "BW");
        countryCodeMap.put(5306, "BR");
        countryCodeMap.put(5412, "BN");
        countryCodeMap.put(5128, "BG");
        countryCodeMap.put(5294, "BF");
        countryCodeMap.put(5213, "BI");
        countryCodeMap.put(5416, "KH");
        countryCodeMap.put(5277, "CM");
        countryCodeMap.put(5314, "CA");
        countryCodeMap.put(5233, "CV");
        countryCodeMap.put(5276, "CF");
        countryCodeMap.put(5316, "CL");
        countryCodeMap.put(5318, "CO");
        countryCodeMap.put(5215, "KM");
        countryCodeMap.put(5779, "CK");
        countryCodeMap.put(5322, "CR");
        countryCodeMap.put(5324, "CU");
        countryCodeMap.put(5422, "CY");
        countryCodeMap.put(5100, "DK");
        countryCodeMap.put(5278, "CD");
        countryCodeMap.put(5525, "DJ");
        countryCodeMap.put(5345, "DM");
        countryCodeMap.put(5326, "DO");
        countryCodeMap.put(5328, "EC");
        countryCodeMap.put(5272, "EG");
        countryCodeMap.put(5372, "SV");
        countryCodeMap.put(5282, "CI");
        countryCodeMap.put(5216, "ER");
        countryCodeMap.put(5607, "EE");
        countryCodeMap.put(5214, "ET");
        countryCodeMap.put(5199, "XX");
        countryCodeMap.put(5508, "FJ");
        countryCodeMap.put(5474, "PH");
        countryCodeMap.put(5104, "FI");
        countryCodeMap.put(5403, "AE");
        countryCodeMap.put(5130, "FR");
        countryCodeMap.put(5902, "FO");
        countryCodeMap.put(5283, "GA");
        countryCodeMap.put(5222, "GM");
        countryCodeMap.put(5724, "GE");
        countryCodeMap.put(5228, "GH");
        countryCodeMap.put(5339, "GD");
        countryCodeMap.put(5134, "GR");
        countryCodeMap.put(5338, "GT");
        countryCodeMap.put(5232, "GN");
        countryCodeMap.put(5231, "GW");
        countryCodeMap.put(5308, "GY");
        countryCodeMap.put(5342, "HT");
        countryCodeMap.put(5348, "HN");
        countryCodeMap.put(5432, "IN");
        countryCodeMap.put(5434, "ID");
        countryCodeMap.put(5436, "IQ");
        countryCodeMap.put(5438, "IR");
        countryCodeMap.put(5142, "IE");
        countryCodeMap.put(5106, "IS");
        countryCodeMap.put(5105, "XX");
        countryCodeMap.put(5442, "IL");
        countryCodeMap.put(5150, "IT");
        countryCodeMap.put(5352, "JM");
        countryCodeMap.put(5444, "JP");
        countryCodeMap.put(5446, "JO");
        countryCodeMap.put(5152, "YU");
        countryCodeMap.put(5758, "YU");
        countryCodeMap.put(5716, "KZ");
        countryCodeMap.put(5234, "KE");
        countryCodeMap.put(5448, "CN");
        countryCodeMap.put(5720, "KG");
        countryCodeMap.put(5274, "KI");
        countryCodeMap.put(5466, "KP");
        countryCodeMap.put(5484, "KR");
        countryCodeMap.put(5761, "XX");
        countryCodeMap.put(5750, "HR");
        countryCodeMap.put(5452, "KW");
        countryCodeMap.put(5999, "XX");
        countryCodeMap.put(5454, "LA");
        countryCodeMap.put(5235, "LS");
        countryCodeMap.put(5609, "LV");
        countryCodeMap.put(5456, "LB");
        countryCodeMap.put(5236, "LR");
        countryCodeMap.put(5238, "LY");
        countryCodeMap.put(5107, "LI");
        countryCodeMap.put(5611, "LT");
        countryCodeMap.put(5108, "LU");
        countryCodeMap.put(5242, "MG");
        countryCodeMap.put(5756, "MK");
        countryCodeMap.put(5297, "MW");
        countryCodeMap.put(5458, "MY");
        countryCodeMap.put(5457, "MV");
        countryCodeMap.put(5243, "ML");
        countryCodeMap.put(5153, "MT");
        countryCodeMap.put(5244, "MA");
        countryCodeMap.put(5248, "MH");
        countryCodeMap.put(5284, "MR");
        countryCodeMap.put(5245, "MU");
        countryCodeMap.put(5487, "XX");
        countryCodeMap.put(5354, "MX");
        countryCodeMap.put(5712, "MD");
        countryCodeMap.put(5109, "MC");
        countryCodeMap.put(5459, "MN");
        countryCodeMap.put(5759, "ME");
        countryCodeMap.put(5240, "MZ");
        countryCodeMap.put(5414, "MM");
        countryCodeMap.put(5247, "NA");
        countryCodeMap.put(5310, "NR");
        countryCodeMap.put(5140, "NL");
        countryCodeMap.put(5464, "NP");
        countryCodeMap.put(5514, "NZ");
        countryCodeMap.put(5356, "NI");
        countryCodeMap.put(5285, "NE");
        countryCodeMap.put(5246, "NG");
        countryCodeMap.put(5397, "XX");
        countryCodeMap.put(5110, "NO");
        countryCodeMap.put(5462, "OM");
        countryCodeMap.put(5472, "PK");
        countryCodeMap.put(5358, "PA");
        countryCodeMap.put(5534, "PG");
        countryCodeMap.put(5364, "PY");
        countryCodeMap.put(5366, "PE");
        countryCodeMap.put(5154, "PL");
        countryCodeMap.put(5156, "PT");
        countryCodeMap.put(5496, "QA");
        countryCodeMap.put(5279, "CG");
        countryCodeMap.put(5158, "RO");
        countryCodeMap.put(5700, "RU");
        countryCodeMap.put(5287, "RW");
        countryCodeMap.put(5623, "SB");
        countryCodeMap.put(5522, "WS");
        countryCodeMap.put(5159, "SM");
        countryCodeMap.put(5621, "ST");
        countryCodeMap.put(5478, "SA");
        countryCodeMap.put(5160, "CH");
        countryCodeMap.put(5288, "SN");
        countryCodeMap.put(5757, "RS");
        countryCodeMap.put(5151, "CS");
        countryCodeMap.put(5298, "SC");
        countryCodeMap.put(5255, "SL");
        countryCodeMap.put(5482, "SG");
        countryCodeMap.put(5778, "SK");
        countryCodeMap.put(5752, "SI");
        countryCodeMap.put(5289, "SO");
        countryCodeMap.put(5162, "SU");
        countryCodeMap.put(5164, "ES");
        countryCodeMap.put(5418, "LK");
        countryCodeMap.put(5311, "VC");
        countryCodeMap.put(5625, "KN");
        countryCodeMap.put(5347, "LC");
        countryCodeMap.put(5103, "XX");
        countryCodeMap.put(5170, "GB");
        countryCodeMap.put(5258, "SD");
        countryCodeMap.put(5344, "SR");
        countryCodeMap.put(5120, "SE");
        countryCodeMap.put(5259, "SZ");
        countryCodeMap.put(5398, "XX");
        countryCodeMap.put(5262, "ZA");
        countryCodeMap.put(5260, "SS");
        countryCodeMap.put(5486, "SY");
        countryCodeMap.put(5722, "TJ");
        countryCodeMap.put(5424, "TW");
        countryCodeMap.put(5266, "TZ");
        countryCodeMap.put(5292, "TD");
        countryCodeMap.put(5492, "TH");
        countryCodeMap.put(5776, "CZ");
        countryCodeMap.put(5129, "CS");
        countryCodeMap.put(5293, "TG");
        countryCodeMap.put(5505, "TO");
        countryCodeMap.put(5374, "TT");
        countryCodeMap.put(5268, "TN");
        countryCodeMap.put(5718, "TM");
        countryCodeMap.put(5273, "TV");
        countryCodeMap.put(5172, "TR");
        countryCodeMap.put(5180, "DE");
        countryCodeMap.put(5102, "XX");
        countryCodeMap.put(5269, "UG");
        countryCodeMap.put(5704, "UA");
        countryCodeMap.put(5174, "HU");
        countryCodeMap.put(5376, "UY");
        countryCodeMap.put(5390, "US");
        countryCodeMap.put(5714, "UZ");
        countryCodeMap.put(5275, "VU");
        countryCodeMap.put(5176, "VA");
        countryCodeMap.put(5392, "VE");
        countryCodeMap.put(5395, "VG");
        countryCodeMap.put(5488, "VN");
        countryCodeMap.put(5402, "YE");
        countryCodeMap.put(5296, "ZM");
        countryCodeMap.put(5295, "ZW");
        countryCodeMap.put(5230, "GQ");
        countryCodeMap.put(5599, "XX");
        countryCodeMap.put(5182, "AT");
        countryCodeMap.put(5435, "TL");
    }

}
