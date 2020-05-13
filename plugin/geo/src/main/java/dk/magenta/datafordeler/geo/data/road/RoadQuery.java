package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.geo.data.SumiffiikQuery;

import java.util.*;

/**
 * Created by lars on 19-05-17.
 */
public class RoadQuery extends SumiffiikQuery<GeoRoadEntity> {

    public static final String CODE = GeoRoadEntity.IO_FIELD_CODE;
    public static final String NAME = GeoRoadEntity.IO_FIELD_NAME;
    public static final String ADDRESSING_NAME = RoadNameRecord.IO_FIELD_ADDRESSING_NAME;
    public static final String MUNICIPALITY = RoadMunicipalityRecord.IO_FIELD_CODE;
    public static final String LOCALITY = RoadLocalityRecord.IO_FIELD_CODE;

    @QueryField(type = QueryField.FieldType.INT, queryName = CODE)
    private List<String> code = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = NAME)
    private List<String> name = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = ADDRESSING_NAME)
    private List<String> addressingName = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY)
    private List<String> locality = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = LOCALITY)
    private List<UUID> localityUUID = new ArrayList<>();

    @QueryField(type = QueryField.FieldType.STRING, queryName = MUNICIPALITY)
    private List<String> municipality = new ArrayList<>();


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


    public List<String> getAddressingName() {
        return addressingName;
    }

    public void setAddressingName(String addressingName) {
        this.addressingName.clear();
        this.addAddressingName(addressingName);
    }

    public void addAddressingName(String addressingName) {
        if (addressingName != null) {
            this.addressingName.add(addressingName);
            this.increaseDataParamCount();
        }
    }

    public List<String> getLocality() {
        return locality;
    }

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



    public List<UUID> getLocalityUUID() {
        return localityUUID;
    }

    public void setLocalityUUID(UUID localityUUID) {
        this.localityUUID.clear();
        this.addLocalityUUID(localityUUID);
    }

    public void addLocalityUUID(UUID localityUUID) {
        if (localityUUID != null) {
            this.localityUUID.add(localityUUID);
            this.increaseDataParamCount();
        }
    }

    public List<String> getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality.clear();
        this.addMunicipality(municipality);
    }

    public void addMunicipality(String municipality) {
        if (municipality != null) {
            this.municipality.add(municipality);
            this.increaseDataParamCount();
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>(super.getSearchParameters());
        map.put(CODE, this.code);
        map.put(NAME, this.name);
        map.put(ADDRESSING_NAME, this.addressingName);
        map.put(LOCALITY, this.locality);
        map.put(MUNICIPALITY, this.municipality);
        return map;
    }

    @Override
    public BaseLookupDefinition getLookupDefinition() {
        BaseLookupDefinition lookupDefinition = super.getLookupDefinition();
        if (this.code != null && !this.code.isEmpty()) {
            lookupDefinition.put(GeoRoadEntity.DB_FIELD_CODE, this.code, Integer.class);
        }
        if (this.name != null && !this.name.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_NAME + BaseLookupDefinition.separator + RoadNameRecord.DB_FIELD_NAME,
                    this.name,
                    String.class
            );
        }
        if (this.addressingName != null && !this.addressingName.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_NAME + BaseLookupDefinition.separator + RoadNameRecord.DB_FIELD_ADDRESSING_NAME,
                    this.addressingName,
                    String.class
            );
        }
        if (this.locality != null && !this.locality.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_LOCALITY + BaseLookupDefinition.separator + RoadLocalityRecord.DB_FIELD_CODE,
                    this.locality,
                    String.class
            );
        }
        if (this.localityUUID != null && !this.localityUUID.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_LOCALITY + BaseLookupDefinition.separator + RoadLocalityRecord.DB_FIELD_REFERENCE + BaseLookupDefinition.separator + Identification.DB_FIELD_UUID,
                    this.localityUUID,
                    UUID.class
            );
        }
        if (this.municipality != null && !this.municipality.isEmpty()) {
            lookupDefinition.put(
                    GeoRoadEntity.DB_FIELD_MUNICIPALITY + BaseLookupDefinition.separator + RoadMunicipalityRecord.DB_FIELD_CODE,
                    this.municipality,
                    Integer.class
            );
        }
        return lookupDefinition;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        super.setFromParameters(parameters);
        this.setCode(parameters.getFirst(CODE));
        this.setName(parameters.getFirst(NAME));
        this.setAddressingName(parameters.getFirst(ADDRESSING_NAME));
        this.setLocality(parameters.getFirst(LOCALITY));
        this.setMunicipality(parameters.getFirst(MUNICIPALITY));
    }

}
