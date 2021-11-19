package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.cvr.output.CompanyRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class KimikRecordOutputWrapper extends CompanyRecordOutputWrapper {

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
        container.addNontemporal(CompanyRecord.IO_FIELD_POSTAL_ADDRESS, record.getLocationAddress().current().iterator().next().getMunicipality().getMunicipalityCode()+"");

    }

    @Override
    protected void fillMetadataContainer(OutputContainer oContainer, CompanyRecord record, Mode mode) {

    }

}
