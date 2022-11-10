package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.util.ListHashMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Map of URL parameters, where each key can have several values
 */
public class ParameterMap extends ListHashMap<String, String> {
    public ParameterMap() {
    }

    public ParameterMap(Map<String, List<String>> initial, boolean decode) {
        super(initial);
        if (decode) {
            this.urldecode();
        }
    }

    public ParameterMap set(String key, String value) {
        this.add(key, value);
        return this;
    }

    public ParameterMap replace(String key, String value) {
        super.remove(key);
        this.add(key, value);
        return this;
    }

    public void urldecode() {
        for (String key : this.keySet()) {
            List<String> list = this.get(key);
            this.put(key, new ArrayList<>(list.stream().map(v -> URLDecoder.decode(v, StandardCharsets.UTF_8)).collect(Collectors.toList())));
        }
    }

    public String asUrlParams() {
        StringJoiner sj = new StringJoiner("&");
        for (String key : this.keySet()) {
            for (String value : this.get(key)) {
                sj.add(key + "=" + java.net.URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        }
        return sj.toString();
    }

    public static ParameterMap fromPath(String path) {
        ParameterMap map = new ParameterMap();
        if (path.contains("?")) {
            path = path.substring(path.indexOf("?") + 1);
        }
        for (String kvp : path.split("&")) {
            int eqIndex = kvp.indexOf("=");
            String key, value;
            if (eqIndex != -1) {
                key = kvp.substring(0, eqIndex);
                value = kvp.substring(eqIndex + 1);
            } else {
                key = kvp;
                value = "";
            }
            map.add(key, value);
        }
        return map;
    }

    public String[] getAsArray(String key) {
        List<String> values = this.get(key);
        return values.toArray(new String[values.size()]);
    }

    public List<String> getI(String key) {
        ArrayList<String> values = new ArrayList<>();
        for (String k : this.keySet()) {
            if (k.equalsIgnoreCase(key)) {
                values.addAll(this.get(k));
            }
        }
        return values;
    }

    public MultiValueMap<String, String> asMultiValuedMap() {
        LinkedMultiValueMap map = new LinkedMultiValueMap<String, String>();
        for (String key : this.keySet()) {
            map.put(key, this.get(key));
        }
        return map;
    }

}
