--state

insert into [dafotestdb007].[dbo].[identification] (uuid, domain)
SELECT i.uuid, i.domain
FROM [dafotestdb005].[dbo].[gladdrreg_state_entity] e
    join [dafotestdb005].[dbo].identification i
on i.id = identification_id


insert
into [dafotestdb007].[dbo].[gladdrreg_state_entity] (identification_id)
select i2.id
from [dafotestdb005].[dbo].[gladdrreg_state_entity] e
    join [dafotestdb005].[dbo].identification i1
on i1.id = e.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
order by e.id


insert
into [dafotestdb007].[dbo].[gladdrreg_state_registration] (lastImportTime, registerChecksum, registrationFrom,
                                                           registrationTo, sequenceNumber, entity_id)
select r.lastImportTime, r.registerChecksum, r.registrationFrom, r.registrationTo, r.sequenceNumber, e2.id
from [dafotestdb005].[dbo].[gladdrreg_state_registration] r
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] e1
on e1.id = r.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] e2 on e2.identification_id = i2.id


insert
into [dafotestdb007].[dbo].[gladdrreg_state_effect] (effectFrom, effectTo, registration_id)
select v.effectFrom, v.effectTo, r2.id
from [dafotestdb005].[dbo].[gladdrreg_state_effect] v
    join [dafotestdb005].[dbo].[gladdrreg_state_registration] r1
on v.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_registration] r2 on r2.entity_id = e2.id
order by v.id


SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_state_data]
ON
insert
into [dafotestdb007].[dbo].[gladdrreg_state_data] (id, lastUpdated, active, note, registration_user, sumiffiik,
                                                   sumiffiik_domain, code, description, name, state_id)
select v2.id,
       d.lastUpdated,
       d.active,
       d.note,
       d.registration_user,
       d.sumiffiik,
       d.sumiffiik_domain,
       d.code,
       d.description,
       d.name,
       e3.identification_id
from [dafotestdb005].[dbo].[gladdrreg_state_data] d
    join [dafotestdb005].[dbo].[gladdrreg_state_effect] v1
on v1.id = d.id
    join [dafotestdb005].[dbo].[gladdrreg_state_registration] r1 on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_effect] v2 on v2.registration_id = r2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] e3 on e3.id=1
order by d.id
SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_state_data] OFF

insert
into [dafotestdb007].[dbo].[gladdrreg_state_effect_gladdrreg_state_data] (effects_id, dataitems_id)
select v2.id, v2.id
from [dafotestdb005].[dbo].[gladdrreg_state_effect] v1
    join [dafotestdb005].[dbo].[gladdrreg_state_registration] r1
on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_effect] v2 on v2.registration_id = r2.id


--municipality

insert
into [dafotestdb007].[dbo].[identification] (uuid, domain)
SELECT i.uuid, i.domain
FROM [dafotestdb005].[dbo].[gladdrreg_municipality_entity] e
    join [dafotestdb005].[dbo].identification i
on i.id = identification_id


insert
into [dafotestdb007].[dbo].[gladdrreg_municipality_entity] (identification_id)
select i2.id
from [dafotestdb005].[dbo].[gladdrreg_municipality_entity] e
    join [dafotestdb005].[dbo].identification i1
on i1.id = e.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
order by e.id


insert
into [dafotestdb007].[dbo].[gladdrreg_municipality_registration] (lastImportTime, registerChecksum, registrationFrom,
                                                                  registrationTo, sequenceNumber, entity_id)
select r.lastImportTime, r.registerChecksum, r.registrationFrom, r.registrationTo, r.sequenceNumber, e2.id
from [dafotestdb005].[dbo].[gladdrreg_municipality_registration] r
    join [dafotestdb005].[dbo].[gladdrreg_municipality_entity] e1
on e1.id = r.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_municipality_entity] e2 on e2.identification_id = i2.id


insert
into [dafotestdb007].[dbo].[gladdrreg_municipality_effect] (effectFrom, effectTo, registration_id)
select v.effectFrom, v.effectTo, r2.id
from [dafotestdb005].[dbo].[gladdrreg_municipality_effect] v
    join [dafotestdb005].[dbo].[gladdrreg_municipality_registration] r1
