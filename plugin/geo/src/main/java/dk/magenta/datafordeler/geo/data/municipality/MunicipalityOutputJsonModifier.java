package dk.magenta.datafordeler.geo.data.municipality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.JsonModifier;

import java.util.Collection;
import java.util.HashMap;

public class MunicipalityOutputJsonModifier extends JsonModifier {

    private final MunicipalityOutputWrapper outputWrapper;
    protected HashMap<Integer, GeoMunicipalityEntity> entities = new HashMap<>();

    public MunicipalityOutputJsonModifier(MunicipalityOutputWrapper outputWrapper, Collection<GeoMunicipalityEntity> MunicipalityEntities) {
        this.outputWrapper = outputWrapper;
        for (GeoMunicipalityEntity municipalityEntity : MunicipalityEntities) {
            this.entities.put(municipalityEntity.getCode(), municipalityEntity);
        }
    }

    /**
     * Modifies a JsonNode by adding data from this.entities
     *
     * @param node
     */
    @Override
    public void modify(ObjectNode node) {
        JsonNode municipalityCode = node.get("kommunekode");
        if (municipalityCode != null) {
            GeoMunicipalityEntity entity = this.entities.get(municipalityCode.asInt());
            if (entity != null) {
                MunicipalityNameRecord name = entity.getName().current(); // TODO: Find den rigtige navnerecord (nyeste?)
                node.put("kommunenavn", name.getName());
            }
        }
    }

    @Override
    public String getName() {
        return "geo_municipality";
    }
}
