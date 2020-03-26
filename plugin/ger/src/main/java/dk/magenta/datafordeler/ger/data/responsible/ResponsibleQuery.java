package dk.magenta.datafordeler.ger.data.responsible;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.ger.data.GerQuery;

import java.util.*;

public class ResponsibleQuery extends GerQuery<ResponsibleEntity> {

    public static final String NAME = ResponsibleEntity.IO_FIELD_NAME;

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

    public List<String> getName() {
        return name;
    }

    public void setName(String name) {
        this.name.clear();
        this.addName(name);
    }

    public void addName(String name) {
        if (name != null) {
            this.name.add(name);
            this.increaseDataParamCount();
        }
    }



    public static final String GERNR = ResponsibleEntity.IO_FIELD_GERNR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = GERNR)
    private List<String> gerNr = new ArrayList<>();

    @Override
    public List<String> getGerNr() {
        return this.gerNr;
    }

    public void setGerNr(int gerNr) {
        this.setGerNr(Integer.toString(gerNr));
    }

    public void setGerNr(String gerNr) {
        this.gerNr.clear();
        this.addGerNr(gerNr);
    }

    public void addGerNr(String gerNr) {
        if (gerNr != null) {
            this.gerNr.add(gerNr);
            this.increaseDataParamCount();
        }
    }



    public static final String CVR_GUID = ResponsibleEntity.IO_FIELD_CVR_PARTICIPANT_GUID;

    @QueryField(type = QueryField.FieldType.STRING, queryName = CVR_GUID)
    private List<String> cvrGuid = new ArrayList<>();

    public List<String> getCvrGuid() {
        return this.cvrGuid;
    }

    public void setCvrGuid(UUID cvrGuid) {
        this.setCvrGuid(cvrGuid.toString());
    }

    public void setCvrGuid(String cvrGuid) {
        this.cvrGuid.clear();
        this.addCvrGuid(cvrGuid);
    }

    public void addCvrGuid(String cvrGuid) {
        if (cvrGuid != null) {
            this.cvrGuid.add(cvrGuid);
            this.increaseDataParamCount();
        }
    }



    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(NAME, this.name);
        map.put(GERNR, this.gerNr);
        map.put(CVR_GUID, this.cvrGuid);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(ResponsibleEntity.DB_FIELD_NAME, this.name, String.class);
        }
        if (this.gerNr != null && !this.gerNr.isEmpty()) {
            lookupDefinition.put(ResponsibleEntity.DB_FIELD_GERNR, this.gerNr, Integer.class);
        }
        if (this.cvrGuid != null && !this.cvrGuid.isEmpty()) {
            lookupDefinition.put(ResponsibleEntity.DB_FIELD_CVR_PARTICIPANT_GUID, this.cvrGuid, UUID.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setName(parameters.getFirst(NAME));
        this.setGerNr(parameters.getFirst(GERNR));
        this.setCvrGuid(parameters.getFirst(CVR_GUID));
    }

    @Override
    public String getEntityClassname() {
        return ResponsibleEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "ger_responsible";
    }

    private static HashMap<String, String> joinHandles = new HashMap<>();

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
        this.addCondition("name", this.name);
        this.addCondition("gernr", this.gerNr, Integer.class);
        this.addCondition("guid", this.cvrGuid, UUID.class);
    }

}
