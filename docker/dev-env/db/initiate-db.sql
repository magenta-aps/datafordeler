BEGIN
    IF NOT EXISTS (SELECT * FROM DatafordelerConfig.dbo.cpr_config)
        BEGIN
        INSERT INTO DatafordelerConfig.dbo.cpr_config (
        id,
        personRegisterType, personRegisterLocalFile, personRegisterFtpAddress,
        directCustomerNumber,
        directPort,
        personRegisterDataCharset,
        roadRegisterDataCharset,
        residenceRegisterDataCharset
        ) VALUES (
        'dk.magenta.datafordeler.cpr.CprPlugin',
        1, '/app/dev-env/local/cpr/download/d170608.l534902', 'ftp://dummy',
        0, 0, 1, 1, 1
        )
        END
    ELSE
        BEGIN
        UPDATE DatafordelerConfig.dbo.cpr_config SET
        personRegisterType = 1,
        personRegisterLocalFile = '/app/dev-env/local/cpr/download/d170608.l534902',
        personRegisterFtpAddress = 'ftp://dummy'
        END
END

BEGIN
    IF NOT EXISTS (SELECT * FROM DatafordelerConfig.dbo.geo_config)
        BEGIN
        INSERT INTO DatafordelerConfig.dbo.geo_config (
            id, charsetName,
            municipalityRegisterType, municipalityRegisterURL,
            localityRegisterType, localityRegisterURL,
            postcodeRegisterType, postcodeRegisterURL,
            roadRegisterType, roadRegisterURL,
            accessAddressRegisterType, accessAddressRegisterURL,
            unitAddressRegisterType, unitAddressRegisterURL,
            buildingRegisterType, buildingRegisterURL
            ) VALUES (
            'dk.magenta.datafordeler.geo.GeoPlugin', 0,
            1, 'file:///app/dev-env/local/geo/municipality.json',
            1, 'file:///app/dev-env/local/geo/locality.json',
            1, 'file:///app/dev-env/local/geo/post.json',
            1, 'file:///app/dev-env/local/geo/road.json',
            1, 'file:///app/dev-env/local/geo/access.json',
            1, 'file:///app/dev-env/local/geo/unit.json',
            1, 'file:///app/dev-env/local/geo/building.json'
            )
            END
    ELSE
        BEGIN
        UPDATE DatafordelerConfig.dbo.geo_config SET
        municipalityRegisterType = 1, municipalityRegisterURL = 'file:///app/dev-env/local/geo/municipality.json',
        localityRegisterType = 1, localityRegisterURL = 'file:///app/dev-env/local/geo/locality.json',
        postcodeRegisterType = 1, postcodeRegisterURL = 'file:///app/dev-env/local/geo/post.json',
        roadRegisterType = 1, roadRegisterURL = 'file:///app/dev-env/local/geo/road.json',
        accessAddressRegisterType = 1, accessAddressRegisterURL = 'file:///app/dev-env/local/geo/access.json',
        unitAddressRegisterType = 1, unitAddressRegisterURL = 'file:///app/dev-env/local/geo/unit.json',
        buildingRegisterType = 1, buildingRegisterURL = 'file:///app/dev-env/local/geo/building.json'
        END
END
GO

-- INSERT INTO DatafordelerConfig.dbo.command (commandBody, commandName, issuer, status) VALUES ('{"plugin": "cpr"}', 'pull', 'dev-env', 0)
-- INSERT INTO DatafordelerConfig.dbo.command (commandBody, commandName, issuer, status) VALUES ('{"plugin": "cvr"}', 'pull', 'dev-env', 0)
-- INSERT INTO DatafordelerConfig.dbo.command (commandBody, commandName, issuer, status) VALUES ('{"plugin": "geo"}', 'pull', 'dev-env', 0)
GO
