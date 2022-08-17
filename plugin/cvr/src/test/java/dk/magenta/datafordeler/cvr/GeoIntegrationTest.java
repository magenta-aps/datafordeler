package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressQuery;
import org.hibernate.Session;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

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
        // Mostly for our own sake during developement
        CompanyRecordQuery query = new CompanyRecordQuery();
        query.addExtraJoin("LEFT JOIN cvr_company.locationAddress cvr_company__locationAddress");
        query.addExtraJoin("LEFT JOIN cvr_company__locationAddress.municipality cvr_company__locationAddress__municipality");
        query.addExtraJoin("LEFT JOIN cvr_company__locationAddress__municipality.municipality cvr_company__locationAddress__municipality__municipality");
        query.setParameter(CompanyRecordQuery.CVRNUMMER,"12345678");

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        if (geoPlugin != null) {

            Session session = sessionManager.getSessionFactory().openSession();
            //System.out.println(QueryManager.getFirstQuery(session, query));
QueryManager.getAllEntitySets(session, query, CompanyRecord.class);

            //AccessAddressQuery q = new AccessAddressQuery();
            //q.setMunicipalityCode();

        }
    }

}
