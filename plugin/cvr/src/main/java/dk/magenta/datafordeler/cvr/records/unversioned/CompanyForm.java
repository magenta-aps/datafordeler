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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static dk.magenta.datafordeler.cvr.records.unversioned.CompanyForm.DB_FIELD_CODE;

/**
 * Nontemporal storage of a company form.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + "cvr_form", indexes = {
        @Index(name = "cvr_company_form_code", columnList = DB_FIELD_CODE)
})
public class CompanyForm extends DatabaseEntry {

    private static Logger log = LogManager.getLogger(CompanyForm.class.getSimpleName());

    //----------------------------------------------------

    public static final String DB_FIELD_DESC_SHORT = "shortDescription";
    public static final String IO_FIELD_DESC_SHORT = "kortBeskrivelse";

    @JsonProperty(value = IO_FIELD_DESC_SHORT)
    @Column(nullable = true)
    private String shortDescription;

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_DESC_LONG = "longDescription";
    public static final String IO_FIELD_DESC_LONG = "langBeskrivelse";

    @JsonProperty(value = IO_FIELD_DESC_LONG)
    @Column(nullable = true)
    private String longDescription;

    public String getLongDescription() {
        return this.longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_CODE = "companyFormCode";
    public static final String IO_FIELD_CODE = "formkode";

    @JsonProperty(value = IO_FIELD_CODE)
    @Column(nullable = false, unique = true)
    private String companyFormCode;

    public String getCompanyFormCode() {
        return companyFormCode;
    }

    public void setCompanyFormCode(String companyFormCode) {
        this.companyFormCode = companyFormCode;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_SOURCE = "responsibleDataSource";
    public static final String IO_FIELD_SOURCE = "dataleverandør";

    @JsonProperty(value = IO_FIELD_SOURCE)
    @Column
    private String responsibleDataSource;

    public String getResponsibleDataSource() {
        return responsibleDataSource;
    }

    public void setResponsibleDataSource(String responsibleDataSource) {
        this.responsibleDataSource = responsibleDataSource;
    }

    //----------------------------------------------------

    /**
     * To avoid hitting the database every time we need a reference to a CompanyForm, we keep
     * a cache of references. This cache is used to get a pointer to the L1 cache, for quick
     * lookup, and avoids the dreaded "duplicate object" issue in Hibernate (where two queries
     * return to equal objects, and saving one makes Hibernate complain that there's another
     * object with this id.
     */
    private static HashMap<String, Long> formCache = new HashMap<>();

    static {
        QueryManager.addCache(formCache);
    }

    public static void initializeCache(Session session) {
        if (formCache.isEmpty()) {
            log.debug("Prepopulating form cache");
            List<CompanyForm> municipalities = QueryManager.getAllItems(session, CompanyForm.class);
            for (CompanyForm form : municipalities) {
                formCache.put(form.companyFormCode, form.getId());
            }
            log.debug("form cache contains " + formCache.size() + " nodes");
        }
    }

    /**
     * Obtain a CompanyForm object, either from cache or from database, if it exists, or creates one if it doesn't.
     *
     * @param code
     * @param shortDescription
     * @param longDescription
     * @param responsibleDataSource
     * @param session
     * @return
     */
    public static CompanyForm getForm(String code, String shortDescription, String longDescription, String responsibleDataSource, Session session) {
        if (code != null) {
            initializeCache(session);
            CompanyForm form = null;
            Long id = formCache.get(code);
            if (id != null) {
                form = session.get(CompanyForm.class, id);
            }
            if (form == null) {
                log.debug("CompanyForm code " + code + " not found in cache, querying database");
                form = QueryManager.getItem(session, CompanyForm.class, Collections.singletonMap(DB_FIELD_CODE, code));
            }
            if (form == null) {
                log.debug("CompanyForm " + code + " not found; creating new");
                form = new CompanyForm();
                form.setCompanyFormCode(code);
                form.setShortDescription(shortDescription);
                form.setLongDescription(longDescription);
                form.setResponsibleDataSource(responsibleDataSource);
                session.persist(form);
            } else {
                log.debug("CompanyForm " + code + " found in cache (" + form.getId() + ")");
            }
            formCache.put(code, form.getId());
            return form;
        } else {
            return null;
        }
    }

    public static CompanyForm getForm(CompanyForm oldForm, Session session) {
        return getForm(oldForm.getCompanyFormCode(), oldForm.getShortDescription(), oldForm.getLongDescription(), oldForm.getResponsibleDataSource(), session);
    }
}
