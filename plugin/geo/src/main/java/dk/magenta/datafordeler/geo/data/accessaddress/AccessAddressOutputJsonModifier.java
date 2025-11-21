package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.geo.data.building.BuildingEntityManager;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityNameRecord;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeNameRecord;

import java.util.HashMap;
import java.util.Set;

public class AccessAddressOutputJsonModifier extends JsonModifier {

    private final AccessAddressOutputWrapper outputWrapper;
    protected HashMap<String, AccessAddressEntity> accessAddressEntities = new HashMap<>();
    protected HashMap<Integer, PostcodeEntity> postcodeEntities = new HashMap<>();
    protected HashMap<String, GeoLocalityEntity> localityEntities = new HashMap<>();

    public AccessAddressOutputJsonModifier(AccessAddressOutputWrapper outputWrapper, ResultSet resultSet) {
        this.outputWrapper = outputWrapper;
        for (AccessAddressEntity accessAddressEntity : (Set<AccessAddressEntity>) resultSet.get(AccessAddressEntity.class)) {
            for (AccessAddressRoadRecord r : accessAddressEntity.getRoad()) {
                String ident = r.getMunicipalityCode() + "|" + r.getRoadCode() + "|" + BuildingEntityManager.stripBnr(accessAddressEntity.getBnr()); // TODO: bør vi også bruge husnummer?
                this.accessAddressEntities.put(ident, accessAddressEntity);
                this.accessAddressEntities.put(accessAddressEntity.getIdentification().getUuid().toString(), accessAddressEntity);
            }
        }
        Set<PostcodeEntity> postcodeEntities = (Set<PostcodeEntity>) resultSet.get(PostcodeEntity.class);
        if (postcodeEntities != null) {
            for (PostcodeEntity postcodeEntity : postcodeEntities) {
                this.postcodeEntities.put(postcodeEntity.getCode(), postcodeEntity);
            }
        }
        Set<GeoLocalityEntity> localityEntities = (Set<GeoLocalityEntity>) resultSet.get(GeoLocalityEntity.class);
        if (localityEntities != null) {
            for (GeoLocalityEntity localityEntity : localityEntities) {
                this.localityEntities.put(localityEntity.getCode(), localityEntity);
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
        System.out.println(this.getClass().getSimpleName() + " modifying " + node.toString()+", has "+this.accessAddressEntities.size()+" in cache");
        JsonNode municipalityCode = node.get("kommunekode");
        JsonNode roadCode = node.get("vejkode");
        JsonNode bnr = node.get("bnr");
        JsonNode uuid = node.get("uuid");

        String ident = null;
        if (municipalityCode != null && roadCode != null && (bnr != null)) {
            ident = municipalityCode.asInt() + "|" + roadCode.asInt() + "|" + BuildingEntityManager.stripBnr(bnr.asText()); // TODO: bør vi også bruge husnummer?
        } else if (uuid != null && !uuid.isNull()) {
            ident = uuid.asText();
        }

        if (ident != null) {
            AccessAddressEntity entity = this.accessAddressEntities.get(ident);
            if (entity != null) {
                AccessAddressPostcodeRecord postcodeRecord = entity.getPostcode().current(); // TODO: Ud fra dato? (bitemporal)
                if (postcodeRecord != null) {
                    node.put("postnummer", postcodeRecord.getPostcode());
                    PostcodeEntity postcodeEntity = this.postcodeEntities.get(postcodeRecord.getPostcode());
                    if (postcodeEntity != null) {
                        PostcodeNameRecord postcodeNameRecord = postcodeEntity.getName().current(); // TODO: Ud fra dato? (bitemporal)
                        if (postcodeNameRecord != null) {
                            node.put("postdistrikt", postcodeNameRecord.getName());
                        }
                    }
                }

                AccessAddressLocalityRecord accessAddressLocalityRecord = entity.getLocality().current(); // TODO: Ud fra dato? (bitemporal)
                if (accessAddressLocalityRecord != null) {
                    node.put("lokalitetskode", accessAddressLocalityRecord.getCode());
                    GeoLocalityEntity localityEntity = this.localityEntities.get(accessAddressLocalityRecord.getCode());
                    if (localityEntity != null) {
                        LocalityNameRecord localityNameRecord = localityEntity.getName().current();// TODO: Ud fra dato? (bitemporal)
                        if (localityNameRecord != null) {
                            node.put("lokalitetsnavn", localityNameRecord.getName());
                        }
                    }
                }


                if (!node.has("vejkode")) {
                    node.put("vejkode", entity.getRoad().current().getRoadCode());
                }
                if (!node.has("kommunekode")) {
                    node.put("kommunekode", entity.getRoad().current().getMunicipalityCode());
                }

            }
        }
    }

    @Override
    public String getName() {
        return "geo_accessaddress";
    }
}
