package dk.magenta.datafordeler.cpr.data.person;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import dk.magenta.datafordeler.cpr.data.person.data.*;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PersonOutputWrapper extends OutputWrapper<PersonEntity> {

    private ObjectMapper objectMapper;

    @Override
    public Object wrapResult(PersonEntity input, BaseQuery query) {
        objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();
        root.put(PersonEntity.IO_FIELD_UUID, input.getUUID().toString());
        root.put(PersonEntity.IO_FIELD_DOMAIN, input.getDomain());
        root.put(PersonEntity.IO_FIELD_CPR_NUMBER, input.getPersonnummer());
        CprBitemporality overlap = new CprBitemporality(query.getRegistrationFrom(), query.getRegistrationTo(), query.getEffectFrom(), query.getEffectTo());
        ArrayNode registreringer = this.getRegistrations(input, overlap);
        root.set(PersonEntity.IO_FIELD_REGISTRATIONS, registreringer);
        return root;
    }

    public ArrayNode getRegistrations(PersonEntity entity, CprBitemporality mustOverlap) {

        ArrayNode registrationsNode = objectMapper.createArrayNode();
        //HashMap<Bitemporality, ObjectNode> data = new HashMap<>();
        ListHashMap<CprBitemporality, PersonBaseData> data = new ListHashMap<>();

        // Populér map med bitemp -> json
        // Loop over alle registrationBorders
        //     apply data i rækkefølge (sorteret efter bitemp)

        for (PersonRegistration registration : entity.getRegistrations()) {
            for (PersonEffect virkning : registration.getEffects()) {
                //ObjectNode dataPiece = objectMapper.createObjectNode();
                CprBitemporality bitemporality = new CprBitemporality(registration.getRegistrationFrom(), registration.getRegistrationTo(), virkning.getEffectFrom(), virkning.getEffectTo());
                for (PersonBaseData personBaseData : virkning.getDataItems()) {
                    data.add(bitemporality, personBaseData);
                }
            }
        }

        ListHashMap<OffsetDateTime, CprBitemporality> startTerminators = new ListHashMap<>();
        ListHashMap<OffsetDateTime, CprBitemporality> endTerminators = new ListHashMap<>();

        for (CprBitemporality bitemporality : data.keySet()) {
            startTerminators.add(bitemporality.registrationFrom, bitemporality);
            endTerminators.add(bitemporality.registrationTo, bitemporality);
        }

        HashSet<OffsetDateTime> allTerminators = new HashSet<>();
        allTerminators.addAll(startTerminators.keySet());
        allTerminators.addAll(endTerminators.keySet());
        // Create a sorted list of all timestamps where Bitemporalities either begin or end
        ArrayList<OffsetDateTime> terminators = new ArrayList<>(allTerminators);
        terminators.sort(Comparator.nullsFirst(OffsetDateTime::compareTo));
        terminators.add(null);

        HashSet<CprBitemporality> presentBitemporalities = new HashSet<>();

        for (int i = 0; i < terminators.size(); i++) {
            OffsetDateTime t = terminators.get(i);
            List<CprBitemporality> startingHere = startTerminators.get(t);
            List<CprBitemporality> endingHere = (t != null) ? endTerminators.get(t) : null;
            if (startingHere != null) {
                presentBitemporalities.addAll(startingHere);
            }
            if (endingHere != null) {
                presentBitemporalities.removeAll(endingHere);
            }
            if (i < terminators.size() - 1) {
                OffsetDateTime next = terminators.get(i + 1);
                if (!presentBitemporalities.isEmpty()) {
                    if (mustOverlap == null || mustOverlap.overlapsRegistration(t, next)) {
                        ObjectNode registrationNode = objectMapper.createObjectNode();
                        registrationsNode.add(registrationNode);
                        registrationNode.put("registreringFra", formatTime(t));
                        registrationNode.put("registreringTil", formatTime(next));

                        ArrayList<CprBitemporality> sortedBitemporalities = new ArrayList<>(presentBitemporalities);
                        sortedBitemporalities.sort(CprBitemporality.effectComparator);

                        for (CprBitemporality bitemporality : sortedBitemporalities) {

                            ArrayList<PersonBaseData> dataItems = data.get(bitemporality);

                            for (PersonBaseData dataItem : dataItems) {
                                OffsetDateTime timestamp = dataItem.getLastUpdated();

                                PersonCoreData personCoreData = dataItem.getCoreData();
                                PersonStatusData personStatusData = dataItem.getStatus();
                                PersonPositionData personPositionData = dataItem.getPosition();
                                if (personCoreData != null) {
                                    if (personCoreData.getCprNumber() != null && !personCoreData.getCprNumber().isEmpty()) {
                                        addEffectDataToRegistration(
                                                registrationNode, PersonCoreData.IO_FIELD_CPR_NUMBER,
                                                createPersonNummerNode(bitemporality, timestamp, personCoreData)
                                        );
                                    }
                                    if (personCoreData.getGender() != null || personStatusData != null || personPositionData != null) {
                                        addEffectDataToRegistration(
                                                registrationNode,
                                                "person",
                                                createKerneDataNode(bitemporality, timestamp, personCoreData, personStatusData, personPositionData)
                                        );
                                    }
                                }

                                PersonBirthData personBirthData = dataItem.getBirth();
                                if (personBirthData != null) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            "fødselsdata",
                                            createFoedselNode(bitemporality, timestamp, personBirthData)
                                    );
                                }

                                PersonChurchData personChurchData = dataItem.getChurch();
                                if (personChurchData != null) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            "folkekirkeoplysning",
                                            createFolkekirkeoplysningNode(bitemporality, timestamp, personChurchData)
                                    );
                                }

                                PersonParentData personFatherData = dataItem.getFather();
                                if (personFatherData != null) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            "foraeldreoplysning",
                                            createForaeldreoplysningNode(bitemporality, timestamp, "FAR_MEDMOR", personFatherData)
                                    );
                                }
                                PersonParentData personMotherData = dataItem.getMother();
                                if (personMotherData != null) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            "foraeldreoplysning",
                                            createForaeldreoplysningNode(bitemporality, timestamp, "MOR", personMotherData)
                                    );
                                }
                                PersonAddressData personAddressData = dataItem.getAddress();
                                PersonAddressConameData personAddressConameData = dataItem.getConame();
                                PersonMoveMunicipalityData personMoveMunicipalityData = dataItem.getMoveMunicipality();
                                if (personAddressData != null || personAddressConameData != null || personMoveMunicipalityData != null) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            "adresseoplysninger",
                                            createAdresseOplysningNode(bitemporality, timestamp, personAddressData, personAddressConameData, personMoveMunicipalityData)
                                    );
                                }

                                PersonNameData personNameData = dataItem.getName();
                                PersonAddressNameData personAddressNameData = dataItem.getAddressingName();
                                if (personNameData != null || personAddressNameData != null) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            PersonBaseData.IO_FIELD_NAME,
                                            createNavnNode(bitemporality, timestamp, personNameData, personAddressNameData)
                                    );
                                }

                                Collection<PersonProtectionData> personProtectionData = dataItem.getProtection();
                                if (personProtectionData != null && !personProtectionData.isEmpty()) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            PersonBaseData.IO_FIELD_PROTECTION,
                                            createBeskyttelseNode(bitemporality, timestamp, personProtectionData)
                                    );
                                }

                                PersonEmigrationData personEmigrationData = dataItem.getMigration();
                                PersonForeignAddressData personForeignAddressData = dataItem.getForeignAddress();
                                if (personEmigrationData != null) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            "udrejseindrejse",
                                            createUdrejseIndrejseNode(bitemporality, timestamp, personEmigrationData, personForeignAddressData)
                                    );
                                }

                                PersonNameAuthorityTextData personNameAuthorityTextData = dataItem.getNameAuthority();
                                if (personNameAuthorityTextData != null) {
                                    addEffectDataToRegistration(
                                            registrationNode,
                                            PersonBaseData.IO_FIELD_NAME_AUTHORITY,
                                            createNavneMyndighedNode(bitemporality, timestamp, personNameAuthorityTextData)
                                    );
                                }

                            }
                        }
                    }
                }
            }
        }
        return registrationsNode;
    }

    protected static String formatTime(OffsetDateTime time) {
        return formatTime(time, false);
    }

    protected static String formatTime(OffsetDateTime time, boolean asDateOnly) {
        if (time == null) return null;
        return time.format(asDateOnly ? DateTimeFormatter.ISO_LOCAL_DATE : DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    protected static String formatTime(LocalDate time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    protected void addEffectDataToRegistration(ObjectNode output, String key, JsonNode value) {
        if (!output.has(key) || output.get(key).isNull()) {
            output.set(key, objectMapper.createArrayNode());
        }
        ((ArrayNode) output.get(key)).add(value);
    }

    protected ObjectNode createDataNode(CprBitemporality bitemporality, OffsetDateTime lastUpdated) {
        return createDataNode(bitemporality, true, lastUpdated);
    }

    protected ObjectNode createDataNode(CprBitemporality bitemporality, boolean includeVirkningTil, OffsetDateTime lastUpdated) {
        ObjectNode output = objectMapper.createObjectNode();
        if (bitemporality != null) {
            output.put(
                    PersonEffect.IO_FIELD_EFFECT_FROM,
                    OffsetDateTimeAdapter.toString(bitemporality.effectFrom)
            );
            if (includeVirkningTil) {
                output.put(
                        PersonEffect.IO_FIELD_EFFECT_TO,
                        OffsetDateTimeAdapter.toString(bitemporality.effectTo)
                );
            }
        }
        output.put(
                PersonBaseData.IO_FIELD_LAST_UPDATED,
                lastUpdated != null ? lastUpdated.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );
        return output;
    }

    protected ObjectNode createPersonNummerNode(CprBitemporality bitemporality, OffsetDateTime lastUpdated, PersonCoreData personCoreData) {
        ObjectNode personnummer = createDataNode(bitemporality, lastUpdated);
        personnummer.put(PersonCoreData.IO_FIELD_CPR_NUMBER, personCoreData.getCprNumber());
        // TODO: Personnummer status enum?
        return personnummer;
    }

    protected ObjectNode createKerneDataNode(
            CprBitemporality bitemporality, OffsetDateTime lastUpdated, PersonCoreData personCoreData, PersonStatusData personStatusData,
            PersonPositionData stilling
    ) {
        ObjectNode output = createDataNode(bitemporality, lastUpdated);
        if (personCoreData.getGender() != null) {
            output.put(
                    PersonCoreData.IO_FIELD_GENDER,
                    personCoreData.getGender().toString()
            );
        }
        if (personStatusData != null) {
            output.put(
                    PersonStatusData.IO_FIELD_STATUS,
                    personStatusData.getStatus()
            );
        }
        if (stilling != null) {
            output.put(
                    PersonPositionData.IO_FIELD_POSITION,
                    stilling.getPosition()
            );
        }
        return output;
    }

    protected ObjectNode createFoedselNode(
            CprBitemporality bitemporality, OffsetDateTime lastUpdated, PersonBirthData personBirthData
    ) {
        ObjectNode output = createDataNode(bitemporality, true, lastUpdated);
        if (personBirthData.getBirthDatetime() != null) {
            output.put(
                    PersonBirthData.IO_FIELD_BIRTH_DATETIME,
                    personBirthData.getBirthDatetime().toLocalDate().toString()
            );
            output.put(
                    PersonBirthData.IO_FIELD_BIRTH_DATETIME_UNCERTAIN,
                    personBirthData.isBirthDatetimeUncertain()
            );
        }
        if (personBirthData.getBirthPlaceCode() != null) {
            output.put(
                    PersonBirthData.IO_FIELD_BIRTH_PLACE_CODE,
                    personBirthData.getBirthPlaceCode()
            );
        }
        if (personBirthData.getBirthAuthorityText() != null) {
            output.put(
                    PersonBirthData.IO_FIELD_BIRTH_AUTHORITY_TEXT,
                    personBirthData.getBirthAuthorityText()
            );
        }
        if (personBirthData.getBirthPlaceName() != null) {
            output.put(
                    PersonBirthData.IO_FIELD_BIRTH_PLACE_NAME,
                    personBirthData.getBirthPlaceName()
            );
        }
        if (personBirthData.getBirthSupplementalText() != null) {
            output.put(
                    PersonBirthData.IO_FIELD_BIRTH_SUPPLEMENTAL_TEXT,
                    personBirthData.getBirthSupplementalText()
            );
        }
        return output;
    }

    protected ObjectNode createFolkekirkeoplysningNode(
            CprBitemporality bitemporality, OffsetDateTime lastUpdated, PersonChurchData personChurchData
    ) {
        ObjectNode output = createDataNode(bitemporality, true, lastUpdated);
        Character relation = personChurchData.getChurchRelation();
        output.put(PersonChurchData.IO_FIELD_CHURCH_RELATION, relation != null ? relation.toString() : null);
        return output;
    }

    protected ObjectNode createForaeldreoplysningNode(
            CprBitemporality bitemporality, OffsetDateTime lastUpdated, String foraelderrolle, PersonParentData personParentData
    ) {
        ObjectNode output = createDataNode(bitemporality, false, lastUpdated);
        output.put(PersonParentData.IO_FIELD_CPR_NUMBER, personParentData.getCprNumber());
        output.put("foraelderrolle", foraelderrolle);
        return output;
    }

    protected ObjectNode createAdresseOplysningNode(
            CprBitemporality bitemporality, OffsetDateTime lastUpdated, PersonAddressData adresse, PersonAddressConameData conavn,
            PersonMoveMunicipalityData flytteKommune
    ) {
        ObjectNode output = createDataNode(bitemporality, lastUpdated);
        output.put(
                PersonAddressConameData.IO_FIELD_CONAME,
                conavn != null ? conavn.getConame() : null
        );
        if (flytteKommune != null) {
            output.put(
                PersonMoveMunicipalityData.IO_FIELD_OUT_DATETIME,
                flytteKommune.getOutDatetime() != null ?
                    flytteKommune.getOutDatetime().toLocalDate().toString() :
                    null
            );
            output.put(
                    PersonMoveMunicipalityData.IO_FIELD_OUT_MUNICIPALITY,
                    flytteKommune.getOutMunicipality()
            );
            output.put(
                PersonMoveMunicipalityData.IO_FIELD_IN_DATETIME,
                flytteKommune.getInDatetime() != null ?
                    flytteKommune.getInDatetime().toLocalDate().toString() :
                    null
            );
        }
        output.set(PersonBaseData.IO_FIELD_ADDRESS, createCprAdresseNode(adresse));
        return output;
    }

    protected ObjectNode createCprAdresseNode(PersonAddressData adresse) {
        if (adresse != null) {
            ObjectNode output = objectMapper.createObjectNode();
            output.put(PersonAddressData.IO_FIELD_BUILDING_NUMBER, adresse.getBuildingNumber());
            output.put(PersonAddressData.IO_FIELD_CITY_NAME, adresse.getCityName());
            output.put(PersonAddressData.IO_FIELD_MUNICIPALITY_CODE, adresse.getMunicipalityCode());
            output.put(PersonAddressData.IO_FIELD_MUNICIPALITY_NAME, adresse.getMunicipalityName());
            output.put(PersonAddressData.IO_FIELD_ROAD_CODE, adresse.getRoadCode());
            output.put(PersonAddressData.IO_FIELD_FLOOR, adresse.getFloor());
            output.put(PersonAddressData.IO_FIELD_HOUSENUMBER, adresse.getHouseNumber());
            output.put(PersonAddressData.IO_FIELD_POSTAL_DISTRICT, adresse.getPostalDistrict());
            output.put(PersonAddressData.IO_FIELD_POSTAL_CODE, adresse.getPostalCode());
            output.put(PersonAddressData.IO_FIELD_DOOR, adresse.getDoor());
            output.put(PersonAddressData.IO_FIELD_ROAD_ADDRESS_NAME, adresse.getRoadAddressName());
            return output;
        }
        return null;
    }

    protected ObjectNode createNavnNode(
            CprBitemporality bitemporality, OffsetDateTime lastUpdated, PersonNameData navn, PersonAddressNameData addresseringsnavn
    ) {
        ObjectNode output = createDataNode(bitemporality, lastUpdated);
        output.put(
                PersonBaseData.IO_FIELD_ADDRESSING_NAME,
                addresseringsnavn != null ? addresseringsnavn.getAddressName() : null
        );
        if (navn != null) {
            output.put(
                PersonNameData.IO_FIELD_LAST_NAME,
                navn.getLastName()
            );
            output.put(
                PersonNameData.IO_FIELD_FIRST_NAMES,
                navn.getFirstNames()
            );
            output.put(
                PersonNameData.IO_FIELD_MIDDLE_NAME,
                navn.getMiddleName()
            );
        } else {
            output.putNull(PersonNameData.IO_FIELD_LAST_NAME);
            output.putNull(PersonNameData.IO_FIELD_FIRST_NAMES);
            output.putNull(PersonNameData.IO_FIELD_MIDDLE_NAME);
        }

        return output;
    }

    protected ArrayNode createBeskyttelseNode(CprBitemporality bitemporality, OffsetDateTime lastUpdated, Collection<PersonProtectionData> beskyttelse) {
        ArrayNode output = objectMapper.createArrayNode();
        for (PersonProtectionData personProtectionData : beskyttelse) {
            ObjectNode item = createDataNode(bitemporality, lastUpdated);
            item.put(PersonProtectionData.IO_FIELD_TYPE, personProtectionData.getProtectionType());
            output.add(item);
        }
        return output;
    }

    protected ObjectNode createUdrejseIndrejseNode(
            CprBitemporality bitemporality, OffsetDateTime lastUpdated, PersonEmigrationData udrejseIndrejse,
            PersonForeignAddressData udenlandsadresse
    ) {
        ObjectNode output = createDataNode(bitemporality, lastUpdated);
        output.put(PersonEmigrationData.IO_FIELD_COUNTRY_CODE, udrejseIndrejse.getCountryCode());
        return output;
    }

    protected ObjectNode createUdrejseAdresseNode(PersonForeignAddressData udenlandsadresse) {
        ObjectNode output = objectMapper.createObjectNode();
        output.put(PersonForeignAddressData.IO_FIELD_ADDRESS_LINE1, udenlandsadresse.getAddressLine1());
        output.put(PersonForeignAddressData.IO_FIELD_ADDRESS_LINE2, udenlandsadresse.getAddressLine2());
        output.put(PersonForeignAddressData.IO_FIELD_ADDRESS_LINE3, udenlandsadresse.getAddressLine3());
        output.put(PersonForeignAddressData.IO_FIELD_ADDRESS_LINE4, udenlandsadresse.getAddressLine4());
        output.put(PersonForeignAddressData.IO_FIELD_ADDRESS_LINE5, udenlandsadresse.getAddressLine5());
        return output;
    }

    protected ObjectNode createNavneMyndighedNode(CprBitemporality bitemporality, OffsetDateTime lastUpdated, PersonNameAuthorityTextData navnemyndighed) {
        ObjectNode output = createDataNode(bitemporality, lastUpdated);
        output.put(PersonNameAuthorityTextData.IO_FIELD_AUTHORITY, navnemyndighed.getText());
        return output;
    }
}
