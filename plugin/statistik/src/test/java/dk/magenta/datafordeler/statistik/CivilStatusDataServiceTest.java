package dk.magenta.datafordeler.statistik;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.util.UnorderedJsonListComparator;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.statistik.services.CivilStatusDataService;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CivilStatusDataServiceTest extends TestBase {

    @MockitoSpyBean
    private CivilStatusDataService civilStatusDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        this.loadPersonData("marriedperson2.txt");
        this.loadPersonData("marriedperson3.txt");
        this.loadAllGeoAdress();
    }

    @AfterEach
    public void cleanup() {
        this.deleteAll();
    }

    private void assertJsonEquals(String jsonExpected, String jsonActual) throws JsonProcessingException {
        Assertions.assertEquals(
                0, new UnorderedJsonListComparator().compare(objectMapper.readTree(jsonExpected), objectMapper.readTree(jsonActual)),
                objectMapper.readTree(jsonExpected) + "   !=   " + objectMapper.readTree(jsonActual)
        );
    }

    /**
     * This is just a test of what has been initiated by the cpr module
     *
     * @throws JsonProcessingException
     */
    @Test
    public void testMarriageInitiated() throws JsonProcessingException, InvalidClientInputException {

        when(civilStatusDataService.getTimeintervallimit()).thenReturn(false);


        Session session = sessionManager.getSessionFactory().openSession();
        PersonRecordQuery query = new PersonRecordQuery();
        query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011234");
        List<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(session, query, PersonEntity.class).collect(Collectors.toList());
        Assertions.assertEquals(1, personEntities.size());
        PersonEntity personEntity = personEntities.get(0);
        Assertions.assertEquals(5, personEntity.getCivilstatus().size());

        query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011235");
        personEntities = QueryManager.getAllEntitiesAsStream(session, query, PersonEntity.class).collect(Collectors.toList());
        Assertions.assertEquals(1, personEntities.size());
        personEntity = personEntities.get(0);
        Assertions.assertEquals(4, personEntity.getCivilstatus().size());
    }


    @Test
    public void testServiceMarried() throws JsonProcessingException {
        when(civilStatusDataService.getTimeintervallimit()).thenReturn(false);
        when(civilStatusDataService.getWriteToLocalFile()).thenReturn(false);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/civilstate_data/?CivSt=G&registrationAfter=1980-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/civilstate_data/?CivSt=G&registrationAfter=1980-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response.getBody(), "Response body");
        String expected = "\"CivSt\";\"CivDto\";\"StatKod\";\"ProdDto\";\"Pnr\";\"AegtePnr\";\"MynKodTxt\";\"KomKod\";\"FoedMynKod\";\"FoedMynTxt\";\"FoedMynKodTxt\";\"LokNavn\";\"LokKortNavn\";\"LokKode\";\"VejKod\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\"\n" +
                "\"G\";;;\"23-09-1991\";\"0101011234\";\"1111111111\";\"0\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"09-08-2019\";;\"09-08-2019\";\"0101011234\";\"1111111112\";\"657\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"09-08-2019\";;\"03-09-2019\";\"0101011234\";\"1111111114\";\"657\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"15-03-2018\";;\"15-03-2018\";\"0101011235\";\"1111111111\";\"340\";\"955\";\"9504\";\"\";\"0\";;;;\"0368\";\"0012\";\"\";\"\";\"\"";

        compareJSONARRAYWithIgnoredValues(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim()), "CivDto");

        response = restTemplate.exchange("/statistik/civilstate_data/?registrationAfter=2019-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response.getBody(), "Response body");
        expected = "\"CivSt\";\"CivDto\";\"StatKod\";\"ProdDto\";\"Pnr\";\"AegtePnr\";\"MynKodTxt\";\"KomKod\";\"FoedMynKod\";\"FoedMynTxt\";\"FoedMynKodTxt\";\"LokNavn\";\"LokKortNavn\";\"LokKode\";\"VejKod\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\"\n" +
                "\"G\";\"09-08-2019\";;\"09-08-2019\";\"0101011234\";\"1111111112\";\"657\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"09-08-2019\";;\"03-09-2019\";\"0101011234\";\"1111111114\";\"657\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"";

        compareJSONARRAYWithIgnoredValues(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim()),
                "CivDto"
        );

        response = restTemplate.exchange("/statistik/civilstate_data/?registrationAfter=1980-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response.getBody(), "Response body");
        expected = "\"CivSt\";\"CivDto\";\"StatKod\";\"ProdDto\";\"Pnr\";\"AegtePnr\";\"MynKodTxt\";\"KomKod\";\"FoedMynKod\";\"FoedMynTxt\";\"FoedMynKodTxt\";\"LokNavn\";\"LokKortNavn\";\"LokKode\";\"VejKod\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\"\n" +
                "\"G\";;;\"23-09-1991\";\"0101011234\";\"1111111111\";\"0\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"09-08-2019\";;\"09-08-2019\";\"0101011234\";\"1111111112\";\"657\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"09-08-2019\";;\"03-09-2019\";\"0101011234\";\"1111111114\";\"657\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"15-03-2018\";;\"15-03-2018\";\"0101011235\";\"1111111111\";\"340\";\"955\";\"9504\";\"\";\"0\";;;;\"0368\";\"0012\";\"\";\"\";\"\"\n" +
                "\"F\";\"16-12-2018\";;\"16-12-2018\";\"0101011235\";\"1111111111\";\"1350\";\"955\";\"9504\";\"\";\"0\";;;;\"0368\";\"0012\";\"\";\"\";\"\"";

        compareJSONARRAYWithIgnoredValues(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim()),
                "CivDto"
        );
    }


    @Test
    public void testCivilStateChangeWithPnr0101011234() throws JsonProcessingException {

        when(civilStatusDataService.getTimeintervallimit()).thenReturn(false);
        when(civilStatusDataService.getWriteToLocalFile()).thenReturn(false);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/civilstate_data/?pnr=0101011234&registrationAfter=1980-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response.getBody(), "Response body");
        String expected = "\"CivSt\";\"CivDto\";\"StatKod\";\"ProdDto\";\"Pnr\";\"AegtePnr\";\"MynKodTxt\";\"KomKod\";\"FoedMynKod\";\"FoedMynTxt\";\"FoedMynKodTxt\";\"LokNavn\";\"LokKortNavn\";\"LokKode\";\"VejKod\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\"\n" +
                "\"G\";;;\"23-09-1991\";\"0101011234\";\"1111111111\";\"0\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"09-08-2019\";;\"09-08-2019\";\"0101011234\";\"1111111112\";\"657\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"\n" +
                "\"G\";\"09-08-2019\";;\"03-09-2019\";\"0101011234\";\"1111111114\";\"657\";\"955\";\"9504\";\"\";\"0\";;;;\"0001\";\"0005\";\"1\";\"tv\";\"1234\"";

        compareJSONARRAYWithIgnoredValues(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim()),
                "CivDto"
        );
    }

    @Test
    public void testCivilStateChangeWithPnr0101011235() throws JsonProcessingException {
        when(civilStatusDataService.getTimeintervallimit()).thenReturn(false);
        when(civilStatusDataService.getWriteToLocalFile()).thenReturn(false);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/civilstate_data/?pnr=0101011235&registrationAfter=1980-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response.getBody(), "Response body");
        String expected = "\"CivSt\";\"CivDto\";\"StatKod\";\"ProdDto\";\"Pnr\";\"AegtePnr\";\"MynKodTxt\";\"KomKod\";\"FoedMynKod\";\"FoedMynTxt\";\"FoedMynKodTxt\";\"LokNavn\";\"LokKortNavn\";\"LokKode\";\"VejKod\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\"\n" +
                "\"G\";\"15-03-2018\";;\"15-03-2018\";\"0101011235\";\"1111111111\";\"340\";\"955\";\"9504\";\"\";\"0\";;;;\"0368\";\"0012\";\"\";\"\";\"\"\n" +
                "\"F\";\"16-12-2018\";;\"16-12-2018\";\"0101011235\";\"1111111111\";\"1350\";\"955\";\"9504\";\"\";\"0\";;;;\"0368\";\"0012\";\"\";\"\";\"\"";

        assertJsonEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim())
        );
    }


}
