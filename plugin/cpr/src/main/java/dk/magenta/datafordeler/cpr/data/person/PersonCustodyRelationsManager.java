package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.cpr.records.person.data.ChildrenDataRecord;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * This module cunstruct at list of CPR-numbers that another CPR-number has custody over.
 * The custody of a CPR-number can come from either having a child,
 * getting custidy tranferred by record 046 or the costidy can by lost when the child passes 18 years old
 */
@Component
public class PersonCustodyRelationsManager {

    @Autowired
    private SessionManager sessionManager;

    public List<String> findRelations(String pnr) {

        Session session = sessionManager.getSessionFactory().openSession();
        PersonRecordQuery query = new PersonRecordQuery();
        query.addPersonnummer(pnr);
        List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
        List<String> custodyArrayList = new ArrayList<String>();
        if(entities.isEmpty()) {
            return custodyArrayList;
        }
        //there can not be more then one person with that cpr-number
        for(ChildrenDataRecord child : entities.get(0).getChildren()) {
            custodyArrayList.add(child.getChildCprNumber());
        }

        query = new PersonRecordQuery();
        for(String item : custodyArrayList) {
            query.addPersonnummer(item);
        }
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
        for(PersonEntity personEntityItem : entities) {
            custodyArrayList.remove(personEntityItem.getPersonnummer());
        }

        query = new PersonRecordQuery();
        query.addCustodyPnr("0101011234");
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
        for(PersonEntity personEntityItem : entities) {
            custodyArrayList.add(personEntityItem.getPersonnummer());
        }
        return custodyArrayList;
    }
}
