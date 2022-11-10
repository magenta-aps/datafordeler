--Find adresses i GAR by joining different adress-items
SELECT *
FROM [Datafordeler_Test].[dbo].[geo_access_address] access_address
    JOIN [Datafordeler_Test].[dbo].[geo_unit_address] unit_address
ON (unit_address.accessAddress_id = access_address.identification_id)
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
WHERE municipalityCode =956 AND roadCode = 160
