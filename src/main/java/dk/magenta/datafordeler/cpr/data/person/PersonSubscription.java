package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_subscription")
public class PersonSubscription extends DatabaseEntry {

    @Column(length = 10, nullable = false, unique = true)
    private String personNumber;

    public String getPersonNumber() {
        return this.personNumber;
    }

    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }
}
