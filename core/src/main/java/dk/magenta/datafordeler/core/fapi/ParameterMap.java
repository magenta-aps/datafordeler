package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.util.ListHashMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Map of URL parameters, where each key can have several values
 */
public class ParameterMap extends ListHashMap<String, String> {
    public ParameterMap() {
    }

    @Override
    public ArrayList<String> put(String key, ArrayList<String> value) {
        value.replaceAll(v -> URLDecoder.decode(v, StandardCharsets.UTF_8));
        return super.put(key, value);
    }

    public ParameterMap(Map<String, List<String>> initial) {
        super(initial);
    }

    @Override
    public void add(String key, String value) {
        super.add(key, URLDecoder.decode(value, StandardCharsets.UTF_8));
    }

    @Override
    public void add(String key, Collection<String> values) {
        ArrayList<String> valueList = new ArrayList<>(values);
        valueList.replaceAll(v -> URLDecoder.decode(v, StandardCharsets.UTF_8));
        super.add(key, valueList);
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
