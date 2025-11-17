BEGIN
    IF NOT EXISTS (SELECT * FROM DatafordelerConfig.dbo.cpr_config)
        BEGIN
        INSERT INTO DatafordelerConfig.dbo.cpr_config (
        id,
        personRegisterType, personRegisterLocalFile, personRegisterFtpAddress,
        directCustomerNumber,
        directPort,
        personRegisterDataCharset,
        roadRegisterDataCharset
        ) VALUES (
        'dk.magenta.datafordeler.cpr.CprPlugin',
        1, '/app/dev-env/local/cpr/download/d170608.l534902', 'ftp://dummy',
        0, 0, 1, 1
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
            tokenService,
            municipalityRegisterType, municipalityRegisterURL,
            localityRegisterType, localityRegisterURL, localityDeletionRegisterURL,
            postcodeRegisterType, postcodeRegisterURL,
            roadRegisterType, roadRegisterURL, roadDeletionRegisterURL,
            accessAddressRegisterType, accessAddressRegisterURL, accessAddressDeletionRegisterURL,
            unitAddressRegisterType, unitAddressRegisterURL, unitAddressDeletionRegisterURL,
            buildingRegisterType, buildingRegisterURL, buildingDeletionRegisterURL
            ) VALUES (
            'dk.magenta.datafordeler.geo.GeoPlugin', 0,
            'https://grunddatatest.nanoq.gl/portal/sharing/rest/generateToken',
            2, 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/4/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
            2, 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/3/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
            'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/10/query?where=DeletedDate>%{editDate}&outFields=*&returnGeometry=true&f=geojson',
            2, 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/5/query?where=Postnummer>0&outFields=*&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson ',
            2, 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/1/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
            'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/11/query?where=DeletedDate>%{editDate}&f=json&outFields=*&orderByFields=DeletedDate',
            2, 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/0/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
            'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/7/query?where=DeletedDate>%{editDate}&f=json&outFields=*&orderByFields=DeletedDate',
            2, 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/6/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=false&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
            'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/9/query?where=DeletedDate>%{editDate}&f=json&outFields=*&orderByFields=DeletedDate',
            2, 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/2/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
            'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/8/query?where=DeletedDate>%{editDate}&f=json&outFields=*&orderByFields=DeletedDate'

            )
        END
    ELSE
        BEGIN
        UPDATE DatafordelerConfig.dbo.geo_config SET
        tokenService = 'https://grunddatatest.nanoq.gl/portal/sharing/rest/generateToken',
        municipalityRegisterType = 2, municipalityRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/4/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
        localityRegisterType = 2, localityRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/3/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
        localityDeletionRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/10/query?where=DeletedDate>%{editDate}&outFields=*&returnGeometry=true&f=geojson',
        postcodeRegisterType = 2, postcodeRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/5/query?where=Postnummer>0&outFields=*&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
        roadRegisterType = 2, roadRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/1/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
        roadDeletionRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/11/query?where=DeletedDate>%{editDate}&f=json&outFields=*&orderByFields=DeletedDate',
        accessAddressRegisterType = 2, accessAddressRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/0/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
        accessAddressDeletionRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/7/query?where=DeletedDate>%{editDate}&f=json&outFields=*&orderByFields=DeletedDate',
        unitAddressRegisterType = 2, unitAddressRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/6/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=false&resultOffset=%{offset}&f=geojson',
        unitAddressDeletionRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/9/query?where=DeletedDate>%{editDate}&f=json&outFields=*&orderByFields=DeletedDate',
        buildingRegisterType = 2, buildingRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/2/query?where=EditDate>%{editDate}&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson',
        buildingDeletionRegisterURL = 'https://grunddatatest.nanoq.gl/server/rest/services/OperationalLayers/Grunddata/MapServer/8/query?where=DeletedDate>%{editDate}&f=json&outFields=*&orderByFields=DeletedDate'
END
END

