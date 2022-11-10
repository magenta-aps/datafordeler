package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.geo.configuration.GeoConfigurationManager;
import dk.magenta.datafordeler.geo.data.accessaddress.*;
import dk.magenta.datafordeler.geo.data.building.BuildingEntityManager;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityEntityManager;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityQuery;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntityManager;
import dk.magenta.datafordeler.geo.data.road.*;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Datafordeler Plugin to fetch, parse and serve Geo data (data on regions, localities, roads, addresses etc.)
 * As with all plugins, it follows the model laid out in the Datafordeler Core
 * project, so it takes care of where to fetch data, how to parse it, how to
 * store it (leveraging the Datafordeler bitemporality model), under what path
 * to serve it, and which roles should exist for data access.
 * The Core and Engine take care of the generic updateRegistrationTo around these, fetching and
 * serving based on the specifics laid out in the plugin.
 */
@Component
public class GeoPlugin extends Plugin {

    public static final String DEBUG_TABLE_PREFIX = "";

    public static final int SRID = 4326;

    @Autowired
    private GeoConfigurationManager configurationManager;

    @Autowired
    private GeoRegisterManager registerManager;


    @Autowired
    private MunicipalityEntityManager municipalityEntityManager;

    @Autowired
    private LocalityEntityManager localityEntityManager;

    @Autowired
    private RoadEntityManager roadEntityManager;

    @Autowired
    private BuildingEntityManager buildingEntityManager;

    @Autowired
    private AccessAddressEntityManager accessAddressEntityManager;

    @Autowired
    private UnitAddressEntityManager unitAddressEntityManager;

    @Autowired
    private PostcodeEntityManager postcodeEntityManager;

    private final GeoRolesDefinition rolesDefinition = new GeoRolesDefinition();

    private final GeoAreaRestrictionDefinition areaRestrictionDefinition;

    public GeoPlugin() {
        this.areaRestrictionDefinition = new GeoAreaRestrictionDefinition(this);
    }

    /**
     * Plugin initialization
     */
    @PostConstruct
    public void init() {
        this.registerManager.addEntityManager(this.municipalityEntityManager);
        this.registerManager.addEntityManager(this.localityEntityManager);
        this.registerManager.addEntityManager(this.roadEntityManager);
        this.registerManager.addEntityManager(this.postcodeEntityManager);
        this.registerManager.addEntityManager(this.buildingEntityManager);
        this.registerManager.addEntityManager(this.accessAddressEntityManager);
        this.registerManager.addEntityManager(this.unitAddressEntityManager);
    }

    /**
     * Return the name for the plugin, used to identify it when issuing commands
     */
    @Override
    public long getVersion() {
        return 1;
    }

    @Override
    public String getName() {
        return "geo";
    }

    /**
     * Return the plugin’s register manager
     */
    @Override
    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    /**
     * Return the plugin’s dk.magenta.datafordeler.geo.configuration manager
     */
    @Override
    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    /**
     * Get a definition of user roles
     */
    @Override
    public RolesDefinition getRolesDefinition() {
        return this.rolesDefinition;
    }

    @Override
    public AreaRestrictionDefinition getAreaRestrictionDefinition() {
        return this.areaRestrictionDefinition;
    }


