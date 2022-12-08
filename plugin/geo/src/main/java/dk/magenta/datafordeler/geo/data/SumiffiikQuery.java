package dk.magenta.datafordeler.geo.data;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.JoinedQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        this.setSumiffiik(parameters.getFirstI(SUMIFFIIK));
        for (JoinedQuery relatedQuery : this.getRelated()) {
            relatedQuery.getJoined().setFromParameters(parameters);
        }
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("sumiffiik", SumiffiikEntity.DB_FIELD_SUMIFFIIK_ID);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        String sumiffiik = this.sumiffiik;
        if (sumiffiik != null) {
            sumiffiik.replaceFirst("^\\{$", "\\{");
            sumiffiik.replaceFirst("\\}$", "\\}");
        }
        this.addCondition("sumiffiik", sumiffiik != null ? Collections.singletonList(sumiffiik) : null);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.sumiffiik == null;
    }
}
