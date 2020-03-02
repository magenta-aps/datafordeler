package dk.magenta.datafordeler.cvr.data.companyunit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.cvr.data.CvrOutputWrapper;
import dk.magenta.datafordeler.cvr.data.shared.LifecycleData;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;

import java.time.OffsetDateTime;
import java.util.List;

public class CompanyUnitOutputWrapper extends CvrOutputWrapper<CompanyUnitEntity> {

    private ObjectMapper objectMapper;

    @Override
    public Object wrapResult(CompanyUnitEntity input, BaseQuery query) {

        objectMapper = new ObjectMapper();

        // Root
        ObjectNode root = objectMapper.createObjectNode();

        root.put(CompanyUnitEntity.IO_FIELD_UUID, input.getUUID().toString());
        root.put(CompanyUnitEntity.IO_FIELD_PNUMBER, input.getPNumber());
        root.putPOJO("id", input.getIdentification());

        // Registreringer
        ArrayNode registreringer = objectMapper.createArrayNode();
        root.set(CompanyUnitEntity.IO_FIELD_REGISTRATIONS, registreringer);

        for (CompanyUnitRegistration companyUnitRegistration : input.getRegistrations()) {
            registreringer.add(wrapRegistrering(companyUnitRegistration));
        }

        return root;
    }

    protected ObjectNode wrapRegistrering(CompanyUnitRegistration input) {
        ObjectNode output = objectMapper.createObjectNode();

        output.put(
                CompanyUnitRegistration.IO_FIELD_REGISTRATION_FROM,
                input.getRegistrationFrom() != null ? input.getRegistrationFrom().toString() : null
        );
        output.put(
                CompanyUnitRegistration.IO_FIELD_REGISTRATION_TO,
                input.getRegistrationTo() != null ? input.getRegistrationTo().toString() : null
        );

        for (CompanyUnitEffect virkning : input.getEffects()) {
            for (CompanyUnitBaseData companyUnitBaseData : virkning.getDataItems()) {

                OffsetDateTime timestamp = companyUnitBaseData.getLastUpdated();

                String name = companyUnitBaseData.getName();
                if (name != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_NAME, createSimpleNode(virkning, timestamp, "navn", name));
                }

                List<Long> associatedCvrNumbers = companyUnitBaseData.getAssociatedCvrNumber();
                if (associatedCvrNumbers != null && !associatedCvrNumbers.isEmpty()) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_CVR_NUMBER, createListNode(virkning, timestamp, "numre", associatedCvrNumbers));
                }

                Address locationAddress = companyUnitBaseData.getLocationAddress();
                if (locationAddress != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_LOCATION_ADDRESS, createAddressNode(virkning, timestamp, locationAddress));
                }

                Address postalAddress = companyUnitBaseData.getLocationAddress();
                if (postalAddress != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_POSTAL_ADDRESS, createAddressNode(virkning, timestamp, postalAddress));
                }

                Industry primaryIndustry = companyUnitBaseData.getPrimaryIndustry();
                if (primaryIndustry != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_PRIMARY_INDUSTRY, createIndustryNode(virkning, timestamp, primaryIndustry));
                }

                Industry secondaryIndustry1 = companyUnitBaseData.getSecondaryIndustry1();
                if (secondaryIndustry1 != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_SECONDARY_INDUSTRY_1, createIndustryNode(virkning, timestamp, secondaryIndustry1));
                }

                Industry secondaryIndustry2 = companyUnitBaseData.getSecondaryIndustry1();
                if (secondaryIndustry2 != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_SECONDARY_INDUSTRY_2, createIndustryNode(virkning, timestamp, secondaryIndustry2));
                }

                Industry secondaryIndustry3 = companyUnitBaseData.getSecondaryIndustry1();
                if (secondaryIndustry3 != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_SECONDARY_INDUSTRY_3, createIndustryNode(virkning, timestamp, secondaryIndustry3));
                }

                Boolean advertProtection = companyUnitBaseData.getAdvertProtection();
                if (advertProtection != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_ADVERTPROTECTION, createSimpleNode(virkning, timestamp, "beskyttelse", advertProtection));
                }

                String phone = companyUnitBaseData.getPhoneNumber();
                if (phone != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_PHONENUMBER, createSimpleNode(virkning, timestamp, "nummer", phone));
                }

                String fax = companyUnitBaseData.getFaxNumber();
                if (fax != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_FAXNUMBER, createSimpleNode(virkning, timestamp, "nummer", fax));
                }

                String email = companyUnitBaseData.getFaxNumber();
                if (email != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_EMAIL, createSimpleNode(virkning, timestamp, "adresse", email));
                }

                LifecycleData lifecycle = companyUnitBaseData.getLifecycleData();
                if (lifecycle != null) {
                    this.addEffectDataToRegistration(output, CompanyUnitBaseData.IO_FIELD_LIFECYCLE, createLifecycleNode(virkning, timestamp, lifecycle));
                }
            }
        }

        return output;
    }

}
