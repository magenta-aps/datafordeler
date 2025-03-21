package dk.magenta.datafordeler.ger;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.ger.configuration.GerConfiguration;
import dk.magenta.datafordeler.ger.configuration.GerConfigurationManager;
import dk.magenta.datafordeler.ger.data.RawData;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.company.CompanyEntityManager;
import dk.magenta.datafordeler.ger.data.company.CompanyQuery;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntityManager;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleQuery;
import dk.magenta.datafordeler.ger.data.unit.UnitEntity;
import dk.magenta.datafordeler.ger.data.unit.UnitEntityManager;
import dk.magenta.datafordeler.ger.data.unit.UnitQuery;
import dk.magenta.datafordeler.ger.parser.SpreadsheetConverter;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.doReturn;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PullTest {

    @Autowired
    private Engine engine;

    @Autowired
    private GerRegisterManager registerManager;

    @Autowired
    SessionManager sessionManager;

    @MockitoSpyBean
    private GerConfigurationManager gerConfigurationManager;

    @Test
    public void pullTest() throws Exception {
        System.out.println("sessionManager: "+sessionManager);
        System.out.println("registerManager: "+registerManager);
        System.out.println("engine: "+engine);
        System.out.println("gerConfigurationManager: "+gerConfigurationManager);
        Assertions.assertNotNull(gerConfigurationManager);

        String sheetFile = "file://" + System.getProperty("user.dir") + "/src/test/resources/GER.test.xlsx";

        GerConfiguration gerConfiguration = new GerConfiguration();
        gerConfiguration.setCompanyRegisterType(GerConfiguration.RegisterType.LOCAL_FILE);
        gerConfiguration.setCompanyRegisterURL(sheetFile);
        gerConfiguration.setUnitRegisterType(GerConfiguration.RegisterType.LOCAL_FILE);
        gerConfiguration.setUnitRegisterURL(sheetFile);
        gerConfiguration.setResponsibleRegisterType(GerConfiguration.RegisterType.LOCAL_FILE);
        gerConfiguration.setResponsibleRegisterURL(sheetFile);

        System.out.println("gerConfigurationManager: "+gerConfigurationManager);

        doReturn(gerConfiguration).when(gerConfigurationManager).getConfiguration();

        Pull pull = new Pull(engine, registerManager);
        pull.run();

        InputStream sheetData = PullTest.class.getResourceAsStream("/GER.test.xlsx");

        SpreadsheetConverter converter = SpreadsheetConverter.getConverterByExtension("xlsx");
        Map<String, List<RawData>> sheets = converter.convert(sheetData);


        Session session = sessionManager.getSessionFactory().openSession();
        try {
            System.out.println("Company");
            String sheetName = "JE";
            List<RawData> sheet = sheets.get(sheetName);
            for (RawData rawData : sheet) {
                int gerNr = rawData.getInt("GERNR");
                CompanyQuery companyQuery = new CompanyQuery();
                companyQuery.setGerNr(gerNr);
                List<CompanyEntity> companyEntities = QueryManager.getAllEntities(session, companyQuery, CompanyEntity.class);
                Assertions.assertEquals(1, companyEntities.size());
                CompanyEntity companyEntity = companyEntities.get(0);
                Map<String, Object> companyMap = companyEntity.asMap();
                Map<String, Object> backConverted = convertToRawData(companyMap, CompanyEntityManager.getKeyMappingEntityToRaw());

                for (String key : rawData.keySet()) {
                    Assertions.assertTrue(
                            backConverted.containsKey(key),
                            key + " expected"
                    );
                    Object rawValue = rawData.get(key);
                    Object backConvertedValue = backConverted.get(key);
                    if (rawValue instanceof Date) {
                        rawValue = normalizeDate(rawValue);
                        backConvertedValue = normalizeDate(backConvertedValue);
                    } else if (backConvertedValue instanceof Date) {
                        Date backConvertedDate = (Date) backConvertedValue;
                        backConvertedValue = String.format("%04d-%02d-%02d", backConvertedDate.getYear() + 1900, backConvertedDate.getMonth() + 1, backConvertedDate.getDate());
                    }
                    //Assertions.assertEquals(String.valueOf(rawValue), String.valueOf(backConvertedValue));
                    if (!Objects.equals(String.valueOf(rawValue), String.valueOf(backConvertedValue))) {
                        Object c = companyMap.get(CompanyEntityManager.getKeyMappingRawToEntity().get(key));
                        System.out.println(
                                rawData.get("GERNR") + " " + key + " = " +
                                        rawValue + (rawValue != null ? (" (" + (rawValue.getClass().getSimpleName()) + ")") : "") +
                                        " | " +
                                        c + (c != null ? (" (" + c.getClass().getSimpleName() + ")") : "")
                        );
                    }
                }
            }


            System.out.println("Unit");
            sheetName = "PE";
            sheet = sheets.get(sheetName);
            for (RawData rawData : sheet) {
                UUID deid = rawData.getUUID("DEID");
                UnitQuery unitQuery = new UnitQuery();
                unitQuery.setDeid(deid);
                List<UnitEntity> unitEntities = QueryManager.getAllEntities(session, unitQuery, UnitEntity.class);
                Assertions.assertEquals(1, unitEntities.size());
                UnitEntity unitEntity = unitEntities.get(0);
                Assertions.assertEquals(deid, unitEntity.getDeid());
                Map<String, Object> unitMap = unitEntity.asMap();
                Map<String, Object> backConverted = convertToRawData(unitMap, UnitEntityManager.getKeyMappingEntityToRaw());

                for (String key : rawData.keySet()) {
                    Assertions.assertTrue(
                            backConverted.containsKey(key),
                            key + " expected"
                    );
                    Object rawValue = rawData.get(key);
                    Object backConvertedValue = backConverted.get(key);
                    if (rawValue instanceof Date) {
                        rawValue = normalizeDate(rawValue);
                        backConvertedValue = normalizeDate(backConvertedValue);
                    } else if (backConvertedValue instanceof Date) {
                        Date backConvertedDate = (Date) backConvertedValue;
                        backConvertedValue = String.format("%04d-%02d-%02d", backConvertedDate.getYear() + 1900, backConvertedDate.getMonth() + 1, backConvertedDate.getDate());
                    }
                    //Assertions.assertEquals(String.valueOf(rawValue), String.valueOf(backConvertedValue));
                    if (!Objects.equals(String.valueOf(rawValue), String.valueOf(backConvertedValue))) {
                        Object c = unitMap.get(UnitEntityManager.getKeyMappingRawToEntity().get(key));
                        System.out.println(
                                rawData.get("GERNR") + " " + key + " = " +
                                        rawValue + (rawValue != null ? (" (" + (rawValue.getClass().getSimpleName()) + ")") : "") +
                                        " | " +
                                        c + (c != null ? (" (" + c.getClass().getSimpleName() + ")") : "")
                        );
                    }
                }
            }

            System.out.println("Responsible");
            sheetName = "Ansvarlige";
            sheet = sheets.get(sheetName);
            for (RawData rawData : sheet) {
                int gerNr = rawData.getInt("GERNR");
                UUID respId = rawData.getUUID("CVR_DELTAGER_GUID");
                ResponsibleQuery responsibleQuery = new ResponsibleQuery();
                responsibleQuery.setGerNr(gerNr);
                responsibleQuery.setCvrGuid(respId);
                List<ResponsibleEntity> responsibleEntities = QueryManager.getAllEntities(session, responsibleQuery, ResponsibleEntity.class);
                Assertions.assertEquals(1, responsibleEntities.size());
                ResponsibleEntity respEntity = responsibleEntities.get(0);
                Assertions.assertEquals(Integer.valueOf(gerNr), respEntity.getGerNumber());
                Assertions.assertEquals(respId, respEntity.getCvrParticipantGuid());
                Map<String, Object> respMap = respEntity.asMap();
                Map<String, Object> backConverted = convertToRawData(respMap, ResponsibleEntityManager.getKeyMappingEntityToRaw());

                for (String key : rawData.keySet()) {
                    Assertions.assertTrue(
                            backConverted.containsKey(key),
                            key + " expected"
                    );
                    Object rawValue = rawData.get(key);
                    Object backConvertedValue = backConverted.get(key);
                    if (rawValue instanceof Date) {
                        rawValue = normalizeDate(rawValue);
                        backConvertedValue = normalizeDate(backConvertedValue);
                    } else if (backConvertedValue instanceof Date) {
                        Date backConvertedDate = (Date) backConvertedValue;
                        backConvertedValue = String.format("%04d-%02d-%02d", backConvertedDate.getYear() + 1900, backConvertedDate.getMonth() + 1, backConvertedDate.getDate());
                    }
                    //Assertions.assertEquals(String.valueOf(rawValue), String.valueOf(backConvertedValue));
                    if (!Objects.equals(String.valueOf(rawValue), String.valueOf(backConvertedValue))) {
                        //Object c = respMap.get(ResponsibleEntityManager.getKeyMappingRawToEntity().get(key));
                        Object c = backConvertedValue;
                        System.out.println(
                                rawData.get("GERNR") + " " + key + " = " +
                                        rawValue + (rawValue != null ? (" (" + (rawValue.getClass().getSimpleName()) + ")") : "") +
                                        " | " +
                                        c + (c != null ? (" (" + c.getClass().getSimpleName() + ")") : "")
                        );
                    }
                }
            }


        } finally {
            session.close();
        }
    }

    private static Map<String, Object> convertToRawData(Map<String, Object> data, Map<String, String> keyMapping) {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : data.keySet()) {
            Object value = data.get(key);
            if (value instanceof LocalDate) {
                LocalDate localDate = (LocalDate) value;
                value = new Date(localDate.getYear() - 1900, localDate.getMonthValue() - 1, localDate.getDayOfMonth());
            } else if (value instanceof Boolean) {
                value = (Boolean) value ? "J" : "N";
            } else if (value instanceof Integer) {
                value = ((Integer) value).longValue();
            } else if (value instanceof UUID) {
                value = value.toString().toUpperCase();
            }
            map.put(keyMapping.get(key), value);
        }
        return map;
    }

    private static Object normalizeDate(Object data) {
        if (data instanceof Date) {
            return RawData.dateAsLong((Date) data);
        }
        if (data instanceof LocalDate) {
            return RawData.dateAsLong((LocalDate) data);
        }
        return data;
    }

}
