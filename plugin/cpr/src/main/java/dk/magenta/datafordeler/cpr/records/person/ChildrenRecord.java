package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.ChildrenDataRecord;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person children (type 045).
 */
public class ChildrenRecord extends PersonDataRecord {

    private CprBitemporality childrenTemporality;

    public ChildrenRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }
    public ChildrenRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);
        this.childrenTemporality = new CprBitemporality(this.getOffsetDateTime("haenstart-barn"));
    }

    public static final Mapping traditionalMapping = new Mapping();
    static {
        traditionalMapping.add("pnrchild", 14, 10);
        traditionalMapping.add("status", 24, 2);
        traditionalMapping.add("haenstart-barn", 26, 12);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_CHILDREN;
    }


    public String getPnrChild() {
        return this.getString("pnrchild", false);
    }

    public OffsetDateTime getEffectDateTime() {
        return childrenTemporality.effectFrom;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new ChildrenDataRecord(
                this.getString("pnrchild", false), 1
        ).setBitemporality(
                this.childrenTemporality
        ));

        return records;
    }

}
