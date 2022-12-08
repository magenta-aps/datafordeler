package dk.magenta.datafordeler.gladdrreg.data.road;

import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.gladdrreg.data.SumiffiikQuery;

import java.util.*;

/**
 * Created by lars on 19-05-17.
 */
public class RoadQuery extends SumiffiikQuery<RoadEntity> {

    public static final String CODE = RoadData.IO_FIELD_CODE;
    public static final String NAME = RoadData.IO_FIELD_NAME;
    public static final String SHORT_NAME = RoadData.IO_FIELD_SHORTNAME;
    public static final String ALTERNATE_NAME = RoadData.IO_FIELD_ALTNAME;
    public static final String CPR_NAME = RoadData.IO_FIELD_CPRNAME;
    public static final String MUNICIPALITY_ID = RoadData.IO_FIELD_MUNICIPALITY;

    @QueryField(type = QueryField.FieldType.INT, queryName = CODE)
    private List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = SHORT_NAME)
    private List<String> shortName = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = ALTERNATE_NAME)
    private List<String> alternateName = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = CPR_NAME)
    private List<String> cprName = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = MUNICIPALITY_ID)
    private List<String> municipalityIdentifier = new ArrayList<>();

    public List<String> getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code.clear();
        this.addCode(code);
    }

    public void addCode(String code) {
        if (code != null) {
            this.code.add(code);
            this.increaseDataParamCount();
        }
    }

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

    public List<String> getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName.clear();
        this.addShortName(shortName);
    }

    public void addShortName(String shortName) {
        if (shortName != null) {
            this.shortName.add(shortName);
            this.increaseDataParamCount();
        }
    }

    public List<String> getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(String alternateName) {
        this.alternateName.clear();
        this.addAlternateName(alternateName);
    }

    public void addAlternateName(String alternateName) {
        if (alternateName != null) {
            this.alternateName.add(alternateName);
            this.increaseDataParamCount();
        }
    }

    public List<String> getCprName() {
        return cprName;
    }

    public void setCprName(String cprName) {
        this.cprName.clear();
        this.addCprName(cprName);
    }

    public void addCprName(String cprName) {
        if (cprName != null) {
            this.cprName.add(cprName);
            this.increaseDataParamCount();
        }
    }

    public List<String> getMunicipalityIdentifier() {
        return this.municipalityIdentifier;
    }

    public void setMunicipalityIdentifier(String municipalityIdentifier) {
        this.municipalityIdentifier.clear();
        this.addMunicipalityIdentifier(municipalityIdentifier);
    }


    public void addMunicipalityIdentifier(String municipalityIdentifier) {
        if (municipalityIdentifier != null) {
            this.municipalityIdentifier.add(municipalityIdentifier);
            this.increaseDataParamCount();
        }
    }

    private List<String> locality = new ArrayList<>();

    public void setLocality(String locality) {
        this.locality.clear();
        this.addLocality(locality);
    }


    public void addLocality(String locality) {
        if (locality != null) {
            this.locality.add(locality);
            this.increaseDataParamCount();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(CODE, this.code);
        map.put(NAME, this.name);
        map.put(SHORT_NAME, this.shortName);
        map.put(ALTERNATE_NAME, this.alternateName);
        map.put(CPR_NAME, this.cprName);
        return map;
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.code != null && !this.code.isEmpty()) {
            lookupDefinition.put(RoadData.DB_FIELD_CODE, this.code, Integer.class);
        }
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(RoadData.DB_FIELD_NAME, this.name, String.class);
        }
        if (this.shortName != null && !this.shortName.isEmpty()) {
            lookupDefinition.put(RoadData.DB_FIELD_SHORTNAME, this.shortName, String.class);
        }
        if (this.alternateName != null && !this.alternateName.isEmpty()) {
            lookupDefinition.put(RoadData.DB_FIELD_ALTNAME, this.alternateName, String.class);
        }
        if (this.cprName != null && !this.cprName.isEmpty()) {
            lookupDefinition.put(RoadData.DB_FIELD_CPRNAME, this.cprName, String.class);
        }
        if (this.municipalityIdentifier != null && !this.municipalityIdentifier.isEmpty()) {
            lookupDefinition.put(RoadData.DB_FIELD_MUNICIPALITY + LookupDefinition.separator + Identification.DB_FIELD_UUID, this.municipalityIdentifier, UUID.class);
        }
        if (this.locality != null && !this.locality.isEmpty()) {
            lookupDefinition.put(RoadData.DB_FIELD_LOCATION + LookupDefinition.separator + Identification.DB_FIELD_UUID, this.locality, UUID.class);
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirstI(CODE));
        this.setName(parameters.getFirstI(NAME));
        this.setShortName(parameters.getFirstI(SHORT_NAME));
        this.setAlternateName(parameters.getFirstI(ALTERNATE_NAME));
        this.setCprName(parameters.getFirstI(CPR_NAME));
    }

    @Override
    public Class<RoadEntity> getEntityClass() {
        return RoadEntity.class;
    }

    @Override
    public Class getDataClass() {
        return RoadData.class;
    }

}
