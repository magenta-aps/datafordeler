package dk.magenta.datafordeler.geo.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SumiffiikEntity extends GeoEntity implements IdentifiedEntity {

    public SumiffiikEntity() {
    }

    public SumiffiikEntity(SumiffiikRawData record) {
        this.setSumiffiikId(record.getProperties().sumiffiikId);
        this.setObjectId(record.getProperties().objectId);
        this.setCreator(record.getProperties().creator);
        this.setCreationDate(record.getProperties().creationDate);
    }


    public static final String DB_FIELD_OBJECT_ID = "objectId";
    @Column(name = DB_FIELD_OBJECT_ID)
    @JsonProperty
    private int objectId;

    public int getObjectId() {
        return this.objectId;
    }

    @JsonProperty(value = "OBJECTID")
    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }


    public static final String DB_FIELD_SUMIFFIIK_ID = "sumiffiikId";
    @Column(name = DB_FIELD_SUMIFFIIK_ID)
    @JsonProperty
    private String sumiffiikId;

    @JsonProperty(value = "sumiffiikId")
    public String getSumiffiikId() {
        return this.sumiffiikId;
    }

    public void setSumiffiikId(String sumiffiikId) {
        this.sumiffiikId = sumiffiikId;
    }

    @JsonProperty(value="globalId")
    public String getGlobalId() {
        return "{" + this.getUUID().toString() + "}";
    }

}
