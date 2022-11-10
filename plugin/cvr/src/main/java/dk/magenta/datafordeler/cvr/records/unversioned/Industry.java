package dk.magenta.datafordeler.cvr.records.unversioned;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static dk.magenta.datafordeler.cvr.records.unversioned.Industry.DB_FIELD_CODE;

/**
 * Nontemporal storage of an industry (code + name).
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + "cvr_industry", indexes = {
        @Index(name = "industryCode", columnList = DB_FIELD_CODE)
})
public class Industry extends DatabaseEntry {

    private static Logger log = LogManager.getLogger(Industry.class.getSimpleName());

    public static final String DB_FIELD_CODE = "industryCode";
    public static final String IO_FIELD_CODE = "branchekode";

    @JsonProperty(value = IO_FIELD_CODE)
    @XmlElement(name = IO_FIELD_CODE)
    @Column(nullable = false, unique = false, name = DB_FIELD_CODE)
    private String industryCode;

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    //----------------------------------------------------


    public static final String DB_FIELD_NAME = "industryText";
    public static final String IO_FIELD_NAME = "branchetekst";

    @JsonProperty(value = IO_FIELD_NAME)
    @XmlElement(name = IO_FIELD_NAME)
    @Column
    private String industryText;

    public String getIndustryText() {
        return this.industryText;
    }

    public void setIndustryText(String industryText) {
        this.industryText = industryText;
    }

    //----------------------------------------------------

    /**
     * To avoid hitting the database every time we need a reference to an Industry, we keep
     * a cache of references. This cache is used to get a pointer to the L1 cache, for quick
     * lookup, and avoids the dreaded "duplicate object" issue in Hibernate (where two queries
     * return to equal objects, and saving one makes Hibernate complain that there's another
     * object with this id.
     */
    private static HashMap<String, Long> industryCache = new HashMap<>();

    static {
        QueryManager.addCache(industryCache);
    }

    public static void initializeCache(Session session) {
        if (industryCache.isEmpty()) {
            log.debug("Prepopulating industry cache");
            List<Industry> industries = QueryManager.getAllItems(session, Industry.class);
            for (Industry industry : industries) {
                industryCache.put(industry.industryCode, industry.getId());
            }
            log.debug("industry cache contains " + industryCache.size() + " nodes");
        }
    }

    /**
     * Obtain an Industry object, either from cache or from database, if it exists, or creates one if it doesn't.
     *
     * @param industryCode
     * @param industryText
     * @param session
     * @return
     */
    public static Industry getIndustry(String industryCode, String industryText, Session session) {
        if (industryCode != null) {
            initializeCache(session);
            Industry industry = null;
            Long id = industryCache.get(industryCode);
            if (id != null) {
                industry = session.get(Industry.class, id);
            }
            if (industry == null) {
                log.debug("Industry code " + industryCode + " not found in cache, querying database");
                industry = QueryManager.getItem(session, Industry.class, Collections.singletonMap(DB_FIELD_CODE, industryCode));
            }
            if (industry == null) {
                log.debug("Industry " + industryCode + " not found; creating new");
                industry = new Industry();
                industry.setIndustryCode(industryCode);
                industry.setIndustryText(industryText);
                session.save(industry);
            } else {
                log.debug("Industry " + industryCode + " found in cache (" + industry.getId() + ")");
            }
            industryCache.put(industryCode, industry.getId());
            return industry;
        } else {
            return null;
        }
    }

}
