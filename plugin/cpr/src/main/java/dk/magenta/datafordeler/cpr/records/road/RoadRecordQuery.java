package dk.magenta.datafordeler.cpr.records.road;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import dk.magenta.datafordeler.cpr.records.road.data.RoadNameBitemporalRecord;

import java.util.*;

/**
 * Container for a query for Persons, defining fields and database lookup
 */
public class RoadRecordQuery extends BaseQuery {


    public static final String VEJKODE = RoadEntity.IO_FIELD_ROAD_CODE;
    public static final String VEJNAVN = RoadNameBitemporalRecord.IO_FIELD_ROADNAME;
    public static final String KOMMUNEKODE = RoadEntity.IO_FIELD_MUNIPALITY_CODE;

    @QueryField(type = QueryField.FieldType.STRING, queryName = VEJKODE)
    private final List<String> vejkoder = new ArrayList<>();

    public Collection<String> getVejkoder() {
        return this.vejkoder;
    }

    public void addVejkode(String vejkode) {
        if (vejkode != null) {
            this.vejkoder.add(vejkode);
            this.updatedParameters();
        }
    }

    public void clearVejkode() {
        this.vejkoder.clear();
        this.updatedParameters();
    }

    public void setVejkode(String vejkode) {
        this.clearVejkode();
        this.addVejkode(vejkode);
    }

    public void setVejkode(int vejkode) {
        this.setVejkode(Integer.toString(vejkode));
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = VEJKODE)
    private final List<String> vejnavne = new ArrayList<>();

    public Collection<String> getVejnavne() {
        return this.vejnavne;
    }

    public void addVejnavn(String vejnavn) {
        if (vejnavn != null) {
            this.vejnavne.add(vejnavn);
            this.updatedParameters();
        }
    }

    public void clearVejnavn() {
        this.vejnavne.clear();
        this.updatedParameters();
    }

    public void setVejnavn(String vejnavn) {
        this.clearVejnavn();
        this.addVejnavn(vejnavn);
    }


    @QueryField(type = QueryField.FieldType.STRING, queryName = KOMMUNEKODE)
    private final List<String> kommunekoder = new ArrayList<>();

    public List<String> getKommunekoder() {
        return kommunekoder;
    }

    public void addKommunekode(String kommunekode) {
        if (kommunekode != null) {
            this.kommunekoder.add(kommunekode);
            this.updatedParameters();
        }
    }

    public void setKommunekode(int kommunekode) {
        this.setKommunekode(Integer.toString(kommunekode));
    }

    public void setKommunekode(String kommunekode) {
        this.clearKommunekode();
        this.addKommunekode(kommunekode);
    }

    public void addKommunekode(int kommunekode) {
        this.addKommunekode(Integer.toString(kommunekode));
    }

    public void clearKommunekode() {
        this.kommunekoder.clear();
        this.updatedParameters();
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(VEJKODE, this.vejkoder);
        map.put(KOMMUNEKODE, this.kommunekoder);
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        if (parameters.containsKey(VEJKODE)) {
            for (String vejkode : parameters.get(VEJKODE)) {
                this.addVejkode(vejkode);
            }
        }
        if (parameters.containsKey(VEJNAVN)) {
            for (String vejnavn : parameters.get(VEJNAVN)) {
                this.addVejnavn(vejnavn);
            }
        }
        if (parameters.containsKey(KOMMUNEKODE)) {
            for (String kommunekode : parameters.get(KOMMUNEKODE)) {
                this.addKommunekode(kommunekode);
            }
        }
    }

    @Override
    public String getEntityClassname() {
        return RoadEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "cpr_road";
    }

    private static final HashMap<String, String> joinHandles = new HashMap<>();

    static {
        joinHandles.put("municipalitycode", RoadEntity.DB_FIELD_MUNIPALITY_CODE);
        joinHandles.put("roadcode", RoadEntity.DB_FIELD_ROAD_CODE);
        joinHandles.put("name", RoadEntity.DB_FIELD_NAME_CODE + BaseQuery.separator + RoadNameBitemporalRecord.DB_FIELD_ROADNAME);
    }

    @Override
    protected Map<String, String> joinHandles() {
        return joinHandles;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {
        this.addCondition("municipalitycode", this.kommunekoder, Integer.class);
        this.addCondition("roadcode", this.vejkoder, Integer.class);
        this.addCondition("name", this.vejnavne);
        this.addCondition("municipalitycode", this.getKommunekodeRestriction(), Integer.class);
    }

    @Override
    protected boolean isEmpty() {
        return this.kommunekoder.isEmpty() && this.vejkoder.isEmpty() && this.vejnavne.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoadRecordQuery that = (RoadRecordQuery) o;
        return Objects.equals(vejkoder, that.vejkoder) &&
                Objects.equals(vejnavne, that.vejnavne) &&
                Objects.equals(kommunekoder, that.kommunekoder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vejkoder, vejnavne, kommunekoder);
    }
}
