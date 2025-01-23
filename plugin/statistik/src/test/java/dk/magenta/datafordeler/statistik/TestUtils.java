package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.road.RoadEntityManager;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.when;

@Component
public class TestUtils {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private dk.magenta.datafordeler.cpr.data.road.RoadEntityManager cprRoadEntityManager;

    @Autowired
    private GeoPlugin geoPlugin;

    @Autowired
    private RoadEntityManager roadEntityManager;

    @Autowired
    private dk.magenta.datafordeler.geo.data.locality.LocalityEntityManager localityEntityManager;

    @Autowired
    private dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntityManager accessAddressEntityManager;

    @Autowired
    private dk.magenta.datafordeler.geo.data.postcode.PostcodeEntityManager postcodeEntityManager;

    @MockitoSpyBean
    private DafoUserManager dafoUserManager;

    public TestUtils(SessionManager sessionManager,
                     PersonEntityManager personEntityManager) {
        this.sessionManager = sessionManager;
        this.personEntityManager = personEntityManager;
    }


    public void loadPersonData(File source) throws Exception {
        loadPersonData(new FileInputStream(source));
    }

    public void loadPersonData(String resource) throws Exception {
        loadPersonData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestUtils.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadRoadData(String resource) throws Exception {
        loadRoadData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestUtils.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadRoadData(InputStream testData) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        cprRoadEntityManager.parseData(testData, importMetadata);
        session.close();
        testData.close();
    }

    public void loadGeoRoadData(String resource) throws DataFordelerException {
        loadGeoRoadData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestUtils.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadGeoRoadData(InputStream testData) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        roadEntityManager.parseData(testData, importMetadata);
        transaction.commit();
        session.close();
    }

    public void loadGeoLocalityData(String resource) throws DataFordelerException {
        loadGeoLocalityData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestUtils.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadGeoLocalityData(InputStream testData) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        localityEntityManager.parseData(testData, importMetadata);
        transaction.commit();
        session.close();
    }


    public void loadAccessLocalityData(String resource) throws DataFordelerException {
        loadAccessLocalityData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestUtils.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadAccessLocalityData(InputStream testData) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        accessAddressEntityManager.parseData(testData, importMetadata);
        transaction.commit();
        session.close();
    }


    public void loadPostalLocalityData(String resource) throws DataFordelerException {
        loadPostalLocalityData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestUtils.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadPostalLocalityData(InputStream testData) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        postcodeEntityManager.parseData(testData, importMetadata);
        transaction.commit();
        session.close();
    }


    public void loadPersonData(InputStream testData) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        personEntityManager.parseData(testData, importMetadata);
        transaction.commit();
        session.close();
    }

    public void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    public <E extends DatabaseEntry> void deleteAll(Class<E> eClass) {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Collection<E> entities = QueryManager.getAllEntities(session, eClass);
        for (E entity : entities) {
            session.delete(entity);
        }
        transaction.commit();
        session.close();
    }

    public void deleteAll() {
        this.deleteAll(PersonEntity.class);
    }

}
