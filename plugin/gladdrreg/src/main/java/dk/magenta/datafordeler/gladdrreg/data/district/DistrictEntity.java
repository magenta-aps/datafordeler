package dk.magenta.datafordeler.gladdrreg.data.district;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Entity;

import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name = "gladdrreg_district_entity", indexes = {
        @Index(name = "gladdrreg_district_identification", columnList = "identification_id")
})
public class DistrictEntity extends Entity<DistrictEntity, DistrictRegistration> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "District";

    @Override
    protected DistrictRegistration createEmptyRegistration() {
        return new DistrictRegistration();
    }
}
