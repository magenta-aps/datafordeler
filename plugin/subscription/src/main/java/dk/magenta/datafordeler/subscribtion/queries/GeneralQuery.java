package dk.magenta.datafordeler.subscribtion.queries;

import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import java.time.OffsetDateTime;


public class GeneralQuery extends PersonRecordQuery {



    public static PersonRecordQuery getPersonQuery(String queryType, OffsetDateTime timestampGTE, OffsetDateTime timestampLTE) {
        switch(queryType) {
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
        switch(queryType) {
            case "anything":
                query.setDataEventTimeAfter(timestampGTE);
                query.setDataEventTimeBefore(timestampLTE);
                return query;
            default:
                query.setDataEvent(queryType);
                query.setDataEventTimeAfter(timestampGTE);
                query.setDataEventTimeBefore(timestampLTE);
                return query;
        }
    }


    public static String getQueryPersonValueObjectFromIdInEvent(String tableName) {
        switch(tableName) {
            case AddressConameDataRecord.TABLE_NAME:
                return "SELECT p FROM AddressConameDataRecord p WHERE p.id IN :id";
            case AddressDataRecord.TABLE_NAME:
                return "SELECT p FROM AddressDataRecord p WHERE p.id IN :id";
            case AddressNameDataRecord.TABLE_NAME:
                return "SELECT p FROM AddressNameDataRecord p WHERE p.id IN :id";
            case CitizenshipDataRecord.TABLE_NAME:
                return "SELECT p FROM CitizenshipDataRecord p WHERE p.id IN :id";
            case CivilStatusDataRecord.TABLE_NAME:
                return "SELECT p FROM CivilStatusDataRecord p WHERE p.id IN :id";
            case NameDataRecord.TABLE_NAME:
                return "SELECT p FROM NameDataRecord p WHERE p.id IN :id";
            default:
                return "";
        }
    }



}
