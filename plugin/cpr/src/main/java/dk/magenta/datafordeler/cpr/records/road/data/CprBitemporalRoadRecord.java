package dk.magenta.datafordeler.cpr.records.road.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;

import jakarta.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class CprBitemporalRoadRecord<S extends CprBitemporalRoadRecord<S>> extends CprBitemporalRecord<RoadEntity, S> {

    public static final String DB_FIELD_ENTITY = CprBitemporalRecord.DB_FIELD_ENTITY;

    public abstract S clone();

    public boolean updateBitemporalityByCloning() {
        return false;
    }

    @JsonIgnore
    public OffsetDateTime getRegistrationTo() {
        return super.getRegistrationTo();
    }

    @JsonIgnore
    public S getCorrectionof() {
        return super.getCorrectionof();
    }

    @JsonIgnore
    public String getOrigin() {
        return super.getOrigin();
    }

    @JsonIgnore
    public int geCnt() {
        return super.getCnt();
    }

    @JsonIgnore
    public boolean isUndone() {
        return super.isUndone();
    }

    @JsonIgnore
    public LocalDate getOriginDate() {
        return super.getOriginDate();
    }

    @JsonIgnore
    public boolean isCorrection() {
        return super.isCorrection();
    }

    @JsonIgnore
    public boolean isTechnicalCorrection() {
        return super.isTechnicalCorrection();
    }

    @JsonIgnore
    public boolean isUndo() {
        return super.isUndo();
    }

    @JsonIgnore
    public Long getReplacesId() {
        return super.getReplacesId();
    }

    @JsonIgnore
    public Long getReplacedById() {
        return super.getReplacedById();
    }

    @JsonIgnore
    public int getCnt() {
        return super.getCnt();
    }

}
