package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.FieldDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ParentDataRecord;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
import dk.magenta.datafordeler.statistik.utils.Filter;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;

public class PersonStatisticsQuery extends PersonRecordQuery {

    private OffsetDateTime effectAt = null;
    private OffsetDateTime registrationAt = null;

    public PersonStatisticsQuery(HttpServletRequest request) {
        this.setRegistrationTimeBefore(Query.parseDateTime(request.getParameter(StatisticsService.REGISTRATION_BEFORE)));
        this.setRegistrationTimeAfter(Query.parseDateTime(request.getParameter(StatisticsService.REGISTRATION_AFTER)));
        this.setEffectTimeBefore(Query.parseDateTime(request.getParameter(StatisticsService.BEFORE_DATE_PARAMETER)));
        this.setEffectTimeAfter(Query.parseDateTime(request.getParameter(StatisticsService.AFTER_DATE_PARAMETER)));
        this.setOriginTimeBefore(LocalDate.parse(request.getParameter(StatisticsService.ORIGIN_BEFORE)));
        this.setOriginTimeAfter(LocalDate.parse(request.getParameter(StatisticsService.ORIGIN_AFTER)));
        String pnr = request.getParameter("pnr");
        if (pnr != null) {
            this.setPersonnummer(pnr);
        }
        this.setPageSize(1000000);
    }


    public PersonStatisticsQuery(Filter filter) {
        this.setRegistrationTimeBefore(filter.registrationBefore);
        this.setRegistrationTimeAfter(filter.registrationAfter);
        this.setRegistrationAt(filter.registrationAt);
        this.setEffectTimeBefore(filter.before);
        this.setEffectTimeAfter(filter.after);
        this.setEffectAt(filter.effectAt);
        this.setOriginTimeBefore(filter.originBefore);
        this.setOriginTimeAfter(filter.originAfter);
        if (filter.onlyPnr != null) {
            for (String pnr : filter.onlyPnr) {
                this.addPersonnummer(pnr);
            }
        }
        this.setPageSize(1000000);
    }


    private LocalDate originTimeAfter = null;

    public LocalDate getOriginTimeAfter() {
        return this.originTimeAfter;
    }

    public void setOriginTimeAfter(LocalDate originTimeAfter) {
        this.originTimeAfter = originTimeAfter;
    }


    private LocalDate originTimeBefore = null;

    public LocalDate getOriginTimeBefore() {
        return this.originTimeBefore;
    }

    public void setOriginTimeBefore(LocalDate originTimeBefore) {
        this.originTimeBefore = originTimeBefore;
    }


    private OffsetDateTime registrationTimeAfter = null;

    public OffsetDateTime getRegistrationTimeAfter() {
        return this.registrationTimeAfter;
    }

    public void setRegistrationTimeAfter(OffsetDateTime registrationTimeAfter) {
        this.registrationTimeAfter = registrationTimeAfter;
    }


    private OffsetDateTime registrationTimeBefore = null;

    public OffsetDateTime getRegistrationTimeBefore() {
        return this.registrationTimeBefore;
    }

    public void setRegistrationTimeBefore(OffsetDateTime registrationTimeBefore) {
        this.registrationTimeBefore = registrationTimeBefore;
    }


    public void setEffectAt(LocalDateTime effectAt) {
        this.effectAt = effectAt.atZone(StatisticsService.cprDataOffset).toOffsetDateTime();
    }

    public void setEffectAt(OffsetDateTime effectAt) {
        this.effectAt = effectAt;
    }

    public OffsetDateTime getEffectAt() {
        return this.effectAt;
    }

    public void setRegistrationAt(LocalDateTime registrationAt) {
        this.registrationAt = registrationAt.atZone(StatisticsService.cprDataOffset).toOffsetDateTime();
    }

    public void setRegistrationAt(OffsetDateTime registrationAt) {
        this.registrationAt = registrationAt;
    }

