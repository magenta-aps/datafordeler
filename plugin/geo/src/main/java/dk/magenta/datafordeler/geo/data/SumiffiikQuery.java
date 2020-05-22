package dk.magenta.datafordeler.geo.data;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lars on 19-05-17.
 */
public abstract class SumiffiikQuery<E extends SumiffiikEntity> extends BaseQuery {

    public static final String SUMIFFIIK = "sumiffiik";

    @QueryField(type = QueryField.FieldType.STRING, queryName = SUMIFFIIK)
    private String sumiffiik;

    public String getSumiffiik() {
        return sumiffiik;
    }

    public void setSumiffiik(String sumiffiik) {
        this.sumiffiik = sumiffiik;
        if (sumiffiik != null) {
            this.updatedParameters();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(SUMIFFIIK, this.sumiffiik);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = new BaseLookupDefinition(this);
        if (this.recordAfter != null) {
            lookupDefinition.put(DataItem.DB_FIELD_LAST_UPDATED, this.recordAfter, OffsetDateTime.class, BaseLookupDefinition.Operator.GT);
        }
        if (this.uuid != null && !this.uuid.isEmpty()) {
            lookupDefinition.put(
                    BaseLookupDefinition.entityref + BaseLookupDefinition.separator + GeoEntity.DB_FIELD_IDENTIFICATION + BaseLookupDefinition.separator + Identification.DB_FIELD_UUID,
                    this.uuid,
                    UUID.class,
                    BaseLookupDefinition.Operator.EQ
            );
        }
        if (this.sumiffiik != null) {
            lookupDefinition.put(
                    BaseLookupDefinition.entityref + BaseLookupDefinition.separator + SumiffiikEntity.DB_FIELD_SUMIFFIIK_ID,
                    this.sumiffiik,
                    String.class
            );
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        this.setSumiffiik(parameters.getFirst(SUMIFFIIK));
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("sumiffiik", SumiffiikEntity.DB_FIELD_SUMIFFIIK_ID);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        String sumiffiik = this.sumiffiik;
        if (sumiffiik != null) {
            sumiffiik.replaceFirst("^\\{$", "\\{");
            sumiffiik.replaceFirst("\\}$", "\\}");
        }
        this.addCondition("sumiffiik", sumiffiik != null ? Collections.singletonList(sumiffiik) : null);
    }

    @Override
    protected boolean isEmpty() {
        return this.sumiffiik != null;
    }
}
