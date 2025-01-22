package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_subscription_list")
public class PersonSubscription extends DatabaseEntry {

    public static final String TABLE_NAME = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_subscription_list";

    public static final String DB_FIELD_CPR_NUMBER = "personNumber";
    public static final String DB_FIELD_CPR_ASSIGNMENT_STATUS = "personAssignmentStatus";
    public static final String DB_FIELD_CPR_ASSIGNMENT_REASON = "personAssignmentReason";


    @CreationTimestamp
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    @Column(name = DB_FIELD_CPR_NUMBER, length = 10, nullable = false, unique = true)
    private String personNumber;

    @Column(name = DB_FIELD_CPR_ASSIGNMENT_REASON, length = 30, nullable = true, unique = false)
    private String reason;

    @Column(name = DB_FIELD_CPR_ASSIGNMENT_STATUS)
    @Enumerated(EnumType.ORDINAL)
    private PersonSubscriptionAssignmentStatus personAssignmentStatus;

    public String getPersonNumber() {
        return this.personNumber;
    }

    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public PersonSubscriptionAssignmentStatus getAssignment() {
        return personAssignmentStatus;
    }

    public void setAssignment(PersonSubscriptionAssignmentStatus personAssignmentStatus) {
        this.personAssignmentStatus = personAssignmentStatus;
    }
}
