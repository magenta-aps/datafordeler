package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.PersonEventDataRecord;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class cprLoadAndmarkSameAddressAsSameas extends TestBase {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    private static final HashMap<String, String> schemaMap = new HashMap<>();

    static {
        schemaMap.put("person", PersonEntity.schema);
    }

    private void loadPerson(String resource, ImportMetadata importMetadata) throws DataFordelerException, IOException {
        InputStream testData = cprLoadAndmarkSameAddressAsSameas.class.getResourceAsStream(resource);
        personEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    @AfterEach
    public void clean() {
        Session session = sessionManager.getSessionFactory().openSession();
        session.close();
    }

    @Test
    public void testPersonLoadAndFilterOnSameAs1() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/doubleadd.txt", importMetadata);
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        PersonRecordQuery query = new PersonRecordQuery();
        query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101014321");
        query.setRegistrationFromAfter(Query.parseDateTime("2019-04-01"));
        query.applyFilters(session);
        List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        List<AddressDataRecord> addresslist = new ArrayList<AddressDataRecord>(entities.get(0).getAddress());
        Assertions.assertEquals(2, addresslist.size());
        List<AddressDataRecord> addresslistRemovedSameas = addresslist.stream().filter(add -> add.getSameAs() == null).collect(Collectors.toList());
        Assertions.assertEquals(1, addresslistRemovedSameas.size());
    }

    /**
     * This unittest validates that it is possible to filter out addresschanges that do not happen based on the eventtype A01
     *
     * @throws DataFordelerException
     * @throws IOException
     */
    @Test
    public void testPersonLoadAndFilterOnSameAs2() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/twoActualMovings.txt", importMetadata);
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        PersonRecordQuery query = new PersonRecordQuery();
        query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011234");
        query.setRegistrationFromAfter(Query.parseDateTime("2018-01-01"));
        query.applyFilters(session);
        List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        List<AddressDataRecord> targetList = new ArrayList<AddressDataRecord>(entities.get(0).getAddress());
        Assertions.assertEquals(4, targetList.size());
        List<AddressDataRecord> target2List = targetList.stream().filter(add -> add.getSameAs() == null).collect(Collectors.toList());
        Assertions.assertEquals(2, target2List.size());

        session.close();
        session = sessionManager.getSessionFactory().openSession();
        query = new PersonRecordQuery();
        query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011234");
        query.setRegistrationFromAfter(Query.parseDateTime("2016-01-01"));
        query.applyFilters(session);

        List<PersonEntity> entities3 = QueryManager.getAllEntities(session, query, PersonEntity.class);


        List<AddressDataRecord> addresslist = new ArrayList<AddressDataRecord>(entities.get(0).getAddress());
        Assertions.assertEquals(true, addresslist.size() > 2);

        List<AddressDataRecord> emplList = addresslist.stream().filter(add -> add.getSameAs() == null).collect(Collectors.toList());
        Assertions.assertEquals(true, emplList.size() >= 2);

        Set<PersonEventDataRecord> eventList = entities3.get(0).getEvent();
        List<PersonEventDataRecord> eventListMove = eventList.stream().filter(event -> "A01".equals(event.getEventId())).collect(Collectors.toList());
        Assertions.assertEquals(2, eventListMove.size());

        List<AddressDataRecord> filteredList = emplList.stream().filter(empl -> eventListMove.stream().anyMatch(dept -> empl.getRegistrationFrom().equals(dept.getTimestamp()))).collect(Collectors.toList());
        Assertions.assertEquals(2, filteredList.size());
    }
}
