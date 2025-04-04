package dk.magenta.datafordeler.cvr.records.unversioned;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static dk.magenta.datafordeler.cvr.records.unversioned.CompanyStatus.DB_FIELD_NAME;

/**
 * Nontemporal storage of a company status.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + "cvr_status", indexes = {@Index(name = "cvrStatus", columnList = DB_FIELD_NAME)})
public class CompanyStatus extends DatabaseEntry {

    private static Logger log = LogManager.getLogger(CompanyStatus.class.getSimpleName());

    //----------------------------------------------------

    public static final String DB_FIELD_NAME = "status";
    public static final String IO_FIELD_NAME = "status";

    @JsonProperty(value = IO_FIELD_NAME)
    @XmlElement(name = IO_FIELD_NAME)
    @Column(nullable = true, unique = true)
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //--------------------------------------------------------------------------

    /**
     * To avoid hitting the database every time we need a reference to a CompanyStatus, we keep
     * a cache of references. This cache is used to get a pointer to the L1 cache, for quick
     * lookup, and avoids the dreaded "duplicate object" issue in Hibernate (where two queries
     * return to equal objects, and saving one makes Hibernate complain that there's another
     * object with this id.
     */
    private static HashMap<String, Long> statusCache = new HashMap<>();

    private static boolean prepopulated = false;

    static {
        QueryManager.addCache(statusCache);
    }

    public static void initializeCache(Session session) {
        if (statusCache.isEmpty()) {
            log.debug("Prepopulating status cache");
            List<CompanyStatus> municipalities = QueryManager.getAllItems(session, CompanyStatus.class);
            for (CompanyStatus status : municipalities) {
                statusCache.put(status.status, status.getId());
            }
            log.debug("status cache contains " + statusCache.size() + " nodes");
        }
    }

    /**
     * Obtain a CompanyStatus object, either from cache or from database, if it exists, or creates one if it doesn't.
     *
     * @param statusText
     * @param session
     * @return
     */
    public static CompanyStatus getStatus(String statusText, Session session) {
        if (statusText != null) {
            initializeCache(session);
            CompanyStatus status = null;
            Long id = statusCache.get(statusText);
            if (id != null) {
                status = session.get(CompanyStatus.class, id);
            }
            if (status == null) {
                log.debug("CompanyStatus code " + statusText + " not found in cache, querying database");
                status = QueryManager.getItem(session, CompanyStatus.class, Collections.singletonMap(DB_FIELD_NAME, statusText));
            }
            if (status == null) {
                log.debug("CompanyStatus " + statusText + " not found; creating new");
                status = new CompanyStatus();
                status.setStatus(statusText);
                session.persist(status);
            } else {
                log.debug("CompanyStatus " + statusText + " found in cache (" + status.getId() + ")");
            }
            statusCache.put(statusText, status.getId());
            return status;
        } else {
            return null;
        }
    }
}