    public String getJoinString(Map<String, String> handles) {
        StringJoiner s = new StringJoiner(" ");
        // Join accessaddress
        if (handles.containsKey("municipalitycode") && handles.containsKey("roadcode") && (handles.containsKey("housenumber") || handles.containsKey("bnr"))) {
            s.add("LEFT JOIN " + AccessAddressRoadRecord.class.getCanonicalName() + " geo_accessaddress__road");
            s.add("ON geo_accessaddress__road.roadCode = " + handles.get("roadcode"));
            s.add("AND geo_accessaddress__road.municipalityCode = " + handles.get("municipalitycode") + "\n");

            if (handles.containsKey("housenumber")) {
                s.add("LEFT JOIN " + AccessAddressHouseNumberRecord.class.getCanonicalName() + " geo_accessaddress__housenumber");
                s.add("ON geo_accessaddress__housenumber.number = " + handles.get("housenumber") + "\n");
            }

            s.add("LEFT JOIN " + AccessAddressEntity.class.getCanonicalName() + " geo_accessaddress");
            s.add("ON geo_accessaddress__road.entity = geo_accessaddress");

            StringJoiner s1 = new StringJoiner(" OR ");
            if (handles.containsKey("bnr")) {
                s1.add("geo_accessaddress.bnr = " + handles.get("bnr"));
            } else if (handles.containsKey("housenumber")) {
                s1.add("geo_accessaddress__housenumber.entity = geo_accessaddress");
            }
            s.add("AND (" + s1 + ")" + "\n");

            s.add("LEFT JOIN " + AccessAddressPostcodeRecord.class.getCanonicalName() + " geo_accessaddress_postcode");
            s.add("ON geo_accessaddress_postcode.entity = geo_accessaddress" + "\n");
            s.add("LEFT JOIN " + PostcodeEntity.class.getCanonicalName() + " geo_postcode");
            s.add("ON geo_postcode.code = geo_accessaddress_postcode.postcode" + "\n");
        }

        // Join road
        if (handles.containsKey("municipalitycode") && handles.containsKey("roadcode")) {
            s.add("LEFT JOIN " + RoadMunicipalityRecord.class.getCanonicalName() + " geo_road_municipality");
            s.add("ON geo_road_municipality.code = " + handles.get("municipalitycode") + "\n");
            s.add("LEFT JOIN " + GeoRoadEntity.class.getCanonicalName() + " geo_road");
            s.add("ON geo_road.code = " + handles.get("roadcode"));
            s.add("AND geo_road_municipality.entity = geo_road" + "\n");

            // Join locality
            s.add("LEFT JOIN " + RoadLocalityRecord.class.getCanonicalName() + " geo_road_locality");
            s.add("ON geo_road_locality.entity = geo_road" + "\n");
            s.add("LEFT JOIN " + GeoLocalityEntity.class.getCanonicalName() + " geo_locality ON");
            s.add("geo_locality.code = geo_road_locality.code" + "\n");
        }

        // Join municipality
        if (handles.containsKey("municipalitycode")) {
            s.add("LEFT JOIN " + GeoMunicipalityEntity.class.getCanonicalName() + " geo_municipality");
            s.add("ON geo_municipality.code = " + handles.get("municipalitycode") + "\n");
        }

        return s.toString();
    }

    public LinkedHashMap<String, Class> getJoinClassAliases(Collection<String> handles) {
        LinkedHashMap<String, Class> aliases = new LinkedHashMap<>();
        if (handles.contains("municipalitycode")) {
            aliases.put("geo_municipality", GeoMunicipalityEntity.class);
            if (handles.contains("roadcode")) {
                aliases.put("geo_road", GeoRoadEntity.class);
            }
            aliases.put("geo_locality", GeoLocalityEntity.class);
            if (handles.contains("housenumber") || handles.contains("bnr")) {
                aliases.put("geo_accessaddress", AccessAddressEntity.class);
                aliases.put("geo_postcode", PostcodeEntity.class);
            }
        }
        return aliases;
    }


    public List<BaseQuery> getQueries(Map<String, String> values) {
        ArrayList<BaseQuery> queries = new ArrayList<>();
        if (values.containsKey("municipalitycode")) {
            MunicipalityQuery municipalityQuery = new MunicipalityQuery();
            municipalityQuery.setCode(values.get("municipalitycode"));
            queries.add(municipalityQuery);
        }

        if (values.containsKey("municipalitycode") && values.containsKey("roadcode")) {
            RoadQuery roadQuery = new RoadQuery();
            roadQuery.setMunicipalityCode(values.get("municipalitycode"));
            roadQuery.setCode(values.get("roadcode"));
            roadQuery.addRelatedLocalityQuery();
            queries.add(roadQuery);
        }

        if (values.containsKey("accessaddress")) {
            AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
            accessAddressQuery.setUUID(UUID.fromString(values.get("accessaddress")));
            accessAddressQuery.addRelatedLocalityQuery();
            accessAddressQuery.addRelatedPostcodeQuery();
            queries.add(accessAddressQuery);
        }

        return queries;
    }


}
