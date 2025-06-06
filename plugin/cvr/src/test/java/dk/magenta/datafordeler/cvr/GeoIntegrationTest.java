package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = Application.class)
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
