package dk.magenta.datafordeler.core.gapi;

import dk.magenta.datafordeler.core.io.Event;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Lars Peter Thomsen
 * @version 0.1
 *
 * Entity Data Model (EDM) Provider for the GAPI interface.
 * Specification of the transferred object goes here
 */

public class GapiEdmProvider extends CsdlAbstractEdmProvider {
    public static final String NAMESPACE = "DataFordeler";

    // EDM Container
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    // Entity Types Names
    public static final String ET_EVENT_NAME = "Event";
    public static final FullQualifiedName ET_EVENT = new FullQualifiedName(NAMESPACE, ET_EVENT_NAME);

    // Entity Set Names
    public static final String ES_EVENTS_NAME = "Events";

    // Complex types (structs)
    public static final FullQualifiedName CT_MESSAGEDATA = new FullQualifiedName(NAMESPACE, "Beskeddata");
    public static final FullQualifiedName CT_OBJECTDATA = new FullQualifiedName(NAMESPACE, "Objektdata");
    public static final FullQualifiedName CT_OBJECTREFERENCE = new FullQualifiedName(NAMESPACE, "Objektreference");

    /**
     * @return Schemas provided by this webservice
     * @throws ODataException
     */
    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        return Collections.singletonList(
            new CsdlSchema()
                .setNamespace(NAMESPACE)
                .setEntityTypes(Arrays.asList(
                    this.getEntityType(ET_EVENT)
                ))
                .setEntityContainer(this.getEntityContainer())
        );
    }

    /**
     * Defines how the incoming event is expected to look. The odata epi causes an error if data doesn't match.
     * @param entityTypeName
     * @return
     * @throws ODataException
     */
    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {

        if (entityTypeName.equals(ET_EVENT)) {
            return new CsdlEntityType()
                .setName(ET_EVENT_NAME)
                .setKey(Arrays.asList(
                    new CsdlPropertyRef().setName("eventID") // Identifying key of Event object
                ))
                .setProperties(Arrays.asList(
                    // One CsdlProperty for each class property that we have in the Event class
                    new CsdlProperty().setName("eventID").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("beskedVersion").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                    new CsdlProperty().setName("beskedData").setType(CT_MESSAGEDATA)
                ));
        }

        return null;
    }

    /**
     * Complex types (json dicts) in the input are defined here.
     * @param complexTypeName
     * @return
     * @throws ODataException
     */
    @Override
    public CsdlComplexType getComplexType(final FullQualifiedName complexTypeName) throws ODataException {
        if (CT_MESSAGEDATA.equals(complexTypeName)) {
            return new CsdlComplexType().setName(CT_MESSAGEDATA.getName()).setProperties(Arrays.asList(
                new CsdlProperty().setName("Objektdata").setType(CT_OBJECTDATA).setNullable(true),
                new CsdlProperty().setName("Objektreference").setType(CT_OBJECTREFERENCE).setNullable(true)
            ));
        }
        if (CT_OBJECTDATA.equals(complexTypeName)) {
            return new CsdlComplexType().setName(CT_OBJECTDATA.getName()).setProperties(Arrays.asList(
                new CsdlProperty().setName("dataskema").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                new CsdlProperty().setName("objektdata").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            ));
        }
        if (CT_OBJECTREFERENCE.equals(complexTypeName)) {
            return new CsdlComplexType().setName(CT_OBJECTREFERENCE.getName()).setProperties(Arrays.asList(
                new CsdlProperty().setName("objektreference").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
            ));
        }
        return null;
    }

    /**
     * @param entityContainer
     * @param entitySetName
     * @return a CsdlEntitySet that describes our entity type, if parameters
     * match our defined container (GapiEdmProvider.CONTAINER) and our defined
     * entitysetname (GapiEdmProvider.ES_EVENTS_NAME)
     */
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        if (entityContainer.equals(CONTAINER)){
            if (entitySetName.equals(ES_EVENTS_NAME)){
                return new CsdlEntitySet()
                    .setName(ES_EVENTS_NAME)
                    .setType(ET_EVENT);
            }
        }
        return null;
    }

    @Override
    public CsdlSingleton getSingleton(FullQualifiedName entityContainer, String singletonName) {
        if (entityContainer.equals(CONTAINER)){
            if (singletonName.equals(ET_EVENT_NAME)){
                return new CsdlSingleton()
                    .setName(ET_EVENT_NAME)
                    .setType(ET_EVENT);
            }
        }
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        return new CsdlEntityContainer()
            .setName(CONTAINER_NAME)
            .setEntitySets(Collections.singletonList(
                this.getEntitySet(CONTAINER, ES_EVENTS_NAME)
            ));
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            return new CsdlEntityContainerInfo().setContainerName(CONTAINER);
        }
        return null;
    }

    public static Event convertEvent(Entity entity) {
        Event event = new Event();
        event.setId(getPrimitiveFromEntity(entity, "eventID"));
        event.setVersion(getPrimitiveFromEntity(entity, "beskedVersion"));
        event.setReference(getPrimitiveFromEntity(entity, "beskedData", "Objektreference", "objektreference"));
        event.setSchema(getPrimitiveFromEntity(entity, "beskedData", "Objektdata", "dataskema"));
        event.setData(getPrimitiveFromEntity(entity, "beskedData", "Objektdata", "objektdata"));
        return event;
    }

    private static String getPrimitiveFromEntity(Entity entity, String... path) {
        Property property = entity.getProperty(path[0]);
        if (property == null) {
            return null;
        }
        for (int i=1; i<path.length; i++) {
            Property nextProperty = null;
            if (property.isNull()) {
                return null;
            }
            for (Property p : property.asComplex().getValue()) {
                if (p.getName().equals(path[i])) {
                    nextProperty = p;
                    break;
                }
            }
            if (nextProperty == null) {
                return null;
            }
            property = nextProperty;
        }
        return (String) property.asPrimitive();
    }

}
