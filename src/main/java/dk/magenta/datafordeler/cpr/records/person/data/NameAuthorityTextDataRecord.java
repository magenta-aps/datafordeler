package dk.magenta.datafordeler.cpr.records.person.data;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Storage for data on a Person's name authority data,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + NameAuthorityTextDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameAuthorityTextDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameAuthorityTextDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameAuthorityTextDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameAuthorityTextDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameAuthorityTextDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameAuthorityTextDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF)
})
public class NameAuthorityTextDataRecord extends AuthorityTextDataRecord<NameAuthorityTextDataRecord> {

    public static final String TABLE_NAME = "cpr_person_name_authoritytext_record";

    public NameAuthorityTextDataRecord() {
    }

    public NameAuthorityTextDataRecord(String text, String correctionMarking) {
        super(text, correctionMarking);
    }



    @Override
    protected NameAuthorityTextDataRecord clone() {
        NameAuthorityTextDataRecord clone = new NameAuthorityTextDataRecord();
        AuthorityTextDataRecord.copy(this, clone);
        return clone;
    }
}