on v.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_municipality_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_municipality_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_municipality_registration] r2 on r2.entity_id = e2.id
order by v.id


SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_municipality_data]
ON
insert
into [dafotestdb007].[dbo].[gladdrreg_municipality_data] (id, lastUpdated, active, note, registration_user, sumiffiik,
                                                          sumiffiik_domain, code, abbrev, name, state_id)
select v2.id,
       d.lastUpdated,
       d.active,
       d.note,
       d.registration_user,
       d.sumiffiik,
       d.sumiffiik_domain,
       d.code,
       d.abbrev,
       d.name,
       s2.identification_id
from [dafotestdb005].[dbo].[gladdrreg_municipality_data] d
    join [dafotestdb005].[dbo].[gladdrreg_municipality_effect] v1
on v1.id = d.id
    join [dafotestdb005].[dbo].[gladdrreg_municipality_registration] r1 on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_municipality_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] s1 on s1.identification_id = d.state_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_municipality_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_municipality_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_municipality_effect] v2 on v2.registration_id = r2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] s2 on s2.id = s1.id
order by d.id
SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_municipality_data] OFF


insert
into [dafotestdb007].[dbo].[gladdrreg_municipality_effect_gladdrreg_municipality_data] (effects_id, dataitems_id)
select v2.id, v2.id
from [dafotestdb005].[dbo].[gladdrreg_municipality_effect] v1
    join [dafotestdb005].[dbo].[gladdrreg_municipality_registration] r1
on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_municipality_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_municipality_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_municipality_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_municipality_effect] v2 on v2.registration_id = r2.id


--postalcode

insert
into [dafotestdb007].[dbo].[identification] (uuid, domain)
SELECT i.uuid, i.domain
FROM [dafotestdb005].[dbo].[gladdrreg_postalcode_entity] e
    join [dafotestdb005].[dbo].identification i
on i.id = identification_id


insert
into [dafotestdb007].[dbo].[gladdrreg_postalcode_entity] (identification_id)
select i2.id
from [dafotestdb005].[dbo].[gladdrreg_postalcode_entity] e
    join [dafotestdb005].[dbo].identification i1
on i1.id = e.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
order by e.id


insert
into [dafotestdb007].[dbo].[gladdrreg_postalcode_registration] (lastImportTime, registerChecksum, registrationFrom,
                                                                registrationTo, sequenceNumber, entity_id)
select r.lastImportTime, r.registerChecksum, r.registrationFrom, r.registrationTo, r.sequenceNumber, e2.id
from [dafotestdb005].[dbo].[gladdrreg_postalcode_registration] r
    join [dafotestdb005].[dbo].[gladdrreg_postalcode_entity] e1
on e1.id = r.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_entity] e2 on e2.identification_id = i2.id


insert
into [dafotestdb007].[dbo].[gladdrreg_postalcode_effect] (effectFrom, effectTo, registration_id)
select v.effectFrom, v.effectTo, r2.id
from [dafotestdb005].[dbo].[gladdrreg_postalcode_effect] v
    join [dafotestdb005].[dbo].[gladdrreg_postalcode_registration] r1
on v.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_postalcode_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_registration] r2 on r2.entity_id = e2.id
order by v.id


SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_postalcode_data]
ON
insert
into [dafotestdb007].[dbo].[gladdrreg_postalcode_data] (id, lastUpdated, active, note, registration_user, sumiffiik,
                                                        sumiffiik_domain, code, name, state_id)
select v2.id,
       d.lastUpdated,
       d.active,
       d.note,
       d.registration_user,
       d.sumiffiik,
       d.sumiffiik_domain,
       d.code,
       d.name,
       s2.identification_id
from [dafotestdb005].[dbo].[gladdrreg_postalcode_data] d
    join [dafotestdb005].[dbo].[gladdrreg_postalcode_effect] v1
on v1.id = d.id
    join [dafotestdb005].[dbo].[gladdrreg_postalcode_registration] r1 on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_postalcode_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] s1 on s1.identification_id = d.state_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_effect] v2 on v2.registration_id = r2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] s2 on s2.id = s1.id
