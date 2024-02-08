package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
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
import java.util.*;

import static java.util.Comparator.naturalOrder;

/**
 * This module construct at list of CPR-numbers that another CPR-number has custody over.
 * The custody of a CPR-number can come from either having a child,
 * getting custody tranferred by record 046 or the custody can by lost when the child passes 18 years old
 */
@Component
public class PersonCustodyRelationsManager {

    private static final Comparator bitemporalComparator = Comparator.comparing(PersonCustodyRelationsManager::getBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprNontemporalRecord::getOriginDate, Comparator.nullsLast(naturalOrder()))
            .thenComparing(CprNontemporalRecord::getDafoUpdated)
            .thenComparing(DatabaseEntry::getId);

    @Autowired
    private SessionManager sessionManager;

    public List<ChildInfo> findRelations(String pnr) throws HttpNotFoundException, InvalidClientInputException {

        Session session = sessionManager.getSessionFactory().openSession();
        PersonRecordQuery query = new PersonRecordQuery();
        query.setParameter(PersonRecordQuery.PERSONNUMMER, pnr);
        List<PersonEntity> requestedInstancesOfPerson = QueryManager.getAllEntities(session, query, PersonEntity.class);
        List<ChildInfo> collectiveCustodyArrayList = new ArrayList<ChildInfo>();
        //Empty list is the person is not found in datafordeler
        if (requestedInstancesOfPerson.isEmpty()) {
            throw new HttpNotFoundException("No entity with CPR number " + pnr + " was found");
        }


        //there can not be more then one person with that cpr-number
        //Find all children of the person
        List<String> childCprArrayList = new ArrayList<String>();

        Set<ChildrenDataRecord> childrenList = requestedInstancesOfPerson.get(0).getChildren();

        for (ChildrenDataRecord child : childrenList) {
            childCprArrayList.add(child.getChildCprNumber());
        }

        //Lookup all the found children
        query = new PersonRecordQuery();
        query.setPageSize(40);
        query.setParameter(PersonRecordQuery.PERSONNUMMER, childCprArrayList);
        if (!childCprArrayList.isEmpty()) {
            List<PersonEntity> childrenOfTheRequestedPerson = QueryManager.getAllEntities(session, query, PersonEntity.class);
            for (PersonEntity child : childrenOfTheRequestedPerson) {
                BirthTimeDataRecord birthTime = findNewestUnclosed(child.getBirthTime());
                String childsFather = "";
                if (child.getFather().size() > 0) {
                    childsFather = findNewestUnclosed(child.getFather()).getCprNumber();
                }
                String childsMother = "";
                if (child.getMother().size() > 0) {
                    childsMother = findNewestUnclosed(child.getMother()).getCprNumber();
                }

                if (LocalDateTime.now().minusYears(18).isAfter(birthTime.getBirthDatetime())) {
                    //If the child is more then 18 years old, it should not be added to the list of custody

                } else if (child.getCustody().size() == 0) {
                    //If there is no registration of custody the child should be added to the parent
                    query = new PersonRecordQuery();
                    query.setParameter(PersonRecordQuery.PERSONNUMMER, child.getPersonnummer());
                    collectiveCustodyArrayList.add(new ChildInfo(child.getPersonnummer(), QueryManager.getAllEntities(session, query, PersonEntity.class).get(0).getStatus().current().get(0).getStatus()));
                } else {
                    List<CustodyDataRecord> currentCustodyList = child.getCustody().current();
                    boolean motherhasCustody = currentCustodyList.stream().anyMatch(r -> r.getRelationType() == 3);
                    boolean fatherhasCustody = currentCustodyList.stream().anyMatch(r -> r.getRelationType() == 4);

                    if (motherhasCustody && childsMother.equals(pnr) || fatherhasCustody && childsFather.equals(pnr)) {
                        query = new PersonRecordQuery();
                        query.setParameter(PersonRecordQuery.PERSONNUMMER, child.getPersonnummer());
                        collectiveCustodyArrayList.add(new ChildInfo(child.getPersonnummer(), QueryManager.getAllEntities(session, query, PersonEntity.class).get(0).getStatus().current().get(0).getStatus()));
                    }
                }
            }
        }

        //Find other persons that the person in quest has custody over
        query = new PersonRecordQuery();
        query.setParameter(PersonRecordQuery.CUSTODYPNR, pnr);
        List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
        for (PersonEntity personEntityItem : entities) {
            collectiveCustodyArrayList.add(new ChildInfo(personEntityItem.getPersonnummer(), personEntityItem.getStatus().current().get(0).getStatus()));
        }
        return collectiveCustodyArrayList;
    }


    public class ChildInfo {
        String pnr;
        int status;


        public ChildInfo(String pnr, int status) {
            this.pnr = pnr;
            this.status = status;
        }


        public String getPnr() {
            return pnr;
        }

        public void setPnr(String pnr) {
            this.pnr = pnr;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }


    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     *
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
