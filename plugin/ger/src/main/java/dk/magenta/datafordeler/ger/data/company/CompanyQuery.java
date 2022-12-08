package dk.magenta.datafordeler.ger.data.company;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.ger.data.GerQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyQuery extends GerQuery<CompanyEntity> {

    public static final String NAME = CompanyEntity.IO_FIELD_NAME;

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private final List<String> name = new ArrayList<>();

    public List<String> getName() {
        return name;
    }

    public void clearName() {
        this.name.clear();
        this.updatedParameters();
    }

    public void setName(String name) {
        this.clearName();
        this.addName(name);
    }

    public void addName(String name) {
        if (name != null) {
            this.name.add(name);
            this.updatedParameters();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(NAME, this.name);
        return map;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.name.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setName(parameters.getFirstI(NAME));
    }

    @Override
    public String getEntityClassname() {
        return CompanyEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "ger_company";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("gernr", CompanyEntity.DB_FIELD_GERNR);
        joinHandles.put("name", CompanyEntity.DB_FIELD_NAME);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("gernr", this.getGerNr(), Integer.class);
        this.addCondition("name", this.name);
    }

}
