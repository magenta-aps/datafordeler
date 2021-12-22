package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.cvr.output.CvrRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.output.UnitRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.records.CompanyUnitMetadataRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

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
public class PunitRecordOutputWrapper extends UnitRecordOutputWrapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer oContainer, CompanyUnitRecord record, Mode mode) {
        CvrOutputContainer container = (CvrOutputContainer) oContainer;

        container.addNontemporal(CompanyUnitRecord.IO_FIELD_P_NUMBER, record.getpNumber());
        container.addNontemporal(CompanyUnitRecord.IO_FIELD_ADVERTPROTECTION, record.getAdvertProtection());
        container.addNontemporal(CompanyUnitRecord.IO_FIELD_UNITNUMBER, record.getUnitNumber());
        container.addNontemporal(CompanyUnitRecord.IO_FIELD_UNITTYPE, record.getUnitType());
        container.addNontemporal(CompanyUnitRecord.IO_FIELD_INDUSTRY_RESPONSIBILITY_CODE, record.getIndustryResponsibilityCode());

        container.addCvrBitemporal("navn", record.getNames(), true);
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_LOCATION_ADDRESS, record.getLocationAddress(), this::createAddressNode);
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_POSTAL_ADDRESS, record.getPostalAddress(), this::createAddressNode);
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_PHONE, record.getPhoneNumber(), true);
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_FAX, record.getFaxNumber(), true);
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_EMAIL, record.getEmailAddress(), true);
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_PRIMARY_INDUSTRY, record.getPrimaryIndustry());
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_SECONDARY_INDUSTRY1, record.getSecondaryIndustry1());
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_SECONDARY_INDUSTRY2, record.getSecondaryIndustry2());
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_SECONDARY_INDUSTRY3, record.getSecondaryIndustry3());

        if (!DATAONLY.equals(mode)) {
            container.addCvrBitemporal("livscyklusAktiv", record.getLifecycle(), this::createLifecycleNode);
            container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_YEARLY_NUMBERS, record.getYearlyNumbers());
            container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_QUARTERLY_NUMBERS, record.getQuarterlyNumbers());
            container.addAttribute(CompanyUnitRecord.IO_FIELD_ATTRIBUTES, record.getAttributes());
            container.addNontemporal(CompanyUnitRecord.IO_FIELD_REGISTER_ERROR, record.getRegisterError());
            container.addNontemporal(CompanyUnitRecord.IO_FIELD_DATA_ACCESS, record.getDataAccess());
            container.addNontemporal(CompanyUnitRecord.IO_FIELD_LAST_LOADED, record.getLastLoaded());
            container.addNontemporal(CompanyUnitRecord.IO_FIELD_LAST_UPDATED, record.getLastUpdated());
            container.addNontemporal(CompanyUnitRecord.IO_FIELD_LOADING_ERROR, record.getLoadingError());
            container.addNontemporal(CompanyUnitRecord.IO_FIELD_NEAREST_FUTURE_DATE, record.getNearestFutureDate());
            container.addNontemporal(CompanyUnitRecord.IO_FIELD_ERRORDESCRIPTION, record.getErrorDescription());
            container.addNontemporal(CompanyUnitRecord.IO_FIELD_EFFECT_AGENT, record.getEffectAgent());
        }
        container.addCvrBitemporal(CompanyUnitRecord.IO_FIELD_COMPANY_LINK, record.getCompanyLinkRecords(), null, true, true);
        container.addNontemporal(CompanyUnitRecord.IO_FIELD_SAMT_ID, record.getSamtId());
        /*
        participants
        */
    }

    @Override
    protected void fillMetadataContainer(OutputContainer oContainer, CompanyUnitRecord record, Mode m) {
        CvrOutputContainer container = (CvrOutputContainer) oContainer;

        CompanyUnitMetadataRecord meta = record.getMetadata();
        container.addNontemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_CVR_RELATION, meta.getNewestCvrRelation());
        container.addNontemporal(CompanyUnitMetadataRecord.IO_FIELD_AGGREGATE_STATUS, meta.getAggregateStatus());
        container.addCvrNontemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_CONTACT_DATA, meta.getMetadataContactRecords(), null, true, true);
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_PRIMARY_INDUSTRY, meta.getNewestPrimaryIndustry());
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_SECONDARY_INDUSTRY1, meta.getNewestSecondaryIndustry1());
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_SECONDARY_INDUSTRY2, meta.getNewestSecondaryIndustry2());
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_SECONDARY_INDUSTRY3, meta.getNewestSecondaryIndustry3());
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_NAME, meta.getNewestName());
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_YEARLY_NUMBERS, Collections.singleton(meta.getNewestYearlyNumbers()));
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_QUARTERLY_NUMBERS, Collections.singleton(meta.getNewestQuarterlyNumbers()));
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_MONTHLY_NUMBERS, Collections.singleton(meta.getNewestMonthlyNumbers()));
        container.addCvrBitemporal(CompanyUnitMetadataRecord.IO_FIELD_NEWEST_LOCATION, meta.getNewestLocation(), this::createAddressNode);
    }

}
