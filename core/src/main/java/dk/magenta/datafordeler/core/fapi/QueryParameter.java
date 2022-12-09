package dk.magenta.datafordeler.core.fapi;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class QueryParameter extends ArrayList<String> {

    private final BaseQuery query;

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

    public List<OffsetDateTime> asOffsetDateTime() throws DateTimeParseException {
        return this.stream().map(BaseQuery::parseDateTime).collect(Collectors.toList());
    }
}
