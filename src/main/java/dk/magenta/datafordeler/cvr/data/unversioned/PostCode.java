package dk.magenta.datafordeler.cvr.data.unversioned;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.QueryManager;
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

import static dk.magenta.datafordeler.cvr.data.unversioned.PostCode.DB_FIELD_CODE;

/**
 * Nontemporal storage of a postcode (code + name)
 */
@Entity
@Table(name = "cvr_postcode", indexes = {
        @Index(name = "companyPostalCode", columnList = DB_FIELD_CODE)
})
public class PostCode extends UnversionedEntity {

    private static Logger log = LogManager.getLogger(PostCode.class.getSimpleName());

    public static final String DB_FIELD_CODE = "postCode";
    public static final String IO_FIELD_CODE = "postnummer";

    @JsonProperty(value = IO_FIELD_CODE)
    @XmlElement(name = IO_FIELD_CODE)
    @Column(nullable = false, unique = false, name = DB_FIELD_CODE)
    private int postCode;

    public int getPostCode() {
        return postCode;
    }

    public void setPostCode(int postCode) {
        this.postCode = postCode;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_DISTRICT = "postDistrict";
    public static final String IO_FIELD_DISTRICT = "postdistrikt";

    @JsonProperty(value = IO_FIELD_DISTRICT)
    @XmlElement(name = IO_FIELD_DISTRICT)
    @Column
    private String postDistrict;

    public String getPostDistrict() {
        return this.postDistrict;
    }

    public void setPostDistrict(String postDistrict) {
        this.postDistrict = postDistrict;
    }

    @JsonProperty
    public Long getId() {
        return super.getId();
    }
    //----------------------------------------------------

    /**
     * To avoid hitting the database every time we need a reference to a PostCode, we keep
     * a cache of references. This cache is used to get a pointer to the L1 cache, for quick
     * lookup, and avoids the dreaded "duplicate object" issue in Hibernate (where two queries
     * return to equal objects, and saving one makes Hibernate complain that there's another
     * object with this id.
     */
    private static HashMap<Integer, Long> postCodeCache = new HashMap<>();

    static {
        QueryManager.addCache(postCodeCache);
    }

    public static void initializeCache(Session session) {
        if (postCodeCache.isEmpty()) {
            log.debug("Prepopulating postCode cache");
            List<PostCode> municipalities = QueryManager.getAllItems(session, PostCode.class);
            for (PostCode postCode : municipalities) {
                postCodeCache.put(postCode.postCode, postCode.getId());
            }
            log.debug("postCode cache contains " + postCodeCache.size() + " nodes");
        }
    }

    /**
     * Obtain a PostCode object, either from cache or from database, if it exists, or creates one if it doesn't.
     * @param code
     * @param postDistrict
     * @param session
     * @return
     */
    public static PostCode getPostcode(int code, String postDistrict, Session session) {
        if (code > 0) {
            initializeCache(session);
            PostCode post = null;
            Long id = postCodeCache.get(code);
            if (id != null) {
                post = session.get(PostCode.class, id);
            }
            if (post == null) {
                log.debug("PostCode code "+code+" not found in cache, querying database");
                post = QueryManager.getItem(session, PostCode.class, Collections.singletonMap(DB_FIELD_CODE, code));
            }
            if (post == null) {
                log.debug("PostCode " + code + " not found; creating new");
                post = new PostCode();
                post.setPostCode(code);
                post.setPostDistrict(postDistrict);
                session.save(post);
            } else {
                log.debug("PostCode " + code + " found (" + post.getId() + ")");
            }
            postCodeCache.put(code, post.getId());
            return post;
        } else {
            return null;
        }
    }

    public static PostCode getPostcode(PostCode old, Session session) {
        return getPostcode(old.getPostCode(), old.getPostDistrict(), session);
    }
}
