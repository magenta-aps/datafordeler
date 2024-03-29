package dk.magenta.datafordeler.ger.data.responsible;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.ger.data.GerQuery;

import java.util.*;

public class ResponsibleQuery extends GerQuery<ResponsibleEntity> {

    public static final String NAME = ResponsibleEntity.IO_FIELD_NAME;

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


    public static final String CVR_GUID = ResponsibleEntity.IO_FIELD_CVR_PARTICIPANT_GUID;

    @QueryField(type = QueryField.FieldType.STRING, queryName = CVR_GUID)
    private final List<String> cvrGuid = new ArrayList<>();

    public List<String> getCvrGuid() {
        return this.cvrGuid;
    }

    public void clearCvrGuid() {
        this.cvrGuid.clear();
        this.updatedParameters();
    }

    public void setCvrGuid(UUID cvrGuid) {
        this.setCvrGuid(cvrGuid.toString());
    }

    public void setCvrGuid(String cvrGuid) {
        this.clearCvrGuid();
        this.addCvrGuid(cvrGuid);
    }

    public void addCvrGuid(String cvrGuid) {
        if (cvrGuid != null) {
            this.cvrGuid.add(cvrGuid);
            this.updatedParameters();
        }
    }


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(NAME, this.name);
        map.put(CVR_GUID, this.cvrGuid);
        return map;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.name.isEmpty() && this.cvrGuid.isEmpty();
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setName(parameters.getFirstI(NAME));
        this.setGerNr(parameters.getFirstI(GERNR));
        this.setCvrGuid(parameters.getFirstI(CVR_GUID));
    }

    @Override
    public String getEntityClassname() {
        return ResponsibleEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "ger_responsible";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("name", ResponsibleEntity.DB_FIELD_NAME);
        joinHandles.put("gernr", ResponsibleEntity.DB_FIELD_GERNR);
        joinHandles.put("guid", ResponsibleEntity.DB_FIELD_CVR_PARTICIPANT_GUID);
        joinHandles.put("lastUpdated", ResponsibleEntity.DB_FIELD_LAST_UPDATED);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("name", this.name);
        this.addCondition("gernr", this.getGerNr(), Integer.class);
        this.addCondition("guid", this.cvrGuid, UUID.class);
    }

}
