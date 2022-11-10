package dk.magenta.datafordeler.core.exception;

/**
 * A class to be thrown when a plugin method receives an object of a class it does not manage
 * This really should not happen, and indicates a coding error in a plugin, the core, or both.
 */
public class WrongSubclassException extends DataFordelerException {

    private final Class[] expectedClasses;
    private final Object receivedObject;

    public WrongSubclassException(Class expectedClass, Object receivedObject) {
        this(new Class[]{expectedClass}, receivedObject);
    }

    public WrongSubclassException(Class[] expectedClasses, Object receivedObject) {
        this.expectedClasses = expectedClasses;
        this.receivedObject = receivedObject;
    }


    public Class[] getExpectedClasses() {
        return expectedClasses;
    }

    public Object getReceivedObject() {
        return receivedObject;
    }

    @Override
    public String getCode() {
        return "datafordeler.plugin.plugin_received_wrong_class";
    }
}
