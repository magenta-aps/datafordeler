package dk.magenta.datafordeler.gladdrreg.data.postalcode;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Entity;

import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_postalcode_entity", indexes = {
        @Index(name = "gladdrreg_postalcode_identification", columnList = "identification_id")
})
public class PostalCodeEntity extends Entity<PostalCodeEntity, PostalCodeRegistration> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "PostalCode";

    @Override
    protected PostalCodeRegistration createEmptyRegistration() {
        return new PostalCodeRegistration();
    }
}
