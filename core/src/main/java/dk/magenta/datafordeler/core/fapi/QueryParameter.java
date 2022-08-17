package dk.magenta.datafordeler.core.fapi;

import java.util.ArrayList;
import java.util.Collection;

public class QueryParameter extends ArrayList<String> {

    private BaseQuery query;

    public QueryParameter(BaseQuery query) {
        this.query = query;
    }

    public boolean add(String value) {
        if (value != null) {
            this.query.updatedParameters();
            return super.add(value);
        }
        return false;
    }

    public void set(String value) {
        this.clear();
        this.add(value);
    }

    public void set(Collection<String> values) {
        this.clear();
        if (values != null) {
            for (String value : values) {
                this.add(value);
            }
        }
    }
}
