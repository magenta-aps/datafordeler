package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.MultiClassQuery;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.AddressMunicipalityRecord;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class GeoIntegrationTest {


    @Autowired
    private Engine engine;

    @SpyBean
    private CvrConfigurationManager configurationManager;

    @Autowired
    private CvrPlugin plugin;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CompanyEntityManager entityManager;

    @Test
    public void testLookup() {
        CompanyRecordQuery companyRecordQuery = new CompanyRecordQuery();
        companyRecordQuery.setKommuneKode(956);
        companyRecordQuery.addForcedJoin(CompanyRecordQuery.KOMMUNEKODE);

        MultiClassQuery m = new MultiClassQuery();
        String companyRoot = "company";
        String roadRoot = "road";
        m.add(companyRecordQuery, CompanyRecord.class.getCanonicalName(), companyRoot, "");

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        EntityManager roadManager = geoPlugin.getEntityManager("Road");
        if (roadManager != null) {
            m.add(roadManager.getJoinQuery(entityManager.getJoinHandles(companyRoot), roadRoot), roadRoot);


            Session session = sessionManager.getSessionFactory().openSession();
            System.out.println(QueryManager.getQuery(session, m));
        }
    }

}
