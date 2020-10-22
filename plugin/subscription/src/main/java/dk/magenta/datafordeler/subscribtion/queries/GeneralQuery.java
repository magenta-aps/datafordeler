package dk.magenta.datafordeler.subscribtion.queries;

import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
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
                PersonDataeventQuery addressEventQuery = new PersonDataeventQuery();
                addressEventQuery.setDataEvent("cpr_person_address_record");
                //addressEventQuery.setRecordAfter(timestamp);
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


}
