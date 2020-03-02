package dk.magenta.datafordeler.geo.data.municipality;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.NameRecord;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + MunicipalityNameRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + MunicipalityNameRecord.TABLE_NAME + MunicipalityNameRecord.DB_FIELD_ENTITY,
                columnList = MunicipalityNameRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + MunicipalityNameRecord.TABLE_NAME + MunicipalityNameRecord.DB_FIELD_NAME,
                columnList = MunicipalityNameRecord.DB_FIELD_NAME
        ),
})
public class MunicipalityNameRecord extends NameRecord<GeoMunicipalityEntity> {

    public static final String TABLE_NAME = "geo_municipality_name";

    public MunicipalityNameRecord() {
    }

    public MunicipalityNameRecord(String name) {
        super(name);
    }

}
