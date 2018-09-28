package dk.magenta.datafordeler.geo.data.postcode;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import dk.magenta.datafordeler.geo.data.SumiffiikEntity;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + PostcodeEntity.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + PostcodeEntity.TABLE_NAME + PostcodeEntity.DB_FIELD_SUMIFFIIK_ID,
                columnList = PostcodeEntity.DB_FIELD_SUMIFFIIK_ID
        ),
})
public class PostcodeEntity extends SumiffiikEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "geo_postcode";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Postcode";

    public PostcodeEntity() {
    }

    public PostcodeEntity(PostcodeRawData record) {
        this.setObjectId(record.getProperties().objectId);
        this.code = record.getProperties().code;
    }

    public static UUID generateUUID(int postcode) {
        String uuidInput = "postcode:"+postcode;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }



    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "kode";
    @Column(name = DB_FIELD_CODE)
    @JsonProperty
    private int code;

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }




    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @OneToMany(mappedBy = PostcodeNameRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME)
    Set<PostcodeNameRecord> name = new HashSet<>();

    public Set<PostcodeNameRecord> getName() {
        return this.name;
    }







    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @OneToMany(mappedBy = PostcodeShapeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SHAPE)
    Set<PostcodeShapeRecord> shape = new HashSet<>();

    public Set<PostcodeShapeRecord> getShape() {
        return this.shape;
    }




    @Override
    public boolean merge(GeoEntity other) {
        return false;
    }

    public void addMonotemporalRecord(GeoMonotemporalRecord record) {
        boolean added = false;
        if (record instanceof PostcodeNameRecord) {
            added = addItem(this.name, record);
        }
        if (record instanceof PostcodeShapeRecord) {
            added = addItem(this.shape, record);
        }
        if (added) {
            record.setEntity(this);
        }
    }

    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }@Override
    public Set<Set<? extends GeoMonotemporalRecord>> getAllRecords() {
        return Collections.emptySet();
    }
}