order by d.id
SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_postalcode_data] OFF


insert
into [dafotestdb007].[dbo].[gladdrreg_postalcode_effect_gladdrreg_postalcode_data] (effects_id, dataitems_id)
select v2.id, v2.id
from [dafotestdb005].[dbo].[gladdrreg_postalcode_effect] v1
    join [dafotestdb005].[dbo].[gladdrreg_postalcode_registration] r1
on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_postalcode_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_postalcode_effect] v2 on v2.registration_id = r2.id


--district


insert
into [dafotestdb007].[dbo].[identification] (uuid, domain)
SELECT i.uuid, i.domain
FROM [dafotestdb005].[dbo].[gladdrreg_district_entity] e
    join [dafotestdb005].[dbo].identification i
on i.id = identification_id


insert
into [dafotestdb007].[dbo].[gladdrreg_district_entity] (identification_id)
select i2.id
from [dafotestdb005].[dbo].[gladdrreg_district_entity] e
    join [dafotestdb005].[dbo].identification i1
on i1.id = e.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
order by e.id


insert
into [dafotestdb007].[dbo].[gladdrreg_district_registration] (lastImportTime, registerChecksum, registrationFrom,
                                                              registrationTo, sequenceNumber, entity_id)
select r.lastImportTime, r.registerChecksum, r.registrationFrom, r.registrationTo, r.sequenceNumber, e2.id
from [dafotestdb005].[dbo].[gladdrreg_district_registration] r
    join [dafotestdb005].[dbo].[gladdrreg_district_entity] e1
on e1.id = r.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_district_entity] e2 on e2.identification_id = i2.id


insert
into [dafotestdb007].[dbo].[gladdrreg_district_effect] (effectFrom, effectTo, registration_id)
select v.effectFrom, v.effectTo, r2.id
from [dafotestdb005].[dbo].[gladdrreg_district_effect] v
    join [dafotestdb005].[dbo].[gladdrreg_district_registration] r1
on v.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_district_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_district_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_district_registration] r2 on r2.entity_id = e2.id
order by v.id


SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_district_data]
ON
insert
into [dafotestdb007].[dbo].[gladdrreg_district_data] (id, lastUpdated, active, note, registration_user, sumiffiik,
                                                      sumiffiik_domain, abbrev, code, name, state_id)
select v2.id,
       d.lastUpdated,
       d.active,
       d.note,
       d.registration_user,
       d.sumiffiik,
       d.sumiffiik_domain,
       d.abbrev,
       d.code,
       d.name,
       s2.identification_id
from [dafotestdb005].[dbo].[gladdrreg_district_data] d
    join [dafotestdb005].[dbo].[gladdrreg_district_effect] v1
on v1.id = d.id
    join [dafotestdb005].[dbo].[gladdrreg_district_registration] r1 on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_district_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] s1 on s1.identification_id = d.state_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_district_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_district_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_district_effect] v2 on v2.registration_id = r2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] s2 on s2.id = s1.id
order by d.id
SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_district_data] OFF


insert
into [dafotestdb007].[dbo].[gladdrreg_district_effect_gladdrreg_district_data] (effects_id, dataitems_id)
select v2.id, v2.id
from [dafotestdb005].[dbo].[gladdrreg_district_effect] v1
    join [dafotestdb005].[dbo].[gladdrreg_district_registration] r1
on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_district_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_district_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_district_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_district_effect] v2 on v2.registration_id = r2.id


--locality

insert
into [dafotestdb007].[dbo].[identification] (uuid, domain)
SELECT i.uuid, i.domain
FROM [dafotestdb005].[dbo].[gladdrreg_locality_entity] e
    join [dafotestdb005].[dbo].identification i
on i.id = identification_id


insert
into [dafotestdb007].[dbo].[gladdrreg_locality_entity] (identification_id)
select i2.id
from [dafotestdb005].[dbo].[gladdrreg_locality_entity] e
    join [dafotestdb005].[dbo].identification i1
on i1.id = e.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
order by e.id


insert
into [dafotestdb007].[dbo].[gladdrreg_locality_registration] (lastImportTime, registerChecksum, registrationFrom,
                                                              registrationTo, sequenceNumber, entity_id)
