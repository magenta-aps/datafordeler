package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.statistik.utils.Filter;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class PersonAdoptionQuery extends PersonStatisticsQuery {

    public PersonAdoptionQuery(HttpServletRequest request) throws InvalidClientInputException {
        super(request);
    }

    public PersonAdoptionQuery(Filter filter) throws InvalidClientInputException {
        super(filter);
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.putAll(getBitemporalHandles("father", PersonEntity.DB_FIELD_FATHER));
        joinHandles.putAll(getBitemporalHandles("mother", PersonEntity.DB_FIELD_MOTHER));
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> joinHandles = new HashMap<>(super.joinHandles());
        joinHandles.putAll(PersonAdoptionQuery.joinHandles);
        return joinHandles;
    }

    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        MultiCondition parentsCondition = new MultiCondition(this.getCondition(), "OR");
        this.applyBitemporalConditions(parentsCondition, "father");
        this.applyBitemporalConditions(parentsCondition, "mother");
        this.addCondition(parentsCondition);
    }

}
