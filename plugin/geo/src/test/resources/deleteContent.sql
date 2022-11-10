--OR DROP TABLE TO REMOVE EVERYTHING
--ALL THIS IS RESTORED FROM THE GAR DATA

--GAR DATA IS DAILY UPDATED
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_block_name]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_building]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_house_number]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_locality]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_number]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_postcode]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_road]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_shape]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_source]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address_status]
DELETE
[Datafordeler_Test].[dbo].[geo_access_address]

DELETE
[Datafordeler_Test].[dbo].[geo_building_locality]
DELETE
[Datafordeler_Test].[dbo].[geo_building_shape]
DELETE
[Datafordeler_Test].[dbo].[geo_building]

DELETE
[Datafordeler_Test].[dbo].[geo_locality_abbreviation]
DELETE
[Datafordeler_Test].[dbo].[geo_locality_municipality]
DELETE
[Datafordeler_Test].[dbo].[geo_locality_name]
DELETE
[Datafordeler_Test].[dbo].[geo_locality_roadcode]
DELETE
[Datafordeler_Test].[dbo].[geo_locality_shape]
DELETE
[Datafordeler_Test].[dbo].[geo_locality_type]
DELETE
[Datafordeler_Test].[dbo].[geo_locality_betegn]
DELETE
[Datafordeler_Test].[dbo].[geo_locality_status]
DELETE
[Datafordeler_Test].[dbo].[geo_locality]

DELETE
[Datafordeler_Test].[dbo].[geo_municipality_name]
DELETE
[Datafordeler_Test].[dbo].[geo_municipality_shape]
DELETE
[Datafordeler_Test].[dbo].[geo_municipality]

DELETE
[Datafordeler_Test].[dbo].[geo_postcode_name]
DELETE
[Datafordeler_Test].[dbo].[geo_postcode_shape]
DELETE
[Datafordeler_Test].[dbo].[geo_postcode]

DELETE
[Datafordeler_Test].[dbo].[geo_road_locality]
DELETE
[Datafordeler_Test].[dbo].[geo_road_municipality]
DELETE
[Datafordeler_Test].[dbo].[geo_road_name]
DELETE
[Datafordeler_Test].[dbo].[geo_road_shape]
DELETE
[Datafordeler_Test].[dbo].[geo_road]

DELETE
[Datafordeler_Test].[dbo].[geo_unit_address_door]
DELETE
[Datafordeler_Test].[dbo].[geo_unit_address_floor]
DELETE
[Datafordeler_Test].[dbo].[geo_unit_address_number]
DELETE
[Datafordeler_Test].[dbo].[geo_unit_address_source]
DELETE
[Datafordeler_Test].[dbo].[geo_unit_address_status]
DELETE
[Datafordeler_Test].[dbo].[geo_unit_address_usage]
DELETE
[Datafordeler_Test].[dbo].[geo_unit_address]

DELETE
FROM [Datafordeler_Test].[dbo].[last_updated]
WHERE plugin='geo'

