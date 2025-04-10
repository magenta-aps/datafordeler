package dk.magenta.datafordeler.statistik.services;

import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.BitemporalSet;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;
import dk.magenta.datafordeler.geo.GeoLookupService;
import dk.magenta.datafordeler.statistik.reportExecution.ReportProgressStatus;
import dk.magenta.datafordeler.statistik.reportExecution.ReportSyncHandler;
import dk.magenta.datafordeler.statistik.utils.Filter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class PersonStatisticsService extends StatisticsService {

    public static final ZoneId cprDataOffset = ZoneId.of("Europe/Copenhagen");
    private final Logger log = LogManager.getLogger(PersonStatisticsService.class.getCanonicalName());

    protected String[] requiredParameters() {
        return new String[]{};
    }

    protected abstract CprPlugin getCprPlugin();

    public int run(Filter filter, OutputStream outputStream, String reportUuid) {


        try (final Session primarySession = this.getSessionManager().getSessionFactory().openSession();
             final Session secondarySession = this.getSessionManager().getSessionFactory().openSession();
             final Session repSyncSession = this.getSessionManager().getSessionFactory().openSession()) {

            ReportSyncHandler repSyncHandler = new ReportSyncHandler(repSyncSession);
            repSyncHandler.setReportStatus(reportUuid, ReportProgressStatus.running);

            primarySession.setDefaultReadOnly(true);
            secondarySession.setDefaultReadOnly(true);
            List<PersonRecordQuery> queries = this.getQueryList(filter);
            Stream<Map<String, String>> concatenation = null;

            System.out.println("There are "+queries.size()+" queries");
            for (PersonRecordQuery query : queries) {
                System.out.println("Query: "+query.toHql());
                System.out.println(query.getConditionParameters());
//                Stream<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(primarySession, query, PersonEntity.class);
                List<PersonEntity> personEntities = QueryManager.getAllEntities(primarySession, query, PersonEntity.class);
                System.out.println(personEntities.size());
                Stream<PersonEntity> personEntityStream = personEntities.stream();
                Stream<Map<String, String>> formatted = this.formatItems(primarySession, personEntityStream, secondarySession, filter);

                concatenation = (concatenation == null) ? formatted : Stream.concat(concatenation, formatted);
            }
            log.info("Start writing persons");

            if (concatenation != null) {
                if (outputStream != null) {
                    log.info("Progress writing persons");
                    return this.writeItems(concatenation.iterator(), outputStream, item -> {
                        log.info("Done writing personsline");

                    });
                }
            }

        } catch (Exception e) {
            log.error("Failed generating report", e);

        } finally {
            log.info("Done writing report");
            try (final Session repSyncSession = this.getSessionManager().getSessionFactory().openSession()) {
                ReportSyncHandler repSyncHandler = new ReportSyncHandler(repSyncSession);
                repSyncHandler.setReportStatus(reportUuid, ReportProgressStatus.done);
            }
        }
        return 0;
    }

    protected abstract List<Map<String, String>> formatPerson(PersonEntity person, Session session, GeoLookupService lookupService, Filter filter);

    protected PersonRecordQuery getQuery(Filter filter) throws InvalidClientInputException {
        PersonRecordQuery personQuery = new PersonRecordQuery();
        if (filter.onlyPnr != null) {
            personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, filter.onlyPnr);
        }
        return personQuery;
    }

    protected List<PersonRecordQuery> getQueryList(Filter filter) throws IOException, InvalidClientInputException {
        return Collections.singletonList(this.getQuery(filter));
    }

    public Stream<Map<String, String>> formatItems(Session personSession, Stream<PersonEntity> personEntities, Session lookupSession, Filter filter) {
        GeoLookupService lookupService = new GeoLookupService(this.getSessionManager());
        return personEntities.flatMap(
                personEntity -> {
                    List<Map<String, String>> output = formatPerson(personEntity, lookupSession, lookupService, filter);
                    personSession.evict(personEntity);
                    return output.stream();
                }
        );
    }

    protected void applyAreaRestrictionsToQuery(PersonRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CprRolesDefinition.READ_CPR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.getCprPlugin().getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }

    public static <R extends CprBitemporalRecord> Set<R> filterRecordsByEffect(Collection<R> records, OffsetDateTime effectAt) {
        HashSet<R> filtered = (HashSet<R>) records.stream().filter(r -> effectAt == null || r.getBitemporality().containsEffect(effectAt, effectAt)).collect(toSet());
        return filtered;
    }

    public static <R extends CprBitemporalRecord> Set<R> filterRecordsByRegistration(Collection<R> records, OffsetDateTime registrationAt) {
        HashSet<R> filtered = new HashSet<>();
        for (R record : records) {
            if (record.getBitemporality().containsRegistration(registrationAt, registrationAt)) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    public static <R extends CprBitemporalRecord> Set<R> filterUndoneRecords(Collection<R> records) {
        HashSet<R> filtered = new HashSet<>();
        for (R record : records) {
            if (!record.isUndone()) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    private static final Comparator bitemporalComparator = Comparator.comparing(PersonStatisticsService::getBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprNontemporalRecord::getOriginDate, Comparator.nullsLast(naturalOrder()))
            .thenComparing(CprNontemporalRecord::getDafoUpdated)
            .thenComparing(DatabaseEntry::getId);

    public static <R extends CprBitemporalRecord> List<R> sortRecords(Collection<R> records) {
        ArrayList<R> recordList = new ArrayList<>(records);
        recordList.sort(bitemporalComparator);
        return recordList;
    }


    /**
     * Find a record which is uncloced in effect, and has a registrationFrom equal to changedToOrIsTime.
     * If none is found find the record with the newest registrationFrom.
     * Otherwise return null
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findRegistrationAtMatchingChangedtimePost(Collection<R> records, OffsetDateTime changedToOrIsTime) {
        R filtered = records.stream().filter(r -> r.getEffectTo() == null && r.getRegistrationFrom().equals(changedToOrIsTime)).findFirst().orElse(null);
        if (filtered == null) {
            Comparator regTimeComparator = Comparator.comparing(R::getRegistrationFrom);
            filtered = (R) records.stream().max(regTimeComparator).orElse(null);
        }
        return filtered;
    }

    /**
     * Find a record which is uncloced in effect, and has a registrationto equal to changedToOrIsTime.
     * If none is found find the record with the newest registrationFrom.
     * Otherwise return null
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findRegistrationAtMatchingChangedtimePre(Collection<R> records, OffsetDateTime changedToOrIsTime) {
        R result = null;
        List<R> filtered = records.stream().filter(r -> r.getEffectTo() == null && r.getRegistrationTo() != null && r.getRegistrationTo().equals(changedToOrIsTime)).collect(toList());
        if (filtered.size() == 0) {
            Comparator regTimeComparator = Comparator.comparing(R::getRegistrationFrom);
            result = (R) records.stream().max(regTimeComparator).orElse(null);
        } else {
            result = filtered.get(0);
        }
        return result;
    }


    /**
     * Find the most important registration according to "bitemporalComparator"
     * Records with a missing OriginDate is removed since they are considered invalid
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findMostImportant(Collection<R> records) {
        return (R) records.stream().max(bitemporalComparator).orElse(null);
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
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null).max(bitemporalComparator).orElse(null);
    }

    /**
     * Find all records which is unclosed in registrationinterval, and which is not undone.
     * Beside of that it has to be either unclosed in the registration interval, or a part of it has to be active at the time defined by intervalstart
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> List<R> findAllUnclosedInRegistrationAndNotUndone(BitemporalSet<R> records, OffsetDateTime intervalstart) {
        List<R> recordList = records.stream().filter(r -> r.getBitemporality().registrationTo == null && !r.isUndone() &&
                (r.getBitemporality().effectTo == null || r.getBitemporality().effectTo.isAfter(intervalstart))).collect(toList());
        return recordList;
    }

    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestUnclosedOnRegistartionAndEffect(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null &&
                r.getBitemporality().effectTo == null).max(bitemporalComparator).orElse(null);
    }

    /**
     * Find the newest unclosed record with specified effect from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestUnclosedWithSpecifiedEffect(Collection<R> records, OffsetDateTime effectAt) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null && r.getBitemporality().containsEffect(effectAt, effectAt)).max(bitemporalComparator).orElse(null);
    }

    /**
     * @param records
     * @param effectAt
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestAfterFilterOnEffect(Collection<R> records, OffsetDateTime effectAt) {
        return (R) records.stream().filter(r -> (effectAt == null || r.getBitemporality().containsEffect(effectAt, effectAt))).max(bitemporalComparator).orElse(null);
    }


    public static CprBitemporality getBitemporality(CprBitemporalRecord record) {
        return record.getBitemporality();
    }


    public static <R extends CprBitemporalRecord> List<R> FilterOnRegistrationFrom(Collection<R> records, OffsetDateTime registrationTimeStart, OffsetDateTime registrationTimeEnd) {
        List<R> filtered = records.stream().filter(r -> r.getRegistrationFrom() != null && (registrationTimeStart == null || r.getRegistrationFrom().isAfter(registrationTimeStart)) && (registrationTimeEnd == null || r.getRegistrationFrom().isBefore(registrationTimeEnd))).collect(toList());
        return filtered;
    }
}