    public OffsetDateTime getRegistrationAt() {
        return this.registrationAt;
    }

    private OffsetDateTime effectTimeAfter = null;

    public OffsetDateTime getEffectTimeAfter() {
        return this.effectTimeAfter;
    }

    public void setEffectTimeAfter(OffsetDateTime effectTimeAfter) {
        this.effectTimeAfter = effectTimeAfter;
    }


    private OffsetDateTime effectTimeBefore = null;

    public OffsetDateTime getEffectTimeBefore() {
        return this.effectTimeBefore;
    }

    public void setEffectTimeBefore(OffsetDateTime effectTimeBefore) {
        this.effectTimeBefore = effectTimeBefore;
    }

    public static String cutPath(String path) {
        int firstSepIndex = path.indexOf(LookupDefinition.separator);
        return (firstSepIndex == -1) ? path : path.substring(0, firstSepIndex);
    }

    protected Condition getOrigintimesCondition(MultiCondition parent, String handle) throws QueryBuildException {
        MultiCondition condition = new MultiCondition(parent, "AND");
        if (this.originTimeAfter != null) {
            condition.add(
                    this.makeCondition(
                            condition,
                            handle,
                            Condition.Operator.GTE,
                            Collections.singletonList(this.originTimeAfter.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                            LocalDate.class,
                            false
                    )
            );
        }
        if (this.originTimeBefore != null) {
            condition.add(
                    this.makeCondition(
                            condition,
                            handle,
                            Condition.Operator.LTE,
                            Collections.singletonList(this.originTimeBefore.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                            OffsetDateTime.class,
                            true
                    )
            );
        }
        return condition;
    }


    protected void applyOriginTimes(FieldDefinition fieldDefinition) {
        // Omtænk denne
        String path = cutPath(fieldDefinition.path);
        String originTimePath = path + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_ORIGIN_DATE;
        if (this.getOriginTimeAfter() != null) {
            fieldDefinition.and(
                    originTimePath,
                    this.getOriginTimeAfter(),
                    LocalDate.class,
                    LookupDefinition.Operator.GTE
            );
        }
        if (this.getOriginTimeBefore() != null) {
            FieldDefinition beforeDefinition = new FieldDefinition(
                    originTimePath,
                    this.getOriginTimeBefore(),
                    LocalDate.class,
                    LookupDefinition.Operator.LTE
            );
            if (this.getOriginTimeAfter() == null) {
                beforeDefinition.or(new FieldDefinition(
                        originTimePath,
                        null,
                        LocalDate.class
                ));
            }
            fieldDefinition.and(beforeDefinition);
        }
    }

    protected FieldDefinition applyOriginTimes(String basePath) {
        // Omtænk denne
        String originTimePath = basePath + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_ORIGIN_DATE;
        FieldDefinition fieldDefinition = null;
        if (this.getOriginTimeAfter() != null) {
            FieldDefinition afterDefinition = new FieldDefinition(
                    originTimePath,
                    this.getOriginTimeAfter(),
                    LocalDate.class,
                    LookupDefinition.Operator.GTE
            );
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, afterDefinition);
        }
        if (this.getOriginTimeBefore() != null) {
            FieldDefinition beforeDefinition = new FieldDefinition(
                    originTimePath,
                    this.getOriginTimeBefore(),
                    LocalDate.class,
                    LookupDefinition.Operator.LTE
            );
            if (this.getOriginTimeAfter() == null) {
                beforeDefinition.or(new FieldDefinition(
                        originTimePath,
                        null,
                        LocalDate.class
                ));
            }
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, beforeDefinition);
        }
        return fieldDefinition;
    }


    protected void applyRegistrationTimes(FieldDefinition fieldDefinition) {
        FieldDefinition registrationDefinition = this.applyRegistrationTimes(
                cutPath(fieldDefinition.path)
        );
        if (registrationDefinition != null) {
            fieldDefinition.and(
                    registrationDefinition
            );
        }
    }

