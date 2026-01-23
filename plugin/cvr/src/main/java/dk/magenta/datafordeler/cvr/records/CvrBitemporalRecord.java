package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.migration.MigrateModel;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.util.ListHashMap;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;

@MappedSuperclass
public abstract class CvrBitemporalRecord extends CvrNontemporalRecord implements Comparable<CvrBitemporalRecord>, MigrateModel {

    public static final String FILTERLOGIC_REGISTRATIONFROM_AFTER = "(" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED + " >= :" + Monotemporal.FILTERPARAM_REGISTRATIONFROM_AFTER + ")";
    public static final String FILTERLOGIC_REGISTRATIONFROM_BEFORE = "(" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED + " < :" + Monotemporal.FILTERPARAM_REGISTRATIONFROM_BEFORE + " OR " + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED + " is null)";
    public static final String FILTERLOGIC_REGISTRATIONTO_AFTER = "";
    public static final String FILTERLOGIC_REGISTRATIONTO_BEFORE = "";


    public static final String FILTERLOGIC_EFFECTFROM_AFTER = "(" + CvrRecordPeriod.DB_FIELD_VALID_FROM + " >= :" + Bitemporal.FILTERPARAM_EFFECTFROM_AFTER + ")";
    public static final String FILTERLOGIC_EFFECTFROM_BEFORE = "(" + CvrRecordPeriod.DB_FIELD_VALID_FROM + " < :" + Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE + " OR " + CvrRecordPeriod.DB_FIELD_VALID_FROM + " is null)";
    public static final String FILTERLOGIC_EFFECTTO_AFTER = "(" + CvrRecordPeriod.DB_FIELD_VALID_TO + " >= :" + Bitemporal.FILTERPARAM_EFFECTTO_AFTER + " OR " + CvrRecordPeriod.DB_FIELD_VALID_TO + " is null)";
    public static final String FILTERLOGIC_EFFECTTO_BEFORE = "(" + CvrRecordPeriod.DB_FIELD_VALID_TO + " < :" + Bitemporal.FILTERPARAM_EFFECTTO_BEFORE + ")";


    public static final String FILTERPARAMTYPE_REGISTRATIONFROM = "java.time.OffsetDateTime";
    public static final String FILTERPARAMTYPE_REGISTRATIONTO = "java.time.OffsetDateTime";

    public static final String FILTERPARAMTYPE_EFFECTFROM = "java.time.OffsetDateTime";
    public static final String FILTERPARAMTYPE_EFFECTTO = "java.time.OffsetDateTime";

    public static final String FILTERPARAMTYPE_LASTUPDATED = "java.time.OffsetDateTime";

    /**
     * We do not want to use any timestamps of type LocalDate
     *
     * @param input
     * @param filter
     * @return
     */
    @Deprecated
    public static Object castFilterParam(Object input, String filter) {
        switch (filter) {
            case Bitemporal.FILTER_EFFECTFROM_AFTER:
            case Bitemporal.FILTER_EFFECTFROM_BEFORE:
            case Bitemporal.FILTER_EFFECTTO_AFTER:
            case Bitemporal.FILTER_EFFECTTO_BEFORE:
                return ((OffsetDateTime) input).toLocalDate();
        }
        return null;
    }


    public static final String DB_FIELD_LAST_UPDATED = "lastUpdated";
    public static final String IO_FIELD_LAST_UPDATED = "sidstOpdateret";

    @Column(name = DB_FIELD_LAST_UPDATED, columnDefinition = "datetime2")
    private OffsetDateTime lastUpdated;

    @JsonIgnore
    @Column(name = DB_FIELD_LAST_UPDATED+"_new")
    protected OffsetDateTime lastUpdatedNew;

    @JsonProperty(value = IO_FIELD_LAST_UPDATED)
    public OffsetDateTime getLastUpdated() {
        return this.lastUpdatedNew;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        // TODO: Vi ændrer timezone her fordi felttypen datetime2 gemmer en naiv datetime i databasen,
        // og udlæser dem med UTC som tidszone.
        // Nyligt parsede datoer har tidszone Grønland. Når der udlæses fra databasen og sammenlignes,
        // er der 1-2 timers forskel pga. tidszone (den naive del er ens, men den parsede har f.eks. +0100)
        // Det betyder at sammenligningen fejler.
        // Derfor kompenserer vi ved at smide UTC på tidspunktet, uden at ændre den naive del, så sammenligningen
        // igen fungerer.
        // Hvis vi går væk fra datetime2 skal vi ændre tilbage til at der bare står
        // this.lastUpdated = lastUpdated
        this.lastUpdated = fixOffsetIn(lastUpdated);
        this.lastUpdatedNew = lastUpdated;
    }


