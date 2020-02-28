package dk.magenta.datafordeler.core.testutil;

import java.util.Map;

public interface Callback {
    void run(String key, Map<String, String[]> params);
}
