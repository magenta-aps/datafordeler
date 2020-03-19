package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

public abstract class JsonModifier<T extends DatabaseEntry> {
    public abstract void modify(ObjectNode input);
    public abstract String getName();
}
