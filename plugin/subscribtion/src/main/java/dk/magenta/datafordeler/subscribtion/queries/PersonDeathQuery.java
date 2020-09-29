package dk.magenta.datafordeler.subscribtion.queries;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class PersonDeathQuery extends PersonGeneralQuery {

    public PersonDeathQuery() {
        super();
    }

    public PersonDeathQuery(HttpServletRequest request) {
        super(request);
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
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
