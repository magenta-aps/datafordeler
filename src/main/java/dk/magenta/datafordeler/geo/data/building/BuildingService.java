package dk.magenta.datafordeler.geo.data.building;

import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.geo.GeoAreaRestrictionDefinition;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.GeoRolesDefinition;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

@RestController("BuildingService")
@RequestMapping("/geo/building/1/rest")
public class BuildingService extends FapiBaseService<BuildingEntity, PostcodeQuery> {

    @Autowired
    private GeoPlugin geoPlugin;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private BuildingOutputWrapper buildingOutputWrapper;

    @PostConstruct
    public void init() {
        //this.monitorService.addAccessCheckPoint("/geo/building/1/rest/1234");
        //this.monitorService.addAccessCheckPoint("/geo/building/1/rest/search?bnr=1234");

        this.setOutputWrapper(this.buildingOutputWrapper);
    }

    @Override
    protected OutputWrapper.Mode getDefaultMode() {
        return OutputWrapper.Mode.DRV;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "building";
    }

    @Override
    protected Class<BuildingEntity> getEntityClass() {
        return BuildingEntity.class;
    }

    @Override
    public Plugin getPlugin() {
        return this.geoPlugin;
    }

    @Override
    protected void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException, AccessRequiredException {
        // All have access
    }

    @Override
    protected PostcodeQuery getEmptyQuery() {
        return new PostcodeQuery();
    }

    @Override
    protected void sendAsCSV(Stream<BuildingEntity> stream, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, HttpNotFoundException {

    }

    @Override
    protected void applyAreaRestrictionsToQuery(PostcodeQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(GeoRolesDefinition.READ_GEO_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.geoPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(GeoAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }

}
