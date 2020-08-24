package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DirectLookupTest {

    @Autowired
    private CprPlugin plugin;

    @SpyBean
    private CprConfigurationManager configurationManager;

    @Autowired
    private CprDirectLookup directLookup;

    @Test
    @Ignore
    public void testLookup() throws Exception {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        configuration.setDirectHost("direkte-demo.cpr.dk");
        configuration.setDirectUsername("some user"); // We're not going to publish our credentials here. Put your own in if you want.
        configuration.setDirectPassword("nice try");
        configuration.setDirectCustomerNumber(0);
        String response = directLookup.lookup("1234567890");
    }

    @Test
    public void testParsePerson1() throws Exception {
        ZoneId timezone = ZoneId.of("Europe/Copenhagen");
        PersonEntity entity = directLookup.parseResponse("038406fJrr7CCxWUDI0178051590000000000000003840120190808000000000010707611234          01000000000000 M1961-07-07 1961-07-07*           Socialrådg.                       002070761123405735731101 01  mf                                      198010102000 196107071034 0000000000000000                                                                                                                                                                                                   0030707611234Mortensen,Jens                                                                                        Boulevarden 101,1 mf                                                6800Varde               05735731101 01  mf    Boulevarden         0080707611234Jens                                                                                        Mortensen                                196107072000 Mortensen,Jens                    00907076112345150                    01007076112345100199103201299*0110707611234F1961-07-07*0120707611234F0706611234                                              198010012000             014070761123413018140770140707611234131281123401507076112341961-07-07*0912414434                                              1961-07-07*0909414385                                              01707076112342019-04-10*          0002                    Terd                              2019-04-10grd                                                                                                                                                                       999999999999900000012");

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(1, statusRecord.getStatus());
        Assert.assertNull(statusRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCore().size());
        PersonCoreDataRecord coreDataRecord = entity.getCore().iterator().next();
        Assert.assertEquals(PersonCoreDataRecord.Koen.MAND, coreDataRecord.getGender());

        Assert.assertEquals(1, entity.getName().size());
        NameDataRecord nameRecord = entity.getName().iterator().next();
        Assert.assertEquals("Jens", nameRecord.getFirstNames());
        Assert.assertEquals("", nameRecord.getMiddleName());
        Assert.assertEquals("Mortensen", nameRecord.getLastName());

        Assert.assertEquals(1, entity.getGuardian().size());
        GuardianDataRecord guardianDataRecord = entity.getGuardian().iterator().next();
        Assert.assertEquals("Terd", guardianDataRecord.getGuardianName());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2019, 4, 10, 0, 0, 0), timezone)), guardianDataRecord.getEffectFrom());
        Assert.assertEquals(2, guardianDataRecord.getGuardianRelationType());

        Assert.assertEquals(1, entity.getChurchRelation().size());
        ChurchDataRecord churchDataRecord = entity.getChurchRelation().iterator().next();
        Assert.assertEquals(Character.valueOf('F'), churchDataRecord.getChurchRelation());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1961, 7, 7, 0, 0, 0), timezone)), churchDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCivilstatus().size());
        CivilStatusDataRecord civilStatusDataRecord = entity.getCivilstatus().iterator().next();
        Assert.assertEquals("F", civilStatusDataRecord.getCivilStatus());
        Assert.assertEquals("0706611234", civilStatusDataRecord.getSpouseCpr());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1980, 10, 1, 20, 0, 0), timezone)), civilStatusDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getAddress().size());
        AddressDataRecord addressDataRecord = entity.getAddress().iterator().next();
        Assert.assertEquals(573, addressDataRecord.getMunicipalityCode());
        Assert.assertEquals(5731, addressDataRecord.getRoadCode());
        Assert.assertEquals("101", addressDataRecord.getHouseNumber());
        Assert.assertEquals("01", addressDataRecord.getFloor());
        Assert.assertEquals("mf", addressDataRecord.getDoor());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1980, 10, 10, 20, 0, 0), timezone)), addressDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getMunicipalityMove().size());
        MoveMunicipalityDataRecord moveMunicipalityDataRecord = entity.getMunicipalityMove().iterator().next();
        Assert.assertEquals(LocalDateTime.of(1961, 7, 7, 10, 34, 0, 0), moveMunicipalityDataRecord.getInDatetime());

        Assert.assertEquals(1, entity.getMother().size());
        ParentDataRecord motherDataRecord = entity.getMother().iterator().next();
        Assert.assertEquals("0912414434", motherDataRecord.getCprNumber());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1961, 7, 7, 0, 0, 0), timezone)), motherDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getFather().size());
        ParentDataRecord fatherDataRecord = entity.getFather().iterator().next();
        Assert.assertEquals("0909414385", fatherDataRecord.getCprNumber());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1961, 7, 7, 0, 0, 0), timezone)), fatherDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getBirthPlace().size());
        BirthPlaceDataRecord birthPlaceDataRecord = entity.getBirthPlace().iterator().next();
        Assert.assertEquals(5150, birthPlaceDataRecord.getAuthority());

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusDataRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(1, statusDataRecord.getStatus());

        Assert.assertEquals(1, entity.getCitizenship().size());
        CitizenshipDataRecord citizenshipDataRecord = entity.getCitizenship().iterator().next();
        Assert.assertEquals(5100, citizenshipDataRecord.getCountryCode());
    }

    @Test
    public void testParsePerson2() throws Exception {
        ZoneId timezone = ZoneId.of("Europe/Copenhagen");
        PersonEntity entity = directLookup.parseResponse("038406m0MXx0pnWUDI0178001709000000000000003840120190815000000000010706611234          01000000000000 K1961-06-07 1961-06-07*           Socialrådg.                       002070661123408518512002 st  tv                                      198010102000 198010101200 0571000000000000*                                                                                                                                                                                                  0030706611234Mortensen,Anne                                                      Solhavehjemmet                    Industrivænget 2,st tv            Hasseris                          9000Aalborg             08518512002 st  tv    Industrivænget      0080706611234Anne                                                                                        Mortensen                                198010012000 Mortensen,Anne                    00907066112348126                    01007066112345100199103201299*0110706611234F1961-06-07*0120706611234F0707611234                                              198010012000             0140706611234051011500701407066112340510811003014070661123405108310390140706611234100715500601407066112341008155018014070661123411031650040140706611234130181102701407066112341301814077014070661123413071550590140706611234170216500401407066112341706155003014070661123420061750690140706611234200715502901407066112342007155037014070661123422051550260140706611234220615504601407066112342310175059014070661123427066500270140706611234290617504801507066112341961-06-07*0901414068                                              1961-06-07*0908414357                                              999999999999900000028");

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(1, statusRecord.getStatus());
        Assert.assertNull(statusRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCore().size());
        PersonCoreDataRecord coreDataRecord = entity.getCore().iterator().next();
        Assert.assertEquals(PersonCoreDataRecord.Koen.KVINDE, coreDataRecord.getGender());

        Assert.assertEquals(1, entity.getName().size());
        NameDataRecord nameRecord = entity.getName().iterator().next();
        Assert.assertEquals("Anne", nameRecord.getFirstNames());
        Assert.assertEquals("", nameRecord.getMiddleName());
        Assert.assertEquals("Mortensen", nameRecord.getLastName());

        Assert.assertEquals(0, entity.getGuardian().size());

        Assert.assertEquals(1, entity.getChurchRelation().size());
        ChurchDataRecord churchDataRecord = entity.getChurchRelation().iterator().next();
        Assert.assertEquals(Character.valueOf('F'), churchDataRecord.getChurchRelation());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1961, 6, 7, 0, 0, 0), timezone)), churchDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCivilstatus().size());
        CivilStatusDataRecord civilStatusDataRecord = entity.getCivilstatus().iterator().next();
        Assert.assertEquals("F", civilStatusDataRecord.getCivilStatus());
        Assert.assertEquals("0707611234", civilStatusDataRecord.getSpouseCpr());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1980, 10, 1, 20, 0, 0), timezone)), civilStatusDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getAddress().size());
        AddressDataRecord addressDataRecord = entity.getAddress().iterator().next();
        Assert.assertEquals(851, addressDataRecord.getMunicipalityCode());
        Assert.assertEquals(8512, addressDataRecord.getRoadCode());
        Assert.assertEquals("2", addressDataRecord.getHouseNumber());
        Assert.assertEquals("st", addressDataRecord.getFloor());
        Assert.assertEquals("tv", addressDataRecord.getDoor());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1980, 10, 10, 20, 0, 0), timezone)), addressDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getMunicipalityMove().size());
        MoveMunicipalityDataRecord moveMunicipalityDataRecord = entity.getMunicipalityMove().iterator().next();
        Assert.assertEquals(LocalDateTime.of(1980, 10, 10, 12, 0, 0, 0), moveMunicipalityDataRecord.getInDatetime());

        Assert.assertEquals(1, entity.getMother().size());
        ParentDataRecord motherDataRecord = entity.getMother().iterator().next();
        Assert.assertEquals("0901414068", motherDataRecord.getCprNumber());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1961, 6, 7, 0, 0, 0), timezone)), motherDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getFather().size());
        ParentDataRecord fatherDataRecord = entity.getFather().iterator().next();
        Assert.assertEquals("0908414357", fatherDataRecord.getCprNumber());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1961, 6, 7, 0, 0, 0), timezone)), fatherDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getBirthPlace().size());
        BirthPlaceDataRecord birthPlaceDataRecord = entity.getBirthPlace().iterator().next();
        Assert.assertEquals(8126, birthPlaceDataRecord.getAuthority());

        Assert.assertEquals(1, entity.getPosition().size());
        PersonPositionDataRecord positionDataRecord = entity.getPosition().iterator().next();
        Assert.assertEquals("Socialrådg.", positionDataRecord.getPosition());

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusDataRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(1, statusDataRecord.getStatus());

        Assert.assertEquals(1, entity.getCitizenship().size());
        CitizenshipDataRecord citizenshipDataRecord = entity.getCitizenship().iterator().next();
        Assert.assertEquals(5100, citizenshipDataRecord.getCountryCode());
    }

    @Test
    public void testParsePerson3() throws Exception {
        ZoneId timezone = ZoneId.of("Europe/Copenhagen");
        PersonEntity entity = directLookup.parseResponse("038406XiK0ZLNMWUDI0178001824000000000000003840120190815000000000011312811234          01000000000000 K1981-12-13 1981-12-13*                                             002131281123408518512002 st  tv                                      198112132000 198112131200 0000000000000000                                                                                                                                                                                                   0031312811234Mortensen,Janne                                                     Solhavehjemmet                    Industrivænget 2,st tv            Hasseris                          9000Aalborg             08518512002 st  tv    Industrivænget      0081312811234Janne                                                                                       Mortensen                                198202052000 Mortensen,Janne                   00913128112348126                    01013128112345100199103201299*0111312811234F1981-12-13*0121312811234U                                                        000000000000             01413128112340303145001014131281123403031450790141312811234030314508701413128112340303145095014131281123403031451090141312811234050717500701413128112341008155069014131281123410100250590141312811234101002506701413128112341206155007014131281123412061550150141312811234120619500901413128112341312185047014131281123416051750460141312811234180214501901413128112341802145027014131281123420061750770141312811234210818506901413128112342205155034014131281123422071550580141312811234260815501801413128112342705195008014131281123429071550590141312811234310517506901513128112341981-12-13*0701614054                                              1982-01-02 0707611234                                              999999999999900000033");

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(1, statusRecord.getStatus());
        Assert.assertNull(statusRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCore().size());
        PersonCoreDataRecord coreDataRecord = entity.getCore().iterator().next();
        Assert.assertEquals(PersonCoreDataRecord.Koen.KVINDE, coreDataRecord.getGender());

        Assert.assertEquals(1, entity.getName().size());
        NameDataRecord nameRecord = entity.getName().iterator().next();
        Assert.assertEquals("Janne", nameRecord.getFirstNames());
        Assert.assertEquals("", nameRecord.getMiddleName());
        Assert.assertEquals("Mortensen", nameRecord.getLastName());

        Assert.assertEquals(0, entity.getGuardian().size());

        Assert.assertEquals(1, entity.getChurchRelation().size());
        ChurchDataRecord churchDataRecord = entity.getChurchRelation().iterator().next();
        Assert.assertEquals(Character.valueOf('F'), churchDataRecord.getChurchRelation());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1981, 12, 13, 0, 0, 0), timezone)), churchDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCivilstatus().size());
        CivilStatusDataRecord civilStatusDataRecord = entity.getCivilstatus().iterator().next();
        Assert.assertEquals("U", civilStatusDataRecord.getCivilStatus());
        Assert.assertEquals("", civilStatusDataRecord.getSpouseCpr());
        Assert.assertNull(civilStatusDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getAddress().size());
        AddressDataRecord addressDataRecord = entity.getAddress().iterator().next();
        Assert.assertEquals(851, addressDataRecord.getMunicipalityCode());
        Assert.assertEquals(8512, addressDataRecord.getRoadCode());
        Assert.assertEquals("2", addressDataRecord.getHouseNumber());
        Assert.assertEquals("st", addressDataRecord.getFloor());
        Assert.assertEquals("tv", addressDataRecord.getDoor());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1981, 12, 13, 20, 0, 0), timezone)), addressDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getMunicipalityMove().size());
        MoveMunicipalityDataRecord moveMunicipalityDataRecord = entity.getMunicipalityMove().iterator().next();
        Assert.assertEquals(LocalDateTime.of(1981, 12, 13, 12, 0, 0, 0), moveMunicipalityDataRecord.getInDatetime());

        Assert.assertEquals(1, entity.getMother().size());
        ParentDataRecord motherDataRecord = entity.getMother().iterator().next();
        Assert.assertEquals("0701614054", motherDataRecord.getCprNumber());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1981, 12, 13, 0, 0, 0), timezone)), motherDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getFather().size());
        ParentDataRecord fatherDataRecord = entity.getFather().iterator().next();
        Assert.assertEquals("0707611234", fatherDataRecord.getCprNumber());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1982, 1, 2, 0, 0, 0), timezone)), fatherDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getBirthPlace().size());
        BirthPlaceDataRecord birthPlaceDataRecord = entity.getBirthPlace().iterator().next();
        Assert.assertEquals(8126, birthPlaceDataRecord.getAuthority());

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusDataRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(1, statusDataRecord.getStatus());

        Assert.assertEquals(1, entity.getCitizenship().size());
        CitizenshipDataRecord citizenshipDataRecord = entity.getCitizenship().iterator().next();
        Assert.assertEquals(5100, citizenshipDataRecord.getCountryCode());
    }

    @Test
    public void testParsePerson4() throws Exception {
        ZoneId timezone = ZoneId.of("Europe/Copenhagen");
        PersonEntity entity = directLookup.parseResponse("038406eyOcufxDWUDI0178001429000000000000003840120190815000000000010508611234          01000000000000 M1961-08-05 2005-10-13                                              002050861123405735731008A010001    c/o Kamma Schmidt                 200509251401 200509251401 0000000000000000                                                                                                                                                                                                   0030508611234Svensson,Carsten Albinus          c/o Kamma Schmidt                                                   Boulevarden 8 A,1,-1                                                6800Varde               05735731008A010001    Boulevarden         004050861123400012016-11-102096-11-10004050861123400022005-10-172105-10-17004050861123400032005-10-172105-10-170080508611234Carsten                                            Albinus                                  Svensson                                 196108051401*Svensson,Carsten Albinus          00905086112345180                    01005086112345180200603130950 0110508611234U1961-08-05*0120508611234P0000000000                                              199108050000*            014050861123401019010090140508611234080193100601505086112341961-08-05*0000000000                                              1961-08-05 0000000000                                              999999999999900000014");

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(1, statusRecord.getStatus());
        Assert.assertNull(statusRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCore().size());
        PersonCoreDataRecord coreDataRecord = entity.getCore().iterator().next();
        Assert.assertEquals(PersonCoreDataRecord.Koen.MAND, coreDataRecord.getGender());

        Assert.assertEquals(1, entity.getName().size());
        NameDataRecord nameRecord = entity.getName().iterator().next();
        Assert.assertEquals("Carsten", nameRecord.getFirstNames());
        Assert.assertEquals("Albinus", nameRecord.getMiddleName());
        Assert.assertEquals("Svensson", nameRecord.getLastName());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1961, 8, 5, 14, 1, 0), timezone)), nameRecord.getEffectFrom());

        Assert.assertEquals(0, entity.getGuardian().size());

        Assert.assertEquals(1, entity.getChurchRelation().size());
        ChurchDataRecord churchDataRecord = entity.getChurchRelation().iterator().next();
        Assert.assertEquals(Character.valueOf('U'), churchDataRecord.getChurchRelation());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1961, 8, 5, 0, 0, 0), timezone)), churchDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCivilstatus().size());
        CivilStatusDataRecord civilStatusDataRecord = entity.getCivilstatus().iterator().next();
        Assert.assertEquals("P", civilStatusDataRecord.getCivilStatus());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1991, 8, 5, 0, 0, 0), timezone)), civilStatusDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getAddress().size());
        AddressDataRecord addressDataRecord = entity.getAddress().iterator().next();
        Assert.assertEquals(573, addressDataRecord.getMunicipalityCode());
        Assert.assertEquals(5731, addressDataRecord.getRoadCode());
        Assert.assertEquals("8A", addressDataRecord.getHouseNumber());
        Assert.assertEquals("01", addressDataRecord.getFloor());
        Assert.assertEquals("1", addressDataRecord.getDoor());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2005, 9, 25, 14, 1, 0), timezone)), addressDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getConame().size());
        AddressConameDataRecord conameDataRecord = entity.getConame().iterator().next();
        Assert.assertEquals("c/o Kamma Schmidt", conameDataRecord.getConame());

        Assert.assertEquals(1, entity.getMunicipalityMove().size());
        MoveMunicipalityDataRecord moveMunicipalityDataRecord = entity.getMunicipalityMove().iterator().next();
        Assert.assertEquals(LocalDateTime.of(2005, 9, 25, 14, 1, 0, 0), moveMunicipalityDataRecord.getInDatetime());

        Assert.assertEquals(0, entity.getMother().size());

        Assert.assertEquals(0, entity.getFather().size());

        Assert.assertEquals(1, entity.getBirthPlace().size());
        BirthPlaceDataRecord birthPlaceDataRecord = entity.getBirthPlace().iterator().next();
        Assert.assertEquals(5180, birthPlaceDataRecord.getAuthority());

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusDataRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(1, statusDataRecord.getStatus());

        Assert.assertEquals(1, entity.getCitizenship().size());
        CitizenshipDataRecord citizenshipDataRecord = entity.getCitizenship().iterator().next();
        Assert.assertEquals(5180, citizenshipDataRecord.getCountryCode());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2006, 3, 13, 9, 50, 0), timezone)), citizenshipDataRecord.getEffectFrom());

        Assert.assertEquals(3, entity.getProtection().size());
        ProtectionDataRecord protectionDataRecord1 = null, protectionDataRecord2 = null, protectionDataRecord3 = null;
        for (ProtectionDataRecord protectionDataRecord : entity.getProtection()) {
            int type = protectionDataRecord.getProtectionType();
            if (type == 1) protectionDataRecord1 = protectionDataRecord;
            if (type == 2) protectionDataRecord2 = protectionDataRecord;
            if (type == 3) protectionDataRecord3 = protectionDataRecord;
        }
        Assert.assertNotNull(protectionDataRecord1);
        Assert.assertNotNull(protectionDataRecord2);
        Assert.assertNotNull(protectionDataRecord3);

        Assert.assertEquals(1, protectionDataRecord1.getProtectionType());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2016, 11, 10, 0, 0, 0), timezone)), protectionDataRecord1.getEffectFrom());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2096, 11, 10, 0, 0, 0), timezone)), protectionDataRecord1.getEffectTo());
        Assert.assertEquals(2, protectionDataRecord2.getProtectionType());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2005, 10, 17, 0, 0, 0), timezone)), protectionDataRecord2.getEffectFrom());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2105, 10, 17, 0, 0, 0), timezone)), protectionDataRecord2.getEffectTo());
        Assert.assertEquals(3, protectionDataRecord3.getProtectionType());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2005, 10, 17, 0, 0, 0), timezone)), protectionDataRecord3.getEffectFrom());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2105, 10, 17, 0, 0, 0), timezone)), protectionDataRecord3.getEffectTo());
    }

    @Test
    public void testParsePerson5() throws Exception {
        ZoneId timezone = ZoneId.of("Europe/Copenhagen");
        PersonEntity entity = directLookup.parseResponse("038406uKBKxWLcWUDI0178001104000000000000003840120190815000000000010607621234          90200502051034 M1962-07-06 2005-10-20                                              0030607621234Petersen,Mads Munk                                                                                                                                                                                                                          0080607621234Mads                                               Munk                                     Petersen                                 196207061029 Petersen,Mads Munk                00906076212345180                    01006076212345180196207061029*0110607621234U1962-07-06 0120607621234D0506650038                                              200502051034             01406076212340506871018014060762123405089210040140607621234060794106801406076212340705901007014060762123407059600110140607621234080789104901506076212341962-07-06*0000000000                                              1962-07-06*0000000000                                              999999999999900000014");

        Assert.assertEquals(1, entity.getCore().size());
        PersonCoreDataRecord coreDataRecord = entity.getCore().iterator().next();
        Assert.assertEquals(PersonCoreDataRecord.Koen.MAND, coreDataRecord.getGender());

        Assert.assertEquals(1, entity.getName().size());
        NameDataRecord nameRecord = entity.getName().iterator().next();
        Assert.assertEquals("Mads", nameRecord.getFirstNames());
        Assert.assertEquals("Munk", nameRecord.getMiddleName());
        Assert.assertEquals("Petersen", nameRecord.getLastName());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1962, 7, 6, 10, 29, 0), timezone)), nameRecord.getEffectFrom());

        Assert.assertEquals(0, entity.getGuardian().size());

        Assert.assertEquals(1, entity.getChurchRelation().size());
        ChurchDataRecord churchDataRecord = entity.getChurchRelation().iterator().next();
        Assert.assertEquals(Character.valueOf('U'), churchDataRecord.getChurchRelation());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1962, 7, 6, 0, 0, 0), timezone)), churchDataRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCivilstatus().size());
        CivilStatusDataRecord civilStatusDataRecord = entity.getCivilstatus().iterator().next();
        Assert.assertEquals("D", civilStatusDataRecord.getCivilStatus());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(2005, 2, 5, 10, 34, 0), timezone)), civilStatusDataRecord.getEffectFrom());
        Assert.assertEquals("0506650038", civilStatusDataRecord.getSpouseCpr());

        Assert.assertEquals(0, entity.getAddress().size());

        Assert.assertEquals(0, entity.getMunicipalityMove().size());

        Assert.assertEquals(0, entity.getMother().size());

        Assert.assertEquals(0, entity.getFather().size());

        Assert.assertEquals(1, entity.getBirthPlace().size());
        BirthPlaceDataRecord birthPlaceDataRecord = entity.getBirthPlace().iterator().next();
        Assert.assertEquals(5180, birthPlaceDataRecord.getAuthority());

        Assert.assertEquals(1, entity.getStatus().size());
        PersonStatusDataRecord statusRecord = entity.getStatus().iterator().next();
        Assert.assertEquals(90, statusRecord.getStatus());
        Assert.assertNull(statusRecord.getEffectFrom());

        Assert.assertEquals(1, entity.getCitizenship().size());
        CitizenshipDataRecord citizenshipDataRecord = entity.getCitizenship().iterator().next();
        Assert.assertEquals(5180, citizenshipDataRecord.getCountryCode());
        Assert.assertEquals(OffsetDateTime.from(ZonedDateTime.of(LocalDateTime.of(1962, 7, 6, 10, 29, 0), timezone)), citizenshipDataRecord.getEffectFrom());
    }
}
