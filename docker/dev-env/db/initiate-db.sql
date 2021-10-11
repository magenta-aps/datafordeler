
UPDATE Datafordeler.dbo.cpr_config SET personRegisterType = 1, personRegisterLocalFile = '/app/dev-env/local/cpr/download/d170608.l534902', personRegisterFtpAddress = 'ftp://dummy'
UPDATE Datafordeler.dbo.geo_config SET
municipalityRegisterType = 1, municipalityRegisterURL = 'file:///app/dev-env/local/geo/municipality.json',
localityRegisterType = 1, localityRegisterURL = 'file:///app/dev-env/local/geo/locality.json',
postcodeRegisterType = 1, postcodeRegisterURL = 'file:///app/dev-env/local/geo/post.json',
roadRegisterType = 1, roadRegisterURL = 'file:///app/dev-env/local/geo/road.json',
accessAddressRegisterType = 1, accessAddressRegisterURL = 'file:///app/dev-env/local/geo/access.json',
unitAddressRegisterType = 1, unitAddressRegisterURL = 'file:///app/dev-env/local/geo/unit.json',
buildingRegisterType = 1, buildingRegisterURL = 'file:///app/dev-env/local/geo/building.json'
GO