select r.lastImportTime, r.registerChecksum, r.registrationFrom, r.registrationTo, r.sequenceNumber, e2.id
from [dafotestdb005].[dbo].[gladdrreg_locality_registration] r
    join [dafotestdb005].[dbo].[gladdrreg_locality_entity] e1
on e1.id = r.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_locality_entity] e2 on e2.identification_id = i2.id


insert
into [dafotestdb007].[dbo].[gladdrreg_locality_effect] (effectFrom, effectTo, registration_id)
select v.effectFrom, v.effectTo, r2.id
from [dafotestdb005].[dbo].[gladdrreg_locality_effect] v
    join [dafotestdb005].[dbo].[gladdrreg_locality_registration] r1
on v.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_locality_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_locality_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_locality_registration] r2 on r2.entity_id = e2.id
order by v.id


SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_locality_data]
ON
insert
into [dafotestdb007].[dbo].[gladdrreg_locality_data] (id, lastUpdated, active, note, registration_user, sumiffiik,
                                                      sumiffiik_domain, abbrev, code, localityState, name, type,
                                                      state_id, district_id, municipality_id, postalCode_id)
select v2.id,
       d.lastUpdated,
       d.active,
       d.note,
       d.registration_user,
       d.sumiffiik,
       d.sumiffiik_domain,
       d.abbrev,
       d.code,
       d.localityState,
       d.name,
       d.type,
       s2.identification_id,
       diu2.id,
       mi2.id,
       pi2.id
from [dafotestdb005].[dbo].[gladdrreg_locality_data] d
    join [dafotestdb005].[dbo].[gladdrreg_locality_effect] v1
on v1.id = d.id
    join [dafotestdb005].[dbo].[gladdrreg_locality_registration] r1 on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_locality_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] s1 on s1.identification_id = d.state_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_locality_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_locality_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_locality_effect] v2 on v2.registration_id = r2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] s2 on s2.id = s1.id
    join [dafotestdb005].[dbo].[gladdrreg_district_entity] di1 on di1.identification_id = d.district_id
    join [dafotestdb005].[dbo].[identification] diu1 on diu1.id = di1.identification_id
    join [dafotestdb007].[dbo].[identification] diu2 on diu2.uuid = diu1.uuid
    join [dafotestdb005].[dbo].[gladdrreg_municipality_entity] m1 on m1.identification_id = d.municipality_id
    join [dafotestdb005].[dbo].[identification] mi1 on mi1.id = m1.identification_id
    join [dafotestdb007].[dbo].[identification] mi2 on mi2.uuid = mi1.uuid
    left join [dafotestdb005].[dbo].[gladdrreg_postalcode_entity] p1 on p1.identification_id = d.postalCode_id
    left join [dafotestdb005].[dbo].[identification] pi1 on pi1.id = p1.identification_id
    left join [dafotestdb007].[dbo].[identification] pi2 on pi2.uuid = pi1.uuid
order by d.id
SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_locality_data] OFF


insert
into [dafotestdb007].[dbo].[gladdrreg_locality_effect_gladdrreg_locality_data] (effects_id, dataitems_id)
select v2.id, v2.id
from [dafotestdb005].[dbo].[gladdrreg_locality_effect] v1
    join [dafotestdb005].[dbo].[gladdrreg_locality_registration] r1
on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_locality_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_locality_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_locality_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_locality_effect] v2 on v2.registration_id = r2.id


--roads

insert
into [dafotestdb007].[dbo].[identification] (uuid, domain)
SELECT i.uuid, i.domain
FROM [dafotestdb005].[dbo].[gladdrreg_road_entity] e
    join [dafotestdb005].[dbo].identification i
on i.id = identification_id


insert
into [dafotestdb007].[dbo].[gladdrreg_road_entity] (identification_id)
select i2.id
from [dafotestdb005].[dbo].[gladdrreg_road_entity] e
    join [dafotestdb005].[dbo].identification i1
on i1.id = e.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
order by e.id


insert
into [dafotestdb007].[dbo].[gladdrreg_road_registration] (lastImportTime, registerChecksum, registrationFrom,
                                                          registrationTo, sequenceNumber, entity_id)
