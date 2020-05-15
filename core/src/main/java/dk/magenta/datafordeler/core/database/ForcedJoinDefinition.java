package dk.magenta.datafordeler.core.database;

import java.util.Arrays;
import java.util.List;

public class ForcedJoinDefinition extends FieldDefinition {
    public ForcedJoinDefinition(String path) {
        super(path, null);
    }

    public List<String> getJoinParts() {
        return Arrays.asList(this.path.split(BaseLookupDefinition.quotedSeparator));
    }
}