BEGIN
    IF NOT EXISTS (SELECT * FROM DatafordelerConfig.dbo.cvr_config)
        BEGIN
        INSERT INTO DatafordelerConfig.dbo.cvr_config (
            id,
            companyRegisterDirectLookupAddress,
            companyRegisterDirectLookupPassword,
            companyRegisterDirectLookupPasswordEncrypted,
            companyRegisterPassword,
            companyRegisterPasswordEncrypted,
            companyRegisterQuery,
            companyRegisterScrollAddress,
            companyRegisterStartAddress,
            companyRegisterType,
            companyRegisterUsername,
            companyUnitRegisterPassword,
            companyUnitRegisterPasswordEncrypted,
            companyUnitRegisterQuery,
            companyUnitRegisterScrollAddress,
            companyUnitRegisterStartAddress,
            companyUnitRegisterType,
            companyUnitRegisterUsername,
            participantRegisterDirectLookupAddress,
            participantRegisterDirectLookupPassword,
            participantRegisterDirectLookupPasswordEncrypted,
            participantRegisterPassword,
            participantRegisterPasswordEncrypted,
            participantRegisterQuery,
            participantRegisterScrollAddress,
            participantRegisterStartAddress,
            participantRegisterType,
            participantRegisterUsername,
            pullCronSchedule
        ) VALUES (
            'dk.magenta.datafordeler.cvr.CvrPlugin',
            'https://erst-api.virk.dk/distribution-service-cvr-ekstern/HentAktuelVirksomhedEkstern/enhedsnr/%{cvr}',
            'password',
            NULL,
            'password',
            NULL,
            '{
                "query": {
                    "bool": {
                        "should": [
                                {
                                    "bool": {
                                        "must": [
                                {
                                    "terms": {
                                        "Vrvirksomhed.beliggenhedsadresse.kommune.kommuneKode":[954, 955, 956, 957, 958, 959, 960, 961, 962]
                                    }
                                },
                                    {
                                    "range": {
                                        "Vrvirksomhed.sidstOpdateret": {
                                            "gte": "%s"
                                        }
                                    }
                                }
                                    ]
                                }
                            },
                                                {
                                    "bool": {
                                        "must": [
                                {
                                    "terms": {
                                        "Vrvirksomhed.cvrNummer":%s
                                    }
                                },
                                    {
                                    "range": {
                                        "Vrvirksomhed.sidstOpdateret": {
                                            "gte": "%s"
                                        }
                                    }
                                }
                                    ]
                                }
                            }
                        ]
                    }
                }
            }',
            'http://distribution.virk.dk/_search/scroll',
            'http://distribution.virk.dk/cvr-permanent/virksomhed/_search',
            2,
            'username',
            'password',
            NULL,
            '{
                "query":{
                    "bool":{
                        "must": [
                            {
                                "terms": {
                                    "VrproduktionsEnhed.beliggenhedsadresse.kommune.kommuneKode":[954, 955, 956, 957, 958, 959, 960, 961, 962]
                                }
                            },
                            {
                                "range": {
                                    "VrproduktionsEnhed.sidstOpdateret": {
                                        "gte": "%s"
                                    }
                                }
                            }
                        ]
                    }
                }
            }',
            'http://distribution.virk.dk/_search/scroll',
            'http://distribution.virk.dk/cvr-permanent/produktionsenhed/_search',
            2,
            'username',
            'https://erst-api.virk.dk/distribution-service-cvr-ekstern/HentAktuelDeltagerEkstern/enhedsnr/%{unit}',
            'password',
            NULL,
            'password',
            NULL,
            '{
                "query":{
                    "bool":{
                        "must": [
                            {
                                "terms": {
                                    "Vrdeltagerperson.beliggenhedsadresse.kommune.kommuneKode":[954, 955, 956, 957, 958, 959, 960, 961, 962]
                                }
                            },
                            {
                                "range": {
                                    "Vrdeltagerperson.sidstOpdateret": {
                                        "gte": "%s"
                                    }
                                }
                            }
                        ]
                    }
                }
            }',
            'http://distribution.virk.dk/_search/scroll',
            'http://distribution.virk.dk/cvr-permanent/virksomhed/_search',
            2,
            'username',
            '0 3 * * *'
        )
        END
END
