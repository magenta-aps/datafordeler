package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;

import java.util.HashSet;
import java.util.Map;


public class MultiClassQuery extends BaseQuery {

    public class SubQuery {
        public BaseQuery query;
        public String entityClassname;
        public String ident;
        public String joiner;
        private BaseLookupDefinition lookupDefinition;

        public <E extends DatabaseEntry> SubQuery(BaseQuery query, String entityClassname, String ident, String joiner) {
            this.query = query;
            this.entityClassname = entityClassname;
            this.ident = ident;
            this.joiner = joiner;
        }

        public BaseLookupDefinition getLookupDefinition() {
            if (this.lookupDefinition == null) {
                this.lookupDefinition = this.query.getLookupDefinition();
            }
            return this.lookupDefinition;
        }
    }
    private HashSet<SubQuery> queries = new HashSet<SubQuery>();

    public void add(BaseQuery query, String entityClassname, String ident, String joiner) {
        this.queries.add(new SubQuery(query, entityClassname, ident, joiner));
    }
    public void add(BaseQuery query, String ident, String joiner) {
        this.queries.add(new SubQuery(query, query.getEntityClassname(), ident, joiner));
    }
    public void add(BaseQuery query, String ident) {
        this.queries.add(new SubQuery(query, query.getEntityClassname(), ident, query.getJoinString()));
    }
    public MultiClassLookupDefinition getLookupDefinition() {
        return new MultiClassLookupDefinition(this.queries);
    }

    @Override
    public void setFromParameters(ParameterMap parameterMap) throws InvalidClientInputException {
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        return null;
    }

}
