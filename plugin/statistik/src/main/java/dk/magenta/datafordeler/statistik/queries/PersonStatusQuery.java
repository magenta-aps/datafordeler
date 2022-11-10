package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.statistik.utils.Filter;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class PersonStatusQuery extends PersonStatisticsQuery {

    public PersonStatusQuery(HttpServletRequest request) {
        super(request);
    }

    public PersonStatusQuery(Filter filter) {
        super(filter);
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("municipalitycode", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE);
        joinHandles.putAll(getBitemporalHandles("municipalitycode", PersonEntity.DB_FIELD_ADDRESS));
        joinHandles.put("undone", PersonEntity.DB_FIELD_ADDRESS + BaseQuery.separator + AddressDataRecord.DB_FIELD_UNDONE);
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonStatusQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        SingleCondition statusCondition = this.addCondition("municipalitycode", Condition.Operator.GTE, 900, Integer.class);
        this.applyBitemporalConditions(statusCondition, "municipalitycode");
        SingleCondition undoneCondition = this.addCondition("undone", Condition.Operator.EQ, false, Boolean.class);
    }

}
