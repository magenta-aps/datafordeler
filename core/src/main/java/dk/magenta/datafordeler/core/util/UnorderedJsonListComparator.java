package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Comparator;

public class UnorderedJsonListComparator implements Comparator<JsonNode> {

    @Override
    public int compare(JsonNode jsonNode1, JsonNode jsonNode2) {
        if (jsonNode1 == null) {
            return jsonNode2 == null ? 0 : 1;
        } else if (jsonNode2 == null) {
            return -1;
        }
        if (jsonNode1.isArray() && jsonNode2.isArray()) {
            if (jsonNode1.size() < jsonNode2.size()) {
                return 1;
            } else if (jsonNode1.size() > jsonNode2.size()) {
                return -1;
            } else {
                for (int i=0; i<jsonNode1.size(); i++) {
                    boolean found = false;
                    int cmp = 0;
                    JsonNode item1 = jsonNode1.get(i);
                    for (int j=0; j<jsonNode2.size() && !found; j++) {
                        cmp = this.compare(item1, jsonNode2.get(j));
                        if (cmp == 0) {
                            found = true;
                        }
                    }
                    if (!found) {
                        return cmp;
                    }
                }
                return 0;
            }
        } else {
            return jsonNode1.equals(jsonNode2) ? 0 : 1;
        }
    }
}
