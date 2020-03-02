package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRegistration;
import dk.magenta.datafordeler.cpr.data.person.data.*;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PersonOutputWrapperPrisme extends OutputWrapper<PersonEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    private LookupService lookupService;

    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    private <T extends CprBitemporalPersonRecord> T getLatest(Collection<T> records) {
        //OffsetDateTime latestEffect = OffsetDateTime.MIN;
        OffsetDateTime latestRegistration = OffsetDateTime.MIN;
        ArrayList<T> latest = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        Bitemporality currentBitemp = new Bitemporality(now, now, now, now);
        OffsetDateTime latestUpdated = OffsetDateTime.MIN;
        for (T record : records) {
            //if (record.getEffectTo() == null || (latestEffect != null && record.getEffectTo().isAfter(latestEffect))) {
            //    latestEffect = record.getEffectTo();
            if (record.getBitemporality().contains(currentBitemp) && !record.getDafoUpdated().isBefore(latestUpdated)) {
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

    public Object wrapRecordResult(PersonEntity input, BaseQuery query) {

        // Root
        NodeWrapper root = new NodeWrapper(objectMapper.createObjectNode());
        root.put("cprNummer", input.getPersonnummer());



        NameDataRecord nameData = this.getLatest(input.getName());
        if (nameData != null) {
            StringJoiner nameJoiner = new StringJoiner(" ");
            if (!nameData.getFirstNames().isEmpty()) {
                nameJoiner.add(nameData.getFirstNames());
            }
            if (!nameData.getMiddleName().isEmpty()) {
                nameJoiner.add(nameData.getMiddleName());
            }
            if (nameJoiner.length() > 0) {
                root.put("fornavn", nameJoiner.toString());
            }
            if (nameData.getLastName() != null && !nameData.getLastName().isEmpty()) {
                root.put("efternavn", nameData.getLastName());
            }
        }

        CivilStatusDataRecord civilStatusData = this.getLatest(input.getCivilstatus());
        if (civilStatusData != null) {
            root.put("civilstand", civilStatusData.getCivilStatus());
            root.put("civilstandsdato", formatDate(civilStatusData.getEffectFrom()));
            if (!civilStatusData.getSpouseCpr().isEmpty()) {
                root.put("ægtefælleCprNummer", civilStatusData.getSpouseCpr());
            }
        }

        root.put("adressebeskyttelse", false);
        /*Collection<ProtectionDataRecord> personProtectionData = input.getProtection();
        if (personProtectionData != null && !personProtectionData.isEmpty()) {
            for (ProtectionDataRecord personProtectionDataItem : personProtectionData) {
                if (personProtectionDataItem.getRegistrationTo() == null && (personProtectionDataItem.getEffectTo() == null || personProtectionDataItem.getEffectTo().isAfter(OffsetDateTime.now())) && personProtectionDataItem.getProtectionType() == 1) {
                    root.put("adressebeskyttelse", true);
                    break;
                }
            }
        }*/

        PersonCoreDataRecord personCoreData = this.getLatest(input.getCore());
        if (personCoreData != null) {
            if (personCoreData.getGender() != null) {
                root.put("køn", (personCoreData.getGender() == PersonCoreDataRecord.Koen.KVINDE) ? "K" : "M");
            }
        }

        PersonNumberDataRecord personNumberDataRecord = this.getLatest(input.getPersonNumber());
        if (personNumberDataRecord != null) {
            String newPnr = personNumberDataRecord.getCprNumber();
            if (newPnr != null && !newPnr.isEmpty() && !input.getPersonnummer().equals(newPnr)) {
                root.put("nytCprNummer", newPnr);
            }
        }

        PersonStatusDataRecord personStatusData = this.getLatest(input.getStatus());
        if (personStatusData != null) {
            root.put("statuskode", personStatusData.getStatus());
            root.put("statuskodedato", this.formatDate(
                    personStatusData.getEffectFrom() != null ? personStatusData.getEffectFrom() : personStatusData.getRegistrationFrom()
            ));
        }

        ForeignAddressDataRecord personForeignAddressData = this.getLatest(input.getForeignAddress());
        AddressDataRecord personAddressData = this.getLatest(input.getAddress());

        if (personForeignAddressData != null && (personAddressData == null || personForeignAddressData.getEffectFrom().isAfter(personAddressData.getEffectFrom()))) {
            String address = personForeignAddressData.join("\n");
            root.put("udlandsadresse", address);
            ForeignAddressEmigrationDataRecord personEmigrationData = this.getLatest(input.getEmigration());
            if (personEmigrationData != null) {
                root.put("landekode", countryCodeMap.get(personEmigrationData.getEmigrationCountryCode()));
                root.put("udrejsedato", formatDate(personEmigrationData.getEffectFrom()));
            }
        } else {
            if (personAddressData != null) {
                root.put("tilflytningsdato", formatDate(personAddressData.getEffectFrom()));
                int municipalityCode = personAddressData.getMunicipalityCode();
                root.put("myndighedskode", municipalityCode);
                int roadCode = personAddressData.getRoadCode();
                String houseNumber = personAddressData.getHouseNumber();
                if (roadCode > 0) {
                    root.put("vejkode", roadCode);

                    Lookup lookup = lookupService.doLookup(municipalityCode, roadCode, houseNumber);

                    root.put("kommune", lookup.municipalityName);

                    String buildingNumber = municipalityCode >= 950 ? personAddressData.getBuildingNumber() : null;
                    String roadName = lookup.roadName;
                    if (roadName != null) {
                        root.put("adresse", this.getAddressFormatted(
                                roadName,
                                personAddressData.getHouseNumber(),
                                null,
                                null, null,
                                personAddressData.getFloor(),
                                personAddressData.getDoor(),
                                buildingNumber
                        ));
                    } else if (buildingNumber != null && !buildingNumber.isEmpty()) {
                        root.put("adresse", formatBNumber(buildingNumber));
                    }

                    root.put("postnummer", lookup.postalCode);
                    root.put("bynavn", lookup.postalDistrict);
                    root.put("stedkode", lookup.localityCode);
                }

                if (municipalityCode > 0 && municipalityCode < 900) {
                    root.put("landekode", "DK");
                } else if (municipalityCode > 900) {
                    root.put("landekode", "GL");
                }
            }
        }

        AddressConameDataRecord personAddressConameData = this.getLatest(input.getConame());
        if (personAddressConameData != null && !personAddressConameData.getConame().isEmpty()) {
            String coname = personAddressConameData.getConame();
            if (coname != null) {
                coname = coname.toLowerCase();
                Matcher m = postboxExtract.matcher(coname);
                if (m.find()) {
                    try {
                        int postbox = Integer.parseInt(m.group(1), 10);
                        root.put("postboks", postbox);
                    } catch (NumberFormatException e) {}
                }
            }
        }
        return root.getNode();
    }

    @Override
    public Object wrapResult(PersonEntity input, BaseQuery query) {
        // Root
        NodeWrapper root = new NodeWrapper(objectMapper.createObjectNode());
        root.put("cprNummer", input.getPersonnummer());

        root.put("adressebeskyttelse", false);

        OffsetDateTime highestStatusTime = OffsetDateTime.MIN;
        OffsetDateTime highestCivilStatusTime = OffsetDateTime.MIN;
        OffsetDateTime highestEmigrationTime = OffsetDateTime.MIN;
        OffsetDateTime highestAddressTime = OffsetDateTime.MIN;
        // Registrations
        List<PersonRegistration> registrations = input.getRegistrations();
        if (registrations != null && !registrations.isEmpty()) {
            //    PersonRegistration personRegistration = registrations.get(registrations.size()-1);
            for (PersonRegistration personRegistration : registrations) {
                if (personRegistration.getRegistrationTo() == null) {
                    for (PersonEffect virkning : personRegistration.getEffects()) {
                        if (virkning.getEffectTo() == null) {
                            OffsetDateTime effectFrom = virkning.getEffectFrom();
                            ArrayList<PersonBaseData> dataItems = new ArrayList<>(virkning.getDataItems());
                            dataItems.sort(Comparator.comparing(d -> d.getLastUpdated()));
                            for (PersonBaseData personBaseData : dataItems) {
                                this.wrapDataObject(root, personBaseData, input);
                            }
                            if (effectFrom != null) {
                                if (effectFrom.isAfter(highestStatusTime)) {
                                    for (PersonBaseData personBaseData : dataItems) {
                                        if (personBaseData.getStatus() != null) {
                                            highestStatusTime = effectFrom;
                                        }
                                    }
                                }
                                if (effectFrom.isAfter(highestCivilStatusTime)) {
                                    for (PersonBaseData personBaseData : dataItems) {
                                        if (personBaseData.getCivilStatus() != null) {
                                            highestCivilStatusTime = effectFrom;
                                        }
                                    }
                                }
                                if (effectFrom.isAfter(highestEmigrationTime)) {
                                    for (PersonBaseData personBaseData : dataItems) {
                                        if (personBaseData.getMigration() != null) {
                                            highestEmigrationTime = effectFrom;
                                        }
                                    }
                                }
                                if (effectFrom.isAfter(highestAddressTime)) {
                                    for (PersonBaseData personBaseData : dataItems) {
                                        if (personBaseData.getAddress() != null) {
                                            highestAddressTime = effectFrom;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (highestStatusTime.isAfter(OffsetDateTime.MIN)) {
            root.put("statuskodedato", highestStatusTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (highestCivilStatusTime.isAfter(OffsetDateTime.MIN)) {
            root.put("civilstandsdato", highestCivilStatusTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (highestEmigrationTime.isAfter(OffsetDateTime.MIN)) {
            root.put("udrejsedato", highestEmigrationTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (highestAddressTime.isAfter(OffsetDateTime.MIN)) {
            root.put("tilflytningsdato", highestAddressTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return root.getNode();
    }

    Pattern postboxExtract = Pattern.compile("bo(?:x|(?:ks))\\s*(\\d+)");

    protected void wrapDataObject(NodeWrapper output, PersonBaseData dataItem, PersonEntity entity) {

        PersonNameData nameData = dataItem.getName();
        if (nameData != null) {
            StringJoiner nameJoiner = new StringJoiner(" ");
            if (!nameData.getFirstNames().isEmpty()) {
                nameJoiner.add(nameData.getFirstNames());
            }
            if (!nameData.getMiddleName().isEmpty()) {
                nameJoiner.add(nameData.getMiddleName());
            }
            if (nameJoiner.length() > 0) {
                output.put("fornavn", nameJoiner.toString());
            }
            if (nameData.getLastName() != null && !nameData.getLastName().isEmpty()) {
                output.put("efternavn", nameData.getLastName());
            }
        }

        PersonCivilStatusData personCivilStatusData = dataItem.getCivilStatus();
        if (personCivilStatusData != null) {
            output.put("civilstand", personCivilStatusData.getCivilStatus());
            //output.put("civilstandsdato", getLastEffectTimeFormatted(dataItem.getEffects(), DateTimeFormatter.ISO_LOCAL_DATE));
            if (!personCivilStatusData.getSpouseCpr().isEmpty()) {
                output.put("ægtefælleCprNummer", personCivilStatusData.getSpouseCpr());
            }
        }

        Collection<PersonProtectionData> personProtectionData = dataItem.getProtection();
        if (personProtectionData != null && !personProtectionData.isEmpty()) {
            for (PersonProtectionData personProtectionDataItem : personProtectionData) {
                int protectionType = personProtectionDataItem.getProtectionType();
                if (protectionType == 1) { // Person- og adressebeskyttelse
                    output.put("adressebeskyttelse", true);
                }
            }
        }

        PersonForeignAddressData personForeignAddressData = dataItem.getForeignAddress();
        if (personForeignAddressData != null) {
            output.put("udlandsadresse", personForeignAddressData.join("\n"));
        }

        PersonEmigrationData personEmigrationData = dataItem.getMigration();
        if (personEmigrationData != null) {
            output.put("landekode", countryCodeMap.get(personEmigrationData.getCountryCode()));
            // output.put("udrejsedato", getLastEffectTimeFormatted(dataItem.getEffects(), DateTimeFormatter.ISO_LOCAL_DATE));
        }

        PersonCoreData personCoreData = dataItem.getCoreData();
        if (personCoreData != null) {
            if (personCoreData.getGender() != null) {
                output.put("køn", (personCoreData.getGender() == PersonCoreData.Koen.KVINDE) ? "K" : "M");
            }
            if (personCoreData.getCprNumber() != null && !personCoreData.getCprNumber().isEmpty() && !entity.getPersonnummer().equals(personCoreData.getCprNumber())) {
                output.put("nytCprNummer", personCoreData.getCprNumber());
            }
        }

        PersonStatusData personStatusData = dataItem.getStatus();
        if (personStatusData != null) {
            output.put("statuskode", personStatusData.getStatus());
            // output.put("statuskodedato", this.getLastEffectTimeFormatted(dataItem.getEffects(), DateTimeFormatter.ISO_LOCAL_DATE));
        }

        PersonAddressData personAddressData = dataItem.getAddress();
        if (personAddressData != null) {
            int municipalityCode = personAddressData.getMunicipalityCode();
            output.put("myndighedskode", municipalityCode);
            int roadCode = personAddressData.getRoadCode();
            String houseNumber = personAddressData.getHouseNumber();
            if (roadCode > 0) {
                output.put("vejkode", roadCode);

                Lookup lookup = lookupService.doLookup(municipalityCode, roadCode, houseNumber);

                output.put("kommune", lookup.municipalityName);

                String buildingNumber = municipalityCode >= 950 ? personAddressData.getBuildingNumber() : null;
                String roadName = lookup.roadName;
                if (roadName != null) {
                    output.put("adresse", this.getAddressFormatted(
                            roadName,
                            personAddressData.getHouseNumber(),
                            null,
                            null, null,
                            personAddressData.getFloor(),
                            personAddressData.getDoor(),
                            buildingNumber
                    ));
                } else if (buildingNumber != null && !buildingNumber.isEmpty()) {
                    output.put("adresse", formatBNumber(buildingNumber));
                }

                output.put("postnummer", lookup.postalCode);
                output.put("bynavn", lookup.postalDistrict);
                output.put("stedkode", lookup.localityCode);
            }

            if (municipalityCode > 0 && municipalityCode < 900) {
                output.put("landekode", "DK");
            } else if (municipalityCode > 900) {
                output.put("landekode", "GL");
            }
        }

        PersonAddressConameData personAddressConameData = dataItem.getConame();
        if (personAddressConameData != null && !personAddressConameData.getConame().isEmpty()) {
            String coname = personAddressConameData.getConame();
            if (coname != null) {
                coname = coname.toLowerCase();
                Matcher m = postboxExtract.matcher(coname);
                if (m.find()) {
                    try {
                        int postbox = Integer.parseInt(m.group(1), 10);
                        output.put("postboks", postbox);
                    } catch (NumberFormatException e) {}
                }
            }
        }
    }

    private String formatDate(OffsetDateTime dateTime) {
        if (dateTime != null) {
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return null;
    }

    private OffsetDateTime getLastEffectTime(Collection<? extends Effect> effects) {
        OffsetDateTime latest = OffsetDateTime.MIN;
        for (Effect effect : effects) {
            OffsetDateTime start = effect.getEffectFrom();
            if (start != null && start.isAfter(latest)) {
                latest = start;
            }
        }
        return latest;
    }

    private String getLastEffectTimeFormatted(Collection<? extends Effect> effects, DateTimeFormatter dateTimeFormatter) {
        OffsetDateTime statusKodeUpdated = this.getLastEffectTime(effects);
        if (statusKodeUpdated.isAfter(OffsetDateTime.MIN)) {
            return statusKodeUpdated.format(dateTimeFormatter);
        }
        return null;
    }

    public String getAddressFormatted(String roadName, String houseNumberFrom, String houseNumberTo, String letterFrom, String letterTo, String floor, String door, String bNumber) {
//        if (this.addressText != null) {
//            return this.addressText;
//        }
        StringBuilder out = new StringBuilder();

        if (roadName != null) {
            out.append(roadName);
        }
        if (door != null) {
            door = door.replaceAll("^0+", "");
        }

        if (houseNumberFrom != null && !houseNumberFrom.isEmpty()) {
            houseNumberFrom = houseNumberFrom.replaceAll("^0+", "");
            out.append(" " + houseNumberFrom + emptyIfNull(letterFrom));
            if (houseNumberTo != null && !houseNumberTo.isEmpty()) {
                out.append("-");
                houseNumberTo = houseNumberTo.replaceAll("^0+", "");
                if (houseNumberTo.equals(houseNumberFrom)) {
                    out.append(emptyIfNull(letterTo));
                } else {
                    out.append(houseNumberTo + emptyIfNull(letterTo));
                }
            }

            if (floor != null && !floor.isEmpty()) {
                out.append(", " + floor + ".");
                if (door != null && !door.isEmpty()) {
                    out.append(" " + door);
                }
            } else if (door != null && !door.isEmpty()) {
                out.append(", " + door);
            }
        } else {
            if (floor != null && !floor.isEmpty()) {
                out.append(" " + floor + ".");
            }
            if (door != null && !door.isEmpty()) {
                out.append(" " + door);
            }
        }

        if (bNumber != null && !bNumber.isEmpty()) {
            out.append(" (" + formatBNumber(bNumber) + ")");
        }

        String result = out.toString().trim();
        if (result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }

    private static String formatBNumber(String bnr) {
        bnr = bnr.replaceAll("^0+", "");
        bnr = bnr.replaceAll("^B-?", "");
        return "B-" + bnr;
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
