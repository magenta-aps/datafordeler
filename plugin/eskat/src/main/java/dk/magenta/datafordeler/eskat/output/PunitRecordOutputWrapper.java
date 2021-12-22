package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.cvr.output.CvrRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.output.UnitRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitMetadataRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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
        List<AddressRecord> addList = record.getLocationAddress().current();
        if(!addList.isEmpty()) {
            container.addCvrBitemporal("navn", record.getNames(), true);
            container.addNontemporal("co", addList.get(0).getCoName());
            container.addNontemporal("postbox", addList.get(0).getPostBox());
            container.addNontemporal("postnummer", addList.get(0).getPostnummer());
            container.addNontemporal("postdistrict", addList.get(0).getPostdistrikt());
            container.addNontemporal("by", addList.get(0).getCityName());
            container.addNontemporal("vej", addList.get(0).getRoadName());
            container.addNontemporal("husnummer", addList.get(0).getHouseNumberFrom());
            container.addNontemporal("kommunekode", addList.get(0).getMunicipality().getMunicipalityCode());
            container.addNontemporal("landekode", addList.get(0).getCountryCode());
        }
    }

    @Override
    protected void fillMetadataContainer(OutputContainer oContainer, CompanyUnitRecord record, Mode m) {
    }

}
