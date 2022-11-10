--Compare two different databases

SELECT count(*)
FROM [Datafordeler_Test].[dbo].[geo_locality]
SELECT count(*)
FROM [Datafordeler].[dbo].[geo_locality]

SELECT count(*)
FROM [Datafordeler_Test].[dbo].[geo_municipality]
SELECT count(*)
FROM [Datafordeler].[dbo].[geo_municipality]

SELECT count(*)
FROM [Datafordeler_Test].[dbo].[geo_postcode]
SELECT count(*)
FROM [Datafordeler].[dbo].[geo_postcode]

SELECT count(*)
FROM [Datafordeler_Test].[dbo].[geo_road]
SELECT count(*)
FROM [Datafordeler].[dbo].[geo_road]

SELECT count(*)
FROM [Datafordeler_Test].[dbo].[geo_unit_address]
SELECT count(*)
FROM [Datafordeler].[dbo].[geo_unit_address]

SELECT count(*)
FROM [Datafordeler_Test].[dbo].[geo_building]
SELECT count(*)
FROM [Datafordeler].[dbo].[geo_building]

SELECT count(*)
FROM [Datafordeler_Test].[dbo].[geo_access_address]
SELECT count(*)
FROM [Datafordeler].[dbo].[geo_access_address]
