package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadCityBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import dk.magenta.datafordeler.cpr.records.road.data.RoadNameBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadPostalcodeBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.service.PersonEntityRecordService;
import dk.magenta.datafordeler.geo.data.accessaddress.*;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityNameRecord;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityNameRecord;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeNameRecord;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadLocalityRecord;
import dk.magenta.datafordeler.geo.data.road.RoadMunicipalityRecord;
import dk.magenta.datafordeler.geo.data.road.RoadNameRecord;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GeoIntegrationTest {


    @Autowired
    private Engine engine;


    @SpyBean
    private DafoUserManager dafoUserManager;


    @SpyBean
    private CprConfigurationManager configurationManager;

    @Autowired
    private CprPlugin plugin;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager entityManager;

    @Autowired
    private PersonEntityRecordService personEntityRecordService;


    @Autowired
    private TestRestTemplate restTemplate;

    //@Before
    public void importData() {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        configuration.setRoadRegisterType(CprConfiguration.RegisterType.DISABLED);
        configuration.setResidenceRegisterType(CprConfiguration.RegisterType.DISABLED);
        configuration.setPersonRegisterType(CprConfiguration.RegisterType.LOCAL_FILE);
        configuration.setPersonRegisterLocalFile("data/test.txt");

        Pull pull = new Pull(engine, plugin);
        pull.run();
    }

    @Test
    public void testLookup2() throws Exception {
        PersonRecordQuery personRecordQuery = new PersonRecordQuery();
        personRecordQuery.addPersonnummer("0101000000");
        personRecordQuery.addPersonnummer("020200*");

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        EntityManager roadManager = geoPlugin.getEntityManager("Road");
        if (roadManager != null) {
            BaseQuery roadQuery = roadManager.getQuery();

            HashMap<String, String> joinHandles = new HashMap<>();
            joinHandles.put("municipalitycode", "municipalitycode");
            joinHandles.put("roadcode", "code");
            personRecordQuery.addRelated(roadQuery, joinHandles);
            
            //m.add(roadManager.getJoinQuery(entityManager.getJoinHandles(personRoot), roadRoot), roadRoot);
            //Session session = sessionManager.getSessionFactory().openSession();
            //System.out.println(QueryManager.getQuery(session, m));

            //OutputWrapper outputWrapper = roadManager.getOutputWrapper();
            //System.out.println(outputWrapper);
        }
    }

    @Test
    public void testLookup3() {


        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);


        Session session = sessionManager.getSessionFactory().openSession();
        session.beginTransaction();

        GeoMunicipalityEntity municipalityEntity1 = new GeoMunicipalityEntity();
        municipalityEntity1.setCode(955);
        session.saveOrUpdate(municipalityEntity1);

        MunicipalityNameRecord municipalityNameRecord1 = new MunicipalityNameRecord();
        municipalityNameRecord1.setName("TestKommune");
        municipalityNameRecord1.setEntity(municipalityEntity1);
        session.saveOrUpdate(municipalityNameRecord1);

        GeoMunicipalityEntity municipalityEntity2 = new GeoMunicipalityEntity();
        municipalityEntity2.setCode(956);
        session.saveOrUpdate(municipalityEntity2);

        MunicipalityNameRecord municipalityNameRecord2 = new MunicipalityNameRecord();
        municipalityNameRecord2.setName("PrøveKommune");
        municipalityNameRecord2.setEntity(municipalityEntity2);
        session.saveOrUpdate(municipalityNameRecord2);

        GeoRoadEntity roadEntity1 = new GeoRoadEntity();
        roadEntity1.setCode(123);
        session.saveOrUpdate(roadEntity1);

        RoadNameRecord roadNameRecord1 = new RoadNameRecord();
        roadNameRecord1.setEntity(roadEntity1);
        roadNameRecord1.setName("Testvej");
        session.saveOrUpdate(roadNameRecord1);
        RoadMunicipalityRecord roadMunicipalityRecord1 = new RoadMunicipalityRecord();
        roadMunicipalityRecord1.setEntity(roadEntity1);
        roadMunicipalityRecord1.setCode(955);
        session.saveOrUpdate(roadMunicipalityRecord1);

        GeoRoadEntity roadEntity2 = new GeoRoadEntity();
        roadEntity2.setCode(456);
        session.saveOrUpdate(roadEntity2);
        RoadNameRecord roadNameRecord2 = new RoadNameRecord();
        roadNameRecord2.setEntity(roadEntity2);
        roadNameRecord2.setName("Prøvevej");
        session.saveOrUpdate(roadNameRecord2);
        RoadMunicipalityRecord roadMunicipalityRecord2 = new RoadMunicipalityRecord();
        roadMunicipalityRecord2.setEntity(roadEntity2);
        roadMunicipalityRecord2.setCode(956);
        session.saveOrUpdate(roadMunicipalityRecord2);

        GeoLocalityEntity localityEntity1 = new GeoLocalityEntity();
        localityEntity1.setCode("007");
        session.saveOrUpdate(localityEntity1);
        LocalityNameRecord localityNameRecord1 = new LocalityNameRecord();
        localityNameRecord1.setName("TestSted");
        localityNameRecord1.setEntity(localityEntity1);
        session.saveOrUpdate(localityNameRecord1);

        RoadLocalityRecord roadLocalityRecord = new RoadLocalityRecord();
        roadLocalityRecord.setCode("007");
        roadLocalityRecord.setEntity(roadEntity1);
        session.saveOrUpdate(roadLocalityRecord);

        GeoLocalityEntity localityEntity2 = new GeoLocalityEntity();
        localityEntity2.setCode("009");
        session.saveOrUpdate(localityEntity2);
        LocalityNameRecord localityNameRecord2 = new LocalityNameRecord();
        localityNameRecord2.setName("PrøveSted");
        localityNameRecord2.setEntity(localityEntity2);
        session.saveOrUpdate(localityNameRecord2);


        PostcodeEntity postcodeEntity = new PostcodeEntity();
        postcodeEntity.setCode(1234);
        session.saveOrUpdate(postcodeEntity);

        PostcodeNameRecord postcodeNameRecord = new PostcodeNameRecord();
        postcodeNameRecord.setName("TestPostdistrikt");
        postcodeNameRecord.setEntity(postcodeEntity);
        session.saveOrUpdate(postcodeNameRecord);

        AccessAddressEntity accessAddressEntity1 = new AccessAddressEntity();
        accessAddressEntity1.setBnr("B-1234");
        session.saveOrUpdate(accessAddressEntity1);
        AccessAddressRoadRecord accessAddressRoadRecord1 = new AccessAddressRoadRecord();
        accessAddressRoadRecord1.setEntity(accessAddressEntity1);
        accessAddressRoadRecord1.setMunicipalityCode(955);
        accessAddressRoadRecord1.setRoadCode(123);
        session.saveOrUpdate(accessAddressRoadRecord1);
        AccessAddressHouseNumberRecord accessAddressHouseNumberRecord = new AccessAddressHouseNumberRecord();
        accessAddressHouseNumberRecord.setNumber("5");
        accessAddressHouseNumberRecord.setEntity(accessAddressEntity1);
        session.saveOrUpdate(accessAddressHouseNumberRecord);

        AccessAddressLocalityRecord accessAddressLocalityRecord1 = new AccessAddressLocalityRecord();
        accessAddressLocalityRecord1.setEntity(accessAddressEntity1);
        accessAddressLocalityRecord1.setCode("007");
        session.saveOrUpdate(accessAddressLocalityRecord1);

        AccessAddressPostcodeRecord accessAddressPostcodeRecord = new AccessAddressPostcodeRecord();
        accessAddressPostcodeRecord.setPostcode(1234);
        accessAddressPostcodeRecord.setEntity(accessAddressEntity1);
        session.saveOrUpdate(accessAddressPostcodeRecord);


        RoadEntity roadEntity3 = new RoadEntity();
        roadEntity3.setRoadcode(789);
        roadEntity3.setMunicipalityCode(101);
        session.saveOrUpdate(roadEntity3);

        RoadNameBitemporalRecord roadNameRecord3 = new RoadNameBitemporalRecord();
        roadNameRecord3.setRoadName("DanskVej");
        roadNameRecord3.setEntity(roadEntity3);
        session.saveOrUpdate(roadNameRecord3);

        RoadPostalcodeBitemporalRecord postCodeRecord3a = new RoadPostalcodeBitemporalRecord();
        postCodeRecord3a.setFromHousenumber("1");
        postCodeRecord3a.setToHousenumber("19");
        postCodeRecord3a.setEven(false);
        postCodeRecord3a.setPostalCode(1001);
        postCodeRecord3a.setPostalDistrict("WrongPostDistrict1");
        postCodeRecord3a.setEntity(roadEntity3);
        session.saveOrUpdate(postCodeRecord3a);

        RoadPostalcodeBitemporalRecord postCodeRecord3b = new RoadPostalcodeBitemporalRecord();
        postCodeRecord3b.setFromHousenumber("2");
        postCodeRecord3b.setToHousenumber("20B");
        postCodeRecord3b.setEven(true);
        postCodeRecord3b.setPostalCode(1002);
        postCodeRecord3b.setPostalDistrict("WrongPostDistrict2");
        postCodeRecord3b.setEntity(roadEntity3);
        session.saveOrUpdate(postCodeRecord3b);

        RoadPostalcodeBitemporalRecord postCodeRecord3c = new RoadPostalcodeBitemporalRecord();
        postCodeRecord3c.setFromHousenumber("21");
        postCodeRecord3c.setToHousenumber("999");
        postCodeRecord3c.setEven(false);
        postCodeRecord3c.setPostalCode(1003);
        postCodeRecord3c.setPostalDistrict("WrongPostDistrict3");
        postCodeRecord3c.setEntity(roadEntity3);
        session.saveOrUpdate(postCodeRecord3c);

        RoadPostalcodeBitemporalRecord postCodeRecord3d = new RoadPostalcodeBitemporalRecord();
        postCodeRecord3d.setFromHousenumber("20C");
        postCodeRecord3d.setToHousenumber("998");
        postCodeRecord3d.setEven(true);
        postCodeRecord3d.setPostalCode(1004);
        postCodeRecord3d.setPostalDistrict("CorrectPostDistrict");
        postCodeRecord3d.setEntity(roadEntity3);
        session.saveOrUpdate(postCodeRecord3d);


        RoadCityBitemporalRecord cityRecord3a = new RoadCityBitemporalRecord();
        cityRecord3a.setFromHousenumber("1");
        cityRecord3a.setToHousenumber("999");
        cityRecord3a.setEven(false);
        cityRecord3a.setCityName("TestBy");
        cityRecord3a.setEntity(roadEntity3);
        session.saveOrUpdate(cityRecord3a);

        RoadCityBitemporalRecord cityRecord3b = new RoadCityBitemporalRecord();
        cityRecord3b.setFromHousenumber("2");
        cityRecord3b.setToHousenumber("998");
        cityRecord3b.setEven(true);
        cityRecord3b.setCityName("TestBy");
        cityRecord3b.setEntity(roadEntity3);
        session.saveOrUpdate(cityRecord3b);


        PersonEntity personEntity = new PersonEntity();
        personEntity.setPersonnummer("1234567890");
        session.saveOrUpdate(personEntity);

        /*
        AddressDataRecord personAddressRecord1 = new AddressDataRecord();
        personAddressRecord1.setEntity(personEntity);
        personAddressRecord1.setMunicipalityCode(955);
        personAddressRecord1.setRoadCode(123);
        personAddressRecord1.setBuildingNumber("B-1234");
        personAddressRecord1.setHouseNumber("5");
        session.saveOrUpdate(personAddressRecord1);
        */


        AddressDataRecord personAddressRecord3 = new AddressDataRecord();
        personAddressRecord3.setEntity(personEntity);
        personAddressRecord3.setMunicipalityCode(101);
        personAddressRecord3.setRoadCode(789);
        personAddressRecord3.setHouseNumber("20C");
        session.saveOrUpdate(personAddressRecord3);




        session.getTransaction().commit();

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        Plugin cprPlugin = pluginManager.getPluginByName("cpr");
        EntityManager accessAddressManager = geoPlugin.getEntityManager("AccessAddress");

        PersonRecordQuery query = new PersonRecordQuery();
        query.setPersonnummer("1234567890");


        // vi bør kunne kalde ét endpoint i geoplugin for at joine alt på gennem en accessaddress,
        // og få lokalitet, postnr, vej og kommune

        // outputwrappere skal kunne tage en entitet ud fra en pulje og indsætte efter behov
        // f.eks. hvis en person har haft flere adresser

        /*
        BaseQuery accessAddressQuery = accessAddressManager.getQuery("municipality", "road", "postcode", "locality");
        HashMap<String, String> joinHandles = new HashMap<>();
        joinHandles.put("municipalitycode", "municipalitycode");
        joinHandles.put("roadcode", "roadcode");
        //joinHandles.put("housenumber", "housenumber");
        //joinHandles.put("bnr", "bnr");
        joinHandles.put("bnr_or_housenumber", "bnr_or_housenumber");
        query.addRelated(accessAddressQuery, joinHandles);


        List<ResultSet<PersonEntity>> personResults = personEntityRecordService.searchByQuery(query, session);
        System.out.println(personResults);

*/
        HashMap<String, String> handles = new HashMap<>();
        handles.put("municipalitycode", "cpr_person__address.municipalityCode");
        handles.put("roadcode", "cpr_person__address.roadCode");
        handles.put("housenumber", "cpr_person__address.houseNumber");
        handles.put("bnr", "cpr_person__address.buildingNumber");

        LinkedHashMap<String, Class> classAliases = new LinkedHashMap<>();
        classAliases.put("cpr_person", PersonEntity.class);
        classAliases.putAll(geoPlugin.getJoinClassAliases(handles.keySet()));
        classAliases.putAll(cprPlugin.getJoinClassAliases(handles.keySet()));

        ParameterMap parameters = new ParameterMap();
        parameters.add("pnr", "1234567890");

        ResponseEntity<String> response = this.restSearch(parameters, "person");
        System.out.println(response.getBody());
    }

    private ResponseEntity<String> restSearch(ParameterMap parameters, String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        System.out.println(parameters.asUrlParams());
        return this.restTemplate.exchange("/cpr/"+type+"/1/rest/search?" + parameters.asUrlParams(), HttpMethod.GET, httpEntity, String.class);
    }

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }
    private void whitelistLocalhost() {
        when(dafoUserManager.getIpWhitelist()).thenReturn(Collections.singleton("127.0.0.1"));
    }

}
