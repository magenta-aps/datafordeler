package dk.magenta.datafordeler.geo.data.fullAdresses;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.geo.AdresseService;
import dk.magenta.datafordeler.geo.data.accessaddress.*;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityNameRecord;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityNameRecord;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeNameRecord;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadMunicipalityRecord;
import dk.magenta.datafordeler.geo.data.road.RoadNameRecord;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressFloorRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

import static dk.magenta.datafordeler.geo.AdresseService.PARAM_DEBUG;

@RestController("GeoGenericUnitAddressService")
@RequestMapping("/geo/fullAddress/1/rest")
public class GenericUnitAddressService {

    private static final Map<String, ParameterType> parameterMappings = new HashMap<String, ParameterType>();

    public enum ParameterType {
        bnr("accessAddressEntity.bnr", false),
        husNummer("accessAddressNumberRecord.number", false),
        etage("unitAddressFloor.floor", false),
        bloknavn("accessAddressBlockNameRecord.name", false),
        kommune_kode("geoMunipialicityEntity.code", true),
        kommune_navn("municipalityName.name", false),
        lokalitet_kode("localityRecord.code", false),
        lokalitet_navn("localityName.name", false),
        post_kode("postcodeEntity.code", true),
        post_navn("postcodeName.name", false),
        vej_kode("roadEntity.code", true),
        vej_navn("roadName.name", false);

        private final String searchString;
        private final boolean numberType;

        ParameterType(String searchString, boolean numberType) {
            this.searchString = searchString;
            this.numberType = numberType;
        }

        public String getSerarchString() {
            return searchString;
        }

        public boolean isNumberType() {
            return numberType;
        }

    }

    static {
        parameterMappings.put("bnr", ParameterType.bnr);
        parameterMappings.put("husNummer", ParameterType.husNummer);
        parameterMappings.put("etage", ParameterType.etage);
        parameterMappings.put("bloknavn", ParameterType.bloknavn);
        parameterMappings.put("kommune_kode", ParameterType.kommune_kode);
        parameterMappings.put("kommune_navn", ParameterType.kommune_navn);
        parameterMappings.put("lokalitet_kode", ParameterType.lokalitet_kode);
        parameterMappings.put("lokalitet_navn", ParameterType.lokalitet_navn);
        parameterMappings.put("post_kode", ParameterType.post_kode);
        parameterMappings.put("post_navn", ParameterType.post_navn);
        parameterMappings.put("vej_kode", ParameterType.vej_kode);
        parameterMappings.put("vej_navn", ParameterType.vej_navn);
    }


    @Autowired
    SessionManager sessionManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private ObjectMapper objectMapper;

    private final Logger log = LogManager.getLogger(AdresseService.class.getCanonicalName());


