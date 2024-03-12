package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.util.StringJoiner;
import dk.magenta.datafordeler.cvr.entitymanager.CvrEntityManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.service.CompanyOwnersService;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class CompanyOwnersTest {


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CvrPlugin plugin;

    @Autowired
    private CvrRegisterManager registerManager;

    private CvrEntityManager entityManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testParseCompanyFile() throws DataFordelerException, URISyntaxException {
        ImportMetadata importMetadata = new ImportMetadata();

        URL testData = ParseTest.class.getResource("/company_in4.json");
        String testDataPath = testData.toURI().toString();
        registerManager.setCvrDemoCompanyFile(testDataPath);

        entityManager = (CvrEntityManager) this.registerManager.getEntityManagers().get(0);
        InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, importMetadata);
        entityManager.parseData(stream, importMetadata);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
//            query.setRegistrationAt(time);
//            query.setEffectAt(time);
            query.applyFilters(session);

            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);

            FilterProvider fp = new SimpleFilterProvider().addFilter(
                    "ParticipantRecordFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept(ParticipantRecord.IO_FIELD_BUSINESS_KEY)
            );
            System.out.println("QUERY RESULT:");
            for (CompanyRecord c : companyList) {
                System.out.println(c.getCvrNumber());
                try {
                    System.out.println(objectMapper.setFilterProvider(fp).writerWithDefaultPrettyPrinter().writeValueAsString(c));
                    System.out.println("\n\n\n");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            Assert.assertEquals(1, companyList.size());

            for (CompanyRecord c : companyList) {
                for (CompanyParticipantRelationRecord p : c.getParticipants()) {
                    System.out.println(p.getParticipantUnitNumber());
                    for (OrganizationRecord o : p.getOrganizations()) {
                        System.out.println(new StringJoiner(", ").addAll(o.getNames().stream().map(n -> n.getName()).collect(Collectors.toSet())).toString());
                        for (OrganizationMemberdataRecord m : o.getMemberData()) {
                            for (AttributeRecord a : m.getAttributes()) {
                                System.out.println("    "+a.getType());
                                for (AttributeValueRecord v : a.getValues()) {
                                    System.out.println("        "+v.getValue()+" ("+v.getBitemporality()+")");
                                }
                            }
                        }
                    }
                }
            }


            ResponseEntity<String> response = restTemplate.exchange(
                    "/cvr/owners/"+companyList.get(0).getCvrNumberString(),
                    HttpMethod.GET,
                    new HttpEntity<>("", new HttpHeaders()),
                    String.class
            );
//            System.out.println(response.getBody());
        }
    }
}
