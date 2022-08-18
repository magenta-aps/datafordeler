package dk.magenta.datafordeler.cvr.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.cvr.records.ParticipantMetadataRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static dk.magenta.datafordeler.core.fapi.OutputWrapper.Mode.DATAONLY;

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
public class ParticipantRecordOutputWrapper extends CvrRecordOutputWrapper<ParticipantRecord> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer oContainer, ParticipantRecord record, Mode mode) {

        CvrOutputContainer container = (CvrOutputContainer) oContainer;

        container.addNontemporal(ParticipantRecord.IO_FIELD_UNIT_NUMBER, record.getUnitNumber());
        container.addNontemporal(ParticipantRecord.IO_FIELD_BUSINESS_KEY, record.getBusinessKey());
        container.addNontemporal(ParticipantRecord.IO_FIELD_UNIT_TYPE, record.getUnitType());
        container.addNontemporal(ParticipantRecord.IO_FIELD_POSITION, record.getPosition());
        container.addNontemporal(ParticipantRecord.IO_FIELD_STATUS_CODE, record.getStatusCode());
        container.addCvrBitemporal("navn", record.getNames(), true);
        container.addCvrBitemporal(ParticipantRecord.IO_FIELD_LOCATION_ADDRESS, record.getLocationAddress(), this::createAddressNode);
        container.addCvrBitemporal(ParticipantRecord.IO_FIELD_POSTAL_ADDRESS, record.getPostalAddress(), this::createAddressNode);
        container.addCvrBitemporal(ParticipantRecord.IO_FIELD_BUSINESS_ADDRESS, record.getBusinessAddress(), this::createAddressNode);
        container.addCvrBitemporal(ParticipantRecord.IO_FIELD_PHONE, record.getPhoneNumber(), true);
        container.addCvrBitemporal(ParticipantRecord.IO_FIELD_FAX, record.getFaxNumber(), true);
        container.addCvrBitemporal(ParticipantRecord.IO_FIELD_EMAIL, record.getEmailAddress(), true);
        container.addAttribute(ParticipantRecord.IO_FIELD_ATTRIBUTES, record.getAttributes());
        container.addNontemporal(ParticipantRecord.IO_FIELD_SAMT_ID, record.getSamtId());

        if (!DATAONLY.equals(mode)) {
            container.addNontemporal(ParticipantRecord.IO_FIELD_REGISTER_ERROR, record.getRegisterError());
            container.addNontemporal(ParticipantRecord.IO_FIELD_DATA_ACCESS, record.getDataAccess());
            container.addNontemporal(ParticipantRecord.IO_FIELD_LAST_LOADED, record.getLastLoaded());
            container.addNontemporal(ParticipantRecord.IO_FIELD_LAST_UPDATED, record.getLastUpdated());
            container.addNontemporal(ParticipantRecord.IO_FIELD_LOADING_ERROR, record.getLoadingError());
            container.addNontemporal(ParticipantRecord.IO_FIELD_NEAREST_FUTURE_DATE, record.getNearestFutureDate());
            container.addNontemporal(ParticipantRecord.IO_FIELD_ERRORDESCRIPTION, record.getErrorDescription());
            container.addNontemporal(ParticipantRecord.IO_FIELD_EFFECT_AGENT, record.getEffectAgent());
        }
    }

    @Override
    protected boolean fillMetadataContainer(OutputContainer oContainer, ParticipantRecord record, Mode m) {
        CvrOutputContainer container = (CvrOutputContainer) oContainer;

        ParticipantMetadataRecord meta = record.getMetadata();
        container.addCvrBitemporal(ParticipantMetadataRecord.IO_FIELD_NEWEST_LOCATION, meta.getNewestLocation(), this::createAddressNode);
        container.addCvrNontemporal(ParticipantMetadataRecord.IO_FIELD_NEWEST_CONTACT_DATA, meta.getMetadataContactRecords(), null, true, true);
        return true;
    }

}
