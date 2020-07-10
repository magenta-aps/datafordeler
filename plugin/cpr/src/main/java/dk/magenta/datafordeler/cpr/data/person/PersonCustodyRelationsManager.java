package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ChildrenDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CustodyDataRecord;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static java.util.Comparator.naturalOrder;

/**
 * This module construct at list of CPR-numbers that another CPR-number has custody over.
 * The custody of a CPR-number can come from either having a child,
 * getting custidy tranferred by record 046 or the costidy can by lost when the child passes 18 years old
 */
@Component
public class PersonCustodyRelationsManager {

    private static Comparator bitemporalComparator = Comparator.comparing(PersonCustodyRelationsManager::getBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprNontemporalRecord::getOriginDate, Comparator.nullsLast(naturalOrder()))
            .thenComparing(CprNontemporalRecord::getDafoUpdated)
            .thenComparing(DatabaseEntry::getId);

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
        //Store all children of the person
        for(ChildrenDataRecord child : entities.get(0).getChildren()) {
            custodyArrayList.add(child.getChildCprNumber());
        }

        //Lookup all the found children
        query = new PersonRecordQuery();
        for(String item : custodyArrayList) {
            query.addPersonnummer(item);
        }
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
        for(PersonEntity child : entities) {
            //If the child has no custodys registered there is no need to do any removings
            if(child.getCustody().size()==0) {
                break;
            }
            BirthTimeDataRecord birthTime = findNewestUnclosed(child.getBirthTime());
            if(LocalDateTime.now().minusYears(18).isAfter(birthTime.getBirthDatetime().minusYears(18))) {
                custodyArrayList.remove(child.getPersonnummer());
            }

            //If the child got another custody assigned remove the child from the list
            CustodyDataRecord custody = findNewestUnclosed(child.getCustody());
            if(!custody.getRelationPnr().equals(pnr)) {
                custodyArrayList.remove(child.getPersonnummer());
            }
        }

        //Find other persons that the person in quest has custody over
        query = new PersonRecordQuery();
        query.addCustodyPnr(pnr);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
        for(PersonEntity personEntityItem : entities) {
            custodyArrayList.add(personEntityItem.getPersonnummer());
        }
        return custodyArrayList;
    }

    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestUnclosed(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null &&
                r.getBitemporality().effectTo == null).max(bitemporalComparator).orElse(null);
    }

    public static CprBitemporality getBitemporality(CprBitemporalRecord record) {
        return record.getBitemporality();
    }
}
