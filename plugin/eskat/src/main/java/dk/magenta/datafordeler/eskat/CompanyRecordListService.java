package dk.magenta.datafordeler.eskat;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.cvr.access.CvrAccessChecker;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.service.CompanyRecordService;
import dk.magenta.datafordeler.eskat.output.EskatRecordOutputWrapper;
import dk.magenta.datafordeler.eskat.query.EskatCompanyRecordQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/eskat/company/1/rest")
public class CompanyRecordListService extends CompanyRecordService {

    @Autowired
    private EskatRecordOutputWrapper companyRecordOutputWrapper;

    public CompanyRecordListService() {
        super();
    }

    @PostConstruct
    public void init() {
        this.setOutputWrapper(this.companyRecordOutputWrapper);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "company";
    }

    public static String getDomain() {
        return "https://data.gl/eskat/company/1/rest/";
    }

    @Override
    protected Class<CompanyRecord> getEntityClass() {
        return CompanyRecord.class;
    }

    @Override
    protected void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException, AccessRequiredException {
        CvrAccessChecker.checkAccess(dafoUserDetails);
    }

    @Override
    protected CompanyRecordQuery getEmptyQuery() {
        return new EskatCompanyRecordQuery();
    }

    @Override
    protected void sendAsCSV(Stream<CompanyRecord> entities, HttpServletRequest request, HttpServletResponse response) throws IOException, HttpNotFoundException {
    }


    @Override
    public List<ResultSet<CompanyRecord>> searchByQuery(CompanyRecordQuery query, Session session) {
        List<ResultSet<CompanyRecord>> allRecords = new ArrayList<>();

        query.setEffectToAfter(BaseQuery.ALWAYSTIMEINTERVAL);

        List<ResultSet<CompanyRecord>> localResults = super.searchByQuery(query, session);
        if (!localResults.isEmpty()) {
            //log.info("There are "+localResults.size()+" local results");
            allRecords.addAll(localResults);
        }

        HashSet<String> cvrNumbers = new HashSet<>(query.getParameter(CompanyRecordQuery.CVRNUMMER));
        if (!cvrNumbers.isEmpty()) {
            cvrNumbers.removeAll(allRecords.stream().map(resultset -> Integer.toString(resultset.getPrimaryEntity().getCvrNumber())).collect(Collectors.toSet()));
            query.setParameter(CompanyRecordQuery.CVRNUMMER, cvrNumbers);
        }
        return allRecords;
    }

}
