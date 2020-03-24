package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class JsonModifier {
    public abstract void modify(ObjectNode input);
    public abstract String getName();
}
