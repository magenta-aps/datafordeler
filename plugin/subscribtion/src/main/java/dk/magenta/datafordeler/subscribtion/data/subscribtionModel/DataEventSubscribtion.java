package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;

@Entity
@Table(name = DataEventSubscribtion.TABLE_NAME, indexes = {


})
public class DataEventSubscribtion extends DatabaseEntry {

    public static final String TABLE_NAME = "subscribtion_dataevent";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Company";


    public static final String DB_FIELD_ENTITY = "entity";


    public DataEventSubscribtion() {
    }



    public DataEventSubscribtion(String dataEventId) {
        this.dataEventId = dataEventId;
    }


    @Column(name="dataEventId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String dataEventId;

    public String getDataEventId() {
        return dataEventId;
    }

    public void setDataEventId(String dataEventId) {
        this.dataEventId = dataEventId;
    }


    @ManyToOne
    private Subscriber subscriber;


    @ManyToOne
    private CprList cprList;

    public CprList getCprList() {
        return cprList;
    }

    public void setCprList(CprList cprList) {
        this.cprList = cprList;
    }

    @ManyToOne
    private CvrList cvrList;
    public CvrList getCvrList() {
        return cvrList;
    }

    public void setCvrList(CvrList cprList) {
        this.cvrList = cvrList;
    }


/*
    @Id
    @Column(name="userId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private long userId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        userId = userId;
    }*/





/*
    public static final String DB_FIELD_CPRNUMBERLIST = "cprNumberList";
    public static final String IO_FIELD_CPRNUMBERLIST = "cprNumberList";
    @Column(name = DB_FIELD_CPRNUMBERLIST)
    @JsonProperty(value = DB_FIELD_CPRNUMBERLIST)
    private CprList cprNumberList;

    public CprList getCprNumberList() {
        return this.cprNumberList;
    }

    public void setCprNumberList(CprList cprNumberList) {
        this.cprNumberList = cprNumberList;
    }



    public static final String DB_FIELD_CVRNUMBERLIST = "cvrNumberList";
    public static final String IO_FIELD_CVRNUMBERLIST = "cvrNumberList";
    @Column(name = DB_FIELD_CPRNUMBERLIST)
    @JsonProperty(value = DB_FIELD_CPRNUMBERLIST)
    private CprList cvrNumberList;

    public CprList getCvrNumberList() {
        return this.cvrNumberList;
    }

    public void setCvrNumberList(CprList cvrNumberList) {
        this.cvrNumberList = cvrNumberList;
    }



    public static final String DB_FIELD_PNUMBERLIST = "pNumberList";
    public static final String IO_FIELD_PNUMBERLIST = "pNumberList";
    @Column(name = DB_FIELD_CPRNUMBERLIST)
    @JsonProperty(value = DB_FIELD_CPRNUMBERLIST)
    private CprList pNumberList;

    public CprList getPNumberList() {
        return this.pNumberList;
    }

    public void setPNumberList(CprList pNumberList) {
        this.pNumberList = cvrNumberList;
    }*/

}
