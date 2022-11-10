-- Find productionunits for cvr-no
SELECT *
FROM [Datafordeler_Test].[dbo].[cvr_record_company] company
    JOIN [Datafordeler_Test].[dbo].[cvr_record_company_unit_relation] unit
ON (unit.companyRecord_id = company.id)
WHERE cvrNumber = ########

-- Find companydetails
SELECT *-- DISTINCT company.cvrNumber, name1.name
FROM [Datafordeler_Test].[dbo].[cvr_record_company] company
    JOIN [Datafordeler_Test].[dbo].[cvr_record_address] addressItem
ON (addressItem.companyRecord_id = company.id)
    JOIN [Datafordeler_Test].[dbo].[cvr_record_company_metadata] metadata ON (metadata.companyRecord_id = company.id)
    JOIN [Datafordeler_Test].[dbo].[cvr_record_name1] name1 ON (name1.companyMetadataRecord_id = metadata.id)
    JOIN [Datafordeler_Test].[dbo].[cvr_record_address_municipality] add_municipality ON (add_municipality.id = addressItem.municipality_id)
    JOIN [Datafordeler_Test].[dbo].[cvr_municipality] municipality ON (municipality.id = add_municipality.municipality_id)

    --WHERE municipality.code < 955 AND addressItem.validTo IS NULL
WHERE company.cvrNumber='########'
  AND addressItem.validTo IS NULL
  AND name1.validTo IS NULL
  AND add_municipality.validTo IS NULL
  AND metadata.validTo IS NULL

-- Find cvr-numrene for aktive p-numre uden cvr-numre
SELECT DISTINCT newestCvrRelation
FROM [Datafordeler_Test].[dbo].[cvr_record_unit_metadata]
WHERE aggregateStatus = 'Aktiv'
  AND (newestCvrRelation)
    NOT IN
    (SELECT company.cvrNumber FROM [Datafordeler_Test].[dbo].[cvr_record_company] company
    JOIN [Datafordeler_Test].[dbo].[cvr_record_company_metadata] metadata ON (metadata.companyRecord_id = company.id)
    WHERE company.validTo IS NULL)


--Slet indhold i "cvr_config"
    TRUNCATE TABLE [Datafordeler_Test].[dbo].[cvr_config]

--Kopier indhold fra tabellen cvr_config_bck til cvr_config
INSERT
INTO [Datafordeler_Test].[dbo].[cvr_config] (id, companyRegisterDirectLookupAddress,
                                             companyRegisterDirectLookupCertificate,
                                             companyRegisterDirectLookupPassword,
                                             companyRegisterDirectLookupPasswordEncrypted, companyRegisterPassword,
                                             companyRegisterPasswordEncrypted, companyRegisterQuery,
                                             companyRegisterScrollAddress,
                                             companyRegisterStartAddress, companyRegisterType, companyRegisterUsername,
                                             companyUnitRegisterPassword, companyUnitRegisterPasswordEncrypted,
                                             companyUnitRegisterQuery, companyUnitRegisterScrollAddress,
                                             companyUnitRegisterStartAddress, companyUnitRegisterType,
                                             companyUnitRegisterUsername, participantRegisterDirectLookupAddress,
                                             participantRegisterDirectLookupCertificate,
                                             participantRegisterDirectLookupPassword,
                                             participantRegisterDirectLookupPasswordEncrypted,
                                             participantRegisterPassword, participantRegisterPasswordEncrypted,
                                             participantRegisterQuery, participantRegisterScrollAddress,
                                             participantRegisterStartAddress, participantRegisterType,
                                             participantRegisterUsername, pullCronSchedule)
SELECT id,
       companyRegisterDirectLookupAddress,
       companyRegisterDirectLookupCertificate,
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
       participantRegisterDirectLookupCertificate,
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
FROM [Datafordeler_Test].[dbo].[cvr_config_bck]


--HQL-version af "Find cvr-numrene for aktive p-numre uden cvr-numre"
    String hql_companies = "SELECT DISTINCT companyUnit.newestCvrRelation FROM " + CompanyUnitMetadataRecord.class.getCanonicalName() + " companyUnit " +
    "WHERE aggregateStatus = 'Aktiv'" +
    "AND (newestCvrRelation) NOT IN " +
    "(SELECT company.cvrNumber FROM " + CompanyRecord.class.getCanonicalName() + " company " +
    "JOIN "+CompanyMetadataRecord.class.getCanonicalName()+" companyMetadata ON company.id"+"=companyMetadata."+CompanyMetadataRecord.DB_FIELD_COMPANY+")";
