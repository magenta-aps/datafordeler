package dk.magenta.datafordeler.geo.data.road;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.util.ListHashMap;

import java.util.Collection;
import java.util.List;

public class RoadOutputJsonModifier extends JsonModifier {

    private RoadOutputWrapper outputWrapper;
    protected ListHashMap<String, GeoRoadEntity> entities = new ListHashMap<>();

    public RoadOutputJsonModifier(RoadOutputWrapper outputWrapper, Collection<GeoRoadEntity> roadEntities) {
        this.outputWrapper = outputWrapper;
        for (GeoRoadEntity roadEntity : roadEntities) {
            for (RoadMunicipalityRecord m : roadEntity.getMunicipality()) {
                this.entities.add(m.getCode() + "|" +roadEntity.getCode(), roadEntity);
            }
        }
    }

    /**
     * Modifies a JsonNode by adding data from this.entities
     * @param node
     */
    @Override
    public void modify(ObjectNode node) {
        JsonNode roadCode = node.get("vejkode");
        JsonNode municipalityCode = node.get("kommunekode");
        if (roadCode != null && municipalityCode != null) {
            List<GeoRoadEntity> entities = this.entities.get(municipalityCode.asInt()+"|"+roadCode.asInt());
            if (entities != null) {
                for (GeoRoadEntity entity : entities) {
                    RoadNameRecord name = entity.getName().current(); // TODO: Find den rigtige vejnavnsrecord (nyeste?)
                    node.put("vejnavn", name.getName());
                }
            }
        }
    }

    @Override
    public String getName() {
        return "geo_road";
    }
}
