package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.cvr.RecordSet;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CvrEntityRecord extends CvrBitemporalRecord implements IdentifiedEntity {

    public abstract List<CvrRecord> getAll();

    public List<CvrRecord> getSince(OffsetDateTime time) {
        ArrayList<CvrRecord> newer = new ArrayList<>();
        for (CvrRecord record : this.getAll()) {
            if (record instanceof CvrBitemporalRecord) {
                OffsetDateTime recordTime = ((CvrBitemporalRecord) record).getLastUpdated();
                if (recordTime == null || time == null || !recordTime.isBefore(time)) {
                    newer.add(record);
                }
            }
        }
        return newer;
    }

    @JsonIgnore
    protected abstract String getDomain();

    public String getFieldName() {
        return "";
    }

    @JsonIgnore
    public abstract Map<String, Object> getIdentifyingFilter();

    public abstract UUID generateUUID();

    public abstract boolean merge(CvrEntityRecord other);

    @Override
    public void save(Session session) {
        System.out.println("Save "+this.getIdentifyingFilter());
        CvrEntityRecord existing = QueryManager.getItem(session, this.getClass(), this.getIdentifyingFilter());
        if (this.identification == null) {
            this.identification = QueryManager.getOrCreateIdentification(session, this.generateUUID(), this.getDomain());
        }

        if (existing != null && existing.merge(this)) {
            session.persist(existing);
        } else {
            session.persist(this);
        }
    }

    public void setDafoUpdateOnTree(OffsetDateTime updateTime) {
        this.setDafoUpdated(updateTime);
        for (CvrRecord record : this.fullSubs()) {
            record.setDafoUpdated(updateTime);
        }
    }

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    protected Identification identification;


    public static final String DB_FIELD_SAMT_ID = "samtId";
    public static final String IO_FIELD_SAMT_ID = "samtId";

    @Column(name = DB_FIELD_SAMT_ID)
    @JsonProperty(value = IO_FIELD_SAMT_ID)
    private long samtId;

    public long getSamtId() {
        return this.samtId;
    }


    public static final String DB_FIELD_REGISTER_ERROR = "registerError";
    public static final String IO_FIELD_REGISTER_ERROR = "fejlRegistreret";

    @Column(name = DB_FIELD_REGISTER_ERROR)
    @JsonProperty(value = IO_FIELD_REGISTER_ERROR)
    private boolean registerError;

    public boolean getRegisterError() {
        return this.registerError;
    }


    public static final String DB_FIELD_DATA_ACCESS = "dataAccess";
    public static final String IO_FIELD_DATA_ACCESS = "dataAdgang";

    @Column(name = DB_FIELD_DATA_ACCESS)
    @JsonProperty(value = IO_FIELD_DATA_ACCESS)
    private long dataAccess;

    public long getDataAccess() {
        return this.dataAccess;
    }


    public static final String DB_FIELD_LOADING_ERROR = "loadingError";
    public static final String IO_FIELD_LOADING_ERROR = "fejlVedIndlaesning";

    @Column(name = DB_FIELD_LOADING_ERROR)
    @JsonProperty(value = IO_FIELD_LOADING_ERROR)
    private boolean loadingError;

    public boolean getLoadingError() {
        return this.loadingError;
    }


    public static final String DB_FIELD_NEAREST_FUTURE_DATE = "nearestFutureDate";
    public static final String IO_FIELD_NEAREST_FUTURE_DATE = "naermesteFremtidigeDato";

    @Column(name = DB_FIELD_NEAREST_FUTURE_DATE)
    @JsonProperty(value = IO_FIELD_NEAREST_FUTURE_DATE)
    private LocalDate nearestFutureDate;

    public void setNearestFutureDate(LocalDate nearestFutureDate) {
        this.nearestFutureDate = nearestFutureDate;
    }

    public LocalDate getNearestFutureDate() {
        return this.nearestFutureDate;
    }


    public static final String DB_FIELD_ERRORDESCRIPTION = "errorDescription";
    public static final String IO_FIELD_ERRORDESCRIPTION = "fejlBeskrivelse";

    @Column(name = DB_FIELD_ERRORDESCRIPTION)
    @JsonProperty(value = IO_FIELD_ERRORDESCRIPTION)
    private String errorDescription;

    public String getErrorDescription() {
        return this.errorDescription;
    }


    public static final String DB_FIELD_EFFECT_AGENT = "effectAgent";
    public static final String IO_FIELD_EFFECT_AGENT = "virkningsAktoer";

    @Column(name = DB_FIELD_EFFECT_AGENT)
    @JsonProperty(value = IO_FIELD_EFFECT_AGENT)
    private String effectAgent;

    public String getEffectAgent() {
        return this.effectAgent;
    }


    @Override
    public Identification getIdentification() {
        return this.identification;
    }

    @Override
    public void forceLoad(Session session) {

    }

    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }

    public void delete(Session session) {
        this.traverse(RecordSet::clear, session::remove);
        session.remove(this);
    }
}
