package dk.magenta.datafordeler.cvr.data.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.cvr.data.CvrEntity;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.UUID;

import static dk.magenta.datafordeler.cvr.data.company.CompanyEntity.DB_FIELD_CVR;
import static dk.magenta.datafordeler.cvr.data.company.CompanyEntity.IO_FIELD_CVR;

/**
 * An Entity representing a company. Bitemporal data is structured as described
 * in {@link dk.magenta.datafordeler.core.database.Entity}
 */
@javax.persistence.Entity
@Table(name="cvr_company_entity", indexes = {
        @Index(name = "cvr_company_identification", columnList = "identification_id", unique = true),
        @Index(name = "cvr_company_cvrnumber", columnList = DB_FIELD_CVR, unique = true)
})
@JsonPropertyOrder({IO_FIELD_CVR, "uuid", "domain"})
public class CompanyEntity extends CvrEntity<CompanyEntity, CompanyRegistration> {

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "virksomhed";

    public CompanyEntity() {
    }

    public CompanyEntity(Identification identification) {
        super(identification);
    }

    public CompanyEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    @Override
    public CompanyRegistration createEmptyRegistration() {
        return new CompanyRegistration();
    }

    public static final String DB_FIELD_CVR = "cvrNumber";
    public static final String IO_FIELD_CVR = "cvrnummer";
    @Column(name = DB_FIELD_CVR)
    private int cvrNumber;

    @JsonProperty(value = IO_FIELD_CVR)
    @XmlElement(name = IO_FIELD_CVR)
    public int getCvrNumber() {
        return cvrNumber;
    }

    public void setCvrNumber(int cvrNumber) {
        this.cvrNumber = cvrNumber;
    }

    public static UUID generateUUID(int cvrNumber) {
        String uuidInput = "company:"+cvrNumber;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }
}