    @RequestMapping("/search")
    public Envelope getLocalities(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams, HttpServletResponse response) throws DataFordelerException, IOException {

        //This was supposed to use the BaseQuery from core, but it was not completely supported

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        boolean debug = "1".equals(request.getParameter(PARAM_DEBUG));

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Envelope envelope = new Envelope();
            DafoUserDetails user = this.dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.info(
                    "Fetching from generic address service"
            );
            loggerHelper.urlInvokePersistablelogs("fullAddress");
            envelope.setRequestTimestamp(user.getCreationTime());
            envelope.setUsername(user.toString());

            String hql = "SELECT DISTINCT accessAddressEntity, unitAddressEntity, localityRecord, postcodeEntity, roadEntity, geoMunipialicityEntity, geoMunipialicityEntity.name AS munipialicityName " +
                    "FROM " + AccessAddressEntity.class.getCanonicalName() + " accessAddressEntity " +
                    "JOIN " + UnitAddressEntity.class.getCanonicalName() + " unitAddressEntity ON unitAddressEntity." + UnitAddressEntity.DB_FIELD_ACCESS_ADDRESS + "=accessAddressEntity." + AccessAddressEntity.DB_FIELD_IDENTIFICATION + " " +
                    "JOIN " + UnitAddressFloorRecord.class.getCanonicalName() + " unitAddressFloor ON unitAddressFloor." + UnitAddressFloorRecord.DB_FIELD_ENTITY + "=unitAddressEntity." + "id" + " " +

                    "JOIN " + AccessAddressHouseNumberRecord.class.getCanonicalName() + " accessAddressNumberRecord ON accessAddressNumberRecord." + AccessAddressHouseNumberRecord.DB_FIELD_ENTITY + "=accessAddressEntity." + "id" + " " +

                    "JOIN " + AccessAddressLocalityRecord.class.getCanonicalName() + " accessAddressLocalityRecord ON accessAddressLocalityRecord." + AccessAddressLocalityRecord.DB_FIELD_ENTITY + "=accessAddressEntity." + "id" + " " +
                    "JOIN " + GeoLocalityEntity.class.getCanonicalName() + " localityRecord ON accessAddressLocalityRecord." + AccessAddressLocalityRecord.DB_FIELD_CODE + "=localityRecord." + GeoLocalityEntity.DB_FIELD_CODE + " " +
                    "JOIN " + LocalityNameRecord.class.getCanonicalName() + " localityName ON localityName." + LocalityNameRecord.DB_FIELD_ENTITY + "=localityRecord." + "id" + " " +

                    "JOIN " + AccessAddressBlockNameRecord.class.getCanonicalName() + " accessAddressBlockNameRecord ON accessAddressBlockNameRecord." + AccessAddressBlockNameRecord.DB_FIELD_ENTITY + "=accessAddressEntity." + "id" + " " +

                    "JOIN " + AccessAddressPostcodeRecord.class.getCanonicalName() + " accessAddressPostcodeRecord ON accessAddressPostcodeRecord." + AccessAddressPostcodeRecord.DB_FIELD_ENTITY + "=accessAddressEntity." + "id" + " " +
                    "JOIN " + PostcodeEntity.class.getCanonicalName() + " postcodeEntity ON accessAddressPostcodeRecord." + AccessAddressPostcodeRecord.DB_FIELD_CODE + "=postcodeEntity." + PostcodeEntity.DB_FIELD_CODE + " " +
                    "JOIN " + PostcodeNameRecord.class.getCanonicalName() + " postcodeName ON postcodeName." + PostcodeNameRecord.DB_FIELD_ENTITY + "=postcodeEntity." + "id" + " " +

                    "JOIN " + AccessAddressRoadRecord.class.getCanonicalName() + " accessAddressRoadRecord ON accessAddressRoadRecord." + AccessAddressRoadRecord.DB_FIELD_ENTITY + "=accessAddressEntity." + "id" + " " +
                    "JOIN " + GeoRoadEntity.class.getCanonicalName() + " roadEntity ON accessAddressRoadRecord." + AccessAddressRoadRecord.DB_FIELD_ROAD_REFERENCE + "=roadEntity." + GeoRoadEntity.DB_FIELD_IDENTIFICATION + " " +
                    "JOIN " + RoadNameRecord.class.getCanonicalName() + " roadName ON roadName." + RoadNameRecord.DB_FIELD_ENTITY + "=roadEntity." + "id" + " " +

                    "JOIN " + RoadMunicipalityRecord.class.getCanonicalName() + " roadMunipialicityRecord ON roadMunipialicityRecord." + RoadMunicipalityRecord.DB_FIELD_CODE + "=accessAddressRoadRecord." + "municipalityCode" + " " +
                    "JOIN " + GeoMunicipalityEntity.class.getCanonicalName() + " geoMunipialicityEntity ON geoMunipialicityEntity." + GeoMunicipalityEntity.DB_FIELD_CODE + "=roadMunipialicityRecord." + RoadMunicipalityRecord.DB_FIELD_CODE + " " +
                    "JOIN " + MunicipalityNameRecord.class.getCanonicalName() + " municipalityName ON municipalityName." + MunicipalityNameRecord.DB_FIELD_ENTITY + "=geoMunipialicityEntity." + "id" + " " +

                    " WHERE geoMunipialicityEntity.code > 900 ";//Just always filter on greenland adresses no matter what

            for (String key : requestParams.keySet()) {
                String parameterName = key;
                String comparator = null;

                if (key.contains(".")) {
                    StringTokenizer tokens = new StringTokenizer(parameterName, ".");
                    parameterName = (String) tokens.nextElement();
                    comparator = (String) tokens.nextElement();
                }

                if (parameterMappings.containsKey(parameterName)) {
                    if (parameterMappings.get(parameterName).isNumberType()) {
                        if ("lt".equals(comparator)) {
                            hql += " AND " + parameterMappings.get(parameterName).getSerarchString() + "<:" + parameterName;
                        } else if ("gt".equals(comparator)) {
                            hql += " AND " + parameterMappings.get(parameterName).getSerarchString() + ">:" + parameterName;
                        } else {
                            hql += " AND " + parameterMappings.get(parameterName).getSerarchString() + "=:" + parameterName;
                        }
                    } else {
                        String value = requestParams.getFirst(key);
                        if (value.contains("*")) {
                            hql += " AND " + parameterMappings.get(parameterName).getSerarchString() + " LIKE :" + parameterName;
                        } else {
                            hql += " AND " + parameterMappings.get(parameterName).getSerarchString() + " = :" + parameterName;
                        }
                    }
                }
            }

            Query query = session.createQuery(hql);

            if (pageSize != null) {
                query.setMaxResults(Integer.valueOf(pageSize));
            } else {
                query.setMaxResults(10);
            }
            if (page != null) {
                int pageIndex = (Integer.valueOf(page) - 1) * query.getMaxResults();
                query.setFirstResult(pageIndex);
            } else {
                query.setFirstResult(0);
            }

            Set<String> params = query.getParameterMetadata().getNamedParameterNames();

            for (String key : requestParams.keySet()) {
                String parameterName = key;
                String comparator = null;
                if (key.contains(".")) {
                    StringTokenizer tokens = new StringTokenizer(parameterName, ".");
                    parameterName = (String) tokens.nextElement();
                    comparator = (String) tokens.nextElement();
                }
                if (params.contains(parameterName)) {
                    if (parameterMappings.get(parameterName).isNumberType()) {

                        int numberValue = Integer.parseInt(requestParams.getFirst(key));
                        query.setParameter(parameterName, numberValue);
                    } else {
                        String value = requestParams.getFirst(key);
                        String valueWithReplaces = value.replace("*", "%");
                        query.setParameter(parameterName, valueWithReplaces);
                    }
                }
            }

            setHeaders(response);
            List<Object[]> resultList = query.getResultList();
            ArrayList<FullAddressDTO> adressElementList = new ArrayList<FullAddressDTO>();

            for (Object[] item : resultList) {
                AccessAddressEntity accessAddressEntity = (AccessAddressEntity) item[0];
                UnitAddressEntity unitAddressEntity = (UnitAddressEntity) item[1];
                GeoLocalityEntity localityRecord = (GeoLocalityEntity) item[2];
                PostcodeEntity postcodeRecord = (PostcodeEntity) item[3];
                GeoRoadEntity roadRecord = (GeoRoadEntity) item[4];
                GeoMunicipalityEntity geoMunicipalityEntity = (GeoMunicipalityEntity) item[5];

                FullAddressDTO output = new FullAddressDTO();

                output.setBnr(accessAddressEntity.getBnr());
                output.setHusNummer(accessAddressEntity.getHouseNumber().stream().findFirst().orElse(null).getNumber());
                output.setVej_kode(accessAddressEntity.getRoad().stream().findFirst().orElse(null).getRoadCode());
                output.setKommune_kode(accessAddressEntity.getRoad().stream().findFirst().orElse(null).getMunicipalityCode());
                output.setBlokNavn(accessAddressEntity.getBlockName().stream().findFirst().orElse(null).getName());
                output.setLokalitet_kode(accessAddressEntity.getLocality().stream().findFirst().orElse(null).getCode());
                output.setPost_kode(accessAddressEntity.getPostcode().stream().findFirst().orElse(null).getPostcode());
                output.setSource(accessAddressEntity.getSource().stream().findFirst().orElse(null).getSource());
                output.setStatus(accessAddressEntity.getStatus().stream().findFirst().orElse(null).getStatus());
                //output.mabeysomething(accessAddressEntity.getBuilding().stream().findFirst().orElse(null).);
                output.setEnhed(unitAddressEntity.getNumber().stream().findFirst().orElse(null).getNumber());
                output.setEtage(unitAddressEntity.getFloor().stream().findFirst().orElse(null).getFloor());
                //output.mabeysomething(unitAddressEntity.getUsage().stream().findFirst().orElse(null).getUsage());
                output.setNummer(unitAddressEntity.getNumber().stream().findFirst().orElse(null).getNumber());
                output.setLokalitet_navn(localityRecord.getName().stream().findFirst().orElse(null).getName());
                output.setLokalitet_type(localityRecord.getType().stream().findFirst().orElse(null).getType());
                output.setPost_navn(postcodeRecord.getName().stream().findFirst().orElse(null).getName());
                output.setVej_navn(roadRecord.getName().stream().findFirst().orElse(null).getName());
                output.setKommune_navn(geoMunicipalityEntity.getName().stream().findFirst().orElse(null).getName());
                if (debug) {
                    output.setAccessAddress_objectId(Integer.toString(accessAddressEntity.getObjectId()));
                    output.setUnitAddress_objectId(Integer.toString(unitAddressEntity.getObjectId()));
                }
                adressElementList.add(output);
            }

            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult() + 1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());

            envelope.setResults(adressElementList);
            setHeaders(response);
            return envelope;
        }
    }


    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }
}
