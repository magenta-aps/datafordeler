package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.MultiClassQuery;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class GeoIntegrationTest {


    @Autowired
    private Engine engine;

    @SpyBean
    private CprConfigurationManager configurationManager;

    @Autowired
    private CprPlugin plugin;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SessionManager sessionManager;

    //@Before
    public void importData() {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        configuration.setRoadRegisterType(CprConfiguration.RegisterType.DISABLED);
        configuration.setResidenceRegisterType(CprConfiguration.RegisterType.DISABLED);
        configuration.setPersonRegisterType(CprConfiguration.RegisterType.LOCAL_FILE);
        configuration.setPersonRegisterLocalFile("data/test.txt");

        Pull pull = new Pull(engine, plugin);
        pull.run();
    }

    @Test
    public void testLookup() {
        PersonRecordQuery personRecordQuery = new PersonRecordQuery();
        personRecordQuery.setPersonnummer("0101000000");
        personRecordQuery.addForcedJoin(PersonRecordQuery.KOMMUNEKODE);

        MultiClassQuery m = new MultiClassQuery();
        String personRoot = "person";
        String roadRoot = "road";
        m.add(personRecordQuery, PersonEntity.class.getCanonicalName(), personRoot, "");

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        EntityManager roadManager = geoPlugin.getEntityManager("Road");
        if (roadManager != null) {
            HashMap<String, String> fieldMap = new HashMap<>();
            fieldMap.put("municipalitycode", BaseLookupDefinition.getParameterPath(personRoot, personRoot, PersonEntity.DB_FIELD_ADDRESS) + BaseLookupDefinition.separator + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE);
            fieldMap.put("roadcode", BaseLookupDefinition.getParameterPath(personRoot, personRoot, PersonEntity.DB_FIELD_ADDRESS) + BaseLookupDefinition.separator + AddressDataRecord.DB_FIELD_ROAD_CODE);
            m.add(roadManager.getJoinQuery(fieldMap, roadRoot), roadRoot);
            Session session = sessionManager.getSessionFactory().openSession();
            System.out.println(QueryManager.getQuery(session, m));

            OutputWrapper outputWrapper = roadManager.getOutputWrapper();
            System.out.println(outputWrapper);
        }
    }

}
