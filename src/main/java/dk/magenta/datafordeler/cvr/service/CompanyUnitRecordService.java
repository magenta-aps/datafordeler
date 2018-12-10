package dk.magenta.datafordeler.cvr.service;

import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrAccessChecker;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.output.UnitRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.query.CompanyUnitRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

@RestController
@RequestMapping("/cvr/unit/1/rest")
public class CompanyUnitRecordService extends FapiBaseService<CompanyUnitRecord, CompanyUnitRecordQuery> {

    @Autowired
    private CvrPlugin cvrPlugin;

    @Autowired
    private UnitRecordOutputWrapper unitRecordOutputWrapper;

    @Autowired
    private MonitorService monitorService;

    public CompanyUnitRecordService() {
        super();
    }

    @PostConstruct
    public void init() {
        this.setOutputWrapper(this.unitRecordOutputWrapper);
        this.monitorService.addAccessCheckPoint("/cvr/unit/1/rest/1234");
        this.monitorService.addAccessCheckPoint("/cvr/unit/1/rest/search?pnummer=1234");
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "unit";
    }

    public static String getDomain() {
        return "https://data.gl/cvr/unit/1/rest/";
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

    protected void applyAreaRestrictionsToQuery(CompanyUnitRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CvrRolesDefinition.READ_CVR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cvrPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }
}
