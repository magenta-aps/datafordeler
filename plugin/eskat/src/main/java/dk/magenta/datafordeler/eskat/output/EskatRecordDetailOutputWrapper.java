package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.eskat.utils.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class for formatting a CompanyUnitRecord to JSON
 */
@Component
public class EskatRecordDetailOutputWrapper {

    @Autowired
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    public ObjectNode fillContainer(CompanyRecord record, Stream<CompanyUnitRecord> pUnitEntities) {
        OutputWrapper.NodeWrapper container = new OutputWrapper.NodeWrapper(objectMapper.createObjectNode());

        container.put(CompanyRecord.IO_FIELD_CVR_NUMBER, record.getCvrNumber());
        container.put(CompanyRecord.IO_FIELD_NAMES, record.getNames().current().iterator().next().getName());
        container.put(CompanyRecord.IO_FIELD_SECONDARY_NAMES, record.getSecondaryNames().current().stream().findFirst().map(f -> f.getName()).orElse(""));
        container.put(StatusRecord.IO_FIELD_STATUSCODE, record.getStatus().current().stream().findFirst().map(f -> f.getStatusText()).orElse(""));

        AddressRecord adress = record.getLocationAddress().current().stream().findFirst().get();
        container.put(AddressRecord.DB_FIELD_CONAME, adress.getCoName());
        container.put(AddressRecord.IO_FIELD_POSTCODE, adress.getPostnummer());
        container.put(AddressRecord.DB_FIELD_TEXT, adress.getAddressText());
        container.put(AddressRecord.IO_FIELD_POSTBOX, adress.getPostBox());
        container.put(AddressRecord.IO_FIELD_POSTDISTRICT, adress.getPostdistrikt());
        container.put(AddressMunicipalityRecord.IO_FIELD_MUNICIPALITY_CODE, adress.getMunicipality().getMunicipalityCode());
        container.put(AddressRecord.IO_FIELD_CITY, adress.getCityName());

        container.put(CompanyRecord.IO_FIELD_PHONE, record.getPhoneNumber().current().stream().findFirst().map(f -> f.getContactInformation()).orElse(""));
        container.put(CompanyRecord.IO_FIELD_EMAIL, record.getEmailAddress().current().stream().findFirst().map(f -> f.getContactInformation()).orElse(""));
        container.put(CompanyRecord.DB_FIELD_FAX, record.getFaxNumber().stream().findFirst().map(f -> f.getContactInformation()).orElse(""));
        container.put("startdato", DateConverter.dateConvert(record.getMetadata().getValidFrom()));
        String driftsform = record.getMetadata().getNewestForm().stream().findFirst().map(f -> f.getCompanyFormCode()).orElse("");
        driftsform += "/"+record.getMetadata().getNewestForm().stream().findFirst().map(f -> f.getLongDescription()).orElse("");
        container.put(CompanyMetadataRecord.IO_FIELD_NEWEST_FORM, driftsform);
        container.put(CompanyIndustryRecord.IO_FIELD_TEXT, record.getPrimaryIndustry().current().stream().findFirst().map(f -> f.getIndustryText()).orElse(""));
        container.put(CompanyIndustryRecord.IO_FIELD_CODE, record.getPrimaryIndustry().current().stream().findFirst().map(f -> f.getIndustryCode()).orElse(""));

        List<PunitEntity> pUnitList =  pUnitEntities.map(f -> new PunitEntity(f.getpNumber()+"", f.getNames().current().stream().findFirst().get().getName(), f.getLocationAddress().current().stream().findFirst().get().getCountryCode())).collect(Collectors.toList());
        ObjectMapper mapper = new ObjectMapper();
        container.putArray(CompanyUnitRecord.IO_FIELD_P_NUMBER, mapper.valueToTree(pUnitList));
        return container.getNode();
    }
}
