package dk.magenta.datafordeler.cpr.direct;

import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;

@Component
public class CprDirectLookup {

    //Documentation:
    //https://cprdocs.atlassian.net/wiki/spaces/CPR/pages/51155340/Gr+nsefladebeskrivelse+til+offentlige+myndigheder+-+CPR+Direkte+PNR

    @Value("${dafo.cpr.person.direct.allowDirectPersonLookup:false}")
    private boolean allowDirectPersonLookup;

    @Autowired
    private CprConfigurationManager configurationManager;

    private String authToken;

    private SSLSocketFactory socketFactory;

    private static final int ERR_TOKEN_EXPIRED = 7;

    private final Logger log = LogManager.getLogger(CprDirectLookup.class.getCanonicalName());

    @PostConstruct
    public void init() {
        this.socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    private CprConfiguration getConfiguration() {
        return configurationManager.getConfiguration();
    }

    public void login() throws DataStreamException {
        try {
            this.login(null);
        } catch (ConfigurationException e) {
            log.error(e); // This can't happen
        }
    }

    public void login(String newPassword) throws DataStreamException, ConfigurationException {
        CprConfiguration configuration = this.getConfiguration();

        String transactionCode = configuration.getDirectTransactionCode();
        String customerNumber = String.format("%04d", configuration.getDirectCustomerNumber());
        String username = String.format("%-8.8s", configuration.getDirectUsername());
        String password = null;
        try {
            password = String.format("%-8.8s", configuration.getDirectPassword());
        } catch (GeneralSecurityException | IOException e) {
            throw new DataStreamException("Failed decrypting stored password from database", e);
        }

        if (newPassword == null) {
            newPassword = "";
        } else if (newPassword.length() != 8) {
            throw new ConfigurationException("Invalid new password; must be exactly 8 characters");
        }

        String LOGON_SUBSCRIPTION_TYPE = "9"; // constant in LOGON record
        String LOGON_DATA_TYPE = "0"; // constant in LOGON record

        // build logon request - a mix of constants and user-specified information
        String requestBody = String.format("%-35.35s",
                String.format("%s,%s%s%s%s%s%s",
                        transactionCode,
                        customerNumber,
                        LOGON_SUBSCRIPTION_TYPE,
                        LOGON_DATA_TYPE,
                        username,
                        password,
                        newPassword
                )); // record should always be 35 bytes

        String response;
        try {
            response = this.request(requestBody);
        } catch (IOException e) {
            throw new DataStreamException("Failed connecting to CPR Direct", e);
        }

        // error code in response starts at position 22 in response
        int errorCode = Integer.parseInt(response.substring(22, 24));

        if (errorCode != 0) {
            throw new DataStreamException("Login failed with error code: " + errorCode + ", errorText: " + response);
        }

        // parse out token, used for authentication on subsequent requests
        this.authToken = response.substring(6, 14);
    }

    public PersonEntity getPerson(String pnr) throws DataStreamException {
        String rawData = null;
        rawData = this.lookup(pnr);
        if (rawData != null) {
            return this.parseResponse(rawData);
        } else {
            return null;
        }
    }

    public String lookup(String pnr) throws DataStreamException {
        if (!allowDirectPersonLookup) {
            return null;
        }

        if (this.authToken == null) {
            this.login();
        }

        CprConfiguration configuration = this.getConfiguration(); // TODO: Cache configuration?

        for (int attempt = 0; attempt < 3; attempt++) {
            String dataType = "06";
            String request = String.format("%-39.39s",
                    configuration.getDirectTransactionCode() +
                            "," +
                            String.format("%04d", configuration.getDirectCustomerNumber()) +
                            dataType +
                            this.authToken +
                            configuration.getDirectUsername() +
                            "00" +
                            pnr
            ); // must be 39 bytes

            String response = null;
            try {
                response = this.request(request);
            } catch (IOException e) {
                throw new DataStreamException("Failed lookup at CPR Direct", e);
            }

            int errorCode = this.parseErrorCode(response);
            if (errorCode == 0) {
                // All ok, return response
                return response;
            }
            if (errorCode == 5) {
                // Unknown cpr
                log.warn("cpr not found " + pnr);
                return null;
            } else if (errorCode == ERR_TOKEN_EXPIRED) {
                // Login and try again
                this.login();
            } else {
                // Some other error, bail
                throw new DataStreamException("Got statuscode " + errorCode + " from CPR Direct");
            }
        }
        throw new DataStreamException("Failed lookup for CPR Direct: 3 attempts unsuccessful");
    }

    private SSLSocket getSocket() throws IOException {
        CprConfiguration configuration = this.getConfiguration();
        return (SSLSocket) this.socketFactory.createSocket(
                configuration.getDirectHost(),
                configuration.getDirectPort()
        );
    }

    private String request(String request) throws IOException {
        SSLSocket client = this.getSocket();
        OutputStream os = client.getOutputStream();
        os.write(request.getBytes());
        os.flush();

        InputStream is = client.getInputStream();

        byte[] response = new byte[4096];
        int totalResponseLength = 0;
        int responseLength;

        while ((responseLength = is.read(response)) > -1) {
            totalResponseLength += responseLength;
        }

        client.close();

        byte[] baCompleteResponse = new byte[totalResponseLength];
        System.arraycopy(response, 0, baCompleteResponse, 0, totalResponseLength);

        return new String(baCompleteResponse, StandardCharsets.ISO_8859_1);
    }

    private int parseErrorCode(String response) {
        int startErrorResponse = 22; // start of error code in response body
        int startDataSection = 28; // start of DATA section in response

        int errorCode = Integer.parseInt(
                response.substring(startErrorResponse, startErrorResponse + 2)
        );

        if (errorCode != 0) {
            // extended reason code available after standard error code
            int reasonCode = Integer.parseInt(response.substring(startErrorResponse + 2, startErrorResponse + 6));
            String reasonText = response.substring(startDataSection).trim();
            log.info("Error reasonCode: " + reasonCode + ", reasonText: " + reasonText);
        }

        return errorCode;
    }

    public PersonEntity parseResponse(String response) throws DataStreamException {
        // dataType determines what records we received, and allows us to parse appropriately
        int dataType = Integer.parseInt(response.substring(5, 6));

        switch (dataType) {
            case 0:
                throw new DataStreamException("Only header record present.");
            case 6:
                ListHashMap<RecordType, String> records = this.getAvailableRecords(response);

                if (records.size() == 0) {
                    throw new DataStreamException("No records found in response.");
                }

                String pnr = null;
                ArrayList<PersonDataRecord> parsedRecords = new ArrayList<>();
                for (RecordType recordType : records.keySet()) {
                    for (String line : records.get(recordType)) {
                        PersonDataRecord parsedRecord = null;
                        try {
                            switch (recordType) {
                                case PERSON_INFO:
                                    parsedRecord = new PersonRecord(line, personRecordMapping);
                                    break;
                                case CURRENT_ADDR:
                                    parsedRecord = new AddressRecord(line, addressRecordMapping);
                                    break;
                                case PROTECTION:
                                    parsedRecord = new ProtectionRecord(line, protectionRecordMapping);
                                    break;
                                case PLAIN_ADDR:
                                    // TODO?
                                    break;
                                case FOREIGN_ADDR:
                                    parsedRecord = new ForeignAddressRecord(line, foreignAddressRecordMapping);
                                    break;
                                case CONTACT_ADDR:
                                    // TODO?
                                    break;
                                case DISAPPEARANCE:
                                    break;
                                case NAME:
                                    parsedRecord = new NameRecord(line, nameRecordMapping);
                                    break;
                                case BIRTHPLACE:
                                    parsedRecord = new BirthRecord(line, birthRecordMapping);
                                    break;
                                case CITIZENSHIP:
                                    parsedRecord = new CitizenshipRecord(line, citizenshipRecordMapping);
                                    break;
                                case CHURCH:
                                    parsedRecord = new ChurchRecord(line, churchRecordMapping);
                                    break;
                                case CIVILSTATUS:
                                    parsedRecord = new CivilStatusRecord(line, civilstatusRecordMapping);
                                    break;
                                case SEPARATION:
                                    break;
                                case CHILDREN:
                                    break;
                                case PARENTS:
                                    parsedRecord = new PersonRecord(line, parentRecordMapping);
                                    break;
                                case CUSTODY:
                                    break;
                                case UNMANAGE:
                                    parsedRecord = new GuardianRecord(line, guardianRecordMapping);
                                    break;
                                case MUNICIPAL_RELATION:
                                    break;
                                case CREDIT_WARNING:
                                    break;
                                case UNMANAGE_EXTRA:
                                    break;
                            }
                        } catch (ParseException e) {
                            throw new DataStreamException("Failed parsing record of type " + recordType, e);
                        }
                        if (parsedRecord != null) {
                            if (pnr == null) {
                                pnr = parsedRecord.getCprNumber();
                            }
                            parsedRecords.add(parsedRecord);
                        }
                    }
                }

                if (pnr != null) {
                    PersonEntity entity = new PersonEntity(PersonEntity.generateUUID(pnr), "Direct lookup");
                    entity.setPersonnummer(pnr);
                    for (PersonDataRecord r : parsedRecords) {
                        for (CprBitemporalRecord bitemporalRecord : r.getBitemporalRecords()) {
                            bitemporalRecord.setDafoUpdated(OffsetDateTime.now());
                            entity.addBitemporalRecord((CprBitemporalPersonRecord) bitemporalRecord, null, false);
                        }
                    }
                    return entity;
                }
            default:
                throw new DataStreamException("Unrecognized record data type: " + dataType);
        }
    }

    private static final Mapping personRecordMapping = new Mapping();

    static {
        personRecordMapping.add("pnrgaeld", 14, 10);
        personRecordMapping.add("status", 24, 2);
        personRecordMapping.add("statushaenstart", 26, 12);
        personRecordMapping.add("statusdto_umrk", 38, 1);
        personRecordMapping.add("koen", 39, 1);
        personRecordMapping.add("foed_dt", 40, 10);
        personRecordMapping.add("foed_dt_umrk", 50, 1);
        personRecordMapping.add("start_dt-person", 51, 10);
        personRecordMapping.add("start_dt_umrk-person", 61, 1);
        personRecordMapping.add("slut_dt-person", 62, 10);
        personRecordMapping.add("slut_dt_umrk-person", 72, 1);
        personRecordMapping.add("stilling", 73, 34);
    }

    private static final Mapping addressRecordMapping = new Mapping();

    static {
        addressRecordMapping.add("komkod", 14, 4);
        addressRecordMapping.add("vejkod", 18, 4);
        addressRecordMapping.add("husnr", 22, 4);
        addressRecordMapping.add("etage", 26, 2);
        addressRecordMapping.add("sidedoer", 28, 4);
        addressRecordMapping.add("bnr", 32, 4);
        addressRecordMapping.add("convn", 36, 34);
        addressRecordMapping.add("tilflydto", 70, 12);
        addressRecordMapping.add("tilflydto_umrk", 82, 1);
        addressRecordMapping.add("tilflykomdto", 83, 12);
        addressRecordMapping.add("tilflykomdt_umrk", 95, 1);
        addressRecordMapping.add("fraflykomkod", 96, 4);
        addressRecordMapping.add("fraflykomdto", 100, 12);
        addressRecordMapping.add("fraflykomdt_umrk", 112, 1);
        addressRecordMapping.add("start_mynkod-adrtxt", 113, 4);
        addressRecordMapping.add("adr1-supladr", 117, 34);
        addressRecordMapping.add("adr2-supladr", 151, 34);
        addressRecordMapping.add("adr3-supladr", 185, 34);
        addressRecordMapping.add("adr4-supladr", 219, 34);
        addressRecordMapping.add("adr5-supladr", 253, 34);
        addressRecordMapping.add("start_dt-adrtxt", 287, 10);
        addressRecordMapping.add("slet_dt-adtxt", 297, 10);
    }

    private static final Mapping protectionRecordMapping = new Mapping();

    static {
        protectionRecordMapping.add("beskyttype", 14, 4);
        protectionRecordMapping.add("start_dt-beskyttelse", 18, 10);
        protectionRecordMapping.add("slet_dt-beskyttelse", 28, 10);
    }

    private static final Mapping foreignAddressRecordMapping = new Mapping();

    static {
        foreignAddressRecordMapping.add("udr_landekod", 14, 4);
        foreignAddressRecordMapping.add("udrdto", 18, 12);
        foreignAddressRecordMapping.add("udrdto_umrk", 30, 1);
        foreignAddressRecordMapping.add("udlandadr1", 31, 34);
        foreignAddressRecordMapping.add("udlandadr2", 65, 34);
        foreignAddressRecordMapping.add("udlandadr3", 99, 34);
        foreignAddressRecordMapping.add("udlandadr4", 133, 34);
        foreignAddressRecordMapping.add("udlandadr5", 167, 34);
    }

    private static final Mapping nameRecordMapping = new Mapping();

    static {
        nameRecordMapping.add("fornvn", 14, 50);
        nameRecordMapping.add("fornvn_mrk", 64, 1);
        nameRecordMapping.add("melnvn", 65, 40);
        nameRecordMapping.add("melnvn_mrk", 105, 1);
        nameRecordMapping.add("efternvn", 106, 40);
        nameRecordMapping.add("efternvn_mrk", 146, 1);
        nameRecordMapping.add("nvnhaenstart", 147, 12);
        nameRecordMapping.add("haenstart_umrk-navne", 159, 1);
        nameRecordMapping.add("adrnvn", 160, 34);
    }

    private static final Mapping birthRecordMapping = new Mapping();

    static {
        birthRecordMapping.add("start_mynkod-fødested", 14, 4);
        birthRecordMapping.add("myntxt-fødested", 18, 20);
    }

    public static final Mapping citizenshipRecordMapping = new Mapping();

    static {
        citizenshipRecordMapping.add("landekod", 14, 4);
        citizenshipRecordMapping.add("haenstart-statsborgerskab", 18, 12);
        citizenshipRecordMapping.add("haenstart_umrk-statsborgerskab", 30, 1);
    }

    public static final Mapping churchRecordMapping = new Mapping();

    static {
        churchRecordMapping.add("fkirk", 14, 1);
        churchRecordMapping.add("start_dt-folkekirke", 15, 10);
        churchRecordMapping.add("start_dt-umrk-folkekirke", 25, 1);
    }

    public static final Mapping civilstatusRecordMapping = new Mapping();

    static {
        civilstatusRecordMapping.add("civst", 14, 1);
        civilstatusRecordMapping.add("aegtepnr", 15, 10);
        civilstatusRecordMapping.add("aegtefoed_dt", 25, 10);
        civilstatusRecordMapping.add("aegtefoeddt_umrk", 35, 1);
        civilstatusRecordMapping.add("aegtenvn", 36, 34);
        civilstatusRecordMapping.add("aegtenvn_mrk", 70, 1);
        civilstatusRecordMapping.add("haenstart-civilstand", 71, 12);
        civilstatusRecordMapping.add("haenstart_umrk-civilstand", 83, 1);
        civilstatusRecordMapping.add("sep_henvis_ts", 84, 12);
    }

    public static final Mapping parentRecordMapping = new Mapping();

    static {
        parentRecordMapping.add("mor_dt", 14, 10);
        parentRecordMapping.add("mor_dt_umrk", 24, 1);
        parentRecordMapping.add("pnrmor", 25, 10);
        parentRecordMapping.add("mor_foed_dt", 35, 10);
        parentRecordMapping.add("mor_foed_dt_umrk", 45, 1);
        parentRecordMapping.add("mornvn", 46, 34);
        parentRecordMapping.add("mornvn_mrk", 80, 1);
        parentRecordMapping.add("far_dt", 81, 10);
        parentRecordMapping.add("far_dt_umrk", 91, 1);
        parentRecordMapping.add("pnrfar", 92, 10);
        parentRecordMapping.add("far_foed_dt", 102, 10);
        parentRecordMapping.add("far_foed_dt_umrk", 112, 1);
        parentRecordMapping.add("farnvn", 113, 34);
        parentRecordMapping.add("farnvn_mrk", 147, 1);
    }


    public static final Mapping guardianRecordMapping = new Mapping();

    static {
        guardianRecordMapping.add("start_dt-umyndig", 14, 10);
        guardianRecordMapping.add("start_dt_umrk-umyndig", 24, 1);
        guardianRecordMapping.add("slet_dt-umyndig", 25, 10);
        guardianRecordMapping.add("umyn_reltyp", 35, 4);
        guardianRecordMapping.add("relpnr", 39, 10);
        guardianRecordMapping.add("start_dt-relpnr_pnr", 49, 10);
        guardianRecordMapping.add("reladrsat_relpnr_txt", 59, 34);
        guardianRecordMapping.add("start_dt-relpnr_txt", 93, 10);
        guardianRecordMapping.add("reltxt1", 103, 34);
        guardianRecordMapping.add("reltxt2", 137, 34);
        guardianRecordMapping.add("reltxt3", 171, 34);
        guardianRecordMapping.add("reltxt4", 205, 34);
        guardianRecordMapping.add("reltxt5", 239, 34);
    }


    private ListHashMap<RecordType, String> getAvailableRecords(String response) {
        ListHashMap<RecordType, String> records = new ListHashMap<>();
        int start = 28; // start of DATA section in response

        // programmatically find out which records are included in the response, and where they begin
        while (start < response.length()) {
            // record type is always 3 characters in length
            String recordType = response.substring(start, start + 3);
            boolean foundRecord = false;

            for (RecordType rec : RecordType.values()) {
                if (recordType.equals(rec.getValue())) {
                    int end = start + rec.getLength();
                    records.add(rec, response.substring(start, end));
                    start = end;
                    foundRecord = true;
                    break;
                }
            }

            if (!foundRecord) {
                start += recordType.length(); // avoid infinite loops
            }
        }
        return records;
    }

    private enum RecordType {
        START("000", 35),
        PERSON_INFO("001", 106),
        CURRENT_ADDR("002", 306), // assume type 'A' record
        PLAIN_ADDR("003", 249),
        PROTECTION("004", 37),
        FOREIGN_ADDR("005", 200),
        CONTACT_ADDR("006", 203),

        DISAPPEARANCE("007", 26),
        NAME("008", 193),
        BIRTHPLACE("009", 37),
        CITIZENSHIP("010", 30),
        CHURCH("011", 25),
        CIVILSTATUS("012", 95),
        SEPARATION("013", 36),
        CHILDREN("014", 23),
        PARENTS("015", 147),
        CUSTODY("016", 58),
        UNMANAGE("017", 272),
        MUNICIPAL_RELATION("018", 60),
        CREDIT_WARNING("050", 29), // NB: credit warning record first in production 1/1/2017
        UNMANAGE_EXTRA("052", 287),
        END("999", 21);

        private final String value;
        private final int recordLength;

        /**
         * Constructor for RecordType enums.
         *
         * @param value        The record number as defined in PNR specification.
         * @param recordLength The length of the record as defined in PNR specification.
         */
        RecordType(String value, int recordLength) {
            this.value = value;
            this.recordLength = recordLength;
        }

        /**
         * Returns the record number for this enum.
         *
         * @return Returns the record number of this enum as a String value.
         */
        public String getValue() {
            return this.value;
        }

        /**
         * Returns the length of this record type. as an integer.
         *
         * @return Integer value containing length of this record type.
         */
        public int getLength() {
            return this.recordLength;
        }
    }

    private static String substr(String s, int start, int length) {
        return s.substring(start, start + length);
    }

    private static int substrInt(String s, int start, int length) {
        return Integer.parseInt(substr(s, start, length));
    }
}