select r.lastImportTime, r.registerChecksum, r.registrationFrom, r.registrationTo, r.sequenceNumber, e2.id
from [dafotestdb005].[dbo].[gladdrreg_road_registration] r
    join [dafotestdb005].[dbo].[gladdrreg_road_entity] e1
on e1.id = r.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_road_entity] e2 on e2.identification_id = i2.id


insert
into [dafotestdb007].[dbo].[gladdrreg_road_effect] (effectFrom, effectTo, registration_id)
select v.effectFrom, v.effectTo, r2.id
from [dafotestdb005].[dbo].[gladdrreg_road_effect] v
    join [dafotestdb005].[dbo].[gladdrreg_road_registration] r1
on v.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_road_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_road_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_road_registration] r2 on r2.entity_id = e2.id
order by v.id


SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_road_data]
ON
insert
into [dafotestdb007].[dbo].[gladdrreg_road_data] (id, lastUpdated, active, note, registration_user, sumiffiik,
                                                  sumiffiik_domain, alternateName, code, cprName, name, shortName,
                                                  state_id, location_id, municipality_id)
select v2.id,
       d.lastUpdated,
       d.active,
       d.note,
       d.registration_user,
       d.sumiffiik,
       d.sumiffiik_domain,
       d.alternateName,
       d.code,
       d.cprName,
       d.name,
       d.shortName,
       s2.identification_id,
       lu2.id,
       mi2.id
from [dafotestdb005].[dbo].[gladdrreg_road_data] d
    join [dafotestdb005].[dbo].[gladdrreg_road_effect] v1
on v1.id = d.id
    join [dafotestdb005].[dbo].[gladdrreg_road_registration] r1 on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_road_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] s1 on s1.identification_id = d.state_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_road_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_road_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_road_effect] v2 on v2.registration_id = r2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] s2 on s2.id = s1.id
    left join [dafotestdb005].[dbo].[gladdrreg_locality_entity] l1 on l1.identification_id = d.location_id
    left join [dafotestdb005].[dbo].[identification] lu1 on lu1.id = l1.identification_id
    left join [dafotestdb007].[dbo].[identification] lu2 on lu2.uuid = lu1.uuid
    left join [dafotestdb005].[dbo].[gladdrreg_municipality_entity] m1 on m1.identification_id = d.municipality_id
    left join [dafotestdb005].[dbo].[identification] mi1 on mi1.id = m1.identification_id
    left join [dafotestdb007].[dbo].[identification] mi2 on mi2.uuid = mi1.uuid

order by d.id
SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_road_data] OFF


insert
into [dafotestdb007].[dbo].[gladdrreg_road_effect_gladdrreg_road_data] (effects_id, dataitems_id)
select v2.id, v2.id
from [dafotestdb005].[dbo].[gladdrreg_road_effect] v1
    join [dafotestdb005].[dbo].[gladdrreg_road_registration] r1
on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_road_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_road_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_road_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_road_effect] v2 on v2.registration_id = r2.id


--bnumre


insert
into [dafotestdb007].[dbo].[identification] (uuid, domain)
SELECT i.uuid, i.domain
FROM [dafotestdb005].[dbo].[gladdrreg_bnumber_entity] e
    join [dafotestdb005].[dbo].identification i
on i.id = identification_id


insert
into [dafotestdb007].[dbo].[gladdrreg_bnumber_entity] (identification_id)
select i2.id
from [dafotestdb005].[dbo].[gladdrreg_bnumber_entity] e
    join [dafotestdb005].[dbo].identification i1
on i1.id = e.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
order by e.id


insert
into [dafotestdb007].[dbo].[gladdrreg_bnumber_registration] (lastImportTime, registerChecksum, registrationFrom,
                                                             registrationTo, sequenceNumber, entity_id)
select r.lastImportTime, r.registerChecksum, r.registrationFrom, r.registrationTo, r.sequenceNumber, e2.id
from [dafotestdb005].[dbo].[gladdrreg_bnumber_registration] r
    join [dafotestdb005].[dbo].[gladdrreg_bnumber_entity] e1
on e1.id = r.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_entity] e2 on e2.identification_id = i2.id


