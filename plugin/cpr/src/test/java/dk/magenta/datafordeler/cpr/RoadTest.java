package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.records.road.RoadRecordQuery;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import dk.magenta.datafordeler.cpr.records.road.data.RoadMemoBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadNameBitemporalRecord;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoadTest extends TestBase {

    private void loadRoad(ImportMetadata importMetadata) throws DataFordelerException, IOException {
        InputStream testData = RoadTest.class.getResourceAsStream("/roaddata.txt");
        roadEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    @Test
    public void testRoadIdempotence() throws IOException, DataFordelerException {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        importMetadata.setTransactionInProgress(true);
        try {
            loadRoad(importMetadata);
            List<RoadEntity> entities = QueryManager.getAllEntities(session, RoadEntity.class);
            JsonNode firstImport = this.getObjectMapper().valueToTree(entities);

            loadRoad(importMetadata);
            entities = QueryManager.getAllEntities(session, RoadEntity.class);
            JsonNode secondImport = this.getObjectMapper().valueToTree(entities);
            assertJsonEquality(firstImport, secondImport, true, true);
        } finally {
            transaction.rollback();
            session.close();
        }
    }

    @Test
    public void testParseRoad() throws IOException, DataFordelerException {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        importMetadata.setTransactionInProgress(true);
        try {
            loadRoad(importMetadata);

            //Search for road and munipiality-code
            RoadRecordQuery query = new RoadRecordQuery();
            query.addKommunekode("0730");
            query.setVejkode("0004");

            List<RoadEntity> entities = QueryManager.getAllEntities(session, query, RoadEntity.class);
            Assertions.assertEquals(1, entities.size());
            RoadEntity entity = entities.get(0);

            Assertions.assertEquals(RoadEntity.generateUUID(730, 4), entity.getUUID());
            Assertions.assertEquals(730, entity.getMunicipalityCode());
            Assertions.assertEquals(4, entity.getRoadcode());
            Assertions.assertEquals(1, entity.getName().size());
            Assertions.assertEquals(3, entity.getMemo().size());
            Assertions.assertEquals(2, entity.getPostcode().size());
            Assertions.assertEquals(0, entity.getCity().size());

            RoadNameBitemporalRecord roadName = entity.getName().iterator().next();

            Assertions.assertTrue(Equality.equal(OffsetDateTime.parse("1900-01-01T12:00+01:00"), roadName.getEffectFrom()));
            Assertions.assertTrue(Equality.equal(OffsetDateTime.parse("2006-12-22T12:00:00+01:00"), roadName.getRegistrationFrom()));

            List<RoadMemoBitemporalRecord> memoIterator = entity.getMemo().stream()
                    .sorted(Comparator.comparing(RoadMemoBitemporalRecord::getNoteLine)).collect(Collectors.toList());

            Assertions.assertEquals("HUSNR.1 - BØRNEINSTITUTION -", memoIterator.get(0).getNoteLine());
            Assertions.assertEquals("HUSNR.2 - EGEDAL -", memoIterator.get(1).getNoteLine());
            Assertions.assertEquals("HUSNR.3 - KIRKE -", memoIterator.get(2).getNoteLine());

            //Validate jsonresponse
            String jsonResponse = this.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(entity);
            assertJsonContains("{\"navn\":[{\"vejnavn\":\"Aalborggade\"}]}", jsonResponse);


            //Search for road-name
            query = new RoadRecordQuery();
            query.setVejnavn("Adelgade");
            entities = QueryManager.getAllEntities(session, query, RoadEntity.class);
            Assertions.assertEquals(2, entities.size());

            Assertions.assertTrue(entities.stream().anyMatch(e -> e.getUUID().equals(RoadEntity.generateUUID(730, 15))));

            Assertions.assertTrue(entities.stream().anyMatch(e -> e.getUUID().equals(RoadEntity.generateUUID(730, 16))));

            //Search for road-name with asterix
            query = new RoadRecordQuery();
            query.setVejnavn("*gade");
            entities = QueryManager.getAllEntities(session, query, RoadEntity.class);
            Assertions.assertEquals(3, entities.size());


        } finally {
            transaction.rollback();
            session.close();
        }
    }

    @Test
    public void testRoadRecordTime() throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = this.getSessionManager().getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        loadRoad(importMetadata);
        transaction.commit();
        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", "2011-06-17T14:06:19.196");
        searchParameters.add("recordAfter", "2011-06-17T14:06:19.196");

        ResponseEntity<String> response = restSearch(searchParameters, "road");
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = this.getObjectMapper().readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(9, results.size());

        for (int i = 0; i < results.size(); i++) {
            ObjectNode roadNode = (ObjectNode) results.get(i);
            Assertions.assertEquals(0, roadNode.get("navn").size());
            Assertions.assertEquals(0, roadNode.get("by").size());
            Assertions.assertEquals(0, roadNode.get("note").size());
            Assertions.assertEquals(0, roadNode.get("postnr").size());
        }

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFraFør", "2011-06-17T14:06:19.196");
        searchParameters.add("recordAfter", "2011-06-17T14:06:19.196");

        response = restSearch(searchParameters, "road");
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = this.getObjectMapper().readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(9, results.size());


        for (int i = 0; i < results.size(); i++) {
            ObjectNode roadNode = (ObjectNode) results.get(i);
            Assertions.assertTrue(roadNode.get("navn").size() > 0);
        }
    }


    @Test
    public void testLookupServiceDk() throws Exception {
        try (Session session = this.getSessionManager().getSessionFactory().openSession()) {
            this.getSessionManager().getSessionFactory().openSession();
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            importMetadata.setTransactionInProgress(true);
            Transaction transaction = session.beginTransaction();
            loadRoad(importMetadata);
            transaction.commit();

            CprLookupService lookupService = new CprLookupService(this.getSessionManager());

            CprLookupDTO lookupDTO = lookupService.doLookup(730, 1, "18");

            Assertions.assertEquals("Randers", lookupDTO.getMunicipalityName());
            Assertions.assertEquals("Aage Beks Vej", lookupDTO.getRoadName());
            Assertions.assertEquals(8920, lookupDTO.getPostalCode());
            Assertions.assertEquals("Randers NV", lookupDTO.getPostalDistrict());

            lookupDTO = lookupService.doLookup(730, 4, "18");

            Assertions.assertEquals("Randers", lookupDTO.getMunicipalityName());
            Assertions.assertEquals("Aalborggade", lookupDTO.getRoadName());
            Assertions.assertEquals(8940, lookupDTO.getPostalCode());
            Assertions.assertEquals("Randers SV", lookupDTO.getPostalDistrict());
        }
    }


    @Test
    public void pull() throws Exception {

        String username = "test";
        String password = "test";
        int port = 2104;

        CprConfiguration configuration = this.getConfiguration();

        InputStream roadContents = this.getClass().getResourceAsStream("/roaddata.txt");
        File roadFile = File.createTempFile("roaddata", "txt");
        roadFile.createNewFile();
        FileUtils.copyInputStreamToFile(roadContents, roadFile);
        roadContents.close();

        this.startFtp(username, password, port, Collections.singletonList(roadFile));
        try {
            configuration.setPersonRegisterType(CprConfiguration.RegisterType.DISABLED);
            configuration.setRoadRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
            configuration.setResidenceRegisterType(CprConfiguration.RegisterType.DISABLED);
            configuration.setRoadRegisterFtpAddress("ftps://localhost:" + port);
            configuration.setRoadRegisterFtpUsername(username);
            configuration.setRoadRegisterFtpPassword(password);
            configuration.setRoadRegisterDataCharset(CprConfiguration.Charset.UTF_8);
            Pull pull = new Pull(this.getEngine(), this.getPlugin());
            pull.run();
        } finally {
            this.stopFtp();
        }

        roadFile.delete();

        Session session = this.getSessionManager().getSessionFactory().openSession();
        try {
            RoadRecordQuery roadQuery = new RoadRecordQuery();
            roadQuery.addKommunekode(730);
            roadQuery.setVejkode(4);
            List<RoadEntity> roadEntities = QueryManager.getAllEntities(session, roadQuery, RoadEntity.class);
            Assertions.assertEquals(1, roadEntities.size());
            Assertions.assertEquals(RoadEntity.generateUUID(730, 4), roadEntities.get(0).getUUID());
        } finally {
            session.close();
        }
    }

}
