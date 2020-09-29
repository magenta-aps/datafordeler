package dk.magenta.datafordeler.subscribtion.queries;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusDataRecord;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonCivilStatusQuery extends PersonGeneralQuery {

    private String civilStatus;

    public PersonCivilStatusQuery() {
        super();
        civilStatus = "D";
    }

    public PersonCivilStatusQuery(HttpServletRequest request) {
        super(request);
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.putAll(getBitemporalHandles("civilstatus", PersonEntity.DB_FIELD_CIVILSTATUS + LookupDefinition.separator + CivilStatusDataRecord.DB_FIELD_CIVIL_STATUS));
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonCivilStatusQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        SingleCondition statusCondition = this.addCondition("civilstatus", Condition.Operator.EQ, this.civilStatus != null ? civilStatus : List.of("G", "F", "E", "P", "O", "L", "D"), String.class);
        this.applyBitemporalConditions(statusCondition, "civilstatus");

        /*super.setupConditions();
        MultiCondition condition = new MultiCondition(this.getCondition(), "OR");
        SingleCondition statusCondition = this.addCondition("municipalitycode", Condition.Operator.GTE, 900, Integer.class);
        this.applyBitemporalConditions(condition, "civilstatus");
        this.addCondition(statusCondition);*/


    }
}
