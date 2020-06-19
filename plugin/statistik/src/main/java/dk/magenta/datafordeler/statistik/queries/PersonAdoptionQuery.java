package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.FieldDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.data.ParentDataRecord;
import dk.magenta.datafordeler.statistik.utils.Filter;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class PersonAdoptionQuery extends PersonStatisticsQuery {

    public PersonAdoptionQuery(HttpServletRequest request) {
        super(request);
    }

    public PersonAdoptionQuery(Filter filter) {
        super(filter);
    }

    /*@Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        lookupDefinition.setMatchNulls(true);

        FieldDefinition fatherDefinition = this.fromPath(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_FATHER);
        FieldDefinition motherDefinition = this.fromPath(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_MOTHER);

        fatherDefinition.or(motherDefinition);
        lookupDefinition.put(fatherDefinition);

        return lookupDefinition;
    }*/

    private static HashMap<String, String> joinHandles = new HashMap<>();

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

    //SELECT DISTINCT e from dk.magenta.datafordeler.cpr.data.person.PersonEntity e  LEFT JOIN e.name e_name LEFT JOIN e.name e_name LEFT JOIN e.father e_father LEFT JOIN e.mother e_mother WHERE e.identification.uuid IS NOT null  AND  ((e_father.registrationFrom >= :e_father_registrationFrom_5 OR e_mother.registrationFrom >= :e_mother_registrationFrom_6))


}
