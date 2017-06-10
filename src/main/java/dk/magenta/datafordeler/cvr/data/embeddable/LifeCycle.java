package dk.magenta.datafordeler.cvr.data.embeddable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Date;

/**
 * Created by jubk on 04-03-2015.
 */
@Embeddable
public class LifeCycle {
    @Column(nullable = true)
    private Date startDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    //----------------------------------------------------

    @Column(nullable = true)
    private Date endDate;

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

/*
    public boolean equals(Object otherObject) {
        if (otherObject == null || otherObject.getClass() != LifeCycle.class) {
            return false;
        }
        return this.equals((LifeCycle) otherObject);
    }
    public boolean equals(LifeCycle otherLifecycle) {
        return Util.compare(this.startDate, otherLifecycle.getStartDate()) &&
                Util.compare(this.endDate, otherLifecycle.getEndDate());
    }*/
}
