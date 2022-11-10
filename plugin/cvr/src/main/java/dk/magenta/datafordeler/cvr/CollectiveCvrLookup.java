package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Lookup functionality for finding companys and participants first in local database.
 * If the recuested item could no be found locally try finding it on virk.dk
 */
@Component
public class CollectiveCvrLookup {

    @Autowired
    private DirectLookup directLookup;

    @Autowired
    private CompanyEntityManager companyEntityManager;

    public void setDirectLookup(DirectLookup directLookup) {
        this.directLookup = directLookup;
    }

    public Collection<CompanyRecord> getCompanies(Session session, Collection<String> cvrNumbers) throws DataFordelerException {
        CompanyRecordQuery query = new CompanyRecordQuery();
        query.setParameter(CompanyRecordQuery.CVRNUMMER, cvrNumbers);

        Collection<CompanyRecord> companyRecords = QueryManager.getAllEntitiesAsStream(session, query, CompanyRecord.class).collect(Collectors.toList());

        for (CompanyRecord record : companyRecords) {
            cvrNumbers.remove(Integer.toString(record.getCvrNumber()));
        }

        if (!cvrNumbers.isEmpty()) {
            companyRecords.addAll(companyEntityManager.directLookup(new HashSet<String>(cvrNumbers), null, null));
        }

        return companyRecords;
    }


    public Collection<ParticipantRecord> participantLookup(Session session, Collection<String> unitNumbers) throws DataFordelerException {
        ParticipantRecordQuery query = new ParticipantRecordQuery();
        query.setParameter(ParticipantRecordQuery.UNITNUMBER, unitNumbers);
        Collection<ParticipantRecord> participantRecords = QueryManager.getAllEntities(session, query, ParticipantRecord.class);

        for (ParticipantRecord record : participantRecords) {
            unitNumbers.remove(Long.toString(record.getUnitNumber()));
        }

        if (!unitNumbers.isEmpty()) {
            participantRecords.addAll(directLookup.participantLookup(unitNumbers));
        }

        return participantRecords;
    }


}
