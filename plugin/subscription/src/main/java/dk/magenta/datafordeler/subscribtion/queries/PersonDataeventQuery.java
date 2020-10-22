package dk.magenta.datafordeler.subscribtion.queries;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PersonDataeventQuery extends PersonRecordQuery {
    //TODO: REFACTOR THIS SHIT


    @QueryField(
            queryNames = {"timestamp", "timestamp"},
            type = QueryField.FieldType.STRING
    )
    protected OffsetDateTime timestamp = OffsetDateTime.now();



    public PersonDataeventQuery() {
        super();
    }


    private static HashMap<String, String> joinHandles = new HashMap<>();







    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonDataeventQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        MultiCondition condition = new MultiCondition(this.getCondition());

        condition.add(this.addCondition("dataeventfield", Condition.Operator.EQ, "Collections.singletonList(this.timestamp)", String.class, false));
        condition.add(this.addCondition("dafoUpdated", Condition.Operator.GT, Collections.singletonList(this.timestamp.plusDays(10)), OffsetDateTime.class, false));
        this.addCondition(condition);
    }

}
