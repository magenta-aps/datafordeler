package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusDataRecord;
import dk.magenta.datafordeler.statistik.utils.CivilStatusFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonCivilStatusQuery extends PersonStatisticsQuery {

    private String civilStatus;

    public PersonCivilStatusQuery(HttpServletRequest request) throws InvalidClientInputException {
        super(request);
    }

    public PersonCivilStatusQuery(CivilStatusFilter filter) throws InvalidClientInputException {
        super(filter);
        civilStatus = filter.getCivilStatus();
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("civilstatus", PersonEntity.DB_FIELD_CIVILSTATUS + BaseQuery.separator + CivilStatusDataRecord.DB_FIELD_CIVIL_STATUS);
        joinHandles.putAll(getBitemporalHandles("civilstatus", PersonEntity.DB_FIELD_CIVILSTATUS));
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonCivilStatusQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        SingleCondition statusCondition = this.addCondition("civilstatus", this.civilStatus != null ? Collections.singletonList(civilStatus) : List.of("G", "F", "E", "P", "O", "L", "D"), String.class);
        this.applyBitemporalConditions(statusCondition, "civilstatus");
    }
}
