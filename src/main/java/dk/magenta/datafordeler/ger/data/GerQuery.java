package dk.magenta.datafordeler.ger.data;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 19-05-17.
 */
public abstract class GerQuery<E extends GerEntity> extends BaseQuery {

    public static final String GERNR = "gernr";

    @QueryField(type = QueryField.FieldType.STRING, queryName = GERNR)
    private List<String> gerNr = new ArrayList<>();

    public List<String> getGerNr() {
        return gerNr;
    }

    public void setGerNr(String gerNr) {
        this.gerNr.clear();
        this.addGerNr(gerNr);
    }

    public void setGerNr(int gerNr) {
        this.setGerNr(Integer.toString(gerNr));
    }

    public void addGerNr(String gerNr) {
        if (gerNr != null) {
            this.gerNr.add(gerNr);
            this.increaseDataParamCount();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(GERNR, this.gerNr);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = new BaseLookupDefinition(this);
        if (this.recordAfter != null) {
            lookupDefinition.put(DataItem.DB_FIELD_LAST_UPDATED, this.recordAfter, OffsetDateTime.class, BaseLookupDefinition.Operator.GT);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        this.setGerNr(parameters.getFirst(GERNR));
    }
}
