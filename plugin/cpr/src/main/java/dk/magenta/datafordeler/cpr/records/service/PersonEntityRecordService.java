package dk.magenta.datafordeler.cpr.records.service;

import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.cpr.CprAccessChecker;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.output.PersonRecordOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/cpr/person/1/rest")
public class PersonEntityRecordService extends FapiBaseService<PersonEntity, PersonRecordQuery> {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private CprPlugin cprPlugin;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private PersonRecordOutputWrapper personRecordOutputWrapper;

    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/cpr/person/1/rest/1234");
        this.monitorService.addAccessCheckPoint("/cpr/person/1/rest/search?personnummer=1234");
        this.setOutputWrapper(this.personRecordOutputWrapper);
    }

    @Override
    protected OutputWrapper.Mode getDefaultMode() {
        return OutputWrapper.Mode.DATAONLY;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "person";
    }

    @Override
    protected Class<PersonEntity> getEntityClass() {
        return PersonEntity.class;
    }

    @Override
    public Plugin getPlugin() {
        return this.cprPlugin;
    }

    @Override
    protected void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException, AccessRequiredException {
        CprAccessChecker.checkAccess(dafoUserDetails);
    }

    @Override
    protected PersonRecordQuery getEmptyQuery() {
        //return new PersonRecordQuery();
        PersonRecordQuery query = new PersonRecordQuery();

        query.addExtraJoin("LEFT JOIN cpr_person.address cpr_person__address");

        Plugin geoPlugin = pluginManager.getPluginByName("geo");

        /*
        EntityManager accessAddressManager = geoPlugin.getEntityManager("AccessAddress");
        // Get an accessaddressquery with joins on the entities we are interested in
        BaseQuery accessAddressQuery = accessAddressManager.getQuery("municipality", "road", "postcode", "locality");
        // Specify how we join with the accessaddressquery
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put("municipalitycode", "municipalitycode");
        joinHandles.put("roadcode", "roadcode");
        joinHandles.put("bnr_or_housenumber", "bnr_or_housenumber");
        query.addRelated(accessAddressQuery, joinHandles);
        */

        HashMap<String, String> handles = new HashMap<>();
        handles.put("municipalitycode", "cpr_person__address.municipalityCode");
        handles.put("roadcode", "cpr_person__address.roadCode");
        handles.put("housenumber", "cpr_person__address.houseNumber");
        handles.put("bnr", "cpr_person__address.buildingNumber");

        query.addExtraJoin(geoPlugin.getJoinString(handles));
        query.addExtraTables(geoPlugin.getJoinClassAliases());
        
        query.addExtraJoin(cprPlugin.getJoinString(handles));
        query.addExtraTables(cprPlugin.getJoinClassAliases());

        return query;
    }

    @Override
    protected void applyAreaRestrictionsToQuery(PersonRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CprRolesDefinition.READ_CPR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cprPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }

    @Override
    protected void sendAsCSV(Stream<PersonEntity> stream, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, HttpNotFoundException {

    }

}
