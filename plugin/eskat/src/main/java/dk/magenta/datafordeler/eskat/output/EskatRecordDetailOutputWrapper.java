package dk.magenta.datafordeler.eskat.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.cvr.output.CompanyRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.query.CompanyUnitRecordQuery;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitLinkRecord;
import dk.magenta.datafordeler.cvr.records.SecNameRecord;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;


@Component
public class EskatRecordDetailOutputWrapper extends CompanyRecordOutputWrapper {

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
        container.addNontemporal("sekundartnavn", record.getSecondaryNames().current().stream().findFirst().map(f -> f.getName()).orElse(""));
        container.addNontemporal("status", record.getStatus().current().stream().findFirst().map(f -> f.getStatusText()).orElse(""));

        AddressRecord adress = record.getLocationAddress().current().stream().findFirst().get();
        container.addNontemporal("co", adress.getCoName());
        container.addNontemporal("postno", adress.getPostnummer());
        container.addNontemporal("adresstext", adress.getAddressText());
        container.addNontemporal("postbox", adress.getPostBox());
        container.addNontemporal("postdistrict", adress.getPostdistrikt());
        container.addNontemporal("munipialicitycode", adress.getMunicipality().getMunicipalityCode());
        container.addNontemporal("by", adress.getCityName());



        container.addNontemporal("phone", record.getPhoneNumber().iterator().next().getContactInformation());
        container.addNontemporal("email", record.getEmailAddress().current().stream().findFirst().map(f -> f.getContactInformation()).orElse(""));
        container.addNontemporal("fax", record.getFaxNumber().stream().findFirst().map(f -> f.getContactInformation()).orElse(""));
        container.addNontemporal("startdato", record.getMetadata().getValidFrom());
        container.addNontemporal("slutdato", record.getMetadata().getValidTo());
        String driftsform = record.getMetadata().getNewestForm().stream().findFirst().map(f -> f.getCompanyFormCode()).orElse("");
        driftsform += "/"+record.getMetadata().getNewestForm().stream().findFirst().map(f -> f.getLongDescription()).orElse("");
        container.addNontemporal("driftsform", driftsform);
        container.addNontemporal("branchetekst", record.getPrimaryIndustry().current().stream().findFirst().map(f -> f.getIndustryText()).orElse(""));
        container.addNontemporal("branchekode", record.getPrimaryIndustry().current().stream().findFirst().map(f -> f.getIndustryCode()).orElse(""));

        /*List<String>  productionUnits = record.getProductionUnits().current().stream().map(f -> Integer.toString(f.getpNumber()));


        CompanyUnitRecordQuery companyUnitRecordQuery = new CompanyUnitRecordQuery();
        companyUnitRecordQuery.setPNummer(productionUnits);
        Session session = this.getSessionManager().getSessionFactory().openSession();

        protected Stream<E> searchByQueryAsStream(Q query, Session session) {
            return QueryManager.getAllEntitiesAsStream(
                    session, query,
                    this.getEntityClass()
            );



            for(CompanyUnitLinkRecord productionUnit: productionUnits) {
                CompanyUnitRecordQuery

                System.out.println(productionUnit.getpNumber());

            }*/


    }

    @Override
    protected void fillMetadataContainer(OutputContainer oContainer, CompanyRecord record, Mode mode) {

    }

}