    public static final String DB_FIELD_LAST_LOADED = "lastLoaded";
    public static final String IO_FIELD_LAST_LOADED = "sidstIndlaest";

    @Column(name = DB_FIELD_LAST_LOADED, columnDefinition = "datetime2")
    @JsonProperty(value = IO_FIELD_LAST_LOADED)
    private OffsetDateTime lastLoaded;

    @JsonIgnore
    @Column(name = DB_FIELD_LAST_LOADED+"_new")
    private OffsetDateTime lastLoadedNew;

    @JsonIgnore
    public OffsetDateTime getLastLoaded() {
        return this.lastLoadedNew;
    }

    public void setLastLoaded(OffsetDateTime lastLoaded) {
        // Samme som setLastUpdated
        this.lastLoaded = fixOffsetIn(lastLoaded);
        this.lastLoadedNew = lastLoaded;
    }

    @JsonIgnore
    public OffsetDateTime getRegistrationFrom() {
        return (this.lastUpdatedNew != null) ? this.lastUpdatedNew : this.lastLoadedNew;
    }

    public void setRegistrationFrom(OffsetDateTime offsetDateTime) {
        this.setLastUpdated(offsetDateTime);
    }


    public static final String IO_FIELD_PERIOD = "periode";

    @Embedded
    @JsonProperty(value = IO_FIELD_PERIOD)
    private CvrRecordPeriod validity;

    public CvrRecordPeriod getValidity() {
        return this.validity;
    }

    public void setValidity(CvrRecordPeriod validity) {
        this.validity = validity;
    }


