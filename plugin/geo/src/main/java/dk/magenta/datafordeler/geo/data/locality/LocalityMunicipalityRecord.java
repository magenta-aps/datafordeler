package dk.magenta.datafordeler.geo.data.locality;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.MunicipalityReferenceRecord;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityMunicipalityRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityMunicipalityRecord.TABLE_NAME + LocalityMunicipalityRecord.DB_FIELD_ENTITY,
                columnList = LocalityMunicipalityRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityMunicipalityRecord.TABLE_NAME + LocalityMunicipalityRecord.DB_FIELD_CODE,
                columnList = LocalityMunicipalityRecord.DB_FIELD_CODE
        ),
})
public class LocalityMunicipalityRecord extends MunicipalityReferenceRecord<LocalityEntity> {

    public static final String TABLE_NAME = "geo_locality_municipality";

    public LocalityMunicipalityRecord() {
    }

    public LocalityMunicipalityRecord(Integer code) {
        super(code);
    }

}
