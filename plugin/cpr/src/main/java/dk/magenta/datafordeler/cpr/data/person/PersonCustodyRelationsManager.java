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
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Comparator.naturalOrder;

/**
 * This module construct at list of CPR-numbers that another CPR-number has custody over.
 * The custody of a CPR-number can come from either having a child,
 * getting custody tranferred by record 046 or the custody can by lost when the child passes 18 years old
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
        List<PersonEntity> requestedInstancesOfPerson = QueryManager.getAllEntities(session, query, PersonEntity.class);
        List<String> collectiveCustodyArrayList = new ArrayList<String>();
        //Empty list is the person is not found in datafordeler
        if(requestedInstancesOfPerson.isEmpty()) {
            return collectiveCustodyArrayList;
        }
        //there can not be more then one person with that cpr-number
        //Find all children of the person
        List<String> childCprArrayList = new ArrayList<String>();
        for(ChildrenDataRecord child : requestedInstancesOfPerson.get(0).getChildren()) {
            childCprArrayList.add(child.getChildCprNumber());
        }

        //Lookup all the found children
        query = new PersonRecordQuery();
        for(String item : childCprArrayList) {
            query.addPersonnummer(item);
        }
        if(!childCprArrayList.isEmpty()) {
            List<PersonEntity> childrenOfTheRequestedPerson = QueryManager.getAllEntities(session, query, PersonEntity.class);
            for (PersonEntity child : childrenOfTheRequestedPerson) {
                BirthTimeDataRecord birthTime = findNewestUnclosed(child.getBirthTime());
                String childsFather = findNewestUnclosed(child.getFather()).getCprNumber();
                String childsMother = findNewestUnclosed(child.getMother()).getCprNumber();

                if (LocalDateTime.now().minusYears(18).isAfter(birthTime.getBirthDatetime())) {
                    //If the child is more then 18 years old, it should not be added to the list of custody

                } else if (child.getCustody().size() == 0) {
                    //If there is no registration of custody the child should be added to the parent
                    collectiveCustodyArrayList.add(child.getPersonnummer());
                } else {
                    boolean motherhasCustody = child.getCustody().stream().anyMatch(r -> r.getBitemporality().registrationTo == null &&
                            r.getBitemporality().effectTo == null && r.getRelationType()==3);
                    boolean fatherhasCustody = child.getCustody().stream().anyMatch(r -> r.getBitemporality().registrationTo == null &&
                            r.getBitemporality().effectTo == null && r.getRelationType()==4);

                    if(motherhasCustody && childsMother.equals(pnr) || fatherhasCustody && childsFather.equals(pnr)) {
                        collectiveCustodyArrayList.add(child.getPersonnummer());
                    }
                }
            }
        }

        //Find other persons that the person in quest has custody over
        query = new PersonRecordQuery();
        query.addCustodyPnr(pnr);
        List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
        for(PersonEntity personEntityItem : entities) {
            collectiveCustodyArrayList.add(personEntityItem.getPersonnummer());
        }
        return collectiveCustodyArrayList;
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
