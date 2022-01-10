package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.cvr.output.CompanyRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.records.AddressMunicipalityRecord;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A class for formatting a CompanyRecord to JSON, for FAPI output. The data hierarchy
 * under a Company is sorted into this format:
 */
@Component
public class EskatRecordOutputWrapper extends CompanyRecordOutputWrapper {

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
        List<AddressRecord> addressSet = record.getLocationAddress().current();
        if(addressSet.size()==0) {
            addressSet = record.getPostalAddress().current();
        }
        container.addNontemporal(AddressMunicipalityRecord.IO_FIELD_MUNICIPALITY_CODE, addressSet.iterator().next().getMunicipality().getMunicipalityCode()+"");
    }

    @Override
    protected void fillMetadataContainer(OutputContainer oContainer, CompanyRecord record, Mode mode) {

    }
}