    protected static HashMap<String, String> getBitemporalHandles(String handle, String path) {
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put(handle + "RegistrationFrom", path + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM);
        joinHandles.put(handle + "RegistrationTo", path + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO);
        joinHandles.put(handle + "EffectFrom", path + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_EFFECT_FROM);
        joinHandles.put(handle + "EffectTo", path + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_EFFECT_TO);
        joinHandles.put(handle + "Origin", path + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_ORIGIN_DATE);
        return joinHandles;
    }

    protected MultiCondition applyBitemporalConditions(Condition parentCondition, String handle) throws QueryBuildException {
        return this.applyBitemporalConditions(parentCondition.asMultiCondition(), handle);
    }
    protected MultiCondition applyBitemporalConditions(MultiCondition parentCondition, String handle) throws QueryBuildException {
        MultiCondition condition = new MultiCondition(parentCondition, "AND");
        parentCondition.add(condition);
        condition.add(this.getRegistrationtimesCondition(condition, handle + "RegistrationFrom", handle + "RegistrationTo"));
        condition.add(this.getEffecttimesCondition(condition, handle + "EffectFrom", handle + "EffectTo"));
        condition.add(this.getOrigintimesCondition(condition, handle + "Origin"));
        return condition;
    }

    protected Condition getRegistrationtimesCondition(MultiCondition parent, String fromHandle, String toHandle) throws QueryBuildException {
        return this.getTimeCondition(parent, fromHandle, toHandle, this.getRegistrationTimeAfter(), this.getRegistrationTimeBefore(), this.getRegistrationAt());
    }

