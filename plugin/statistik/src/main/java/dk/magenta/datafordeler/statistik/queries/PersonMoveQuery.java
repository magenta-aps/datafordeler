package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.FieldDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.Condition;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.SingleCondition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.AddressRecord;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.statistik.utils.Filter;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PersonMoveQuery extends PersonStatisticsQuery {

    public PersonMoveQuery(HttpServletRequest request) {
        super(request);
    }

    public PersonMoveQuery(Filter filter) {
        super(filter);
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        lookupDefinition.setMatchNulls(true);

        FieldDefinition addressDefinition = this.fromPath(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_ADDRESS);
        FieldDefinition migrationDefinition = this.fromPath(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_FOREIGN_ADDRESS_EMIGRATION);

        addressDefinition.or(migrationDefinition);
        lookupDefinition.put(addressDefinition);

        return lookupDefinition;
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

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

    /*
    * SELECT DISTINCT e
    * from dk.magenta.datafordeler.cpr.data.person.PersonEntity e
    * LEFT JOIN e.name e_name
    * LEFT JOIN e.name e_name
    * LEFT JOIN e.address e_address
    * LEFT JOIN e.emigration e_emigration
    * WHERE e.identification.uuid IS NOT null
    * AND  ((e.personnummer IN (:e_personnummer_0_list)))
    * AND ((
    * (e_address.registrationFrom >= :e_address_registrationFrom_3 AND e_address.registrationFrom <= :e_address_registrationFrom_4)
    *  OR (e_emigration.registrationFrom >= :e_emigration_registrationFrom_5 AND e_emigration.registrationFrom <= :e_emigration_registrationFrom_6)
    * ))
    * */

}
