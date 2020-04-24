package dk.magenta.datafordeler.geo.data.fullAdresses;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.geo.AdresseService;
import dk.magenta.datafordeler.geo.data.accessaddress.*;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadMunicipalityRecord;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController("GeoGenericUnitAddressService")
@RequestMapping("/geo/genericunitaddress")
public class GenericUnitAddressService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Logger log = LogManager.getLogger(AdresseService.class);


    /*@RequestMapping(path="/{uuid}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Envelope getRest(@PathVariable("uuid") String uuid, @RequestParam MultiValueMap<String, String> requestParams, HttpServletRequest request)*/

    @RequestMapping("/fullAdress")
    public void getLocalities(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams, HttpServletResponse response) throws DataFordelerException, IOException {

        try(Session session = sessionManager.getSessionFactory().openSession();) {


            String hql = "SELECT DISTINCT accessAddressEntity, unitAddressEntity, localityRecord, postcodeEntity, roadEntity, geoMunipialicityEntity  " +
                    "FROM "+ AccessAddressEntity.class.getCanonicalName()+" accessAddressEntity "+
                    "JOIN "+ UnitAddressEntity.class.getCanonicalName() + " unitAddressEntity ON unitAddressEntity."+UnitAddressEntity.DB_FIELD_ACCESS_ADDRESS+"=accessAddressEntity."+AccessAddressEntity.DB_FIELD_IDENTIFICATION+" "+
                    "JOIN "+ AccessAddressHouseNumberRecord.class.getCanonicalName() + " accessAddressNumberRecord ON accessAddressNumberRecord."+AccessAddressHouseNumberRecord.DB_FIELD_ENTITY+"=accessAddressEntity."+"id"+" "+

                    "JOIN "+ AccessAddressLocalityRecord.class.getCanonicalName() + " accessAddressLocalityRecord ON accessAddressLocalityRecord."+AccessAddressLocalityRecord.DB_FIELD_ENTITY+"=accessAddressEntity."+"id"+" "+
                    "JOIN "+ GeoLocalityEntity.class.getCanonicalName() + " localityRecord ON accessAddressLocalityRecord."+AccessAddressLocalityRecord.DB_FIELD_CODE+"=localityRecord."+GeoLocalityEntity.DB_FIELD_CODE+" "+

                    "JOIN "+ AccessAddressBlockNameRecord.class.getCanonicalName() + " accessAddressBlockNameRecord ON accessAddressBlockNameRecord."+AccessAddressBlockNameRecord.DB_FIELD_ENTITY+"=accessAddressEntity."+"id"+" "+

                    "JOIN "+ AccessAddressPostcodeRecord.class.getCanonicalName() + " accessAddressPostcodeRecord ON accessAddressPostcodeRecord."+AccessAddressPostcodeRecord.DB_FIELD_ENTITY+"=accessAddressEntity."+"id"+" "+
                    "JOIN "+ PostcodeEntity.class.getCanonicalName() + " postcodeEntity ON accessAddressPostcodeRecord."+AccessAddressPostcodeRecord.DB_FIELD_CODE+"=postcodeEntity."+PostcodeEntity.DB_FIELD_CODE+" "+

                    "JOIN "+ AccessAddressRoadRecord.class.getCanonicalName() + " accessAddressRoadRecord ON accessAddressRoadRecord."+AccessAddressRoadRecord.DB_FIELD_ENTITY+"=accessAddressEntity."+"id"+" "+
                    "JOIN "+ GeoRoadEntity.class.getCanonicalName() + " roadEntity ON accessAddressRoadRecord."+AccessAddressRoadRecord.DB_FIELD_ROAD_REFERENCE+"=roadEntity."+ GeoRoadEntity.DB_FIELD_IDENTIFICATION+" "+

                    "JOIN "+ RoadMunicipalityRecord.class.getCanonicalName() + " roadMunipialicityRecord ON roadMunipialicityRecord."+RoadMunicipalityRecord.DB_FIELD_CODE+"=accessAddressRoadRecord."+"municipalityCode"+" "+
                    "JOIN "+ GeoMunicipalityEntity.class.getCanonicalName() + " geoMunipialicityEntity ON geoMunipialicityEntity."+GeoMunicipalityEntity.DB_FIELD_CODE+"=roadMunipialicityRecord."+RoadMunicipalityRecord.DB_FIELD_CODE+" "+

                    " WHERE accessAddressEntity.bnr=:bnr"+
                    "";//B-3197

            Query query = session.createQuery(hql);

            for(String key : requestParams.keySet()) {
                query.setParameter(key, requestParams.getFirst(key));
            }



            query.setMaxResults(10);

            setHeaders(response);

            List<Object[]> resultList = query.getResultList();

            ArrayList<FullAdressDTO> adressElementList = new ArrayList<FullAdressDTO>();

            for(Object[] item : resultList) {
                AccessAddressEntity accessAddressEntity = (AccessAddressEntity)item[0];
                UnitAddressEntity unitAddressEntity = (UnitAddressEntity)item[1];
                GeoLocalityEntity localityRecord = (GeoLocalityEntity)item[2];
                PostcodeEntity postcodeRecord = (PostcodeEntity)item[3];
                GeoRoadEntity roadRecord = (GeoRoadEntity)item[4];
                GeoMunicipalityEntity geoMunicipalityEntity = (GeoMunicipalityEntity)item[5];

                FullAdressDTO output = new FullAdressDTO();
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
                output.setDoer(unitAddressEntity.getDoor().stream().findFirst().orElse(null).getDoor());
                output.setEtage(unitAddressEntity.getFloor().stream().findFirst().orElse(null).getFloor());
                //output.mabeysomething(unitAddressEntity.getUsage().stream().findFirst().orElse(null).getUsage());
                output.setNummer(unitAddressEntity.getNumber().stream().findFirst().orElse(null).getNumber());
                output.setLokalitet_navn(localityRecord.getName().stream().findFirst().orElse(null).getName());
                output.setLokalitet_type(localityRecord.getType().stream().findFirst().orElse(null).getType());
                output.setBnr(postcodeRecord.getName().stream().findFirst().orElse(null).getName());
                output.setVej_navn(roadRecord.getName().stream().findFirst().orElse(null).getName());
                output.setKommune_navn(geoMunicipalityEntity.getName().stream().findFirst().orElse(null).getName());
                adressElementList.add(output);
            }

            response.getWriter().write(objectMapper.writeValueAsString(adressElementList));

        }

        setHeaders(response);
    }


    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }
}
