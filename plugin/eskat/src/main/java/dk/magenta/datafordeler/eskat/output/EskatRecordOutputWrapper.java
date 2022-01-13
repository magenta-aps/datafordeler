package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.output.CompanyRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.records.AddressMunicipalityRecord;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.SecNameRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dk.magenta.datafordeler.core.fapi.OutputWrapper.Mode.LEGACY;

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
        if(!record.getNames().current().isEmpty()) {
            container.addNontemporal("navn", record.getNames().current().stream().findFirst().get().getName());
        }
        List<AddressRecord> addressSet = record.getLocationAddress().current();
        if(addressSet.size()==0) {
            addressSet = record.getPostalAddress().current();
        }
        if(!addressSet.isEmpty()) {
            container.addNontemporal(AddressMunicipalityRecord.IO_FIELD_MUNICIPALITY_CODE, addressSet.stream().findFirst().get().getMunicipality().getMunicipalityCode() + "");
        }
    }


    @Override
    protected boolean fillMetadataContainer(OutputContainer oContainer, CompanyRecord record, Mode mode) {
        return false;
    }
}
