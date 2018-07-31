package dk.magenta.datafordeler.statistik.queries;

import dk.magenta.datafordeler.core.database.FieldDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonQuery;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBirthData;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.PersonStatusDataRecord;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static dk.magenta.datafordeler.statistik.services.StatisticsService.cprDataOffset;

public class PersonBirthQuery extends PersonStatisticsQuery {

    public PersonBirthQuery(HttpServletRequest request) {
        super(request);
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = super.getLookupDefinition();
        lookupDefinition.setMatchNulls(true);

        /*FieldDefinition fieldDefinition = new FieldDefinition(
                LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_BIRTHTIME,
                null,
                Integer.class,
                LookupDefinition.Operator.NE
        );*/
        /*FieldDefinition fieldDefinition = new FieldDefinition(
                PersonBaseData.DB_FIELD_BIRTH,
                null,
                Integer.class,
                LookupDefinition.Operator.NE
        );*/
        FieldDefinition fieldDefinition;
        if (this.getEffectTimeAfter() == null) {
            fieldDefinition = this.fromPath(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_BIRTHTIME);
        } else {
            fieldDefinition = new FieldDefinition(
                    PersonEntity.DB_FIELD_BIRTHTIME + LookupDefinition.separator + BirthTimeDataRecord.DB_FIELD_BIRTH_DATETIME,
                    this.getEffectTimeAfter().toLocalDateTime(),
                    LocalDateTime.class,
                    LookupDefinition.Operator.GTE
            );
            this.applyRegistrationTimes(fieldDefinition);
            this.applyEffectTimes(fieldDefinition);
        }

        lookupDefinition.put(fieldDefinition);
        return lookupDefinition;
    }

}
