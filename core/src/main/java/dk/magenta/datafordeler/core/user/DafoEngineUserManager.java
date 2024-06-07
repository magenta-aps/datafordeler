package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.PluginManagerCallbackHandler;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.SystemRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * An implementation of DafoUserManager that is backed by the DAFO user database.
 */
@Component
public class DafoEngineUserManager extends DafoUserManager implements PluginManagerCallbackHandler {
    @Autowired
    UserQueryManager userQueryManager;

    @Autowired
    PluginManager pluginManager;

    @PostConstruct
    public void Init() {
        this.pluginManager.addPostConstructCallBackHandler(this);
    }

    @Override
    public void executePluginManagerCallback(PluginManager pluginManager) {
        initializeSystemRoles();
        initializeAreaRestrictions();
    }

    /**
     * Looks up additional UserProfile data (system roles and area restrictions) in the database
     * before adding the UserProfiles to the SamlDafoUserDetails.
     *
     * @param samlDafoUserDetails - A SamlDafoUserDetails object to add UserProfiles to.
     */
    @Override
    public void addUserProfilesToSamlUser(SamlDafoUserDetails samlDafoUserDetails) {
        System.out.println("loading user "+samlDafoUserDetails.getIdentity());
        for (String profileName : samlDafoUserDetails.getAssertionUserProfileNames()) {
            UserProfile userProfile = userQueryManager.getUserProfileByName(profileName);
            if (userProfile != null) {
                samlDafoUserDetails.addUserProfile(userProfile);
            } else {
                // TODO: Log warning about unsupported UserProfile in token
            }
        }
    }

    /**
     * Ensures that all SystemRoles from currently loaded plugins are stored in the DAFO admin
     * database.
     */
    public void initializeSystemRoles() {
        Set<String> storedSystemRoleNames = userQueryManager.getAllStoredSystemRoleNames();
        ArrayList<SystemRole> newSystemRoles = new ArrayList<>();

        for (Plugin plugin : pluginManager.getPlugins()) {
            RolesDefinition rolesDefinition = plugin.getRolesDefinition();
            if (rolesDefinition != null) {
                for (SystemRole systemRole : rolesDefinition.getRoles()) {
                    if (!storedSystemRoleNames.contains(systemRole.getRoleName())) {
                        newSystemRoles.add(systemRole);
                    }
                }
            }
        }

        // Sort by numeric value of type, so that parents are created before children.
        newSystemRoles.sort(
                Comparator.comparingInt((SystemRole s) -> s.getType().getNumericValue())
        );

        for (SystemRole systemRole : newSystemRoles) {
            userQueryManager.insertSystemRole(systemRole);
        }
    }

    public void initializeAreaRestrictions() {
        List<AreaRestrictionType> newAreaRestrictionTypes = new ArrayList<>();
        List<AreaRestriction> newAreaRestrictions = new ArrayList<>();

        Set<String> storedAreaRestrictionTypes = userQueryManager.getAllAreaRestrictionTypeLookupNames();
        Set<String> storedAreaRestrictions = userQueryManager.getAllAreaRestrictionLookupNames();

        // Find unstored types and areas
        for (Plugin plugin : pluginManager.getPlugins()) {
            AreaRestrictionDefinition areaRestrictionDefinition = plugin.getAreaRestrictionDefinition();
            if (areaRestrictionDefinition != null) {
                Collection<AreaRestrictionType> typesList = areaRestrictionDefinition.getAreaRestrictionTypes();
                if (typesList != null) {
                    // Make sure we have unique values
                    typesList = new HashSet<>(typesList);
                    for (AreaRestrictionType areaRestrictionType : typesList) {
                        if (!storedAreaRestrictionTypes.contains(areaRestrictionType.lookupName())) {
                            newAreaRestrictionTypes.add(areaRestrictionType);
                        }
                        HashSet<AreaRestriction> choices = new HashSet<>(areaRestrictionType.getChoices());
                        for (AreaRestriction areaRestriction : choices) {
                            if (!storedAreaRestrictions.contains(areaRestriction.lookupName())) {
                                newAreaRestrictions.add(areaRestriction);
                            }
                        }
                    }
                }
            }
        }
        for (AreaRestrictionType newType : newAreaRestrictionTypes) {
            userQueryManager.insertAreaRestrictionType(newType);
        }
        for (AreaRestriction area : newAreaRestrictions) {
            userQueryManager.insertAreaRestriction(area);
        }
    }
}
