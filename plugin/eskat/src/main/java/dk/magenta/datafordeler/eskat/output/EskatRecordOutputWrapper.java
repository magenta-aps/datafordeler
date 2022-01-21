package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.output.CompanyRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.eskat.utils.DateConverter;
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

    /**
     * We are not using the current functionality here, since we also need to find information about closed companies, we find the last attribute with .reduce((first, second) -> second)
     * @param oContainer
     * @param record
     * @param mode
     */
    @Override
    protected void fillContainer(OutputContainer oContainer, CompanyRecord record, Mode mode) {
        CvrOutputContainer container = (CvrOutputContainer) oContainer;

        container.addNontemporal(CompanyRecord.IO_FIELD_CVR_NUMBER, record.getCvrNumber());
        if(!record.getNames().isEmpty()) {
            container.addNontemporal("navn", record.getNames().stream().reduce((first, second) -> second).get().getName());
        }
        BitemporalSet<AddressRecord> addressSet = record.getLocationAddress();
        if(addressSet.isEmpty()) {
            addressSet = record.getPostalAddress();
        }
        //Adressset is either Locationaddress or fallback to PostalAddress
        if(!addressSet.isEmpty()) {
            container.addNontemporal(AddressMunicipalityRecord.IO_FIELD_MUNICIPALITY_CODE, Integer.toString(addressSet.stream().reduce((first, second) -> second).get().getMunicipality().getMunicipalityCode()));
            container.addNontemporal(AddressRecord.IO_FIELD_POSTCODE, addressSet.stream().reduce((first, second) -> second).get().getPostnummer());
            container.addNontemporal(AddressRecord.IO_FIELD_POSTDISTRICT, addressSet.stream().reduce((first, second) -> second).get().getPostdistrikt());
        }
        StatusRecord statusRecord = record.getStatus().stream().reduce((first, second) -> second).orElse(null);
        if(statusRecord!=null) {
            container.addNontemporal(StatusRecord.IO_FIELD_STATUSCODE,  record.getStatus().stream().reduce((first, second) -> second).get().getStatusText());
        }
        if(record.getLifecycle().size()>0) {
            LifecycleRecord lifeCycle = record.getLifecycle().stream().reduce((first, second) -> second).get();
            container.addNontemporal("startdato", DateConverter.dateConvert(lifeCycle.getValidFrom()));
            container.addNontemporal("slutdato", DateConverter.dateConvert(lifeCycle.getValidTo()));
        }
    }


    @Override
    protected boolean fillMetadataContainer(OutputContainer oContainer, CompanyRecord record, Mode mode) {
        return false;
    }
}
