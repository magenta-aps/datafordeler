package dk.magenta.dafosts.saml.metadata;

import com.github.ulisesbocchio.spring.boot.security.saml.resource.SpringResourceWrapperOpenSAMLResource;
import dk.magenta.dafosts.library.DafoIdPData;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.saml.controller.PassiveGetTokenController;
import org.apache.commons.collections.map.ListOrderedMap;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;

import java.time.LocalDateTime;
import java.util.*;

/**
 * A metadatamanager that provides extra methods for updating the list of available IdPs from the DAFO admin database.
 */
public class DafoCachingMetadataManager extends CachingMetadataManager {
    private static final Logger logger = LoggerFactory.getLogger(PassiveGetTokenController.class);

    private Map<String, DafoMetadataProvider> dafoMetadataProviderMap = new HashMap<>();
    private ResourceBackedMetadataProvider defaultMetadataProvider = null;
    private LocalDateTime lastUpdate = LocalDateTime.now().minusYears(50);

    private DatabaseQueryManager queryManager;
    private ParserPool parserPool = new BasicParserPool();

    public DafoCachingMetadataManager() throws MetadataProviderException {
        super(Collections.EMPTY_LIST);
    }

    /**
     * Loads in the dataproviders stored in the DAFO admin database.
     * If not primary IdPs are read from the database a primary IdP will be loaded from the specified default
     * metadata location.
     * @param defaultIdpMetadataLocation A string containing the location from which to load the metadata for the
     *                                   default IdP if one is not provided by the database.
     * @throws ResourceException
     * @throws MetadataProviderException
     */
    public void initialize(String defaultIdpMetadataLocation, DatabaseQueryManager queryManager)
            throws ResourceException, MetadataProviderException {
        this.queryManager = queryManager;
        updateDafoMetadataProviders();
        /*
         TODO: Ticket #21274: defaultMetadataProvider is only disabled for now.
         TODO: All code referencing it needs to be removed as well.
        if(defaultIdpMetadataLocation != null) {
            DefaultResourceLoader loader = new DefaultResourceLoader();
            defaultMetadataProvider = new ResourceBackedMetadataProvider(
                    new Timer(),
                    new SpringResourceWrapperOpenSAMLResource(loader.getResource(defaultIdpMetadataLocation.trim()))
            );
            defaultMetadataProvider.setParserPool(parserPool);
            addMetadataProvider(defaultMetadataProvider);
        }
        */
    }

    public DatabaseQueryManager getDatabaseQueryManager() {
        return queryManager;
    }

    public void updateDafoMetadataProviders() {
        LocalDateTime lastDbUpdate = queryManager.getLastIdPUpdate();
        if(!lastDbUpdate.isAfter(lastUpdate)) {
            return;
        }
        Map<String, LocalDateTime> providersInDatabase = queryManager.getIdPUpdateMap();

        for(String identityId : providersInDatabase.keySet()) {
            DafoIdPData newData = null;
            if(dafoMetadataProviderMap.containsKey(identityId)) {
                DafoMetadataProvider existing = dafoMetadataProviderMap.get(identityId);
                if(existing.getLastUpdate().isBefore(providersInDatabase.get(identityId))) {
                    removeMetadataProvider(existing);
                    newData = queryManager.getIdPDataByName(identityId);
                }
            } else {
                newData = queryManager.getIdPDataByName(identityId);
            }
            if(newData != null) {
                try {
                    DafoMetadataProvider newProvider = new DafoMetadataProvider(newData, parserPool);
                    newProvider.initialize();
                    if(newProvider.getMetadata() != null) {
                        addMetadataProvider(newProvider);
                        dafoMetadataProviderMap.put(identityId, newProvider);
                        updateDafoMetadataManagerAlias(newProvider);
                    }
                }
                catch(MetadataProviderException e) {
                    logger.warn("Failed to load metadata for " + identityId + ": " + e.getMessage());
                }
            }
        }

        // Delete any providers that are no longer present in the database
        for(String identityId : dafoMetadataProviderMap.keySet()) {
            if (!providersInDatabase.containsKey(identityId)) {
                DafoMetadataProvider toRemove = dafoMetadataProviderMap.get(identityId);
                dafoMetadataProviderMap.remove(identityId);
                removeMetadataProvider(toRemove);
            }
        }

        // Save last update time
        lastUpdate = lastDbUpdate;
    }


    /**
     * Updates the alias in the ExtendedMetadata for the specified DafoMetadataProvider
     * @param dafoMetadataProvider - The DafoMetadataProvider to update alias for
     */
    protected void updateDafoMetadataManagerAlias(DafoMetadataProvider dafoMetadataProvider) {
        try {
            ExtendedMetadata extendedMetadata = getExtendedMetadata(dafoMetadataProvider.getEntityId());
            extendedMetadata.setAlias(dafoMetadataProvider.getName());
        } catch(MetadataProviderException e) {
            logger.warn("Unable update metadata alias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns a map that maps IdP EntityIDs to their aliases
     * @return A map of EntityIDs to aliases
     */
    public Map<String, String> getIdpProviderMap() throws MetadataProviderException {
        updateDafoMetadataProviders();
        Map<String, String> result = new ListOrderedMap();

        if(defaultMetadataProvider != null) {
            try {
                String entityId = ((EntityDescriptor)defaultMetadataProvider.getMetadata()).getEntityID();
                result.put(entityId, "DAFO IdP");
            } catch (Exception e) {
                logger.warn("Unable to get IdentityID for default metadata provider: " + e.getMessage());
            }
        }
        for(DafoMetadataProvider dafoMetadataProvider : dafoMetadataProviderMap.values()) {
            result.put(dafoMetadataProvider.getEntityId(), dafoMetadataProvider.getName());
        }

        return result;
    }

    public boolean isDefaultMetadataProvider(String entityID) {
        if(defaultMetadataProvider == null) {
            return false;
        }
        try {
            String defaultEntityId = ((EntityDescriptor) defaultMetadataProvider.getMetadata()).getEntityID();
            return (defaultEntityId ==  entityID);
        } catch(MetadataProviderException e) {
            e.printStackTrace();
        }
        return false;
    }

    public DafoMetadataProvider getDafoMetadataProvider(String entityID) {
        if(dafoMetadataProviderMap.containsKey(entityID)) {
            return dafoMetadataProviderMap.get(entityID);
        } else {
            return null;
        }
    }
}
