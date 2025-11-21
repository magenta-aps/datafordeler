package dk.magenta.datafordeler.geo.data.locality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;

import java.util.HashMap;
import java.util.Set;

public class LocalityOutputJsonModifier extends JsonModifier {

    private final LocalityOutputWrapper outputWrapper;
    protected HashMap<String, GeoLocalityEntity> localityEntities = new HashMap<>();

    public LocalityOutputJsonModifier(LocalityOutputWrapper outputWrapper, ResultSet resultSet) {
        this.outputWrapper = outputWrapper;
        for (GeoLocalityEntity geoLocalityEntity : (Set<GeoLocalityEntity>) resultSet.get(GeoLocalityEntity.class)) {
            String code = geoLocalityEntity.getCode();
            if (code != null && !code.isEmpty()) {
                this.localityEntities.put(code, geoLocalityEntity);
            }
        }
    }

    /**
     * Modifies a JsonNode by adding data from this.entities
     *
     * @param node
     */
    @Override
    public void modify(ObjectNode node) {
        System.out.println(this.getClass().getSimpleName() + " modifying " + node.toString()+", has "+this.localityEntities.size()+" in cache");
        JsonNode localityCode = node.get("lokalitetskode");
        if (localityCode != null && !localityCode.isNull()) {
            GeoLocalityEntity entity = this.localityEntities.get(localityCode.asText());
            if (entity != null) {
                System.out.println("Found entity for " + localityCode.asText() + ": "+entity.getId());
                LocalityNameRecord localityNameRecord = entity.getName().current();
                System.out.println("Adding lokalitetsnavn: "+localityNameRecord.getName());
                node.put("lokalitetsnavn", localityNameRecord.getName());
            }
        }
    }

    @Override
    public String getName() {
        return "geo_locality";
    }
}
