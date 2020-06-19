package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.FieldDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.PersonStatusDataRecord;
import dk.magenta.datafordeler.statistik.utils.Filter;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PersonDeathQuery extends PersonStatisticsQuery {

    public PersonDeathQuery(HttpServletRequest request) {
        super(request);
    }

    public PersonDeathQuery(Filter filter) {
        super(filter);
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();

        FieldDefinition fieldDefinition = new FieldDefinition(
                PersonEntity.DB_FIELD_STATUS + LookupDefinition.separator + PersonStatusDataRecord.DB_FIELD_STATUS,
                90,
                Integer.class
        );

        this.applyOriginTimes(fieldDefinition);
        this.applyRegistrationTimes(fieldDefinition);
        this.applyEffectTimes(fieldDefinition);

        lookupDefinition.put(fieldDefinition);

        return lookupDefinition;
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.putAll(getBitemporalHandles("status", PersonEntity.DB_FIELD_STATUS + LookupDefinition.separator + PersonStatusDataRecord.DB_FIELD_STATUS));
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonDeathQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        SingleCondition statusCondition = this.addCondition("status", Condition.Operator.EQ, 90, Integer.class);
        this.applyBitemporalConditions(statusCondition, "status");
    }

}
