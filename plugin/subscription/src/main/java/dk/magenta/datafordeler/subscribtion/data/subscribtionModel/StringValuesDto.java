package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import java.util.List;

public class StringValuesDto {
    private String key;
    private List<String> values;

    public StringValuesDto() {
    }

    public StringValuesDto(String key, List<String> values) {
        this.key = key;
        this.values = values;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
