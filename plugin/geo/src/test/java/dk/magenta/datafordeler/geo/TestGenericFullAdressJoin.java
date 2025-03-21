package dk.magenta.datafordeler.geo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.geo.data.accessaddress.*;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityNameRecord;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityNameRecord;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeNameRecord;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadMunicipalityRecord;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestGenericFullAdressJoin extends GeoTest {

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    public void initialize() throws Exception {
        this.loadAll();
        this.loadCprAddress();
    }


    @Test
    public void testJoiningOfAdressElements() throws JsonProcessingException {

/*

SELECT *  FROM
	[Datafordeler_Test].[dbo].[geo_access_address] access_address

	JOIN [Datafordeler_Test].[dbo].[geo_unit_address] unit_address ON (unit_address.accessAddress_id = access_address.identification_id)
	JOIN [Datafordeler_Test].[dbo].[geo_unit_address_door] unit_address_door ON (unit_address_door.entity_id = unit_address.id)
	JOIN [Datafordeler_Test].[dbo].[geo_unit_address_floor] unit_address_floor ON (unit_address_floor.entity_id = unit_address.id)
	JOIN [Datafordeler_Test].[dbo].[geo_unit_address_number] unit_address_number ON (unit_address_number.entity_id = unit_address.id)
	JOIN [Datafordeler_Test].[dbo].[geo_unit_address_status] unit_address_status ON (unit_address_status.entity_id = unit_address.id)
	JOIN [Datafordeler_Test].[dbo].[geo_unit_address_usage] unit_address_usage ON (unit_address_usage.entity_id = unit_address.id)

	JOIN [Datafordeler_Test].[dbo].[geo_access_address_house_number] access_address_house_number ON (access_address_house_number.entity_id = access_address.id)

	JOIN [Datafordeler_Test].[dbo].[geo_access_address_locality] access_locality ON (access_locality.entity_id = access_address.id)
	JOIN [Datafordeler_Test].[dbo].[geo_locality] locality ON (access_locality.reference_id = locality.identification_id)
	JOIN [Datafordeler_Test].[dbo].[geo_locality_abbreviation] locality_abbreviation ON (locality_abbreviation.entity_id = locality.id)
	JOIN [Datafordeler_Test].[dbo].[geo_locality_type] locality_type ON (locality_type.entity_id = locality.id)
	JOIN [Datafordeler_Test].[dbo].[geo_locality_name] locality_name ON (locality_name.entity_id = locality.id)
	JOIN [Datafordeler_Test].[dbo].[geo_access_address_status] access_status ON (access_status.entity_id = access_address.id)

	JOIN [Datafordeler_Test].[dbo].[geo_access_address_block_name] access_address_block_name ON (access_address_block_name.entity_id = access_address.id)

	JOIN [Datafordeler_Test].[dbo].[geo_access_address_postcode] access_postcode ON (access_postcode.entity_id = access_address.id)
	JOIN [Datafordeler_Test].[dbo].[geo_postcode] postcode ON (access_postcode.reference_id = postcode.identification_id)
	JOIN [Datafordeler_Test].[dbo].[geo_postcode_name] postcode_name ON (postcode_name.entity_id = postcode.id)

	JOIN [Datafordeler_Test].[dbo].[geo_access_address_road] access_road ON (access_road.entity_id = access_address.id)
	JOIN [Datafordeler_Test].[dbo].[geo_road] road ON (access_road.reference_id = road.identification_id)
	JOIN [Datafordeler_Test].[dbo].[geo_road_name] road_name ON (road_name.entity_id = road.id)

	JOIN [Datafordeler_Test].[dbo].[geo_road_municipality] road_municipality ON (road_municipality.entity_id = road.id)
	JOIN [Datafordeler_Test].[dbo].[geo_municipality] municipality ON (municipality.code = road_municipality.code)
	JOIN [Datafordeler_Test].[dbo].[geo_municipality_name] municipality_name ON (municipality_name.entity_id = municipality.id)
	WHERE bnr = 'B-1404'
*/

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            String hql = "SELECT DISTINCT accessAddressEntity, unitAddressEntity, localityRecord, accessAddressPostcodeRecord, accessAddressRoadRecord, postcodeEntity, geoMunipialicityEntity, roadEntity  " +
                    "FROM " + AccessAddressEntity.class.getCanonicalName() + " accessAddressEntity " +
                    "JOIN " + UnitAddressEntity.class.getCanonicalName() + " unitAddressEntity ON unitAddressEntity." + UnitAddressEntity.DB_FIELD_ACCESS_ADDRESS + "=accessAddressEntity." + AccessAddressEntity.DB_FIELD_IDENTIFICATION + " " +
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

                    "JOIN " + RoadMunicipalityRecord.class.getCanonicalName() + " roadMunipialicityRecord ON roadMunipialicityRecord." + RoadMunicipalityRecord.DB_FIELD_CODE + "=accessAddressRoadRecord." + "municipalityCode" + " " +
                    "JOIN " + GeoMunicipalityEntity.class.getCanonicalName() + " geoMunipialicityEntity ON geoMunipialicityEntity." + GeoMunicipalityEntity.DB_FIELD_CODE + "=roadMunipialicityRecord." + RoadMunicipalityRecord.DB_FIELD_CODE + " " +
                    "JOIN " + MunicipalityNameRecord.class.getCanonicalName() + " municipalityName ON municipalityName." + MunicipalityNameRecord.DB_FIELD_ENTITY + "=geoMunipialicityEntity." + "id" + " " +

                    " WHERE accessAddressEntity.bnr=:bnr AND municipalityName.name='Kommuneqarfik Sermersooq'" +
                    "";//B-3197

            //"";//B-3197

            Query query = session.createQuery(hql);

            query.setParameter("bnr", "B-3197");


            Assertions.assertTrue(query.getResultList().size() > 0);

            System.out.println(query.getResultList().size());

            for (Object item : query.getResultList()) {
                {
                    System.out.println(objectMapper.writeValueAsString(item));
                }
            }
        }
    }

}
