package dk.magenta.datafordeler.cpr.data.residence;

import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.cpr.data.CprQuery;
import dk.magenta.datafordeler.cpr.data.residence.data.ResidenceBaseData;

import java.util.*;

/**
 * Container for a query for Residences, defining fields and database lookup
 */
public class ResidenceQuery extends CprQuery<ResidenceEntity> {

    public static final String KOMMUNEKODE = ResidenceBaseData.IO_FIELD_MUNICIPALITY_CODE;
    public static final String VEJKODE = ResidenceBaseData.IO_FIELD_ROAD_CODE;
    public static final String HUSNUMMER = ResidenceBaseData.IO_FIELD_HOUSENUMBER;
    public static final String ETAGE = ResidenceBaseData.IO_FIELD_FLOOR;
    public static final String SIDE_DOER = ResidenceBaseData.IO_FIELD_DOOR;


    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : new String[]{
                KOMMUNEKODE, VEJKODE, HUSNUMMER,
                ETAGE, SIDE_DOER
        }) {
            map.put(key, this.getParameter(key));
        }
        return map;
    }

    @Override
    public void setFromParameters(ParameterMap parameters) throws InvalidClientInputException {
        for (String key : new String[]{KOMMUNEKODE, VEJKODE}) {
            ensureNumeric(key, parameters.getI(key));
        }
        for (String key : new String[]{
                KOMMUNEKODE, VEJKODE, HUSNUMMER, ETAGE, SIDE_DOER,
        }) {
            this.setParameter(key, parameters.getI(key));
        }
    }

    @Override
    public String getEntityClassname() {
        return ResidenceEntity.class.getCanonicalName();
    }

    @Override
    public String getEntityIdentifier() {
        return "cpr_residence";
    }

    @Override
    protected Map<String, String> joinHandles() {
        return null;
    }

    @Override
    protected void setupConditions() throws QueryBuildException {

    }

    @Override
    public Class<ResidenceEntity> getEntityClass() {
        return ResidenceEntity.class;
    }

    @Override
    public Class getDataClass() {
        return ResidenceBaseData.class;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.parametersEmpty();
    }

}
