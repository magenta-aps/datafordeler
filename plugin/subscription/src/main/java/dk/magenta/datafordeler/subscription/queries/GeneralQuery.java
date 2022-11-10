package dk.magenta.datafordeler.subscription.queries;

import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.BaseNameRecord;
import dk.magenta.datafordeler.cvr.records.StatusRecord;

import java.time.OffsetDateTime;


public class GeneralQuery extends PersonRecordQuery {


    public static PersonRecordQuery getPersonQuery(String queryType, OffsetDateTime timestampGTE, OffsetDateTime timestampLTE) {
        switch (queryType) {
            case "anything":
                PersonRecordQuery personQuery = new PersonRecordQuery();
                personQuery.setRecordAfter(timestampGTE);
                return personQuery;
            default:
                PersonRecordQuery addressEventQuery = new PersonRecordQuery();
                addressEventQuery.setDataEvent(queryType);
                addressEventQuery.setDataEventTimeAfter(timestampGTE);//TODO: consider joining this on DB-level
                addressEventQuery.setDataEventTimeBefore(timestampLTE);
                return addressEventQuery;
        }
    }

    public static CompanyRecordQuery getCompanyQuery(String queryType, OffsetDateTime timestampGTE, OffsetDateTime timestampLTE) {
        CompanyRecordQuery query = new CompanyRecordQuery();
        query.setDataEventTimeAfter(timestampGTE);
        query.setDataEventTimeBefore(timestampLTE);
        switch (queryType) {
            case "anything":
                return query;
            default:
                query.setDataEvent(queryType);
                return query;
        }
    }


    public static String getQueryPersonValueObjectFromIdInEvent(String tableName) {
        switch (tableName) {
            case AddressConameDataRecord.TABLE_NAME:
                return "SELECT p FROM " + AddressConameDataRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            case AddressDataRecord.TABLE_NAME:
                return "SELECT p FROM " + AddressDataRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            case AddressNameDataRecord.TABLE_NAME:
                return "SELECT p FROM " + AddressNameDataRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            case CitizenshipDataRecord.TABLE_NAME:
                return "SELECT p FROM " + CitizenshipDataRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            case CivilStatusDataRecord.TABLE_NAME:
                return "SELECT p FROM " + CivilStatusDataRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            case NameDataRecord.TABLE_NAME:
                return "SELECT p FROM " + NameDataRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            default:
                return "";
        }
    }

    public static String getQueryCompanyValueObjectFromIdInEvent(String tableName) {
        switch (tableName) {
            case BaseNameRecord.TABLE_NAME:
                return "SELECT p FROM " + BaseNameRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            case AddressRecord.TABLE_NAME:
                return "SELECT p FROM " + AddressRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            case StatusRecord.TABLE_NAME:
                return "SELECT p FROM " + StatusRecord.class.getCanonicalName() + " p WHERE p.id IN :id";
            default:
                return "";
        }
    }


}
