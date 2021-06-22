select * FROM ([Datafordeler_Test].[dbo].[cpr_person_entity] person
			JOIN [Datafordeler_Test].[dbo].[cpr_person_address_record] addr ON (addr.entity_id = person.id))
	WHERE person.personnummer = '##########'
select * FROM ([Datafordeler_Test].[dbo].[cpr_person_entity] person
			JOIN [Datafordeler_Test].[dbo].[cpr_person_status_record] civil ON (civil.entity_id = person.id))
	WHERE civil.effectTo IS NULL AND civil.registrationTo IS NULL AND civil.undone = 0 AND (civil.status = 1 OR civil.status = 3 OR civil.status = 5 OR civil.status = 7) AND person.personnummer = '##########'
	 
	 
	 -------------------------------
(SELECT DISTINCT person.personnummer FROM [Datafordeler_Test].[dbo].[cpr_person_entity] person
			JOIN [Datafordeler_Test].[dbo].[cpr_person_status_record] civil ON (civil.entity_id = person.id)
	WHERE civil.effectTo IS NULL AND civil.registrationTo IS NULL AND civil.undone = 0 AND (civil.status = 1 OR civil.status = 3 OR civil.status = 5 OR civil.status = 7))
except
(SELECT DISTINCT person.personnummer FROM [Datafordeler_Test].[dbo].[cpr_person_entity] person
			JOIN [Datafordeler_Test].[dbo].[cpr_person_address_record] addr ON (addr.entity_id = person.id)
	WHERE (addr.effectTo IS NULL AND addr.registrationTo IS NULL AND addr.undone = 0))