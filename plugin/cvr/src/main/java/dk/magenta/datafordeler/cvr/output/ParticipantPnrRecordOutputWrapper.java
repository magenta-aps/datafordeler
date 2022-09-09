package dk.magenta.datafordeler.cvr.output;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.springframework.stereotype.Component;

@Component
public class ParticipantPnrRecordOutputWrapper extends ParticipantRecordOutputWrapper {

    @Override
    protected void fillContainer(OutputContainer oContainer, ParticipantRecord record, Mode mode) {
        super.fillContainer(oContainer, record, mode);
        CvrOutputContainer container = (CvrOutputContainer) oContainer;
        container.addNontemporal(ParticipantRecord.IO_FIELD_BUSINESS_KEY, record.getBusinessKey());
    }

    protected FilterProvider getFilterProvider() {
        return new SimpleFilterProvider().addFilter("ParticipantRecordFilter", SimpleBeanPropertyFilter.serializeAll());
    }

}
