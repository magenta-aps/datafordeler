package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.eskat.utils.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


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
@Component
public class PunitRecordOutputWrapper {

    @Autowired
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }


    public ObjectNode fillContainer(CompanyUnitRecord record) {
        ObjectNode container = getObjectMapper().createObjectNode();

        container.put(CompanyUnitRecord.IO_FIELD_P_NUMBER, record.getpNumber());
        AddressRecord addressList = record.getLocationAddress().current().stream().findFirst().orElse(null);
        container.put(CompanyUnitRecord.IO_FIELD_NAMES, record.getNames().current().stream().findFirst().get().getName());
        if(addressList!=null) {
            container.put(AddressRecord.IO_FIELD_CONAME, addressList.getCoName());
            container.put(AddressRecord.IO_FIELD_POSTBOX, addressList.getPostBox());
            container.put(AddressRecord.IO_FIELD_POSTCODE, addressList.getPostnummer());
            container.put(AddressRecord.IO_FIELD_POSTDISTRICT, addressList.getPostdistrikt());
            container.put(AddressRecord.IO_FIELD_CITY, addressList.getCityName());
            container.put(AddressRecord.IO_FIELD_ROADNAME, addressList.getRoadName());
            container.put(AddressRecord.IO_FIELD_HOUSE_FROM, addressList.getHouseNumberFrom());
            container.put(AddressMunicipalityRecord.IO_FIELD_MUNICIPALITY_CODE, addressList.getMunicipality().getMunicipalityCode());
            container.put(AddressRecord.IO_FIELD_COUNTRYCODE, addressList.getCountryCode());
        }

        ContactRecord emailContact = record.getEmailAddress().current().stream().findFirst().orElse(null);
        container.put(CompanyUnitRecord.IO_FIELD_FAX, emailContact==null ? "":emailContact.getContactInformation());

        ContactRecord faxContact = record.getFaxNumber().current().stream().findFirst().orElse(null);
        container.put(CompanyUnitRecord.IO_FIELD_EMAIL, faxContact==null ? "":faxContact.getContactInformation());

        container.put("startdato", DateConverter.dateConvert(record.getMetadata().getValidFrom()));
        container.put("slutdato", DateConverter.dateConvert(record.getMetadata().getValidTo()));
        container.put(CompanyIndustryRecord.IO_FIELD_TEXT, record.getPrimaryIndustry().current().stream().findFirst().map(f -> f.getIndustryText()).orElse(""));
        container.put(CompanyIndustryRecord.IO_FIELD_CODE, record.getPrimaryIndustry().current().stream().findFirst().map(f -> f.getIndustryCode()).orElse(""));
        container.put(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_CVR_RELATION, record.getMetadata().getNewestCvrRelation());

        return container;
    }


}
