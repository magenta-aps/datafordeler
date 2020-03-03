package dk.magenta.datafordeler.geo.data;

import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;

import java.util.List;

public abstract class RawData {

    public abstract List<GeoMonotemporalRecord> getMonotemporalRecords();

}
