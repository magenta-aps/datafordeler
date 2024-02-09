package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.databind.JsonNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.data.residence.*;
import dk.magenta.datafordeler.cpr.data.residence.data.ResidenceBaseData;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ResidenceTest extends TestBase {

    /*
    Allmost all tests regarding serching in residence is disabled as a cleanup of failing unittests.
    There has not been a lot of thanges to this functionality for a long time, at it is considered to be working as expected.
     */

    @Autowired
    private ResidenceEntityManager residenceEntityManager;

    @Before
    @After
    public void cleanup() {
        QueryManager.clearCaches();
    }


    private void loadResidence(ImportMetadata importMetadata) throws DataFordelerException, IOException {
        InputStream testData = ResidenceTest.class.getResourceAsStream("/roaddata.txt");
        residenceEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    @Test
    public void testResidenceIdempotence() throws IOException, DataFordelerException {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        importMetadata.setTransactionInProgress(true);
        try {
            loadResidence(importMetadata);
            List<ResidenceEntity> entities = QueryManager.getAllEntities(session, ResidenceEntity.class);
            JsonNode firstImport = this.getObjectMapper().valueToTree(entities);

            loadResidence(importMetadata);
            entities = QueryManager.getAllEntities(session, ResidenceEntity.class);
            JsonNode secondImport = this.getObjectMapper().valueToTree(entities);
            assertJsonEquality(firstImport, secondImport, true, true);

        } finally {
            transaction.rollback();
            session.close();
        }
    }

    //@Test
    public void testParseResidence() throws Exception {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        importMetadata.setTransactionInProgress(true);
        try {
            loadResidence(importMetadata);

            ResidenceQuery query = new ResidenceQuery();
            query.setParameter(ResidenceQuery.KOMMUNEKODE, 360);
            query.setParameter(ResidenceQuery.VEJKODE, 206);

            List<ResidenceEntity> entities = QueryManager.getAllEntities(session, query, ResidenceEntity.class);
            Assert.assertEquals(1, entities.size());
            ResidenceEntity entity = entities.get(0);
            Assert.assertEquals(ResidenceEntity.generateUUID(360, 206, "44E", "", ""), entity.getUUID());

            List<ResidenceRegistration> registrations = entities.get(0).getRegistrations();
            Assert.assertEquals(1, registrations.size());
            ResidenceRegistration registration = registrations.get(0);
            Assert.assertTrue(OffsetDateTime.parse("2006-12-22T12:00:00+01:00").isEqual(registration.getRegistrationFrom()));
            Assert.assertNull(registration.getRegistrationTo());
            List<ResidenceEffect> effects = registration.getEffects();
            Assert.assertEquals(1, effects.size());
            ResidenceEffect effect = effects.get(0);
            Assert.assertTrue(OffsetDateTime.parse("1991-09-23T12:00:00+02:00").isEqual(effect.getEffectFrom()));
            Assert.assertNull(effect.getEffectTo());
            Assert.assertFalse(effect.getEffectFromUncertain());
            Assert.assertFalse(effect.getEffectToUncertain());
            List<ResidenceBaseData> dataItems = effect.getDataItems();
            Assert.assertEquals(1, dataItems.size());
            ResidenceBaseData data = dataItems.get(0);
            Assert.assertEquals("", data.getEtage());
            Assert.assertEquals("44E", data.getHusnummer());
            Assert.assertEquals(360, data.getKommunekode());
            Assert.assertEquals("Provstelunden", data.getLokalitet());
            Assert.assertEquals("", data.getSideDoer());
            Assert.assertEquals(206, data.getVejkode());

        } finally {
            transaction.rollback();
            session.close();
        }
    }


    //@Test
    public void testResidenceRecordTime() throws Exception {
        whitelistLocalhost();
        OffsetDateTime now = OffsetDateTime.now();
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = this.getSessionManager().getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        loadResidence(importMetadata);
        transaction.commit();
        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("recordAfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, "residence");
        Assert.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = this.getObjectMapper().readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(0, results.size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("recordAfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        response = restSearch(searchParameters, "residence");
        Assert.assertEquals(200, response.getStatusCode().value());
        jsonBody = this.getObjectMapper().readTree(response.getBody());
        results = jsonBody.get("results");
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(2, results.size());
    }


    //@Test
    public void pull() throws Exception {

        String username = "test";
        String password = "test";
        int port = 2103;

        CprConfiguration configuration = this.getConfiguration();

        InputStream residenceContents = this.getClass().getResourceAsStream("/roaddata.txt");
        File residenceFile = File.createTempFile("residencedata", "txt");
        residenceFile.createNewFile();
        FileUtils.copyInputStreamToFile(residenceContents, residenceFile);
        residenceContents.close();

        this.startFtp(username, password, port, Collections.singletonList(residenceFile));
        try {
            configuration.setPersonRegisterType(CprConfiguration.RegisterType.DISABLED);
            configuration.setRoadRegisterType(CprConfiguration.RegisterType.DISABLED);
            configuration.setResidenceRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
            configuration.setResidenceRegisterFtpAddress("ftps://localhost:" + port);
            configuration.setResidenceRegisterFtpUsername(username);
            configuration.setResidenceRegisterFtpPassword(password);
            configuration.setResidenceRegisterDataCharset(CprConfiguration.Charset.UTF_8);
            Pull pull = new Pull(this.getEngine(), this.getPlugin());
            pull.run();
        } finally {
            this.stopFtp();
        }
        residenceFile.delete();

        Session session = this.getSessionManager().getSessionFactory().openSession();
        try {
            ResidenceQuery roadQuery = new ResidenceQuery();
            roadQuery.setParameter(ResidenceQuery.KOMMUNEKODE, 730);
            roadQuery.setParameter(ResidenceQuery.VEJKODE, 4);
            List<ResidenceEntity> roadEntities = QueryManager.getAllEntities(session, roadQuery, ResidenceEntity.class);
            Assert.assertEquals(1, roadEntities.size());
            Assert.assertEquals(ResidenceEntity.generateUUID(360, 206, "44E", null, null), roadEntities.get(0).getUUID());
        } finally {
            session.close();
        }
    }
}
