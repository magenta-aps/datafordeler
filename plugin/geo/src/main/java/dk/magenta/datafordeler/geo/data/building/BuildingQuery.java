package dk.magenta.datafordeler.geo.data.building;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
public class BuildingQuery extends SumiffiikQuery<BuildingEntity> {

    public static final String ANR = BuildingEntity.IO_FIELD_ANR;
    public static final String BNR = BuildingEntity.IO_FIELD_BNR;

    @QueryField(type = QueryField.FieldType.STRING, queryName = ANR)
    private List<String> anr = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = BNR)
    private List<String> bnr = new ArrayList<>();

    public List<String> getAnr() {
        return anr;
    }

    public void setAnr(String anr) {
        this.anr.clear();
        this.updatedParameters();
        this.addAnr(anr);
    }

    public void addAnr(String anr) {
        if (anr != null) {
            this.anr.add(anr);
            this.increaseDataParamCount();
            this.updatedParameters();
        }
    }



    public List<String> getBnr() {
        return bnr;
    }

    public void setBnr(String bnr) {
        this.bnr.clear();
        this.updatedParameters();
        this.addBnr(bnr);
    }

    public void addBnr(String bnr) {
        if (bnr != null) {
            this.bnr.add(bnr);
            this.increaseDataParamCount();
            this.updatedParameters();
        }
    }




    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(ANR, this.anr);
        map.put(BNR, this.bnr);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.anr != null && !this.anr.isEmpty()) {
            lookupDefinition.put(BuildingEntity.DB_FIELD_ANR, this.anr, String.class);
        }
        if (this.bnr != null && !this.bnr.isEmpty()) {
            lookupDefinition.put(BuildingEntity.DB_FIELD_BNR, this.bnr, String.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setAnr(parameters.getFirst(ANR));
        this.setBnr(parameters.getFirst(BNR));
    }

    @Override
    public String getEntityClassname() {
        return BuildingEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "geo_building";
    }


    private static HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("anr", BuildingEntity.DB_FIELD_ANR);
        joinHandles.put("bnr", BuildingEntity.DB_FIELD_BNR);
    }

    @Override
    protected Map<String, String> joinHandles() {
        HashMap<String, String> handles = new HashMap<>();
        handles.putAll(super.joinHandles());
        handles.putAll(joinHandles);
        return handles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        super.setupConditions();
        this.addCondition("anr", this.anr);
        this.addCondition("bnr", this.bnr);
    }

}
