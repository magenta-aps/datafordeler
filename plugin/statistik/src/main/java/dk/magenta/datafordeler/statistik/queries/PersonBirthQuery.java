package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.FieldDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ParentDataRecord;
import dk.magenta.datafordeler.statistik.utils.Filter;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class PersonBirthQuery extends PersonStatisticsQuery {

    public PersonBirthQuery(HttpServletRequest request) {
        super(request);
    }

    public PersonBirthQuery(Filter filter) {
        super(filter);
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        lookupDefinition.setMatchNulls(true);
        FieldDefinition fieldDefinition;
        if (this.getEffectTimeAfter() == null) {
            fieldDefinition = this.fromPath(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_BIRTHTIME);
        } else {
            fieldDefinition = new FieldDefinition(
                    PersonEntity.DB_FIELD_BIRTHTIME + LookupDefinition.separator + BirthTimeDataRecord.DB_FIELD_BIRTH_DATETIME,
                    this.getEffectTimeAfter().toLocalDateTime(),
                    LocalDateTime.class,
                    LookupDefinition.Operator.GTE
            );
            this.applyOriginTimes(fieldDefinition);
            this.applyRegistrationTimes(fieldDefinition);
            this.applyEffectTimes(fieldDefinition);
        }
        lookupDefinition.put(fieldDefinition);
        return lookupDefinition;
    }



    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("birth", PersonEntity.DB_FIELD_BIRTHTIME + LookupDefinition.separator + BirthTimeDataRecord.DB_FIELD_BIRTH_DATETIME);
        joinHandles.putAll(getBitemporalHandles("birth", PersonEntity.DB_FIELD_BIRTHTIME));
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonBirthQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        Condition condition = (this.getEffectTimeAfter() == null) ? this.getCondition() :  this.addCondition("birth", Condition.Operator.GTE, this.getEffectTimeAfter().toLocalDateTime(), LocalDateTime.class);
        this.applyBitemporalConditions(condition, "birth");
    }

}
