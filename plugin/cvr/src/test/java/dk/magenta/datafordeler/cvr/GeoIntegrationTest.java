package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import org.hibernate.Session;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GeoIntegrationTest extends TestBase {

    @Autowired
    private PluginManager pluginManager;

    @Test
    public void testLookup() {
        // Mostly for our own sake during developement
        CompanyRecordQuery query = new CompanyRecordQuery();
        query.addExtraJoin("LEFT JOIN cvr_company.locationAddress cvr_company__locationAddress");
        query.addExtraJoin("LEFT JOIN cvr_company__locationAddress.municipality cvr_company__locationAddress__municipality");
        query.addExtraJoin("LEFT JOIN cvr_company__locationAddress__municipality.municipality cvr_company__locationAddress__municipality__municipality");
        query.setParameter(CompanyRecordQuery.CVRNUMMER, "12345678");

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        if (geoPlugin != null) {

            Session session = sessionManager.getSessionFactory().openSession();
            QueryManager.getAllEntitySets(session, query, CompanyRecord.class);

        }
    }

}
