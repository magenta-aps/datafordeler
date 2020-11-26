--OR DROP TABLE TO REMOVE EVERYTHING
--ALL THIS IS RESTORED FROM THE FILES FROM THE CPR-SOURCE

--PERSON INFORMATION THIS IS DATA WHICH IS UPDATED BY DAILY UPDATES
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_address_coname_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_address_name_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_address_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_birthplace_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_birthplace_verification_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_birthtime_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_children_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_church_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_church_verification_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_citizenship_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_citizenship_verification_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_civilstatus_authoritytext_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_civilstatus_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_civilstatus_verification_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_core_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_custody_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_data_event_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_event_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_foreignaddress_migration_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_foreignaddress_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_guardian_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_movemunicipality_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_name_authoritytext_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_name_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_name_verification_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_number_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_parent_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_parent_verification_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_position_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_protection_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_status_record];
TRUNCATE TABLE [Datafordeler_Test].[dbo].[cpr_person_subscription_list];
DELETE [Datafordeler_Test].[dbo].[cpr_person_entity];

--ROAD DATA IS ONLY UPDATED INITIALLY BY I FILE BEING COPIED FROM THE SOURCE
DELETE [Datafordeler_Test].[dbo].[cpr_postcode];
DELETE [Datafordeler_Test].[dbo].[cpr_residence_data];
DELETE [Datafordeler_Test].[dbo].[cpr_residence_effect];
DELETE [Datafordeler_Test].[dbo].[cpr_residence_effect_cpr_residence_data];
DELETE [Datafordeler_Test].[dbo].[cpr_residence_entity];
DELETE [Datafordeler_Test].[dbo].[cpr_residence_registration];
DELETE [Datafordeler_Test].[dbo].[cpr_road_city_record];
DELETE [Datafordeler_Test].[dbo].[cpr_road_entity];
DELETE [Datafordeler_Test].[dbo].[cpr_road_memo_record];
DELETE [Datafordeler_Test].[dbo].[cpr_road_name_record];
DELETE [Datafordeler_Test].[dbo].[cpr_road_postalcode_record];

DELETE FROM [Datafordeler_Test].[dbo].[last_updated] WHERE plugin='cpr'

