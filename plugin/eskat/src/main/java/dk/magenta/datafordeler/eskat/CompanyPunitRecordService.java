package dk.magenta.datafordeler.eskat;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrAccessChecker;
import dk.magenta.datafordeler.cvr.query.CompanyUnitRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.service.CompanyRecordService;
import dk.magenta.datafordeler.cvr.service.CompanyUnitRecordService;
import dk.magenta.datafordeler.eskat.output.PunitRecordOutputWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@RequestMapping("/eskat/punit/1/rest")
public class CompanyPunitRecordService extends FapiBaseService<CompanyUnitRecord, CompanyUnitRecordQuery> {

    @Autowired
    private CvrPlugin cvrPlugin;

    private Logger log = LogManager.getLogger(CompanyRecordService.class.getCanonicalName());

    @Autowired
    private PunitRecordOutputWrapper punitRecordOutputWrapper;

    public CompanyPunitRecordService() {
        super();
    }

    @PostConstruct
    public void init() {
        this.setOutputWrapper(this.punitRecordOutputWrapper);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "punit";
    }

    public static String getDomain() {
        return "https://data.gl/eskat/unit/1/rest/";
    }

    @Override
    protected Class<CompanyUnitRecord> getEntityClass() {
        return CompanyUnitRecord.class;
    }

    @Override
    public Plugin getPlugin() {
        return this.cvrPlugin;
    }

    @Override
    protected void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException, AccessRequiredException {
        CvrAccessChecker.checkAccess(dafoUserDetails);
    }

    @Override
    protected void sendAsCSV(Stream<CompanyUnitRecord> entities, HttpServletRequest request, HttpServletResponse response) throws IOException, HttpNotFoundException {
    }

    @Override
    protected CompanyUnitRecordQuery getEmptyQuery() {
        return new CompanyUnitRecordQuery();
    }

    @Override
    public List<ResultSet<CompanyUnitRecord>> searchByQuery(CompanyUnitRecordQuery query, Session session) {
        List<ResultSet<CompanyUnitRecord>> allRecords = new ArrayList<>();

        List<ResultSet<CompanyUnitRecord>> localResults = super.searchByQuery(query, session);
        if (!localResults.isEmpty()) {
            log.info("There are "+localResults.size()+" local results");
            allRecords.addAll(localResults);
        }

        HashSet<String> pNumbers = new HashSet<>(query.getPNummer());
        if (!pNumbers.isEmpty()) {
            pNumbers.removeAll(localResults.stream().map(resultset -> Integer.toString(resultset.getPrimaryEntity().getpNumber())).collect(Collectors.toSet()));
            query.setPNummer(pNumbers);
        }
        return allRecords;
    }
}