    protected FieldDefinition applyRegistrationTimes(String basePath) {
        String registrationFromPath = basePath + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM;
        String registrationToPath = basePath + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO;
        FieldDefinition fieldDefinition = null;
        if (this.getRegistrationTimeAfter() != null) {
            FieldDefinition afterDefinition = new FieldDefinition(
                    registrationFromPath,
                    this.getRegistrationTimeAfter(),
                    OffsetDateTime.class,
                    LookupDefinition.Operator.GTE
            );
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, afterDefinition);
        }
        if (this.getRegistrationTimeBefore() != null) {
            FieldDefinition beforeDefinition = new FieldDefinition(
                    registrationFromPath,
                    this.getRegistrationTimeBefore(),
                    OffsetDateTime.class,
                    LookupDefinition.Operator.LTE
            );
            if (this.getRegistrationTimeAfter() == null) {
                beforeDefinition.or(new FieldDefinition(
                        registrationFromPath,
                        null,
                        OffsetDateTime.class,
                        LookupDefinition.Operator.EQ
                ));
            }
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, beforeDefinition);
        }


        if (this.registrationAt != null) {
            FieldDefinition atDefinition1 = new FieldDefinition(
                    registrationFromPath,
                    this.registrationAt,
                    OffsetDateTime.class,
                    LookupDefinition.Operator.LTE
            );
            atDefinition1.or(
                    registrationFromPath,
                    null,
                    OffsetDateTime.class
            );
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, atDefinition1);

            FieldDefinition atDefinition2 = new FieldDefinition(
                    registrationToPath,
                    this.registrationAt,
                    OffsetDateTime.class,
                    LookupDefinition.Operator.GTE
            );
            atDefinition2.or(
                    registrationToPath,
                    null,
                    OffsetDateTime.class
            );
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, atDefinition2);

        }


        return fieldDefinition;
    }



    protected Condition getEffecttimesCondition(MultiCondition parent, String fromHandle, String toHandle) throws QueryBuildException {
        return this.getTimeCondition(parent, fromHandle, toHandle, this.getEffectTimeAfter(), this.getEffectTimeBefore(), this.getEffectAt());
    }

    protected void applyEffectTimes(FieldDefinition fieldDefinition) {
        FieldDefinition effectDefinition = this.applyEffectTimes(
                cutPath(fieldDefinition.path)
        );
        if (effectDefinition != null) {
            fieldDefinition.and(
                    effectDefinition
            );
        }
    }

    protected FieldDefinition applyEffectTimes(String basePath) {
        FieldDefinition fieldDefinition = null;
        String effectFromPath = basePath + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_EFFECT_FROM;
        String effectToPath = basePath + LookupDefinition.separator + CprBitemporalRecord.DB_FIELD_EFFECT_TO;
        if (this.getEffectTimeAfter() != null) {
            FieldDefinition afterDefinition = new FieldDefinition(
                    effectFromPath,
                    this.getEffectTimeAfter(),
                    OffsetDateTime.class,
                    LookupDefinition.Operator.GTE
            );
            fieldDefinition = afterDefinition;
        }
        if (this.getEffectTimeBefore() != null) {
            FieldDefinition beforeDefinition = new FieldDefinition(
                    effectFromPath,
                    this.getEffectTimeBefore(),
                    OffsetDateTime.class,
                    LookupDefinition.Operator.LTE
            );
            if (this.getEffectTimeAfter() == null) {
                beforeDefinition.or(new FieldDefinition(
                        effectFromPath,
                        null,
                        OffsetDateTime.class
                ));
            }
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, beforeDefinition);
        }

        if (this.getEffectAt() != null) {
            FieldDefinition atDefinition1 = new FieldDefinition(
                    effectFromPath,
                    this.getEffectAt(),
                    OffsetDateTime.class,
                    LookupDefinition.Operator.LTE
            );
            atDefinition1.or(new FieldDefinition(
                    effectFromPath,
                    null,
                    OffsetDateTime.class
            ));
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, atDefinition1);

            FieldDefinition atDefinition2 = new FieldDefinition(
                    effectToPath,
                    this.getEffectAt(),
                    OffsetDateTime.class,
                    LookupDefinition.Operator.GTE
            );
            atDefinition2.or(
                    effectToPath,
                    null,
                    OffsetDateTime.class
            );
            fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, atDefinition2);
        }

        return fieldDefinition;
    }

    public static FieldDefinition and(FieldDefinition left, FieldDefinition right) {
        if (right == null) return left;
        if (left == null) return right;
        left.and(right);
        return left;
    }

    protected FieldDefinition fromPath(String basePath) {
        FieldDefinition fieldDefinition = null;
        fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, this.applyOriginTimes(basePath));
        fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, this.applyRegistrationTimes(basePath));
        fieldDefinition = PersonStatisticsQuery.and(fieldDefinition, this.applyEffectTimes(basePath));
        return fieldDefinition;
    }





    private Condition getTimeCondition(MultiCondition parent, String fromHandle, String toHandle, OffsetDateTime afterTime, OffsetDateTime beforeTime, OffsetDateTime atTime) throws QueryBuildException {
        MultiCondition condition = new MultiCondition(parent, "AND");
        if (afterTime != null) {
            condition.add(
                    this.makeCondition(
                            condition,
                            fromHandle,
                            Condition.Operator.GTE,
                            Collections.singletonList(afterTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                            OffsetDateTime.class,
                            false
                    )
            );
        }
        if (beforeTime != null) {
            condition.add(
                    this.makeCondition(
                            condition,
                            fromHandle,
                            Condition.Operator.LTE,
                            Collections.singletonList(beforeTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                            OffsetDateTime.class,
                            true
                    )
            );
        }
        if (atTime != null) {
            condition.add(
                    this.makeCondition(
                            condition,
                            fromHandle,
                            Condition.Operator.LTE,
                            Collections.singletonList(atTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                            OffsetDateTime.class,
                            true
                    )
            );
            condition.add(
                    this.makeCondition(
                            condition,
                            toHandle,
                            Condition.Operator.GTE,
                            Collections.singletonList(atTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                            OffsetDateTime.class,
                            true
                    )
            );
        }
        return condition;
    }

}
