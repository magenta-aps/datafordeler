package dk.magenta.datafordeler.cpr.data.unversioned;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

import javax.xml.bind.annotation.XmlTransient;

@MappedSuperclass
public abstract class UnversionedEntity extends DatabaseEntry {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @XmlTransient
    private Identification identification;

    public Identification getIdentification() {
        return this.identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }
}
