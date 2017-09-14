package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.cpr.data.person.*;
import dk.magenta.datafordeler.cpr.data.person.PersonOutputWrapper;
import dk.magenta.datafordeler.cpr.data.residence.*;
import dk.magenta.datafordeler.cpr.data.residence.data.ResidenceBaseData;
import dk.magenta.datafordeler.cpr.data.road.*;
import dk.magenta.datafordeler.cpr.data.road.data.RoadMemoData;
import dk.magenta.datafordeler.cpr.data.road.data.RoadPostcodeData;
import jdk.nashorn.internal.ir.ObjectNode;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 14-06-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class ParseTest {

    @Autowired
    private QueryManager queryManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private RoadEntityManager roadEntityManager;

    @Autowired
    private ResidenceEntityManager residenceEntityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private OutputWrapper<PersonEntity> personOutputWrapper = new PersonOutputWrapper();

    @Test
    public void testParsePerson() throws Exception {
        Session session = null;
        try {
            InputStream testData = ParseTest.class.getResourceAsStream("/persondata.txt");
            personEntityManager.parseRegistration(testData);
            testData.close();

            PersonQuery query = new PersonQuery();
            //query.setCprNumber("121008217");
            query.setFornavn("Tester");
            session = sessionManager.getSessionFactory().openSession();

            List<PersonEntity> firstEntities = queryManager.getAllEntities(session, query, PersonEntity.class);
            List<Object> output = personOutputWrapper.wrapResults(firstEntities);
            Assert.assertEquals(1, output.size());

            String firstImport = objectMapper.writeValueAsString(output);
            session.close();

            testData = ParseTest.class.getResourceAsStream("/persondata.txt");
            personEntityManager.parseRegistration(testData);
            testData.close();

            session = sessionManager.getSessionFactory().openSession();

            List<PersonEntity> secondEntities = queryManager.getAllEntities(session, query, PersonEntity.class);

            String secondImport = objectMapper.writeValueAsString(personOutputWrapper.wrapResults(secondEntities));
            assertJsonEquality(objectMapper.readTree(firstImport), objectMapper.readTree(secondImport), true, true);

            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(secondImport)));

            session.close();

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Test
    public void testParseRoad() throws IOException, DataFordelerException {
        Session session = null;
        try {
            InputStream testData = ParseTest.class.getResourceAsStream("/roaddata.txt");
            long start = Instant.now().toEpochMilli();
            roadEntityManager.parseRegistration(testData);
            testData.close();
            System.out.println("Parsed road data in "+ (Instant.now().toEpochMilli() - start) + " ms");
            session = sessionManager.getSessionFactory().openSession();


            RoadQuery query = new RoadQuery();
            query.addKommunekode("0730");
            query.setVejkode("0004");

            List<RoadEntity> entities = queryManager.getAllEntities(session, query, RoadEntity.class);
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entities));
            Assert.assertEquals(1, entities.size());
            RoadEntity roadEntity = entities.get(0);
            Assert.assertEquals(CprPlugin.getDomain(), roadEntity.getDomain());
            Assert.assertEquals(730, roadEntity.getKommunekode());
            Assert.assertEquals(4, roadEntity.getVejkode());
            Assert.assertEquals(2, roadEntity.getRegistrations().size());
            RoadRegistration registration1 = roadEntity.getRegistrations().get(0);
            Assert.assertEquals(0, registration1.getSequenceNumber());
            Assert.assertTrue(Equality.equal(OffsetDateTime.parse("2006-12-22T12:00:00+01:00"), registration1.getRegistrationFrom()));
            Assert.assertTrue(Equality.equal(OffsetDateTime.parse("2008-05-30T09:11:00+02:00"), registration1.getRegistrationTo()));
            List<RoadEffect> effects1 = registration1.getSortedEffects();
            Assert.assertEquals(2, effects1.size());
            RoadEffect effect11 = effects1.get(0);

            Assert.assertTrue(Equality.equal(OffsetDateTime.parse("1900-01-01T12:00:00+01:00"), effect11.getEffectFrom()));
            Assert.assertNull(effect11.getEffectTo());
            Assert.assertEquals("Aalborggade", effect11.getData().get("adresseringsnavn"));
            Assert.assertEquals("Aalborggade", effect11.getData().get("vejnavn"));
            Assert.assertFalse(effect11.getEffectFromUncertain());
            Assert.assertFalse(effect11.getEffectToUncertain());

            RoadEffect effect12 = effects1.get(1);
            Assert.assertTrue(Equality.equal(OffsetDateTime.parse("1996-03-12T07:42:00+01:00"), effect12.getEffectFrom()));
            Assert.assertNull(effect12.getEffectTo());
            List<RoadMemoData> memo = (List<RoadMemoData>) effect12.getData().get("memo");
            Assert.assertEquals(1, memo.get(0).getMemoNumber());
            Assert.assertEquals("HUSNR.1 - BØRNEINSTITUTION -", memo.get(0).getMemoText());
            Assert.assertEquals(2, memo.get(1).getMemoNumber());
            Assert.assertEquals("HUSNR.2 - EGEDAL -", memo.get(1).getMemoText());
            Assert.assertEquals(3, memo.get(2).getMemoNumber());
            Assert.assertEquals("HUSNR.3 - KIRKE -", memo.get(2).getMemoText());

            RoadRegistration registration2 = roadEntity.getRegistrations().get(1);
            Assert.assertEquals(1, registration2.getSequenceNumber());
            Assert.assertTrue(Equality.equal(OffsetDateTime.parse("2008-05-30T09:11:00+02:00"), registration2.getRegistrationFrom()));
            Assert.assertNull(registration2.getRegistrationTo());
            List<RoadEffect> effects2 = registration2.getSortedEffects();
            Assert.assertEquals(3, effects2.size());
            RoadEffect effect21 = effects2.get(0);
            Assert.assertNull(effect21.getEffectFrom());
            Assert.assertNull(effect21.getEffectTo());
            Assert.assertFalse(effect21.getEffectFromUncertain());
            Assert.assertFalse(effect21.getEffectToUncertain());
            List<RoadPostcodeData> post = (List<RoadPostcodeData>) effect21.getData().get("postcode");
            Assert.assertEquals(2, post.size());
            Assert.assertEquals("001", post.get(0).getHouseNumberFrom());
            Assert.assertEquals("999", post.get(0).getHouseNumberTo());
            Assert.assertFalse(post.get(0).isEven());
            Assert.assertEquals(8940, post.get(0).getPostCode().getPostnummer());
            Assert.assertEquals("Randers SV", post.get(0).getPostCode().getPostdistrikt());
            Assert.assertEquals("002", post.get(1).getHouseNumberFrom());
            Assert.assertEquals("998", post.get(1).getHouseNumberTo());
            Assert.assertTrue(post.get(1).isEven());
            Assert.assertEquals(8940, post.get(1).getPostCode().getPostnummer());
            Assert.assertEquals("Randers SV", post.get(1).getPostCode().getPostdistrikt());
            Assert.assertFalse(effect21.getEffectFromUncertain());
            Assert.assertFalse(effect21.getEffectToUncertain());

            String firstImport = objectMapper.writeValueAsString(entities);






            testData = ParseTest.class.getResourceAsStream("/roaddata.txt");
            roadEntityManager.parseRegistration(testData);
            testData.close();
            session = sessionManager.getSessionFactory().openSession();
            entities = queryManager.getAllEntities(session, query, RoadEntity.class);
            String secondImport = objectMapper.writeValueAsString(entities);
            session.close();


            assertJsonEquality(objectMapper.readTree(firstImport), objectMapper.readTree(secondImport), true, true);

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Test
    public void testParseResidence() throws Exception {
        Session session = null;
        try {
            InputStream testData = ParseTest.class.getResourceAsStream("/roaddata.txt");
            residenceEntityManager.parseRegistration(testData);
            testData.close();

            ResidenceQuery query = new ResidenceQuery();
            query.addKommunekode("0360");
            query.setVejkode("0206");
            session = sessionManager.getSessionFactory().openSession();

            List<ResidenceEntity> entities = queryManager.getAllEntities(session, query, ResidenceEntity.class);
            Assert.assertEquals(1, entities.size());
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
            session.close();


            String firstImport = objectMapper.writeValueAsString(entities);



            testData = ParseTest.class.getResourceAsStream("/roaddata.txt");
            residenceEntityManager.parseRegistration(testData);
            testData.close();

            session = sessionManager.getSessionFactory().openSession();

            entities = queryManager.getAllEntities(session, query, ResidenceEntity.class);
            String secondImport = objectMapper.writeValueAsString(entities);

            assertJsonEquality(objectMapper.readTree(firstImport), objectMapper.readTree(secondImport), true, true);



        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Test
    public void testFindRegistrations() {
        OffsetDateTime open = null;
        OffsetDateTime time1 = OffsetDateTime.parse("2001-01-01T00:00:00Z");
        OffsetDateTime time2 = OffsetDateTime.parse("2002-02-02T00:00:00Z");
        OffsetDateTime time3 = OffsetDateTime.parse("2003-03-03T00:00:00Z");
        OffsetDateTime time4 = OffsetDateTime.parse("2004-04-04T00:00:00Z");

        // Find/create registrations on empty entity, with specific range
        RoadEntity entity1 = new RoadEntity();
        List<RoadRegistration> result1 = entity1.findRegistrations(time1, time4);
        Assert.assertEquals(1, result1.size());
        Assert.assertTrue(Equality.equal(time1, result1.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time4, result1.get(0).getRegistrationTo()));

        // Find/create registrations on empty entity, with open start
        RoadEntity entity2 = new RoadEntity();
        List<RoadRegistration> result2 = entity2.findRegistrations(open, time3);
        Assert.assertEquals(1, result2.size());
        Assert.assertTrue(Equality.equal(open, result2.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time3, result2.get(0).getRegistrationTo()));

        // Find/create registrations on empty entity, with open end
        RoadEntity entity3 = new RoadEntity();
        List<RoadRegistration> result3 = entity3.findRegistrations(time2, open);
        Assert.assertEquals(1, result3.size());
        Assert.assertTrue(Equality.equal(time2, result3.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(open, result3.get(0).getRegistrationTo()));

        // Find/create registrations on empty entity, with open start and end
        RoadEntity entity4 = new RoadEntity();
        List<RoadRegistration> result4 = entity4.findRegistrations(open, open);
        Assert.assertEquals(1, result4.size());
        Assert.assertTrue(Equality.equal(open, result4.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(open, result4.get(0).getRegistrationTo()));

        // Find/create registrations on entity with one registration, with matching range
        RoadEntity entity5 = new RoadEntity();
        RoadRegistration registration5 = entity5.createRegistration();
        registration5.setRegistrationFrom(time1);
        registration5.setRegistrationTo(time4);
        List<RoadRegistration> result5 = entity5.findRegistrations(time1, time4);
        Assert.assertEquals(1, result5.size());
        Assert.assertTrue(Equality.equal(time1, result5.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time4, result5.get(0).getRegistrationTo()));

        // Find/create registrations on entity with one registration, with off-range
        RoadEntity entity6 = new RoadEntity();
        RoadRegistration registration6 = entity6.createRegistration();
        registration6.setRegistrationFrom(time2);
        registration6.setRegistrationTo(time4);
        List<RoadRegistration> result6 = entity6.findRegistrations(time1, time3);
        Assert.assertEquals(2, result6.size());
        Assert.assertTrue(Equality.equal(time1, result6.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time2, result6.get(0).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time2, result6.get(1).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time3, result6.get(1).getRegistrationTo()));

        // Find/create registrations on entity with one registration, with range off at the end
        RoadEntity entity7 = new RoadEntity();
        RoadRegistration registration7 = entity7.createRegistration();
        registration7.setRegistrationFrom(time1);
        registration7.setRegistrationTo(time3);
        List<RoadRegistration> result7 = entity7.findRegistrations(time1, time4);
        Assert.assertEquals(2, result7.size());
        Assert.assertTrue(Equality.equal(time1, result7.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time3, result7.get(0).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time3, result7.get(1).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time4, result7.get(1).getRegistrationTo()));

        // Find/create registrations on entity with two registrations, with matching range
        RoadEntity entity8 = new RoadEntity();
        RoadRegistration registration8a = entity8.createRegistration();
        registration8a.setRegistrationFrom(time1);
        registration8a.setRegistrationTo(time2);
        RoadRegistration registration8b = entity8.createRegistration();
        registration8b.setRegistrationFrom(time2);
        registration8b.setRegistrationTo(time3);
        List<RoadRegistration> result8 = entity8.findRegistrations(time1, time3);
        Assert.assertEquals(2, result8.size());
        Assert.assertTrue(Equality.equal(time1, result8.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time2, result8.get(0).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time2, result8.get(1).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time3, result8.get(1).getRegistrationTo()));

        // Find/create registrations on entity with two registrations, with non-matching range
        RoadEntity entity9 = new RoadEntity();
        RoadRegistration registration9a = entity9.createRegistration();
        registration9a.setRegistrationFrom(open);
        registration9a.setRegistrationTo(time2);
        RoadRegistration registration9b = entity9.createRegistration();
        registration9b.setRegistrationFrom(time2);
        registration9b.setRegistrationTo(time4);
        List<RoadRegistration> result9 = entity9.findRegistrations(time1, time3);
        Assert.assertEquals(2, result9.size());
        Assert.assertTrue(Equality.equal(time1, result9.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time2, result9.get(0).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time2, result9.get(1).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time3, result9.get(1).getRegistrationTo()));

        // Find/create registrations on entity with two registrations (non-aligned), with non-matching range
        RoadEntity entity10 = new RoadEntity();
        RoadRegistration registration10a = entity10.createRegistration();
        registration10a.setRegistrationFrom(open);
        registration10a.setRegistrationTo(time2);
        RoadRegistration registration10b = entity10.createRegistration();
        registration10b.setRegistrationFrom(time3);
        registration10b.setRegistrationTo(open);
        List<RoadRegistration> result10 = entity10.findRegistrations(time1, time4);
        Assert.assertEquals(3, result10.size());
        Assert.assertTrue(Equality.equal(time1, result10.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time2, result10.get(0).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time2, result10.get(1).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time3, result10.get(1).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time3, result10.get(2).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time4, result10.get(2).getRegistrationTo()));


        // Find/create registrations on entity with two registrations (non-aligned), with open range
        RoadEntity entity11 = new RoadEntity();
        RoadRegistration registration11a = entity11.createRegistration();
        registration11a.setRegistrationFrom(time1);
        registration11a.setRegistrationTo(time2);
        RoadRegistration registration11b = entity11.createRegistration();
        registration11b.setRegistrationFrom(time3);
        registration11b.setRegistrationTo(time4);
        List<RoadRegistration> result11 = entity11.findRegistrations(open, open);
        Assert.assertEquals(5, result11.size());
        Assert.assertEquals(5, entity11.getRegistrations().size());
        Assert.assertTrue(Equality.equal(open, result11.get(0).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time1, result11.get(0).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time1, result11.get(1).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time2, result11.get(1).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time2, result11.get(2).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time3, result11.get(2).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time3, result11.get(3).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(time4, result11.get(3).getRegistrationTo()));
        Assert.assertTrue(Equality.equal(time4, result11.get(4).getRegistrationFrom()));
        Assert.assertTrue(Equality.equal(open, result11.get(4).getRegistrationTo()));

        List<RoadRegistration> result11a = entity11.findRegistrations(open, open);
        Assert.assertEquals(5, result11a.size());
        Assert.assertEquals(5, entity11.getRegistrations().size());
    }

    private static void assertJsonEquality(JsonNode node1, JsonNode node2, boolean ignoreArrayOrdering, boolean printDifference) {
        try {
            Assert.assertEquals(node1.isNull(), node2.isNull());
            Assert.assertEquals(node1.isArray(), node2.isArray());
            Assert.assertEquals(node1.isObject(), node2.isObject());
            Assert.assertEquals(node1.isLong(), node2.isLong());
            Assert.assertEquals(node1.isInt(), node2.isInt());
            Assert.assertEquals(node1.isShort(), node2.isShort());
            Assert.assertEquals(node1.isBoolean(), node2.isBoolean());
            Assert.assertEquals(node1.isTextual(), node2.isTextual());
            if (node1.isArray()) {
                Assert.assertEquals(node1.size(), node2.size());
                if (ignoreArrayOrdering) {
                    for (int i = 0; i < node1.size(); i++) {
                        boolean match = false;
                        for (int j = 0; j < node2.size(); j++) {
                            try {
                                assertJsonEquality(node1.get(i), node2.get(j), true, false);
                                match = true;
                            } catch (AssertionError e) {
                            }
                        }
                        if (!match) {
                            throw new AssertionError();
                        }
                    }
                } else {
                    for (int i = 0; i < node1.size(); i++) {
                        assertJsonEquality(node1.get(i), node2.get(i), false, printDifference);
                    }
                }
            } else if (node1.isObject()) {
                Assert.assertEquals(node1.size(), node2.size());
                Iterator<String> keys = node1.fieldNames();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Assert.assertNotNull(node2.get(key));
                    assertJsonEquality(node1.get(key), node2.get(key), ignoreArrayOrdering, printDifference);
                }
            } else {
                Assert.assertEquals(node1.asText(), node2.asText());
            }
        } catch (AssertionError e) {
            if (printDifference) {
                System.out.println("\n" + node1 + "\n != \n" + node2 + "\n\n\n");
            }
            throw e;
        }
    }

}
