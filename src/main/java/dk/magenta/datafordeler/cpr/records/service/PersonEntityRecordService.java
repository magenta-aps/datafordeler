package dk.magenta.datafordeler.cpr.records.service;

import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
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
import java.util.Collection;

@RestController
@RequestMapping("/cpr/person/1/rest")
public class PersonEntityRecordService extends FapiService<PersonEntity, PersonRecordQuery> {

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
        return OutputWrapper.Mode.LEGACY;
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
        return new PersonRecordQuery();
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

}
