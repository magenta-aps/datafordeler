package dk.magenta.datafordeler.cvr.data.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.cvr.data.CvrOutputWrapper;
import dk.magenta.datafordeler.cvr.data.shared.LifecycleData;
import dk.magenta.datafordeler.cvr.data.shared.ParticipantRelationData;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.CompanyForm;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * A class for formatting a CompanyEntity to JSON, for FAPI output. The data hierarchy
 * under a Company is sorted into this format:
 * {
 *     "UUID": <company uuid>
 *     "cvrnummer": <company cvr number>
 *     "id": {
 *         "domaene": <company domain>
 *     },
 *     registreringer: [
 *          {
 *              "registreringFra": <registrationFrom>,
 *              "registreringTil": <registrationTo>,
 *              "navn": [
 *              {
 *                  "navn": <companyName1>
 *                  "virkningFra": <effectFrom1>
 *                  "virkningTil": <effectTo1>
 *              },
 *              {
 *                  "navn": <companyName2>
 *                  "virkningFra": <effectFrom2>
 *                  "virkningTil": <effectTo2>
 *              }
 *              ]
 *          }
 *     ]
 * }
 */
public class CompanyOutputWrapper extends CvrOutputWrapper<CompanyEntity> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object wrapResult(CompanyEntity input, BaseQuery query) {

        // Root
        ObjectNode root = objectMapper.createObjectNode();
        root.put(CompanyEntity.IO_FIELD_UUID, input.getUUID().toString());
        root.put(CompanyEntity.IO_FIELD_CVR, input.getCvrNumber());
        root.putPOJO("id", input.getIdentification());

        // Registrations
        ArrayNode registreringer = objectMapper.createArrayNode();
        root.set(CompanyEntity.IO_FIELD_REGISTRATIONS, registreringer);
        for (CompanyRegistration companyRegistration : input.getRegistrations()) {
            registreringer.add(wrapRegistrering(companyRegistration));
        }

        return root;
    }

    protected ObjectNode wrapRegistrering(CompanyRegistration input) {
        ObjectNode output = objectMapper.createObjectNode();

        output.put(
                CompanyRegistration.IO_FIELD_REGISTRATION_FROM,
                input.getRegistrationFrom() != null ? input.getRegistrationFrom().toString() : null
        );
        output.put(
                CompanyRegistration.IO_FIELD_REGISTRATION_TO,
                input.getRegistrationTo() != null ? input.getRegistrationTo().toString() : null
        );

        for (CompanyEffect virkning : input.getEffects()) {

            for (CompanyBaseData companyBaseData : virkning.getDataItems()) {

                OffsetDateTime timestamp = companyBaseData.getLastUpdated();
                String companyName = companyBaseData.getCompanyName();
                if (companyName != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_NAME,  createSimpleNode(virkning, timestamp, "navn", companyName));
                }

                CompanyForm companyForm = companyBaseData.getCompanyForm();
                if (companyForm != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_FORM, createFormNode(virkning, timestamp, companyForm));
                }

                Address locationAddress = companyBaseData.getLocationAddress();
                if (locationAddress != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_LOCATION_ADDRESS, createAddressNode(virkning, timestamp, locationAddress));
                }

                Address postalAddress = companyBaseData.getLocationAddress();
                if (postalAddress != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_POSTAL_ADDRESS, createAddressNode(virkning, timestamp, postalAddress));
                }

                Industry primaryIndustry = companyBaseData.getPrimaryIndustry();
                if (primaryIndustry != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_PRIMARY_INDUSTRY, createIndustryNode(virkning, timestamp, primaryIndustry));
                }

                Industry secondaryIndustry1 = companyBaseData.getSecondaryIndustry1();
                if (secondaryIndustry1 != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_SECONDARY_INDUSTRY_1, createIndustryNode(virkning, timestamp, secondaryIndustry1));
                }

                Industry secondaryIndustry2 = companyBaseData.getSecondaryIndustry1();
                if (secondaryIndustry2 != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_SECONDARY_INDUSTRY_2, createIndustryNode(virkning, timestamp, secondaryIndustry2));
                }

                Industry secondaryIndustry3 = companyBaseData.getSecondaryIndustry1();
                if (secondaryIndustry3 != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_SECONDARY_INDUSTRY_3, createIndustryNode(virkning, timestamp, secondaryIndustry3));
                }

                Boolean advertProtection = companyBaseData.getAdvertProtection();
                if (advertProtection != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_ADVERTPROTECTION, createSimpleNode(virkning, timestamp, "beskyttelse", advertProtection));
                }

                String phone = companyBaseData.getPhoneNumber();
                if (phone != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_PHONENUMBER, createSimpleNode(virkning, timestamp, "nummer", phone));
                }

                String fax = companyBaseData.getFaxNumber();
                if (fax != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_FAXNUMBER, createSimpleNode(virkning, timestamp, "nummer", fax));
                }

                String email = companyBaseData.getFaxNumber();
                if (email != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_EMAIL, createSimpleNode(virkning, timestamp, "adresse", email));
                }

                LifecycleData lifecycle = companyBaseData.getLifecycleData();
                if (lifecycle != null) {
                    this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_LIFECYCLE, createLifecycleNode(virkning, timestamp, lifecycle));
                }

                Set<ParticipantRelationData> participantRelationData = companyBaseData.getParticipantRelations();
                if (participantRelationData != null && !participantRelationData.isEmpty()) {
                    for (ParticipantRelationData participantRelation : participantRelationData) {
                        this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_PARTICIPANT_RELATIONS, createCompanyParticipantNode(virkning, timestamp, participantRelation));
                    }
                }

                Set<CompanyUnitLink> unitRelationData = companyBaseData.getUnitData();
                if (unitRelationData != null && !unitRelationData.isEmpty()) {
                    for (CompanyUnitLink unitRelation : unitRelationData) {
                        this.addEffectDataToRegistration(output, CompanyBaseData.IO_FIELD_UNITS, createCompanyUnitLinkNode(virkning, timestamp, unitRelation));
                    }
                }

            }
        }
        return output;
    }

    private ObjectNode createFormNode(Effect virkning, OffsetDateTime lastUpdated, CompanyForm form) {
        ObjectNode formNode = createVirkning(virkning, lastUpdated);
        formNode.put(CompanyForm.IO_FIELD_CODE, form.getCompanyFormCode());
        formNode.put(CompanyForm.IO_FIELD_SOURCE, form.getResponsibleDataSource());
        return formNode;
    }

    private ObjectNode createCompanyParticipantNode(Effect virkning, OffsetDateTime lastUpdated, ParticipantRelationData participantRelation) {
        ObjectNode participantRelationNode = createVirkning(virkning, lastUpdated);
        participantRelationNode.set(ParticipantRelationData.IO_FIELD_PARTICIPANT, this.createIdentificationNode(participantRelation.getParticipant()));
        return participantRelationNode;
    }

    private ObjectNode createCompanyUnitLinkNode(Effect virkning, OffsetDateTime lastUpdated, CompanyUnitLink unitLink) {
        ObjectNode unitLinkNode = createVirkning(virkning, lastUpdated);
        unitLinkNode.put(CompanyUnitLink.IO_FIELD_PNUMBER, unitLink.getpNumber());
        unitLinkNode.set(CompanyUnitLink.IO_FIELD_IDENTIFICATION, this.createIdentificationNode(unitLink.getIdentification()));
        return unitLinkNode;
    }

}
