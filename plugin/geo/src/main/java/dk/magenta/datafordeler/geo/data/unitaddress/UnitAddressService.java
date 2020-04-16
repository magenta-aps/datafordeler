package dk.magenta.datafordeler.geo.data.unitaddress;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

@RestController("GeoUnitAddressService")
@RequestMapping("/geo/unitaddress/1/rest")
public class UnitAddressService extends FapiBaseService<UnitAddressEntity, UnitAddressQuery> {

    @Autowired
    private GeoPlugin geoPlugin;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private UnitAddressOutputWrapper unitAddressOutputWrapper;

    @PostConstruct
    public void init() {
        //this.monitorService.addAccessCheckPoint("/geo/unitaddress/1/rest/1234");
        //this.monitorService.addAccessCheckPoint("/geo/unitaddress/1/rest/search?bnr=1234");

        this.setOutputWrapper(this.unitAddressOutputWrapper);
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
        return "unitaddress";
    }

    @Override
    protected Class<UnitAddressEntity> getEntityClass() {
        return UnitAddressEntity.class;
    }

    @Override
    public Plugin getPlugin() {
        return this.geoPlugin;
    }

    @Override
    protected void checkAccess(DafoUserDetails dafoUserDetails) {
        // All have access
    }

    @Override
    protected UnitAddressQuery getEmptyQuery() {
        return new UnitAddressQuery();
    }

    @Override
    protected void sendAsCSV(Stream<UnitAddressEntity> stream, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    }

    @Override
    protected void applyAreaRestrictionsToQuery(UnitAddressQuery query, DafoUserDetails user) {
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
