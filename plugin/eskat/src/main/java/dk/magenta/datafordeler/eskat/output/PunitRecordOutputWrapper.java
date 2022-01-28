package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.eskat.utils.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


/**
 * A class for formatting a CompanyUnitRecord to JSON
 */
@Component
public class PunitRecordOutputWrapper {

    @Autowired
    private ObjectMapper objectMapper;

    public ObjectNode fillContainer(CompanyUnitRecord record) {
        ObjectNode container = objectMapper.createObjectNode();

        container.put(CompanyUnitRecord.IO_FIELD_P_NUMBER, record.getpNumber());
        AddressRecord addressList = record.getLocationAddress().currentStream().findFirst().orElse(null);
        container.put(CompanyUnitRecord.IO_FIELD_NAMES, record.getNames().currentStream().findFirst().get().getName());
        if(addressList!=null) {
            container.put(AddressRecord.IO_FIELD_CONAME, addressList.getCoName());
            container.put(AddressRecord.IO_FIELD_POSTBOX, addressList.getPostBox());
            container.put(AddressRecord.IO_FIELD_POSTCODE, addressList.getPostnummer());
            container.put(AddressRecord.IO_FIELD_POSTDISTRICT, addressList.getPostdistrikt());
            container.put(AddressRecord.IO_FIELD_CITY, addressList.getCityName());
            container.put(AddressRecord.DB_FIELD_TEXT, addressList.getAddressText());
            container.put(AddressMunicipalityRecord.IO_FIELD_MUNICIPALITY_CODE, addressList.getMunicipality().getMunicipalityCode());
            container.put(AddressRecord.IO_FIELD_COUNTRYCODE, addressList.getCountryCode());
        }

        ContactRecord emailContact = record.getEmailAddress().currentStream().findFirst().orElse(null);
        container.put(CompanyUnitRecord.IO_FIELD_FAX, emailContact==null ? "":emailContact.getContactInformation());

        ContactRecord faxContact = record.getFaxNumber().currentStream().findFirst().orElse(null);
        container.put(CompanyUnitRecord.IO_FIELD_EMAIL, faxContact==null ? "":faxContact.getContactInformation());

        container.put("startdato", DateConverter.dateConvert(record.getMetadata().getValidFrom()));
        container.put("slutdato", DateConverter.dateConvert(record.getMetadata().getValidTo()));
        container.put(CompanyIndustryRecord.IO_FIELD_TEXT, record.getPrimaryIndustry().current().stream().findFirst().map(f -> f.getIndustryText()).orElse(""));
        container.put(CompanyIndustryRecord.IO_FIELD_CODE, record.getPrimaryIndustry().current().stream().findFirst().map(f -> f.getIndustryCode()).orElse(""));
        container.put(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_CVR_RELATION, record.getMetadata().getNewestCvrRelation());

        List<Long> participantList = record.getParticipants().currentStream().map(participant -> participant.getRelationParticipantRecord().getBusinessKey()).collect(Collectors.toList());
        ArrayNode arrayNode = container.putArray(CompanyUnitRecord.IO_FIELD_PARTICIPANTS);
        for (Long item : participantList) {
            arrayNode.add(item);
        }
        return container;
    }
}
