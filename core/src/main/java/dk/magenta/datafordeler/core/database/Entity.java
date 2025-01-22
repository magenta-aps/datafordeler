package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.util.Equality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * An Entity represents a top-level item in the database as well as in service
 * output, such as a Person or a Company (to be implemented as subclasses in plugins)
 * Entities usually hold very little data on their own, but links to a series of
 * bitemporality objects (Registrations, and further down Effects), that in turn
 * hold leaf nodes (DataItems) containing the bulk of the associated data.
 */
@MappedSuperclass
@Embeddable
public abstract class Entity<E extends Entity, R extends Registration> extends DatabaseEntry implements IdentifiedEntity {

    @Transient
    private final Logger log;

    protected Logger getLog() {
        return this.log;
    }

    public static final String DB_FIELD_IDENTIFICATION = "identification";
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    @JsonIgnore
    protected Identification identification;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "entity")
    @OrderBy("registrationFrom asc") // Refers to sequenceNumber in Registration class
    @Filters({
            @Filter(name = Registration.FILTER_REGISTRATION_FROM, condition = "(registrationToBefore >= :" + Registration.FILTERPARAM_REGISTRATION_FROM + " OR registrationToBefore is null)"),
            @Filter(name = Registration.FILTER_REGISTRATION_TO, condition = "(registrationFromBefore < :" + Registration.FILTERPARAM_REGISTRATION_TO + ")")
    })
    protected List<R> registrations;

    @Transient
    private UUID uuid;
    @Transient
    private String domain;

    public Entity() {
        this.registrations = new ArrayList<R>();
        this.log = LogManager.getLogger(this.getClass().getCanonicalName());
    }

    public Entity(Identification identification) {
        this();
        this.identification = identification;
    }

    public Entity(UUID uuid, String domain) {
        this();
        this.uuid = uuid;
        this.domain = domain;
    }

    @Override
    @JsonIgnore
    public Identification getIdentification() {
        return this.identification;
    }

    public static final String IO_FIELD_UUID = "uuid";

    @JsonProperty(value = IO_FIELD_UUID)
    public UUID getUUID() {
        if (this.identification != null) {
            return this.identification.getUuid();
        }
        return this.uuid;
    }

    public void setIdentifikation(Identification identification) {
        this.identification = identification;
    }

    @JsonProperty("uuid")
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
        //this.identification.setUuid(uuid);
    }


    public static final String IO_FIELD_DOMAIN = "domain";

    @JsonProperty(value = IO_FIELD_DOMAIN)
    public String getDomain() {
        if (this.identification != null) {
            return this.identification.getDomain();
        }
        return this.domain;
    }


    @JsonProperty("domaene")
    public void setDomain(String domain) {
        this.domain = domain;
        //this.identification.setDomain(domain);
    }

    public static final String IO_FIELD_REGISTRATIONS = "registreringer";

    @OrderBy("registrationFrom asc")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = IO_FIELD_REGISTRATIONS)
    public List<R> getRegistrations() {
        ArrayList<R> registrations = new ArrayList<>(this.registrations);
        Collections.sort(registrations);
        return registrations;
    }

    /**
     * Add a registration to the list of registrations in this entity
     */
    public void addRegistration(R registration) {
        if (!this.registrations.contains(registration)) {
            this.registrations.add(registration);
        }
    }

    /**
     * Finds a registration under this entity that starts at the given OffsetDateTime
     * Returns null if none are found
     */
    public R getRegistration(OffsetDateTime registrationFrom) {
        for (R registration : this.registrations) {
            if (registration.getRegistrationFrom() == null ? registrationFrom == null : registration.getRegistrationFrom().equals(registrationFrom)) {
                return registration;
            }
        }
        return null;
    }

    public R getRegistrationAt(OffsetDateTime time) {
        for (R registration : this.registrations) {
            OffsetDateTime from = registration.getRegistrationFrom();
            OffsetDateTime to = registration.getRegistrationTo();
            if ((from == null || from.isBefore(time) || from.isEqual(time)) && (to == null || to.isAfter(time) || to.isEqual(time))) {
                return registration;
            }
        }
        return null;
    }

    /**
     * Finds a registration under this entity that starts and ends at the given
     * OffsetDateTime pair
     * Returns null if none are found
     */
    public R getRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        for (R registration : this.registrations) {
            if ((registration.getRegistrationFrom() == null ? registrationFrom == null : registration.getRegistrationFrom().equals(registrationFrom)) &&
                    (registration.getRegistrationTo() == null ? registrationTo == null : registration.getRegistrationTo().equals(registrationTo))) {
                return registration;
            }
        }
        return null;
    }

    public List<R> getRegistrationsStartingBetween(OffsetDateTime start, OffsetDateTime end) {
        ArrayList<R> registrations = new ArrayList<R>();
        for (R registration : this.getRegistrations()) {
            OffsetDateTime from = registration.getRegistrationFrom();
            if (from != null && (from.isEqual(start) || from.isAfter(start)) && (end == null || from.isBefore(end))) {
                registrations.add(registration);
            }
        }
        return registrations;
    }

    public void forceLoad(Session session) {
        for (R registration : this.registrations) {
            registration.forceLoad(session);
        }
    }

    protected abstract R createEmptyRegistration();

    /**
     * For import purposes, creates an empty Registration of the associated class,
     * pointing to this Entity
     */
    public final R createRegistration() {
        R registration = this.createEmptyRegistration();
        registration.setEntity(this);
        return registration;
    }


    public void dedupRegistrations(Session session, boolean onlyDetect) {
        ArrayList<R> orderedRegistrations = new ArrayList<>(this.getRegistrations());
        Collections.sort(orderedRegistrations);
        R last = null;
        HashSet<R> toDelete = new HashSet<>();
        for (R registration : orderedRegistrations) {
            if (last != null && last.equalTime(registration)) {
                this.log.info("Registration collision on entity " + this.getId() + ": " + registration.registrationFrom + "|" + registration.registrationTo);
                if (!onlyDetect) {
                    registration.mergeInto(last);
                    toDelete.add(registration);
                    this.registrations.remove(registration);
                }
            } else {
                last = registration;
            }
        }
        if (!onlyDetect) {
            for (R registration : toDelete) {
                session.delete(registration);
            }
        }
    }


    public List<R> findRegistrations(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        // Find/create all necessary Registrations
        Logger log = this.getLog();
        log.debug("Finding registrations within bounds " + registrationFrom + " - " + registrationTo);

        ArrayList<R> registrations = new ArrayList<>();
        ArrayList<R> orderedRegistrations = new ArrayList<>(this.getRegistrations());
        Collections.sort(orderedRegistrations);

        OffsetDateTime latestEnd = OffsetDateTime.MIN;
        for (R existingRegistration : orderedRegistrations) {
            R registration = existingRegistration;
            log.debug("Looking at registration " + registration.getRegistrationFrom() + " - " + registration.getRegistrationTo());

            // There is a gap, or a missing registration at the start
            if (
                    latestEnd != null &&
                            latestEnd.isBefore(nFrom(registration.getRegistrationFrom())) &&
                            (latestEnd.isAfter(nFrom(registrationFrom)) || nFrom(registrationFrom).isBefore(nFrom(registration.getRegistrationFrom())))
            ) {
                log.debug("Gap found at " + latestEnd + " - " + registration.getRegistrationFrom() + ", creating registration");
                R newReg = this.createRegistration();
                newReg.setRegistrationFrom(latestEnd.isEqual(OffsetDateTime.MIN) ? registrationFrom : latestEnd);
                newReg.setRegistrationTo(registration.getRegistrationFrom());
                registrations.add(newReg);
            }
            latestEnd = registration.getRegistrationTo();

            // If the registration starts before our requested start, but ends after, we must do a split and move on with the second part
            if (
                    nFrom(registration.getRegistrationFrom()).isBefore(nFrom(registrationFrom)) &&
                            nTo(registration.getRegistrationTo()).isAfter(nFrom(registrationFrom))
            ) {
                log.debug("Registration straddles our start, split it at " + registrationFrom);
                registration = (R) registration.split(registrationFrom);
                log.debug("Registration is now " + registration.getRegistrationFrom() + " - " + registration.getRegistrationTo());
            }
            // If the registration ends after our requested end, but begins before that, do a split
            if (
                    nFrom(registration.getRegistrationFrom()).isBefore(nTo(registrationTo)) &&
                            nTo(registration.getRegistrationTo()).isAfter(nTo(registrationTo))
            ) {
                log.debug("Registration straddles our end, split it at " + registrationTo);
                registration.split(registrationTo);
                log.debug("Registration is now " + registration.getRegistrationFrom() + " - " + registration.getRegistrationTo());
            }
            // If the registration lies within our bounds, include it
            if (
                    (
                            Equality.equal(registration.getRegistrationFrom(), registrationFrom) || // Check exact match
                                    (nFrom(registration.getRegistrationFrom()).isAfter(nFrom(registrationFrom))) // Check if it lies after our requested start
                    ) && (
                            Equality.equal(registration.getRegistrationTo(), registrationTo) || // Check exact match
                                    (nTo(registration.getRegistrationTo()).isBefore(nTo(registrationTo))) // Check if it lies before our requested end
                    )
            ) {
                log.debug("Registration lies within bounds, adding it to list");
                registrations.add(registration);
            }
        }

        // If the last existing registration ends before our requested end, create a new registration there
        OffsetDateTime requestedEndTime = registrationTo == null ? OffsetDateTime.MAX : registrationTo;
        if (latestEnd != null && latestEnd.isBefore(requestedEndTime)) {
            log.debug(this.getUUID() + " Last registration ended before our requested end, create missing registration at " + (latestEnd.isEqual(OffsetDateTime.MIN) ? registrationFrom : latestEnd) + " - " + registrationTo);
            R registration = this.createRegistration();
            registration.setRegistrationFrom(latestEnd.isEqual(OffsetDateTime.MIN) ? registrationFrom : latestEnd);
            registration.setRegistrationTo(registrationTo);
            registrations.add(registration);
        }

        //orderedRegistrations = new ArrayList<>(this.getRegistrations()); // hvorfor ikke registrations???
        orderedRegistrations = new ArrayList<>(registrations); // hvorfor ikke registrations???
        Collections.sort(orderedRegistrations);
        int seqNo = 0;
        for (R registration : orderedRegistrations) {
            registration.setSequenceNumber(seqNo);
            seqNo++;
        }

        return registrations;
    }

    @JsonIgnore
    public Set<DataItem> getCurrent() {
        OffsetDateTime now = OffsetDateTime.now();
        R registration = this.getRegistrationAt(now);
        HashSet<DataItem> dataItems = new HashSet<>();
        if (registration != null) {
            for (Object effectObject : registration.getEffectsAt(now)) {
                Effect effect = (Effect) effectObject;
                for (Object dataObject : effect.getDataItems()) {
                    DataItem data = (DataItem) dataObject;
                    dataItems.add(data);
                }
            }
        }
        return dataItems;
    }

    private static OffsetDateTime nFrom(OffsetDateTime a) {
        if (a == null) return OffsetDateTime.MIN;
        return a;
    }

    private static OffsetDateTime nTo(OffsetDateTime a) {
        if (a == null) return OffsetDateTime.MAX;
        return a;
    }

    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> set) {
        OffsetDateTime last = OffsetDateTime.MIN;
        IdentifiedEntity newestEntity = null;
        for (IdentifiedEntity item : set) {
            if (item instanceof Entity) {
                Entity entity = (Entity) item;
                for (Object oRegistration : entity.getRegistrations()) {
                    Registration registration = (Registration) oRegistration;
                    OffsetDateTime registrationTime = registration.getLastImportTime();
                    if (registrationTime != null && registrationTime.isAfter(last)) {
                        last = registrationTime;
                        newestEntity = item;
                        break;
                    }
                }
            }
        }
        return newestEntity;
    }

}
