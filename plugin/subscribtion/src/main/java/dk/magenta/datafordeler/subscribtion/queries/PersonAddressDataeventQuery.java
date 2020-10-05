package dk.magenta.datafordeler.subscribtion.queries;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PersonAddressDataeventQuery extends PersonGeneralQuery {


    @QueryField(
            queryNames = {"timestamp", "timestamp"},
            type = QueryField.FieldType.STRING
    )
    protected OffsetDateTime timestamp = OffsetDateTime.now();



    public PersonAddressDataeventQuery() {
        super();
    }

    public PersonAddressDataeventQuery(HttpServletRequest request) {
        super(request);
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.putAll(getBitemporalHandles("dataeventfield", "dataevent.field"));

    }






    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonAddressDataeventQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        MultiCondition condition = new MultiCondition(this.getCondition());







        //if (this.recordAfter != null) {
       // condition.add(this.addCondition("timestamp", Condition.Operator.GT, Collections.singletonList(this.timestamp.plusDays(10)), OffsetDateTime.class, false));


        condition.add(this.addCondition("dataeventfield", Condition.Operator.EQ, "Collections.singletonList(this.timestamp)", String.class, false));
        condition.add(this.addCondition("dafoUpdated", Condition.Operator.GT, Collections.singletonList(this.timestamp.plusDays(10)), OffsetDateTime.class, false));

        this.setRecordAfter(OffsetDateTime.now().plusYears(5));


        //}


        this.addCondition(condition);
    }

}
