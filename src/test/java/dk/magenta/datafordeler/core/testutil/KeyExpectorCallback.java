package dk.magenta.datafordeler.core.testutil;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Future;

public class KeyExpectorCallback extends ExpectorCallback implements Callback, Future<Boolean> {

    private String expectedKey;
    private String expectedValue;
    private boolean found = false;
    private boolean cancelled = false;
    private boolean done = false;

    public KeyExpectorCallback(String expectedKey, String expectedValue) {
        this.expectedKey = expectedKey;
        this.expectedValue = expectedValue;
    }

    @Override
    public void run(String key, Map<String, String[]> params) {
        if (this.expectedKey == null) {
            this.found = true;
        }
        if (params.containsKey(this.expectedKey)) {
            String[] values = params.get(this.expectedKey);
            if (Arrays.binarySearch(values, this.expectedValue) != -1) {
                this.found = true;
            }
        }
    }
}
