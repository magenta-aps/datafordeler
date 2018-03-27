package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.cvr.data.CvrEntityManager;
import dk.magenta.datafordeler.cvr.data.company.CompanyEntity;
import dk.magenta.datafordeler.cvr.data.company.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitEntity;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitEntityManager;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantEntity;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantEntityManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class ParseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CvrPlugin plugin;

    private static HashMap<String, String> schemaMap = new HashMap<>();
    static {
        schemaMap.put("virksomhed", CompanyEntity.schema);
        schemaMap.put("produktionsenhed", CompanyUnitEntity.schema);
        schemaMap.put("deltager", ParticipantEntity.schema);
    }

    @Test
    public void testParseCompanyFile() throws DataFordelerException, IOException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/company_in.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                CompanyEntityManager entityManager = (CompanyEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                List<? extends Registration> registrations = entityManager.parseData(item.get("_source").get("Vrvirksomhed"), importMetadata, session);
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(registrations.get(0).getEntity()));

                Collections.sort(registrations);
                Assert.assertEquals(OffsetDateTime.parse("1999-11-29T16:33:00+01:00"), registrations.get(0).getRegistrationFrom());
                Assert.assertEquals(OffsetDateTime.parse("1999-11-29T16:37:00+01:00"), registrations.get(0).getRegistrationTo());
                Assert.assertEquals(91, registrations.size());
                //Assert.assertEquals(OffsetDateTime.parse("1999-11-29T16:33:47+01:00"), registrations.get(0).getRegistrationFrom());
                //Assert.assertEquals(OffsetDateTime.parse("1999-11-29T16:33:51+01:00"), registrations.get(0).getRegistrationTo());
                //Assert.assertEquals(123, registrations.size());
            }
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }
    }

    @Test
    public void testParseUnitFile() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/unit.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                CompanyUnitEntityManager entityManager = (CompanyUnitEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                List<? extends Registration> registrations = entityManager.parseData(item.get("_source").get("VrproduktionsEnhed"), importMetadata, session);
                System.out.println("registrations.size: " + registrations.size());
                System.out.println(objectMapper.writeValueAsString(registrations));
            }
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }
    }

    @Test
    public void testParseParticipantFile() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/person.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            Assert.assertEquals(1, itemList.size());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                ParticipantEntityManager entityManager = (ParticipantEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                List<? extends Registration> registrations = entityManager.parseData(item.get("_source").get("Vrdeltagerperson"), importMetadata, session);
                System.out.println("registrations.size: " + registrations.size());
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(registrations));
                Assert.assertEquals(5, registrations.size());
            }
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }
    }

}