    @JsonIgnore
    public LocalDate getValidFrom() {
        if (this.validity != null) {
            return this.validity.getValidFrom();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public LocalDate getValidTo() {
        if (this.validity != null) {
            return this.validity.getValidTo();
        } else {
            return null;
        }
    }


    /*public void merge(CvrBitemporalRecord other) {
        if (other != null && other.getClass() == this.getClass()) {

        }
    }*/

    /**
     * For sorting purposes; we implement the Comparable interface, so we should
     * provide a comparison method. Here, we sort CvrRecord objects by registrationFrom, with nulls first
     */
    @Override
    public int compareTo(CvrBitemporalRecord o) {
        OffsetDateTime oUpdated = o == null ? null : o.getRegistrationFrom();
        if (this.getRegistrationFrom() == null && oUpdated == null) return 0;
        if (this.getRegistrationFrom() == null) return -1;
        return this.getRegistrationFrom().compareTo(oUpdated);
    }

    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    @Column(columnDefinition = "datetime2")
    private OffsetDateTime registrationTo;

    @JsonIgnore
    @Column(name="registrationTo"+"_new")
    private OffsetDateTime registrationToNew;


    @JsonIgnore
    public OffsetDateTime getRegistrationTo() {
        return this.registrationToNew;
    }

    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = fixOffsetIn(registrationTo);
        this.registrationToNew = registrationTo;
    }


    public boolean hasRegistrationAt(OffsetDateTime time) {
        return (this.getRegistrationFrom() == null || this.getRegistrationFrom().isBefore(time)) && (this.getRegistrationTo() == null || this.getRegistrationTo().isAfter(time));
    }

    /**
     * Given a Collection of CvrRecord objects, group them into buckets that share
     * bitemporality. That way, we can treat all records in a bucket the same way,
     * thus we won’t have to look up the appropriate Registration/Effect more than once
     */
    public static <T extends CvrBitemporalRecord> ListHashMap<String, T> sortIntoGroups(Collection<T> records) {
        // Sort the records into groups that share bitemporality
        ListHashMap<String, T> recordGroups = new ListHashMap<>();
        for (T record : records) {
            // Find the appropriate registration object
            OffsetDateTime registrationFrom = record.getRegistrationFrom();
            OffsetDateTime registrationTo = record.getRegistrationTo();
            LocalDate effectFrom = record.getValidFrom();
            LocalDate effectTo = record.getValidTo();
            String groupKey = registrationFrom + "|" + registrationTo + "|" + effectFrom + "|" + effectTo;
            recordGroups.add(groupKey, record);
        }
        return recordGroups;
    }

    public static <T extends CvrBitemporalRecord> Pair<Collection<T>, Collection<T>> closeRegistrations(Collection<T> records) {
        boolean output = false;
        int unclosedCount = 0;
        ArrayList<T> updated = new ArrayList<>();
        ArrayList<T> toDelete = new ArrayList<>();
        for (T record : records) {
            if (record.getRegistrationTo() == null && record.getEffectTo() == null) {
                unclosedCount++;
            }
        }
        Comparator<T> comparator = Comparator.comparing(T::getRegistrationFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                                            .thenComparing(T::getEffectFrom, Comparator.nullsFirst(Comparator.naturalOrder()));
        ArrayList<T> recordList = new ArrayList<>(records);

        recordList.sort(comparator);
        if (output) {
            if (recordList.size() > 1) {
                System.out.println("-------------------------------------------------------------------------");
                System.out.println(records.iterator().next().getClass().getSimpleName() + " has " + records.size() + " records, of which " + unclosedCount + " are unclosed");
                System.out.println("    --------");
                for (T record : recordList) {
                    System.out.println("    " + String.format("%8s", record.getId().toString()) + "    " + String.format("%30s", record.debug_name()) + "    " + record.getBitemporality() + (record.getRegistrationTo() == null && record.getEffectTo() == null ? " (unclosed)" : ""));
                }
                System.out.println("    --------");
            }
        }

        for (T current : recordList) {
            if (!toDelete.contains(current)) {
                if (
                        (current.getRegistrationFrom() != null && current.getRegistrationTo() != null) &&
                        (
                                Equality.equal(current.getRegistrationFrom(), current.getRegistrationTo()) ||
                                Equality.equal(current.getRegistrationFrom().plusHours(2), current.getRegistrationTo()) ||
                                Equality.equal(current.getRegistrationFrom().plusHours(3), current.getRegistrationTo())
                        )
                ) {
                    if (output) {
                        System.out.println("    " + current.getId() + " has no registration time range, should delete");
                    }
                    toDelete.add(current);
                }
            }
        }
        recordList.removeAll(toDelete);

/*
        for (T current : recordList) {
            if (!toDelete.contains(current)) {
                List<T> trailing = recordList.stream()
                        .filter(record -> !toDelete.contains(record))
                        .filter(record -> !Objects.equals(record.getId(), current.getId()))
                        .filter(record -> record.equalData(current))
                        .filter(record -> record.getBitemporality().equals(current.getBitemporality()))
                        .toList();
                if (!trailing.isEmpty()) {
                    for (T trailingRecord : trailing) {
                        if (output) {
                            System.out.println("    " + trailingRecord.getId() + " is trailing " + current.getId() + ", should delete " + trailingRecord.getId());
                        }
                        toDelete.add(trailingRecord);
                    }
                }
            }
        }
        recordList.removeAll(toDelete);
*/
/*
        for (T current : recordList) {
            if (!toDelete.contains(current)) {
                List<T> trailing = recordList.stream()
                        .filter(record -> !toDelete.contains(record))
                        .filter(record -> !Objects.equals(record, current))
                        .filter(record -> record.equalData(current))
                        .filter(record -> record.getBitemporality().equalEffect(current.getBitemporality()))
                        .filter(record -> record.getBitemporality().equals(current.getBitemporality(), Bitemporality.COMPARE_REGISTRATION_TO))
                        .filter(record ->
                                Equality.equal(current.getRegistrationFrom(), record.getRegistrationFrom().minusHours(2)) ||
                                Equality.equal(current.getRegistrationFrom(), record.getRegistrationFrom().minusHours(3))
                        )
                        .toList();
                if (!trailing.isEmpty()) {
                    for (T trailingRecord : trailing) {
                        if (output) {
                            System.out.println("    " + trailingRecord.getId() + " is trailing " + current.getId() + ", should delete " + current.getId());
                        }
                        toDelete.add(current);
                    }
                }
            }
        }
*/
        if (unclosedCount > 1) {
            for (T current : recordList) {
                if (current.getRegistrationTo() == null && current.getEffectTo() == null) {
                    // For every group there can only ever be one that has both registrationTo=null and effectTo=null
                    // Find records that have open bitemporality,
                    // and find other records that are registered and effected after them
                    Stream<T> candidates = recordList.stream().filter(c -> c != current);
                    if (current.getRegistrationFrom() != null) {
                        candidates = candidates
                                .filter(record -> record.getRegistrationFrom() != null)
                                .filter(record -> record.getRegistrationFrom().isAfter(
                                        current.getRegistrationFrom()
                                ));
                    }
                    if (current.getEffectFrom() != null) {
                        candidates = candidates
                                .filter(record -> record.getEffectFrom() != null)
                                .filter(record -> record.getEffectFrom().isAfter(
                                        current.getEffectFrom()
                                ));
                    }
                    List<T> cList = candidates.toList();
                    T next = cList.stream().min(comparator).orElse(null);
                    if (next != null) {
                        if (output) {
                            System.out.println("    " + current.getId() + " is unclosed, and there are " + cList.size() + " records with registration and effect after it");
                        }
                        OffsetDateTime registrationCut = next.getRegistrationFrom();
                        try {
                            T clone = (T) current.clone();
                            OffsetDateTime effectCut = next.getEffectFrom();
                            // Do we have a matching record and a clone already?

                            // Any records matching the first bit (cutoff registration, same effect), basically what current would become?
                            T matching1 = recordList.stream()
                                    .filter(record -> Equality.equal(record.getRegistrationFrom(), current.getRegistrationFrom()))
                                    .filter(record -> Equality.equal(record.getRegistrationTo(), registrationCut))
                                    .filter(record -> record.getBitemporality().equalEffect(current.getBitemporality()))
                                    .findFirst().orElse(null);


                            if (effectCut != null) {
                                clone.setEffectTo(effectCut.minus(1, ChronoUnit.MICROS));
                            }
                            clone.setRegistrationFrom(registrationCut);


                            // Any records matching the second bit, what the clone would become?
                            T matching2 = recordList.stream().filter(record -> record.getBitemporality().equals(clone.getBitemporality())).findFirst().orElse(null);

                            if (matching1 != null && matching2 != null) {
                                if (output) {
                                    System.out.println("    Should delete " + current.getId() + ", it is represented in " + matching1.getId() + " and " + matching2.getId());
                                }
                                toDelete.add(current);
                            } else {
                                if (output) {
                                    System.out.println("    Splitting " + current.getId() + " at registration " + registrationCut + " and effect " + effectCut + " to align with " + next.getId());
                                }
                                /*updated.add(clone);
                                records.add(clone);
                                current.setRegistrationTo(registrationCut);
                                updated.add(current);*/
                            }

                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
/*
                        if (current.getRegistrationFrom() != null) {
                            candidates = recordList.stream().filter(c -> c != current);
                            candidates = candidates
                                    .filter(record -> record.getRegistrationFrom() != null)
                                    .filter(record -> record.getRegistrationFrom().isAfter(current.getRegistrationFrom()));
                            next = candidates.min(comparator).orElse(null);
                            if (next != null) {
                                current.setRegistrationTo(next.getRegistrationFrom());
                            }
                        }
*/
                    }
                }
            }
        }
/*
        ListHashMap<CvrRecordPeriod, T> effectGroups = new ListHashMap<>();
        for (T record : records) {
            effectGroups.add(record.getValidity(), record);
        }
        for (CvrRecordPeriod period : effectGroups.keySet()) {
            ArrayList<T> effectGroup = effectGroups.get(period);
            if (effectGroup.size() > 1) {
                effectGroup.sort(
                        Comparator.comparing(T::getRegistrationFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(T::getDafoUpdated, Comparator.nullsFirst(Comparator.naturalOrder()))
                );
                T previous = null;
                for (T record : effectGroup) {
                    if (previous != null && !Equality.equal(record.getRegistrationFrom(), previous.getRegistrationFrom()) && !Equality.equal(record.getRegistrationFrom(), previous.getRegistrationTo())) {
                        if (output) {
                            System.out.println("Closing " + previous.getClass().getSimpleName() + " " + previous.getId() + ", changing registrationTo from " + previous.getRegistrationTo() + " to " + record.getRegistrationFrom() + " to match " + record.getId());
                        }
                        previous.setRegistrationTo(record.getRegistrationFrom());
                        updated.add(previous);
                    }
                    previous = record;
                }
            }
        }
*/
        /*
        ArrayList<T> registrationOrdered = new ArrayList<>(records);
        if (!records.isEmpty()) {
            registrationOrdered.sort(
                    Comparator.comparing(T::getRegistrationFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(T::getRegistrationTo, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(T::getEffectFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(T::getEffectTo, Comparator.nullsLast(Comparator.naturalOrder()))
            );
            T previous = null;

            for (T current : registrationOrdered) {
                if (previous != null) {
                    if (previous.getRegistrationTo() == null && previous.getEffectTo() == null && current.getEffectTo() != null) {
                        // A value has ended; what was once open (without effectTo) is now closed in another registration
                        // Update the previous registration to end when this one begins
                        previous.setRegistrationTo(current.getRegistrationFrom());
                    }

//                if (current.getBitemporality().contains(previous.getBitemporality()) && previous.equals(current)) {
//                Should remove previous; it is wholly contained in current and has the same value
//                }
//                if (previous.equals(current) && previous.getBitemporality().containsEffect(current.getBitemporality()) && previous.getBitemporality().overlaps(current.getBitemporality())) {
//                    De to har samme værdi og ligger bitemporalt sammen. De skal sættes sammen til én
//                    Se f.eks. Magenta's branchedata
//                    current.setRegistrationFrom(previous.getRegistrationFrom());
//                    current.setEffectFrom(Bitemporality.min(previous.getEffectFrom(), current.getEffectFrom(), true));
//                    current.setEffectTo(Bitemporality.max(previous.getEffectTo(), current.getEffectTo(), true));
//                    current.setRegistrationFrom(Bitemporality.min(previous.getRegistrationFrom(), current.getRegistrationFrom(), true));
//                    current.setRegistrationTo(Bitemporality.max(previous.getRegistrationTo(), current.getRegistrationTo(), true));
//                    // remove previous
//                }

                }
                previous = current;
            }
            registrationOrdered.sort(
                    Comparator.comparing(T::getRegistrationFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(T::getRegistrationTo, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(T::getEffectFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(T::getEffectTo, Comparator.nullsLast(Comparator.naturalOrder()))
            );

        }*/

        return Pair.of(updated, toDelete);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CvrBitemporalRecord that = (CvrBitemporalRecord) o;
        return Equality.equal(lastUpdated, that.lastUpdated) &&
                Equality.equal(lastLoaded, that.lastLoaded) &&
                Objects.equals(this.getValidFrom(), that.getValidFrom()) &&
                Objects.equals(this.getValidTo(), that.getValidTo()) &&
                Equality.equal(registrationTo, that.registrationTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                lastUpdated != null ? lastUpdated.toEpochSecond() : null,
                lastLoaded != null ? lastLoaded.toEpochSecond() : null,
                this.getValidFrom(),
                this.getValidTo(),
                registrationTo != null ? registrationTo.toEpochSecond() : null
        );
    }

    @JsonIgnore
    public OffsetDateTime getEffectFrom() {
        if (this.validity != null) {
            return Bitemporality.convertTime(this.validity.getValidFrom());
        }
        return null;
    }

    public void setEffectFrom(OffsetDateTime offsetDateTime) {
        if (this.validity == null) {
            this.validity = new CvrRecordPeriod();
        }
        this.validity.setValidFrom(convertTime(offsetDateTime));
    }

    @JsonIgnore
    public OffsetDateTime getEffectTo() {
        if (this.validity != null) {
            return Bitemporality.convertTime(this.validity.getValidTo());
        }
        return null;
    }

    public void setEffectTo(OffsetDateTime offsetDateTime) {
        if (this.validity == null) {
            this.validity = new CvrRecordPeriod();
        }
        this.validity.setValidTo(convertTime(offsetDateTime));
    }

    public boolean hasEffectAt(OffsetDateTime time) {
        return this.validity == null || this.validity.isValidAt(convertTime(time));
    }

    @JsonIgnore
    public Bitemporality getBitemporality() {
        return new Bitemporality(this.getRegistrationFrom(), this.getRegistrationTo(), this.getValidFrom(), this.getValidTo());
    }

    public static LocalDate convertTime(OffsetDateTime time) {
        return time != null ? time.atZoneSameInstant(ZoneOffset.UTC).toLocalDate() : null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        CvrBitemporalRecord clone = (CvrBitemporalRecord) super.clone();
        clone.validity = new CvrRecordPeriod();
        clone.setRegistrationFrom(this.getRegistrationFrom());
        clone.setRegistrationTo(this.getRegistrationTo());
        clone.setEffectFrom(this.getEffectFrom());
        clone.setEffectTo(this.getEffectTo());
        //clone.clonedFrom = this.getId();
        return clone;
    }

    public void updateTimestamp() {
        super.updateTimestamp();
        this.lastUpdatedNew = Bitemporal.fixOffsetOut(this.lastUpdated);
        this.lastLoadedNew = Bitemporal.fixOffsetOut(this.lastLoaded);
        this.registrationToNew = Bitemporal.fixOffsetOut(this.registrationTo);
    }

    public static List<String> updateFields() {
        return Arrays.asList(DB_FIELD_LAST_LOADED, DB_FIELD_LAST_UPDATED, "registrationTo");
    }
}
