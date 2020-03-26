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
import org.hibernate.Session;
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
        CompanyRecordQuery query = new CompanyRecordQuery();
        query.addExtraJoin("LEFT JOIN cvr_company.locationAddress cvr_company__locationAddress");
        query.addExtraJoin("LEFT JOIN cvr_company__locationAddress.municipality cvr_company__locationAddress__municipality");
        query.addExtraJoin("LEFT JOIN cvr_company__locationAddress__municipality.municipality cvr_company__locationAddress__municipality__municipality");
        query.setCvrNumre("12345678");

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        if (geoPlugin != null) {

            HashMap<String, String> handles = new HashMap<>();
            handles.put("municipalitycode", "cvr_company__locationAddress__municipality__municipality.code");
            handles.put("roadcode", "cvr_company__locationAddress.roadCode");

            query.addExtraJoin(geoPlugin.getJoinString(handles));
            query.addExtraTables(geoPlugin.getJoinClassAliases(handles.keySet()));

            //query.addExtraJoin(cprPlugin.getJoinString(handles));
            //query.addExtraTables(cprPlugin.getJoinClassAliases());


            Session session = sessionManager.getSessionFactory().openSession();
            System.out.println(QueryManager.getQuery(session, query));
        }
    }

}
