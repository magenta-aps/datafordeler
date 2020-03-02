package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.cvr.data.company.CompanyEntity;
import dk.magenta.datafordeler.cvr.data.company.CompanyRegistration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class DedupTest {

    private Logger log = LogManager.getLogger(this.getClass().getName());

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private Engine engine;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void dedup() throws Exception {
        Session session = sessionManager.getSessionFactory().openSession();
        CompanyEntity e = QueryManager.getEntity(session, UUID.fromString("1507f591-9b8b-3d8f-9628-a8efe2dcb179"), CompanyEntity.class);
        ArrayList<CompanyRegistration> orderedRegistrations = new ArrayList<>(e.getRegistrations());
        Collections.sort(orderedRegistrations);
        CompanyRegistration last = null;
        for (CompanyRegistration registration : orderedRegistrations) {
            if (last != null && last.compareTo(registration) == 0) {
                System.out.println("Registration collision on "+registration.getRegistrationFrom()+"|"+registration.getRegistrationTo());
            }
            last = registration;
        }
    }

}