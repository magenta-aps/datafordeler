package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.RecordSet;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public void closeRegistrations(Session session) {
        HashSet<CvrBitemporalRecord> updated = new HashSet<>();
        HashSet<Class<? extends CvrBitemporalRecord>> omitClasses = new HashSet<>();
        omitClasses.add(CompanyParticipantRelationRecord.class);
        this.traverse(
            cvrRecords -> {
                Class<? extends CvrRecord> recordClass = cvrRecords.getRecordClass();
                if (CvrBitemporalRecord.class.isAssignableFrom(recordClass)) {
                    if (!omitClasses.contains(recordClass)) {
                        Set<CvrBitemporalRecord> bitemporalRecords = cvrRecords.stream()
                                .map((Function<CvrRecord, CvrBitemporalRecord>) cvrRecord -> (CvrBitemporalRecord) cvrRecord)
                                .collect(Collectors.toSet());
                        updated.addAll(CvrBitemporalRecord.closeRegistrations(bitemporalRecords));

                        System.out.println("original: "+cvrRecords);
                        System.out.println("updated : "+bitemporalRecords);
                        /*cvrRecords.clear();
                        for (CvrBitemporalRecord record : bitemporalRecords) {
                            cvrRecords.add(record);
                        }
                        cvrRecords.addAll(bitemporalRecords.stream().map(
                                (Function<CvrBitemporalRecord, CvrRecord>) cvrRecord -> (CvrRecord) cvrRecord
                        ).collect(Collectors.toSet()));*/
                    }
                }
            },
            null,
            true
        );

        for (CvrBitemporalRecord bitemporalRecord : updated) {
            System.out.println("persisting "+bitemporalRecord.getBitemporality());
            if (bitemporalRecord.getId() == null) {
                session.persist(bitemporalRecord);
            } else if (!session.contains(bitemporalRecord)) {
                session.merge(bitemporalRecord);
            }
        }
    }

    public void cleanupBitemporalSets(Session session) {
        this.traverse(
            recordSet -> {
                if (recordSet instanceof BitemporalSet<?, ?>) {
                    CvrRecord first = recordSet.isEmpty() ? null : recordSet.iterator().next();
                    if (first != null) {
                        CvrRecord parent = recordSet.getParent();
                        StringJoiner hql = new StringJoiner(" ");
                        hql.add("from " + first.getClass().getCanonicalName() + " record");
                        hql.add("where record." + recordSet.getField() + " = :parent");
                        String clause = recordSet.getClause();
                        if (clause != null) {
                            for (String subclause : clause.split(" AND ")) {
                                hql.add("AND record." + subclause);
                            }
                        }
                        Query q = session.createQuery(hql.toString(), first.getClass());
                        q.setParameter("parent", parent);
                        List<CvrBitemporalRecord> records = q.getResultList();

                        ListHashMap<Integer, CvrBitemporalRecord> groups = new ListHashMap<>();
                        for (CvrBitemporalRecord record : records) {
                            int hash = record.hashCode();
                            groups.add(hash, record);
                        }
                        for (Integer hash : groups.keySet()) {
                            CvrBitemporalRecord newest = groups.get(hash).stream().sorted(Comparator.comparing(CvrRecord::getDafoUpdated).reversed()).findFirst().get();
                            for (CvrBitemporalRecord record : groups.get(hash)) {
                                if (record != newest) {
                                    session.remove(record);
                                    recordSet.remove(record);
                                }
                            }
                        }
                    }
                }
            },
            null,
            true
        );
    }
}
