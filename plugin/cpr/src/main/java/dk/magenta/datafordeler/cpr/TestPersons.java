package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.PersonStatusDataRecord;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class TestPersons {

    @Autowired
    private SessionManager sessionManager;

    @PostConstruct
    public void createTestPersons() {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        String cpr = "1111110000";
        if (QueryManager.getItem(session, PersonEntity.class, Collections.singletonMap(PersonEntity.DB_FIELD_CPR_NUMBER, cpr)) == null) {

            CprBitemporality bitemporality = new CprBitemporality(null, null, null, false, null, false);
            PersonEntity testPerson = new PersonEntity(PersonEntity.generateUUID(cpr), "https://data.gl/cpr/person/1/rest/");
            testPerson.setPersonnummer(cpr);
            session.save(testPerson.identification);
            session.save(testPerson);

            AddressDataRecord testAddress = new AddressDataRecord(956, 102, null, "32A", "3. sal", null, null, null, null, null, null, 0, 0);
            testAddress.setBitemporality(bitemporality);
            testAddress.setEntity(testPerson);
            session.save(testAddress);

            BirthTimeDataRecord testBirthTime = new BirthTimeDataRecord(
                    LocalDateTime.of(1980, 1, 1, 0, 0, 0),
                    false ,0
            );
            testBirthTime.setBitemporality(bitemporality);
            testBirthTime.setEntity(testPerson);
            session.save(testBirthTime);

            PersonStatusDataRecord testStatus = new PersonStatusDataRecord();
            testStatus.setStatus(5);
            testStatus.setBitemporality(bitemporality);
            testStatus.setEntity(testPerson);
            session.save(testStatus);

            System.out.println("TESTPERSON CREATED");
        }
        transaction.commit();
        session.close();
    }
}
