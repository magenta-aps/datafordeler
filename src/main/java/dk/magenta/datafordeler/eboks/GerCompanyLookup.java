package dk.magenta.datafordeler.eboks;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.company.CompanyQuery;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GerCompanyLookup {

    public HashSet<CompanyEntity> lookup(Session session, Collection<String> cvrNumbers) {
        CompanyQuery query = new CompanyQuery();
        for (String cvrNumber : cvrNumbers) {
            query.addGerNr(cvrNumber);
        }
        List<CompanyEntity> companyEntities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
        return new HashSet<>(companyEntities);
    }
}
