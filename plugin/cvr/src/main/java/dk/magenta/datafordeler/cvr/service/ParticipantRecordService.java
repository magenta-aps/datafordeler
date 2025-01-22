package dk.magenta.datafordeler.cvr.service;

import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrAccessChecker;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.output.ParticipantRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/cvr/participant/1/rest")
public class ParticipantRecordService extends FapiBaseService<ParticipantRecord, ParticipantRecordQuery> {

    @Autowired
    private CvrPlugin cvrPlugin;

    private final Logger log = LogManager.getLogger(CompanyRecordService.class.getCanonicalName());

    @Autowired
    private ParticipantRecordOutputWrapper participantRecordOutputWrapper;

    @Autowired
    private MonitorService monitorService;

    public ParticipantRecordService() {
        super();
    }

    @PostConstruct
    public void init() {
        this.setOutputWrapper(this.participantRecordOutputWrapper);
        this.monitorService.addAccessCheckPoint("/cvr/participant/1/rest/1234");
        this.monitorService.addAccessCheckPoint("/cvr/participant/1/rest/search?deltagernummer=1234");
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "participant";
    }

    public static String getDomain() {
        return "https://data.gl/cvr/participant/1/rest/";
    }

    @Override
    protected Class<ParticipantRecord> getEntityClass() {
        return ParticipantRecord.class;
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
    protected void sendAsCSV(Stream<ParticipantRecord> entities, HttpServletRequest request, HttpServletResponse response) throws IOException, HttpNotFoundException {
    }

    @Override
    protected ParticipantRecordQuery getEmptyQuery() {
        return new ParticipantRecordQuery();
    }

    protected void applyAreaRestrictionsToQuery(ParticipantRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CvrRolesDefinition.READ_CVR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cvrPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }

    @Override
    public List<ResultSet<ParticipantRecord>> searchByQuery(ParticipantRecordQuery query, Session session) {
        List<ResultSet<ParticipantRecord>> allRecords = new ArrayList<>();

        List<ResultSet<ParticipantRecord>> localResults = super.searchByQuery(query, session);
        if (!localResults.isEmpty()) {
            log.info("There are " + localResults.size() + " local results");
            allRecords.addAll(localResults);
        }

        HashSet<String> eNumbers = new HashSet<>(query.getParameter(ParticipantRecordQuery.UNITNUMBER));
        if (!eNumbers.isEmpty()) {
            eNumbers.removeAll(localResults.stream().map(resultset -> Long.toString(resultset.getPrimaryEntity().getUnitNumber())).collect(Collectors.toSet()));
            query.setParameter(ParticipantRecordQuery.UNITNUMBER, eNumbers);
        }

        return allRecords;
    }
}
