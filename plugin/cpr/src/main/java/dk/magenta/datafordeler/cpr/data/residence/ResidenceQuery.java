package dk.magenta.datafordeler.cpr.data.residence;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.data.CprQuery;
import dk.magenta.datafordeler.cpr.data.residence.data.ResidenceBaseData;

import java.util.*;

/**
 * Container for a query for Residences, defining fields and database lookup
 */
public class ResidenceQuery extends CprQuery<ResidenceEntity> {

    public static final String KOMMUNEKODE = ResidenceBaseData.IO_FIELD_MUNICIPALITY_CODE;
    public static final String VEJKODE = ResidenceBaseData.IO_FIELD_ROAD_CODE;
    public static final String HUSNUMMER = ResidenceBaseData.IO_FIELD_HOUSENUMBER;
    public static final String ETAGE = ResidenceBaseData.IO_FIELD_FLOOR;
    public static final String SIDE_DOER = ResidenceBaseData.IO_FIELD_DOOR;


    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private List<String> kommunekoder = new ArrayList<>();

    public Collection<String> getKommunekoder() {
        return this.kommunekoder;
    }

    public void addKommunekode(String kommunekode) {
        this.kommunekoder.add(kommunekode);
        if (kommunekode != null) {
            this.updatedParameters();
        }
    }

    public void addKommunekode(int kommunekode) {
        this.addKommunekode(String.format("%03d", kommunekode));
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = VEJKODE)
    private String vejkode;

    public String getVejkode() {
        return this.vejkode;
    }

    public void setVejkode(String vejkode) {
        this.vejkode = vejkode;
        if (vejkode != null) {
            this.updatedParameters();
        }
    }

    public void setVejkode(int vejkode) {
        this.setVejkode(String.format("%03d", vejkode));
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = HUSNUMMER)
    private String husnummer;

    public String getHusnummer() {
        return this.husnummer;
    }

    public void setHusnummer(String husnummer) {
        this.husnummer = husnummer;
        if (husnummer != null) {
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = ETAGE)
    private String etage;

    public String getEtage() {
        return this.etage;
    }

    public void setEtage(String etage) {
        this.etage = etage;
        if (etage != null) {
            this.updatedParameters();
        }
    }

    @QueryField(type = QueryField.FieldType.STRING, queryName = SIDE_DOER)
    private String sideDoer;

    public String getSideDoer() {
        return this.sideDoer;
    }

    public void setSideDoer(String sideDoer) {
        this.sideDoer = sideDoer;
        if (sideDoer != null) {
            this.updatedParameters();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(KOMMUNEKODE, this.kommunekoder);
        map.put(VEJKODE, this.vejkode);
        map.put(HUSNUMMER, this.husnummer);
        map.put(ETAGE, this.etage);
        map.put(SIDE_DOER, this.sideDoer);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        this.setVejkode(parameters.getFirst(VEJKODE));
        this.setHusnummer(parameters.getFirst(HUSNUMMER));
        this.setEtage(parameters.getFirst(ETAGE));
        this.setSideDoer(parameters.getFirst(SIDE_DOER));
        if (parameters.containsKey(KOMMUNEKODE)) {
            for (String kommunekode : parameters.get(KOMMUNEKODE)) {
                this.addKommunekode(kommunekode);
            }
        }
    }

    @Override
    public String getEntityClassname() {
        return ResidenceEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "cpr_residence";
    }

    @Override
    protected Map<String, String> joinHandles() {
        return null;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {

    }

    @Override
    public Class<ResidenceEntity> getEntityClass() {
        return ResidenceEntity.class;
    }

    @Override
    public Class getDataClass() {
        return ResidenceBaseData.class;
    }


    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (!this.kommunekoder.isEmpty()) {
            lookupDefinition.put(ResidenceBaseData.DB_FIELD_MUNICIPALITY_CODE, this.kommunekoder, Integer.class);
        }
        if (this.vejkode != null) {
            lookupDefinition.put(ResidenceBaseData.DB_FIELD_ROAD_CODE, this.vejkode, Integer.class);
        }
        if (this.husnummer != null) {
            lookupDefinition.put(ResidenceBaseData.DB_FIELD_HOUSENUMBER, this.husnummer, String.class);
        }
        if (this.etage != null) {
            lookupDefinition.put(ResidenceBaseData.DB_FIELD_FLOOR, this.etage, String.class);
        }
        if (this.sideDoer != null) {
            lookupDefinition.put(ResidenceBaseData.DB_FIELD_DOOR, this.sideDoer, String.class);
        }
        if (!this.getKommunekodeRestriction().isEmpty()) {
            lookupDefinition.put(ResidenceBaseData.DB_FIELD_MUNICIPALITY_CODE, this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }

    @Override
    protected boolean isEmpty() {
        return this.kommunekoder.isEmpty() && this.vejkode.isEmpty() && this.husnummer.isEmpty() && this.etage.isEmpty() && this.sideDoer.isEmpty();
    }

}
