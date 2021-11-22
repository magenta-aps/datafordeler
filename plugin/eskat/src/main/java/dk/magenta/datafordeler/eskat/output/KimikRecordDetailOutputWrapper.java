package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.cvr.output.CompanyRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class KimikRecordDetailOutputWrapper extends CompanyRecordOutputWrapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer oContainer, CompanyRecord record, Mode mode) {
        CvrOutputContainer container = (CvrOutputContainer) oContainer;

        container.addNontemporal(CompanyRecord.IO_FIELD_CVR_NUMBER, record.getCvrNumber());
        container.addNontemporal("navn", record.getNames().current().iterator().next().getName());

        AddressRecord adress = record.getLocationAddress().current().iterator().next();
        container.addNontemporal("co", adress.getCoName());
        container.addNontemporal("postno", adress.getPostnummer());
        container.addNontemporal("adresstext", adress.getAddressText());
        container.addNontemporal("postbox", adress.getPostBox());
        container.addNontemporal("postdistrict", adress.getPostdistrikt());
        container.addNontemporal("munipialicitycode", adress.getMunicipality().getMunicipalityCode());
        container.addNontemporal("phone", record.getPhoneNumber().iterator().next().getContactInformation());

        container.addNontemporal("founding", record.getMetadata().getFoundingDate());
        container.addNontemporal("companyform", record.getMetadata().getNewestForm().iterator().next().getCompanyFormCode()+"/"+
                record.getMetadata().getNewestForm().iterator().next().getLongDescription());

        container.addNontemporal("industryCode", record.getMetadata().getLatestNewestPrimaryIndustry().getIndustryCode());
        container.addNontemporal("industryText", record.getMetadata().getLatestNewestPrimaryIndustry().getIndustryText());


    }

    @Override
    protected void fillMetadataContainer(OutputContainer oContainer, CompanyRecord record, Mode mode) {

    }

}
