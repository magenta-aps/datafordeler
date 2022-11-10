package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.data.PersonStatusDataRecord;
import dk.magenta.datafordeler.statistik.utils.Filter;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class PersonDeathQuery extends PersonStatisticsQuery {

    public PersonDeathQuery(HttpServletRequest request) {
        super(request);
    }

    public PersonDeathQuery(Filter filter) {
        super(filter);
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("status", PersonEntity.DB_FIELD_STATUS + BaseQuery.separator + PersonStatusDataRecord.DB_FIELD_STATUS);
        joinHandles.putAll(getBitemporalHandles("status", PersonEntity.DB_FIELD_STATUS));
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
