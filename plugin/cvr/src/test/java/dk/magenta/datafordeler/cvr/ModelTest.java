package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.cvr.data.company.*;
import dk.magenta.datafordeler.cvr.data.companyunit.*;
import dk.magenta.datafordeler.cvr.data.participant.*;
import dk.magenta.datafordeler.cvr.data.shared.ParticipantLink;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.CompanyForm;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;
import dk.magenta.datafordeler.cvr.data.unversioned.Municipality;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class ModelTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Test
    public void testCompany() throws DataFordelerException, JsonProcessingException {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        CompanyEntity company = new CompanyEntity();
        try {
            Identification identification = new Identification(UUID.randomUUID(), "test");
            company.setIdentifikation(identification);
            company.setCvrNumber(78975790);

            CompanyRegistration registrering = new CompanyRegistration(OffsetDateTime.parse("2017-01-01T00:00:00+00:00"), OffsetDateTime.parse("2018-01-01T00:00:00+00:00"), 1);
            CompanyEffect effect1 = new CompanyEffect(registrering, OffsetDateTime.parse("2017-07-01T00:00:00+00:00"), null);
            CompanyEffect effect2 = new CompanyEffect(registrering, OffsetDateTime.parse("2017-08-01T00:00:00+00:00"), null);

            CompanyBaseData companyBase1 = new CompanyBaseData();
            companyBase1.addEffect(effect1);

            CompanyBaseData companyBase2 = new CompanyBaseData();
            companyBase2.addEffect(effect2);

            companyBase1.setLivsforloebStart(OffsetDateTime.parse("2000-01-01T00:00:00+00:00"));


            companyBase1.setAdvertProtection(true);

            companyBase1.setCompanyName("Some company name");


            Municipality municipality = Municipality.getMunicipality(101, "Copenhagen", session);

            HashMap<String, Object> adresse1 = new HashMap<>();
            adresse1.put(Address.DB_FIELD_ROADNAME, "FoobarRoad");
            adresse1.put(Address.DB_FIELD_ROADCODE, 1234);
            adresse1.put(Address.DB_FIELD_HOUSE_FROM, "12");
            adresse1.put(Address.DB_FIELD_MUNICIPALITY, municipality);
            Address locationAddress = QueryManager.getItem(session, Address.class, adresse1);
            if (locationAddress == null) {
                locationAddress = new Address();
                locationAddress.setRoadName((String) adresse1.get(Address.DB_FIELD_ROADNAME));
                locationAddress.setRoadCode((Integer) adresse1.get(Address.DB_FIELD_ROADCODE));
                locationAddress.setHouseNumberFrom((String) adresse1.get(Address.DB_FIELD_HOUSE_FROM));
                locationAddress.setMunicipality(municipality);
                session.saveOrUpdate(locationAddress);
            }
            companyBase1.setLocationAddress(locationAddress);


            HashMap<String, Object> adresse2 = new HashMap<>();
            adresse2.put(Address.DB_FIELD_ROADNAME, "HelloWorldRoad");
            adresse2.put(Address.DB_FIELD_ROADCODE, 5678);
            adresse2.put(Address.DB_FIELD_HOUSE_FROM, "34");
            adresse2.put(Address.DB_FIELD_MUNICIPALITY, municipality);
            Address postalAddress = QueryManager.getItem(session, Address.class, adresse2);
            if (postalAddress == null) {
                postalAddress = new Address();
                postalAddress.setRoadName((String) adresse2.get(Address.DB_FIELD_ROADNAME));
                postalAddress.setRoadCode((Integer) adresse2.get(Address.DB_FIELD_ROADCODE));
                postalAddress.setHouseNumberFrom((String) adresse2.get(Address.DB_FIELD_HOUSE_FROM));
                postalAddress.setMunicipality(municipality);
                session.saveOrUpdate(postalAddress);
            }
            companyBase1.setPostalAddress(postalAddress);


            Industry primaryIndustry = QueryManager.getItem(session, Industry.class, Collections.singletonMap(Industry.DB_FIELD_CODE, "123456"));
            if (primaryIndustry == null) {
                primaryIndustry = new Industry();
                primaryIndustry.setIndustryCode("123456");
                primaryIndustry.setIndustryText("It company");
                session.saveOrUpdate(primaryIndustry);
            }
            companyBase2.setPrimaryIndustry(primaryIndustry);


            Industry secondaryIndustry1 = QueryManager.getItem(session, Industry.class, Collections.singletonMap(Industry.DB_FIELD_CODE, "112358"));
            if (secondaryIndustry1 == null) {
                secondaryIndustry1 = new Industry();
                secondaryIndustry1.setIndustryCode("112358");
                secondaryIndustry1.setIndustryText("Psychiatric institution");
                session.saveOrUpdate(secondaryIndustry1);
            }
            companyBase2.setSecondaryIndustry1(secondaryIndustry1);

            companyBase1.setPhoneNumber("87654321", false);
            companyBase1.setFaxNumber("11112222", false);
            companyBase1.setEmailAddress("test@example.com", false);

            CompanyForm companyForm = QueryManager.getItem(session, CompanyForm.class, Collections.singletonMap(CompanyForm.DB_FIELD_CODE, "123"));
            if (companyForm == null) {
                companyForm = new CompanyForm();
                companyForm.setCompanyFormCode("123");
                companyForm.setShortDescription("A/S");
                companyForm.setResponsibleDataSource("E&S");
                session.saveOrUpdate(companyForm);
            }
            companyBase1.setCompanyForm(companyForm);

            companyBase1.setYearlyEmployeeNumbers(2017, 1, 2, 1, 2, 1, 2);

            companyBase1.setQuarterlyEmployeeNumbers(2017, 2, 1, 2, 1, 2, 1, 2);

            ParticipantLink participantLink = QueryManager.getItem(session, ParticipantLink.class, Collections.singletonMap(ParticipantLink.DB_FIELD_VALUE, 44446666));
            if (participantLink == null) {
                participantLink = new ParticipantLink();
                participantLink.setValue(44446666);
                session.saveOrUpdate(participantLink);
            }
            companyBase1.addParticipant(participantLink);


            CompanyUnitLink productionUnit = QueryManager.getItem(session, CompanyUnitLink.class, Collections.singletonMap(CompanyUnitLink.DB_FIELD_PNUMBER, 314159265));
            if (productionUnit == null) {
                productionUnit = new CompanyUnitLink();
                productionUnit.setpNumber(314159265);
                session.saveOrUpdate(productionUnit);
            }

            CompanyEffect effect3 = new CompanyEffect(registrering, OffsetDateTime.parse("2017-09-01T00:00:00+00:00"), null);
            CompanyBaseData companyBase3 = new CompanyBaseData();
            companyBase3.addEffect(effect3);
            companyBase3.addCompanyUnit(314159265, null);


            QueryManager.saveRegistration(session, company, registrering);
            transaction.commit();


            CompanyQuery companyQuery = new CompanyQuery();
            companyQuery.setVirksomhedsnavn("Some company name");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, companyQuery, CompanyEntity.class).size());

            companyQuery = new CompanyQuery();
            companyQuery.setCvrNumre("78975790");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, companyQuery, CompanyEntity.class).size());

            companyQuery = new CompanyQuery();
            companyQuery.setVirksomhedsform("123");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, companyQuery, CompanyEntity.class).size());

            companyQuery = new CompanyQuery();
            companyQuery.setTelefonnummer("87654321");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, companyQuery, CompanyEntity.class).size());

            companyQuery = new CompanyQuery();
            companyQuery.setTelefaxnummer("11112222");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, companyQuery, CompanyEntity.class).size());

            companyQuery = new CompanyQuery();
            companyQuery.setEmailadresse("test@example.com");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, companyQuery, CompanyEntity.class).size());

        } finally {
            try {
                transaction = session.beginTransaction();
                session.delete(company);
                transaction.commit();
            } catch (Exception e) {}
            session.close();
            QueryManager.clearCaches();
        }
    }

    @Test
    public void testCompanyUnit() throws DataFordelerException, JsonProcessingException {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        try {
            Identification companyIdentification = new Identification(UUID.randomUUID(), "test");
            CompanyEntity company = new CompanyEntity();
            company.setIdentifikation(companyIdentification);
            company.setCvrNumber(24681012);

            Identification unitIdentification = new Identification(UUID.randomUUID(), "test");
            CompanyUnitEntity unit = new CompanyUnitEntity();
            unit.setIdentifikation(unitIdentification);
            Assert.assertEquals(unitIdentification, unit.getIdentification());

            unit.setPNumber(11223344);
            Assert.assertEquals(11223344, unit.getPNumber());

            CompanyUnitRegistration registrering = new CompanyUnitRegistration(OffsetDateTime.parse("2017-01-01T00:00:00+00:00"), OffsetDateTime.parse("2018-01-01T00:00:00+00:00"), 1);
            CompanyUnitEffect virkning = new CompanyUnitEffect(registrering, OffsetDateTime.parse("2017-07-01T00:00:00+00:00"), null);

            CompanyUnitBaseData unitBase = new CompanyUnitBaseData();
            unitBase.addEffect(virkning);

            unitBase.setIsPrimary(true);
            Assert.assertEquals(true, unitBase.getIsPrimary());
            unitBase.setIsPrimary(false);
            Assert.assertEquals(false, unitBase.getIsPrimary());

            OffsetDateTime startDate = OffsetDateTime.parse("2000-01-01T00:00:00+00:00");
            unitBase.setLifecycleStart(startDate);
            Assert.assertTrue(startDate.isEqual(unitBase.getLifecycleData().getStartDate()));
            Assert.assertNull(unitBase.getLifecycleData().getEndDate());


            unitBase.setName("Some company unit name");
            Assert.assertEquals("Some company unit name", unitBase.getName());
            unitBase.addAssociatedCvrNumber(24681012L);
            System.out.println(unitBase.getAssociatedCvrNumber());
            Assert.assertTrue(unitBase.getAssociatedCvrNumber().contains(24681012L));


            Municipality kommune = QueryManager.getItem(session, Municipality.class, Collections.singletonMap(Municipality.DB_FIELD_CODE, 101));
            if (kommune == null) {
                kommune = new Municipality();
                kommune.setCode(101);
                kommune.setName("Copenhagen");
                session.saveOrUpdate(kommune);
            }

            HashMap<String, Object> adresse = new HashMap<>();
            adresse.put(Address.DB_FIELD_ROADNAME, "FoobarRoad");
            adresse.put(Address.DB_FIELD_ROADCODE, 1234);
            adresse.put(Address.DB_FIELD_HOUSE_FROM, "12");
            adresse.put(Address.DB_FIELD_MUNICIPALITY, kommune);
            Address beliggenhedsadresse = QueryManager.getItem(session, Address.class, adresse);
            if (beliggenhedsadresse == null) {
                beliggenhedsadresse = new Address();
                beliggenhedsadresse.setRoadName((String) adresse.get(Address.DB_FIELD_ROADNAME));
                beliggenhedsadresse.setRoadCode((Integer) adresse.get(Address.DB_FIELD_ROADCODE));
                beliggenhedsadresse.setHouseNumberFrom((String) adresse.get(Address.DB_FIELD_HOUSE_FROM));
                beliggenhedsadresse.setMunicipality(kommune);
                session.saveOrUpdate(beliggenhedsadresse);
            }
            unitBase.setLocationAddress(beliggenhedsadresse);
            Assert.assertEquals(101, unitBase.getLocationAddress().getMunicipality().getCode());
            Assert.assertEquals("Copenhagen", unitBase.getLocationAddress().getMunicipality().getName());
            Assert.assertEquals("FoobarRoad", unitBase.getLocationAddress().getRoadName());
            Assert.assertEquals(1234, unitBase.getLocationAddress().getRoadCode());
            Assert.assertEquals("12", unitBase.getLocationAddress().getHouseNumberFrom());


            Industry primaryIndustry = QueryManager.getItem(session, Industry.class, Collections.singletonMap(Industry.DB_FIELD_CODE, "123456"));
            if (primaryIndustry == null) {
                primaryIndustry = new Industry();
                primaryIndustry.setIndustryCode("123456");
                primaryIndustry.setIndustryText("It company");
                session.saveOrUpdate(primaryIndustry);
            }
            unitBase.setPrimaryIndustry(primaryIndustry);
            Assert.assertEquals("123456", unitBase.getPrimaryIndustry().getIndustryCode());


            unitBase.setPhoneNumber("87654321", false);
            Assert.assertEquals("87654321", unitBase.getPhoneNumber());

            unitBase.setFaxNumber("11112222", true);
            Assert.assertEquals("11112222", unitBase.getFaxNumber());

            unitBase.setEmailAddress("test@example.com", false);
            Assert.assertEquals("test@example.com", unitBase.getEmailAddress());

            unitBase.setYearlyEmployeeNumbers(2017, 1, 2, 1, 2, 1, 2);

            unitBase.setQuarterlyEmployeeNumbers(2017, 2, 1, 1, 1, 1, 1, 1);

            ParticipantLink deltagerlink = QueryManager.getItem(session, ParticipantLink.class, Collections.singletonMap(ParticipantLink.DB_FIELD_VALUE, 44446666));
            if (deltagerlink == null) {
                deltagerlink = new ParticipantLink();
                deltagerlink.setValue(44446666);
                session.saveOrUpdate(deltagerlink);
            }
            unitBase.addParticipant(deltagerlink);

            session.saveOrUpdate(company);
            session.saveOrUpdate(unit);
            session.saveOrUpdate(registrering);
/*
        try {
            //QueryManager.saveRegistration(session, unit, registrering);
            transaction.commit();
        //} catch (DataFordelerException e) {
        //    transaction.rollback();
        } finally {
            session.close();
        }
*/


            System.out.println("--------------------------------------");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(unit));
            System.out.println("--------------------------------------");
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }
    }

    @Test
    public void testParticipant() throws DataFordelerException, JsonProcessingException {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {

            ParticipantEntity participant = new ParticipantEntity(UUID.randomUUID(), "test");
            participant.setParticipantNumber(12345);

            ParticipantRegistration registration = new ParticipantRegistration(OffsetDateTime.parse("2017-07-01T00:00:00Z"), null, 1);

            ParticipantEffect effect = new ParticipantEffect(registration, OffsetDateTime.parse("2017-01-01T00:00:00Z"), null);

            ParticipantBaseData participantBase = new ParticipantBaseData();
            participantBase.addEffect(effect);


            participantBase.addName("Mickey Mouse");
            participantBase.setUnitNumber(123456);
            Assert.assertTrue(participantBase.getNames().contains("Mickey Mouse"));
            Assert.assertEquals(1, participantBase.getNames().size());


            ParticipantType type = QueryManager.getItem(session, ParticipantType.class, Collections.singletonMap(ParticipantType.DB_FIELD_NAME, "Person"));
            if (type == null) {
                type = new ParticipantType();
                type.setName("Person");
                session.saveOrUpdate(type);
            }
            participantBase.setUnitType(type);
            Assert.assertEquals("Person", participantBase.getUnitType());


            ParticipantRole role = QueryManager.getItem(session, ParticipantRole.class, Collections.singletonMap(ParticipantType.DB_FIELD_NAME, "CEO"));
            if (role == null) {
                role = new ParticipantRole();
                role.setName("CEO");
                session.saveOrUpdate(role);
            }
            participantBase.setRole(role);
            Assert.assertEquals("CEO", participantBase.getRole());


            ParticipantStatus status = QueryManager.getItem(session, ParticipantStatus.class, Collections.singletonMap(ParticipantType.DB_FIELD_NAME, "Deceased"));
            if (status == null) {
                status = new ParticipantStatus();
                status.setName("Deceased");
                session.saveOrUpdate(status);
            }
            participantBase.setStatus(status);
            Assert.assertEquals("Deceased", participantBase.getStatus());

            QueryManager.saveRegistration(session, participant, registration);

            System.out.println("--------------------------------------");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(participant));
            System.out.println("--------------------------------------");
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }

    }
}
