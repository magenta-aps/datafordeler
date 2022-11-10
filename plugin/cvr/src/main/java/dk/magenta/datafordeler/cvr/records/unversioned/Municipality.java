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

/**
 * Nontemporal storage of a municipality (code + name).
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + "cvr_municipality", indexes = {
        @Index(name = "cvr_municipality_code", columnList = Municipality.DB_FIELD_CODE),
        @Index(name = "cvr_municipality_name", columnList = Municipality.DB_FIELD_NAME)
})
public class Municipality extends DatabaseEntry {

    private static Logger log = LogManager.getLogger(Municipality.class.getSimpleName());

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "kommunekode";

    @JsonProperty(value = IO_FIELD_CODE)
    @XmlElement(name = IO_FIELD_CODE)
    @Column(nullable = false, unique = false)
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "kommunenavn";

    @JsonProperty(value = IO_FIELD_NAME)
    @XmlElement(name = IO_FIELD_NAME)
    @Column(nullable = false)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public Long getId() {
        return super.getId();
    }


    //----------------------------------------------------

    /**
     * To avoid hitting the database every time we need a reference to a Municipality, we keep
     * a cache of references. This cache is used to get a pointer to the L1 cache, for quick
     * lookup, and avoids the dreaded "duplicate object" issue in Hibernate (where two queries
     * return to equal objects, and saving one makes Hibernate complain that there's another
     * object with this id.
     */
    private static HashMap<Integer, Long> municipalityCache = new HashMap<>();

    static {
        QueryManager.addCache(municipalityCache);
    }

    public static void initializeCache(Session session) {
        if (municipalityCache.isEmpty()) {
            log.debug("Prepopulating municipality cache");
            List<Municipality> municipalities = QueryManager.getAllItems(session, Municipality.class);
            for (Municipality municipality : municipalities) {
                municipalityCache.put(municipality.code, municipality.getId());
            }
            log.debug("municipality cache contains " + municipalityCache.size() + " nodes");
        }
    }

    /**
     * Obtain a Municipality object, either from cache or from database, if it exists, or creates one if it doesn't.
     *
     * @param code
     * @param name
     * @param session
     * @return
     */
    public static Municipality getMunicipality(int code, String name, Session session) {
        if (code > 0) {
            initializeCache(session);
            Municipality municipality = null;
            Long id = municipalityCache.get(code);
            if (id != null) {
                municipality = session.get(Municipality.class, id);
            }
            if (municipality == null) {
                log.debug("Municipality code " + code + " not found in cache, querying database");
                municipality = QueryManager.getItem(session, Municipality.class, Collections.singletonMap(DB_FIELD_CODE, code));
                if (municipality != null && municipality.name.equals("") && name != null) {
                    municipality.setName(name);
                    session.save(municipality);
                }
            }
            if (municipality == null) {
                log.debug("Municipality " + code + " not found; creating new, with name " + name);
                municipality = new Municipality();
                municipality.setCode(code);
                if (name == null) {
                    name = "";
                }
                municipality.setName(name);
                session.save(municipality);
            } else {
                log.debug("Municipality " + code + " found (" + municipality.getId() + ")");
            }
            municipalityCache.put(code, municipality.getId());
            return municipality;

        } else {
            return null;
        }
    }

    public static Municipality getMunicipality(Municipality old, Session session) {
        return getMunicipality(old.getCode(), old.getName(), session);
    }

}
