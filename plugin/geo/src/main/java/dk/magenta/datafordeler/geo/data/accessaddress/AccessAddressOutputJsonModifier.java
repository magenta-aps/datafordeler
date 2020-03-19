package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityNameRecord;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeNameRecord;

import java.util.HashMap;
import java.util.Set;

public class AccessAddressOutputJsonModifier extends JsonModifier {

    private AccessAddressOutputWrapper outputWrapper;
    protected HashMap<String, AccessAddressEntity> accessAddressEntities = new HashMap<>();
    protected HashMap<Integer, PostcodeEntity> postcodeEntities = new HashMap<>();
    protected HashMap<String, GeoLocalityEntity> localityEntities = new HashMap<>();

    public AccessAddressOutputJsonModifier(AccessAddressOutputWrapper outputWrapper, ResultSet resultSet) {
        this.outputWrapper = outputWrapper;
        for (AccessAddressEntity accessAddressEntity : (Set<AccessAddressEntity>) resultSet.get(AccessAddressEntity.class)) {
            for (AccessAddressRoadRecord r : accessAddressEntity.getRoad()) {
                String ident = r.getMunicipalityCode()+"|"+r.getRoadCode()+"|"+accessAddressEntity.getBnr(); // TODO: bør vi også bruge husnummer?
                this.accessAddressEntities.put(ident, accessAddressEntity);
            }
        }
        for (PostcodeEntity postcodeEntity : (Set<PostcodeEntity>) resultSet.get(PostcodeEntity.class)) {
            this.postcodeEntities.put(postcodeEntity.getCode(), postcodeEntity);
        }
        for (GeoLocalityEntity localityEntity : (Set<GeoLocalityEntity>) resultSet.get(GeoLocalityEntity.class)) {
            this.localityEntities.put(localityEntity.getCode(), localityEntity);
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
        JsonNode bnr = node.get("bnr");

        if (municipalityCode != null && roadCode != null && (bnr != null)) {
            String ident = municipalityCode.asInt()+"|"+roadCode.asInt()+"|"+bnr.asText(); // TODO: bør vi også bruge husnummer?
            AccessAddressEntity entity = this.accessAddressEntities.get(ident);
            if (entity != null) {
                AccessAddressPostcodeRecord postcodeRecord = entity.getPostcode().current(); // TODO: Ud fra dato? (bitemporal)
                PostcodeEntity postcodeEntity = this.postcodeEntities.get(postcodeRecord.getPostcode());
                node.put("postnr", postcodeEntity.getCode());
                PostcodeNameRecord postcodeNameRecord = postcodeEntity.getName().current(); // TODO: Ud fra dato? (bitemporal)
                node.put("postdistrikt", postcodeNameRecord.getName());

                AccessAddressLocalityRecord accessAddressLocalityRecord = entity.getLocality().current(); // TODO: Ud fra dato? (bitemporal)
                GeoLocalityEntity localityEntity = this.localityEntities.get(accessAddressLocalityRecord.getCode());
                node.put("lokalitetskode", accessAddressLocalityRecord.getCode());
                LocalityNameRecord localityNameRecord = localityEntity.getName().current();// TODO: Ud fra dato? (bitemporal)
                node.put("lokalitetsnavn", localityNameRecord.getName());

            } else {
                System.out.println("access address not found with ident: "+ident);
                System.out.println("We have:" + this.accessAddressEntities.keySet());
            }
        }
    }

    @Override
    public String getName() {
        return "geo_accessaddress";
    }
}
