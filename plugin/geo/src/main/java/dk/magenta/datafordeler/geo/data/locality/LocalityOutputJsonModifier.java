package dk.magenta.datafordeler.geo.data.locality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.util.DoubleHashMap;

import java.util.Set;

public class LocalityOutputJsonModifier extends JsonModifier {

    private LocalityOutputWrapper outputWrapper;
    protected DoubleHashMap<Integer, Integer, GeoLocalityEntity> localityEntities = new DoubleHashMap<>();

    public LocalityOutputJsonModifier(LocalityOutputWrapper outputWrapper, ResultSet resultSet) {
        this.outputWrapper = outputWrapper;
        for (GeoLocalityEntity geoLocalityEntity : (Set<GeoLocalityEntity>) resultSet.get(GeoLocalityEntity.class)) {
            int municipalityCode = geoLocalityEntity.getMunicipality().current().getCode();
            int roadCode = geoLocalityEntity.getLocalityRoadcode().current().getCode();
            if (roadCode != 0) {
                this.localityEntities.put(municipalityCode, roadCode, geoLocalityEntity);
            }
        }
    }

    /**
     * Modifies a JsonNode by adding data from this.entities
     * @param node
     */
    @Override
    public void modify(ObjectNode node) {
        JsonNode municipalityCode = node.get("kommunekode");
        JsonNode roadCode = node.get("vejkode");

        if (municipalityCode != null && roadCode != null && !municipalityCode.isNull() && !roadCode.isNull()) {
            GeoLocalityEntity entity = this.localityEntities.get(municipalityCode.asInt(), roadCode.asInt());
            if (entity != null) {
                node.put("lokalitetskode", entity.getCode());
                LocalityNameRecord localityNameRecord = entity.getName().current();
                node.put("lokalitetsnavn", localityNameRecord.getName());
            }
        }
    }

    @Override
    public String getName() {
        return "geo_locality";
    }
}
