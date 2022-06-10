-- Find forældre til et barn
SELECT * FROM 
	Datafordeler_Test.[dbo].[cpr_person_birthtime_record] birth
	JOIN Datafordeler_Test.[dbo].[cpr_person_entity] person ON (
		person.id=birth.entity_id
	)
	JOIN Datafordeler_Test.[dbo].[cpr_person_parent_record] parent ON (
		parent.entity_id = person.id
	)
  where person.personnummer='##########'


-- Find aktuelle persondetaljer
SELECT * FROM
	Datafordeler_Test.[dbo].[cpr_person_entity] person
			JOIN Datafordeler_Test.[dbo].[cpr_person_address_record] addreaarecord ON (addreaarecord.entity_id = person.id)
			JOIN Datafordeler_Test.[dbo].[cpr_person_name_record] namer ON (namer.entity_id = person.id)
			 where namer.firstNames = '****' and namer.lastName = '*****' AND
			 addreaarecord.effectTo is NULL AND addreaarecord.registrationTo is NULL AND addreaarecord.undone = 0


--Find personer med over 100 adresseskift
SELECT DISTINCT person.personnummer FROM
	Datafordeler_Test.[dbo].[cpr_person_entity] person
			JOIN Datafordeler_Test.[dbo].[cpr_person_address_record] statusi ON (statusi.entity_id = person.id)
WHERE
personnummer in
     (SELECT personnummer FROM (Datafordeler_Test.[dbo].[cpr_person_entity] person
			JOIN Datafordeler_Test.[dbo].[cpr_person_address_record] statusi ON (statusi.entity_id = person.id))
	WHERE statusi.registrationTo IS NULL
	 GROUP BY personnummer HAVING COUNT(personnummer)>100);

--Find personer med administrative adresser
SELECT person.personnummer FROM
	[dafodb007].[dbo].[cpr_person_entity] person
			JOIN [dafodb007].[dbo].[cpr_person_address_record] statusi ON (statusi.entity_id = person.id)
WHERE statusi.roadCode>5000
and registrationTo is null and effectTo is null
