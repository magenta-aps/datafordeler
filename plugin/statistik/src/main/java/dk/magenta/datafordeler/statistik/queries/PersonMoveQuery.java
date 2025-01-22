package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.statistik.utils.Filter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class PersonMoveQuery extends PersonStatisticsQuery {

    public PersonMoveQuery(HttpServletRequest request) throws InvalidClientInputException {
        super(request);
    }

    public PersonMoveQuery(Filter filter) throws InvalidClientInputException {
        super(filter);
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.putAll(getBitemporalHandles("address", PersonEntity.DB_FIELD_ADDRESS));
        joinHandles.putAll(getBitemporalHandles("migration", PersonEntity.DB_FIELD_FOREIGN_ADDRESS_EMIGRATION));
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonMoveQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        MultiCondition condition = new MultiCondition(this.getCondition(), "OR");
        this.applyBitemporalConditions(condition, "address");
        this.applyBitemporalConditions(condition, "migration");
        this.addCondition(condition);
    }

}
