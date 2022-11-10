package dk.magenta.datafordeler.ger.data;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
public abstract class GerQuery<E extends GerEntity> extends BaseQuery {

    public static final String GERNR = "gernr";

    @QueryField(type = QueryField.FieldType.STRING, queryName = GERNR)
    private final List<String> gerNr = new ArrayList<>();

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
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(GERNR, this.gerNr);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        this.setGerNr(parameters.getFirst(GERNR));
    }


    @Override
    protected boolean isEmpty() {
        return this.gerNr.isEmpty();
    }


}
