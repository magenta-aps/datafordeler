package dk.magenta.datafordeler.cpr.records.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.util.DoubleHashMap;
import dk.magenta.datafordeler.cpr.records.road.data.RoadCityBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import dk.magenta.datafordeler.cpr.records.road.data.RoadNameBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadPostalcodeBitemporalRecord;

import java.util.List;
import java.util.Set;

public class RoadOutputJsonModifier extends JsonModifier {

    private final RoadRecordOutputWrapper outputWrapper;
    protected DoubleHashMap<Integer, Integer, RoadEntity> entities = new DoubleHashMap<>();

    public RoadOutputJsonModifier(RoadRecordOutputWrapper outputWrapper, ResultSet resultSet) {
        this.outputWrapper = outputWrapper;
        for (RoadEntity roadEntity : (Set<RoadEntity>) resultSet.get(RoadEntity.class)) {
            this.entities.put(roadEntity.getMunicipalityCode(), roadEntity.getRoadcode(), roadEntity);
        }
    }

    /**
     * Modifies a JsonNode by adding data from this.entities
     *
     * @param node
     */
    @Override
    public void modify(ObjectNode node) {
        System.out.println(this.getClass().getSimpleName() + " modifying " + node.toString()+", has "+this.entities.size()+" in cache");
        JsonNode roadCode = node.get("vejkode");
        JsonNode municipalityCode = node.get("kommunekode");
        JsonNode houseNumber = node.get("husnummer");
        if (roadCode != null && municipalityCode != null) {
            RoadEntity entity = this.entities.get(municipalityCode.asInt(), roadCode.asInt());
            if (entity != null) {
                List<RoadNameBitemporalRecord> name = entity.getName().current(); // TODO: Find den rigtige vejnavnsrecord (nyeste?)
                if (!name.isEmpty()) {
                    node.put("vejnavn", name.iterator().next().getRoadName());
                }
                for (RoadPostalcodeBitemporalRecord post : entity.getPostcode().current()) {
                    if (houseNumber == null || post.matches(houseNumber.asText())) {
                        node.put("postnummer", post.getPostalCode());
                        node.put("postdistrikt", post.getPostalDistrict());
                        break;
                    }
                }
                for (RoadCityBitemporalRecord city : entity.getCity().current()) {
                    if (houseNumber == null || city.matches(houseNumber.asText())) {
                        node.put("lokalitetsnavn", city.getCityName());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "cpr_road";
    }
}
