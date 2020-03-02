package dk.magenta.datafordeler.cpr.records;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.CprData;
import dk.magenta.datafordeler.cpr.data.CprEffect;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class CprGeoRecord<V extends CprEffect, D extends CprData<V, D>> extends CprDataRecord<V, D> {

    public CprGeoRecord(String line) throws ParseException {
        super(line);
    }

    @Override
    public String getRecordType() {
        return null;
    }

    public abstract boolean populateBaseData(D data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata);

    protected abstract V createEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain);

    protected abstract D createEmptyBaseData();

    public abstract Set<V> getEffects();

    public abstract HashSet<OffsetDateTime> getRegistrationTimestamps();

    public abstract List<CprBitemporality> getBitemporality();

}
