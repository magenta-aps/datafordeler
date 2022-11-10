package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.exception.QueryBuildException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A join between two tables (or rather, two queries based on entity tables)
 * It's basically a structure holding references to both queries, and a map of which handles from each query fit together
 * A "handle" in this case being the predefined name of a field somewhere in the hierarchy, e.g.
 * "roadCode" representing "person__address.roadCode"
 */
public class JoinedQuery extends Condition {
    private final BaseQuery base;
    private final BaseQuery joined;
    Map<String, String> joinHandles;

    private static final Logger log = LogManager.getLogger(JoinedQuery.class.getCanonicalName());


    public JoinedQuery(BaseQuery base, BaseQuery joined, Map<String, String> joinHandleKeys, MultiCondition parentCondition) throws QueryBuildException {
        // joinHandleKeys er map f.eks. muncode, roadcode, husnr, bnr
        super(parentCondition);
        this.base = base;
        this.joined = joined;
        this.joinHandles = new HashMap<>();
        // Each pair in the map should join together (in hql with a WHERE entry)
        // calling useJoinHandle tells each query to perform internal joins to make sure the handle is available
        // e.g. personQuery.useJoinHandle("roadCode") will add a join for the person address table (LEFT JOIN person.address person__address), returning the hql path for the handle (person__address.roadCode)
        for (String baseJoinHandle : joinHandleKeys.keySet()) {
            String remoteJoinHandle = joinHandleKeys.get(baseJoinHandle);


            this.joinHandles.put(this.base.useJoinHandle(baseJoinHandle), this.joined.useJoinHandle(remoteJoinHandle));
        }
    }

    public BaseQuery getBase() {
        return this.base;
    }

    public BaseQuery getJoined() {
        return this.joined;
    }

    @Override
    public String toHql() {
        // Create a hql string representing this join, for insertion in a JOIN clause
        // resolved join handles for each table are inserted in pairs
        StringJoiner s = new StringJoiner(" AND ");
        for (String baseJoinHandle : this.joinHandles.keySet()) {
            String remoteJoinHandle = this.joinHandles.get(baseJoinHandle);

            if (baseJoinHandle.contains(",") || remoteJoinHandle.contains(",")) {
                String[] baseJoinHandleItems = baseJoinHandle.split(",");
                String[] remoteJoinHandleItems = remoteJoinHandle.split(",");
                if (baseJoinHandleItems.length != remoteJoinHandleItems.length) {
                    log.error("Error: csep mismatch in join: " + baseJoinHandle + " vs " + remoteJoinHandle);
                } else {
                    StringJoiner o = new StringJoiner(" OR ");
                    for (int i = 0; i < baseJoinHandleItems.length; i++) {
                        o.add("(" + baseJoinHandleItems[i] + " = " + remoteJoinHandleItems[i] + ")");
                    }
                    s.add("(" + o + ")");
                }
            } else {
                s.add("(" + baseJoinHandle + " = " + remoteJoinHandle + ")");
            }
        }
        return s.toString();
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.emptyMap();
    }

    @Override
    public boolean isEmpty() {
        return this.joinHandles.isEmpty();
    }

    @Override
    public int size() {
        return this.joinHandles.size();
    }
}