insert
into [dafotestdb007].[dbo].[gladdrreg_bnumber_effect] (effectFrom, effectTo, registration_id)
select v.effectFrom, v.effectTo, r2.id
from [dafotestdb005].[dbo].[gladdrreg_bnumber_effect] v
    join [dafotestdb005].[dbo].[gladdrreg_bnumber_registration] r1
on v.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_bnumber_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_registration] r2 on r2.entity_id = e2.id
order by v.id


SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_bnumber_data]
ON
insert
into [dafotestdb007].[dbo].[gladdrreg_bnumber_data] (id, lastUpdated, active, note, registration_user, sumiffiik,
                                                     sumiffiik_domain, b_callname, b_type, code, state_id, location_id,
                                                     municipality_id)
select v2.id,
       d.lastUpdated,
       d.active,
       d.note,
       d.registration_user,
       d.sumiffiik,
       d.sumiffiik_domain,
       d.b_callname,
       d.b_type,
       d.code,
       s2.identification_id,
       lu2.id,
       mi2.id
from [dafotestdb005].[dbo].[gladdrreg_bnumber_data] d
    join [dafotestdb005].[dbo].[gladdrreg_bnumber_effect] v1
on v1.id = d.id
    join [dafotestdb005].[dbo].[gladdrreg_bnumber_registration] r1 on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_bnumber_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] s1 on s1.identification_id = d.state_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_effect] v2 on v2.registration_id = r2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] s2 on s2.id = s1.id
    left join [dafotestdb005].[dbo].[gladdrreg_locality_entity] l1 on l1.identification_id = d.location_id
    left join [dafotestdb005].[dbo].[identification] lu1 on lu1.id = l1.identification_id
    left join [dafotestdb007].[dbo].[identification] lu2 on lu2.uuid = lu1.uuid
    left join [dafotestdb005].[dbo].[gladdrreg_municipality_entity] m1 on m1.identification_id = d.municipality_id
    left join [dafotestdb005].[dbo].[identification] mi1 on mi1.id = m1.identification_id
    left join [dafotestdb007].[dbo].[identification] mi2 on mi2.uuid = mi1.uuid

order by d.id
SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_bnumber_data] OFF


insert
into [dafotestdb007].[dbo].[gladdrreg_bnumber_effect_gladdrreg_bnumber_data] (effects_id, dataitems_id)
select v2.id, v2.id
from [dafotestdb005].[dbo].[gladdrreg_bnumber_effect] v1
    join [dafotestdb005].[dbo].[gladdrreg_bnumber_registration] r1
on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_bnumber_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_bnumber_effect] v2 on v2.registration_id = r2.id


--adresser


insert
into [dafotestdb007].[dbo].[identification] (uuid, domain)
SELECT i.uuid, i.domain
FROM [dafotestdb005].[dbo].[gladdrreg_address_entity] e
    join [dafotestdb005].[dbo].identification i
on i.id = identification_id


insert
into [dafotestdb007].[dbo].[gladdrreg_address_entity] (identification_id)
select i2.id
from [dafotestdb005].[dbo].[gladdrreg_address_entity] e
    join [dafotestdb005].[dbo].identification i1
on i1.id = e.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
order by e.id


insert
into [dafotestdb007].[dbo].[gladdrreg_address_registration] (lastImportTime, registerChecksum, registrationFrom,
                                                             registrationTo, sequenceNumber, entity_id)
select r.lastImportTime, r.registerChecksum, r.registrationFrom, r.registrationTo, r.sequenceNumber, e2.id
from [dafotestdb005].[dbo].[gladdrreg_address_registration] r
    join [dafotestdb005].[dbo].[gladdrreg_address_entity] e1
on e1.id = r.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_address_entity] e2 on e2.identification_id = i2.id


insert
into [dafotestdb007].[dbo].[gladdrreg_address_effect] (effectFrom, effectTo, registration_id)
select v.effectFrom, v.effectTo, r2.id
from [dafotestdb005].[dbo].[gladdrreg_address_effect] v
    join [dafotestdb005].[dbo].[gladdrreg_address_registration] r1
