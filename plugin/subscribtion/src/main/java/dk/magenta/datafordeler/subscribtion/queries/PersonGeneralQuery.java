package dk.magenta.datafordeler.subscribtion.queries;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.DataEventSubscribtion;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;

public class PersonGeneralQuery extends PersonRecordQuery {


    public static final String BEFORE_DATE_PARAMETER = "beforeDate";
    public static final String AFTER_DATE_PARAMETER = "afterDate";
    public static final String EFFECT_DATE_PARAMETER = "effectDate";
    public static final String ONLY_PNR = "pnr";

    public static final String REGISTRATION_AFTER = "registrationAfter";
    public static final String REGISTRATION_BEFORE = "registrationBefore";
    public static final String REGISTRATION_AT = "registrationAt";

    public static final String ORIGIN_AFTER = "originAfter";
    public static final String ORIGIN_BEFORE = "originBefore";




    public OffsetDateTime effectAt;
    public OffsetDateTime after;
    public OffsetDateTime before;
    public OffsetDateTime registrationAfter;
    public OffsetDateTime registrationBefore;
    public OffsetDateTime registrationAt;
    public LocalDate originAfter;
    public LocalDate originBefore;



    public PersonGeneralQuery(HttpServletRequest request) {
        this.setRegistrationTimeBefore(Query.parseDateTime(request.getParameter(REGISTRATION_BEFORE)));
        this.setRegistrationTimeAfter(Query.parseDateTime(request.getParameter(REGISTRATION_AFTER)));
        this.setEffectTimeBefore(Query.parseDateTime(request.getParameter(BEFORE_DATE_PARAMETER)));
        this.setEffectTimeAfter(Query.parseDateTime(request.getParameter(AFTER_DATE_PARAMETER)));
        /*this.setOriginTimeBefore(LocalDate.parse(request.getParameter(ORIGIN_BEFORE)));
        this.setOriginTimeAfter(LocalDate.parse(request.getParameter(ORIGIN_AFTER)));*/
        String pnr = request.getParameter("pnr");
        if (pnr != null) {
            this.setPersonnummer(pnr);
        }
        this.setPageSize(1000000);
    }


    public PersonGeneralQuery() {
        this.setRegistrationTimeBefore(this.registrationBefore);
        this.setRegistrationTimeAfter(this.registrationAfter);
        this.setRegistrationAt(this.registrationAt);
        this.setEffectTimeBefore(this.before);
        this.setEffectTimeAfter(this.after);
        this.setEffectAt(this.effectAt);
        this.setOriginTimeBefore(this.originBefore);
        this.setOriginTimeAfter(this.originAfter);
        /*if (this.onlyPnr != null) {
            for (String pnr : this.onlyPnr) {
                this.addPersonnummer(pnr);
            }
        }*/
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


    public void setEffectAt(OffsetDateTime effectAt) {
        this.effectAt = effectAt;
    }

    public OffsetDateTime getEffectAt() {
        return this.effectAt;
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

    protected Condition getEffecttimesCondition(MultiCondition parent, String fromHandle, String toHandle) throws QueryBuildException {
        return this.getTimeCondition(parent, fromHandle, toHandle, this.getEffectTimeAfter(), this.getEffectTimeBefore(), this.getEffectAt());
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


    public static PersonRecordQuery getPersonQuery(String queryType, OffsetDateTime timestamp) {
        switch(queryType) {
            case "person.address":
                PersonAddressChangeQuery addressQuery = new PersonAddressChangeQuery();
                addressQuery.setRegistrationTimeAfter(timestamp);//TODO: consider joining this on DB-level
                return addressQuery;
            case "person.civil":
                PersonCivilStatusQuery civilstatusQuery = new PersonCivilStatusQuery();
                civilstatusQuery.setRegistrationTimeAfter(timestamp);//TODO: consider joining this on DB-level
                return civilstatusQuery;
            case "person.name":
                PersonDeathQuery deathQuery = new PersonDeathQuery();
                deathQuery.setRegistrationTimeAfter(timestamp);//TODO: consider joining this on DB-level
                return deathQuery;
            default:
                PersonRecordQuery personQuery = new PersonRecordQuery();
                personQuery.setRecordAfter(timestamp);
                return personQuery;
        }
    }


}
