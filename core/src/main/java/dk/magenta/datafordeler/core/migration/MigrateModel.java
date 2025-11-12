package dk.magenta.datafordeler.core.migration;

import java.util.Collections;
import java.util.List;

public interface MigrateModel {
    void updateTimestamp();

    static List<String> updateFields() {
        return Collections.emptyList();
    }
}