on v.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_address_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_address_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_address_registration] r2 on r2.entity_id = e2.id
order by v.id


SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_address_data]
ON
insert
into [dafotestdb007].[dbo].[gladdrreg_address_data] (id, lastUpdated, active, note, registration_user, sumiffiik,
                                                     sumiffiik_domain, floor, houseNumber, residence, room, state_id,
                                                     bNumber_id, municipality_id, road_id)
select v2.id,
       d.lastUpdated,
       d.active,
       d.note,
       d.registration_user,
       d.sumiffiik,
       d.sumiffiik_domain,
       d.floor,
       d.houseNumber,
       d.residence,
       d.room,
       s2.identification_id,
       bu2.id,
       mi2.id,
       ri2.id
from [dafotestdb005].[dbo].[gladdrreg_address_data] d
    join [dafotestdb005].[dbo].[gladdrreg_address_effect] v1
on v1.id = d.id
    join [dafotestdb005].[dbo].[gladdrreg_address_registration] r1 on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_address_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].[gladdrreg_state_entity] s1 on s1.identification_id = d.state_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_address_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_address_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_address_effect] v2 on v2.registration_id = r2.id
    join [dafotestdb007].[dbo].[gladdrreg_state_entity] s2 on s2.id = s1.id
    left join [dafotestdb005].[dbo].[gladdrreg_bnumber_entity] b1 on b1.identification_id = d.bNumber_id
    left join [dafotestdb005].[dbo].[identification] bu1 on bu1.id = b1.identification_id
    left join [dafotestdb007].[dbo].[identification] bu2 on bu2.uuid = bu1.uuid
    left join [dafotestdb005].[dbo].[gladdrreg_municipality_entity] m1 on m1.identification_id = d.municipality_id
    left join [dafotestdb005].[dbo].[identification] mi1 on mi1.id = m1.identification_id
    left join [dafotestdb007].[dbo].[identification] mi2 on mi2.uuid = mi1.uuid
    left join [dafotestdb005].[dbo].[gladdrreg_road_entity] ro1 on ro1.identification_id = d.road_id
    left join [dafotestdb005].[dbo].[identification] ri1 on ri1.id = ro1.identification_id
    left join [dafotestdb007].[dbo].[identification] ri2 on ri2.uuid = ri1.uuid

order by d.id
SET IDENTITY_INSERT [dafotestdb007].[dbo].[gladdrreg_address_data] OFF


insert
into [dafotestdb007].[dbo].[gladdrreg_address_effect_gladdrreg_address_data] (effects_id, dataitems_id)
select v2.id, v2.id
from [dafotestdb005].[dbo].[gladdrreg_address_effect] v1
    join [dafotestdb005].[dbo].[gladdrreg_address_registration] r1
on v1.registration_id = r1.id
    join [dafotestdb005].[dbo].[gladdrreg_address_entity] e1 on e1.id = r1.entity_id
    join [dafotestdb005].[dbo].identification i1 on i1.id = e1.identification_id
    join [dafotestdb007].[dbo].[identification] i2 on i2.uuid = i1.uuid
    join [dafotestdb007].[dbo].[gladdrreg_address_entity] e2 on e2.identification_id = i2.id
    join [dafotestdb007].[dbo].[gladdrreg_address_registration] r2 on r2.entity_id = e2.id
    join [dafotestdb007].[dbo].[gladdrreg_address_effect] v2 on v2.registration_id = r2.id
    SET IDENTITY_INSERT [dafotestdb007].[dbo].[record_collection] ON
insert
into [dafotestdb007].[dbo].[record_collection] (id)
SELECT r.id
FROM [dafotestdb005].[dbo].[record_collection] r
SET IDENTITY_INSERT [dafotestdb007].[dbo].[record_collection] OFF


SET IDENTITY_INSERT [dafotestdb007].[dbo].[record]
ON
insert
into [dafotestdb007].[dbo].[record] (id, sourceData, sourceReference, timestamp, collection_id)
SELECT r.id, r.sourceData, r.sourceReference, r.timestamp, r.collection_id
FROM [dafotestdb005].[dbo].[record] r
SET IDENTITY_INSERT [dafotestdb007].[dbo].[record] OFF
