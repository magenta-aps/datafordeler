package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.residence.ResidenceEntityManager;
import dk.magenta.datafordeler.cpr.data.road.RoadEntityManager;
import dk.magenta.datafordeler.cpr.records.road.RoadRecordQuery;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Datafordeler Plugin to fetch, parse and serve CPR data (data on people, roads and
 * administrative regions).
 * As with all plugins, it follows the model laid out in the Datafordeler Core
 * project, so it takes care of where to fetch data, how to parse it, how to
 * store it (leveraging the Datafordeler bitemporality model), under what path
 * to serve it, and which roles should exist for data access.
 * The Core and Engine take care of the generic updateRegistrationTo around these, fetching and
 * serving based on the specifics laid out in the plugin.
 */
@Component
public class CprPlugin extends Plugin {

    public static final String DEBUG_TABLE_PREFIX = "";

    @Autowired
    private CprConfigurationManager configurationManager;

    @Autowired
    private CprRegisterManager registerManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private ResidenceEntityManager residenceEntityManager;

    @Autowired
    private RoadEntityManager roadEntityManager;

    private final CprRolesDefinition rolesDefinition = new CprRolesDefinition();

    private final CprAreaRestrictionDefinition areaRestrictionDefinition;

    public CprPlugin() {
        this.areaRestrictionDefinition = new CprAreaRestrictionDefinition(this);
    }

    /**
     * Plugin initialization
     */
    @PostConstruct
    public void init() {
        this.registerManager.addEntityManager(this.personEntityManager);
        this.registerManager.addEntityManager(this.residenceEntityManager);
        this.registerManager.addEntityManager(this.roadEntityManager);
        this.registerManager.setPlugin(this);
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
        return "cpr";
    }

    /**
     * Return the plugin’s register manager
     */
    @Override
    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    /**
     * Return the plugin’s configuration manager
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
        s.add("LEFT JOIN " + RoadEntity.class.getCanonicalName() + " cpr_road");
        s.add("ON cpr_road.roadcode = " + handles.get("roadcode"));
        s.add("AND cpr_road.municipalityCode = " + handles.get("municipalitycode"));
        return s.toString();
    }

    public LinkedHashMap<String, Class> getJoinClassAliases(Collection<String> handles) {
        LinkedHashMap<String, Class> aliases = new LinkedHashMap<>();
        if (handles.contains("municipalitycode") && handles.contains("roadcode")) {
            aliases.put("cpr_road", RoadEntity.class);
        }
        return aliases;
    }

    public List<BaseQuery> getQueries(Map<String, String> values) {
        ArrayList<BaseQuery> queries = new ArrayList<>();
        if (values.containsKey("municipalitycode") && values.containsKey("roadcode")) {
            RoadRecordQuery recordQuery = new RoadRecordQuery();
            recordQuery.setVejkode(values.get("roadcode"));
            recordQuery.setKommunekode(values.get("municipalitycode"));
            queries.add(recordQuery);
        }
        return queries;
    }

}
