package dk.magenta.datafordeler.cvr.configuration;

import dk.magenta.datafordeler.core.configuration.Configuration;
import dk.magenta.datafordeler.core.util.Encryption;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Configuration instance class, holding configuration values in fields
 */
@javax.persistence.Entity
@Table(name="cvr_config")
public class CvrConfiguration implements Configuration {

    public enum RegisterType {
        DISABLED(0),
        LOCAL_FILE(1),
        REMOTE_HTTP(2);

        private int value;
        RegisterType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }


    @Id
    @Column(name = "id")
    private final String plugin = CvrPlugin.class.getName();

    // Midnight every january 1st
    @Column
    private String pullCronSchedule = null;//"0 0 0 1 1 ?";

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType companyRegisterType = RegisterType.LOCAL_FILE;

    @Column
    private String companyRegisterStartAddress = "http://distribution.virk.dk/cvr-permanent/virksomhed/_search";

    @Column
    private String companyRegisterScrollAddress = "http://distribution.virk.dk/_search/scroll";

    @Column
    private String companyRegisterUsername = "";

    @Column
    private String companyRegisterPassword = "";

    @Column
    private byte[] companyRegisterPasswordEncrypted;

    @Transient
    private File companyRegisterPasswordEncryptionFile;

    public void setCompanyRegisterPasswordEncryptionFile(File companyRegisterPasswordEncryptionFile) {
        this.companyRegisterPasswordEncryptionFile = companyRegisterPasswordEncryptionFile;
    }

    @Column(length = 8192)
    public String companyRegisterQuery = "{\n" +
            "  \"query\": {\n" +
            "    \"bool\": {\n" +
            "      \"should\": [\n" +
            "        {\n" +
            "          \"bool\": {\n" +
            "            \"must\": [\n" +
            "              {\n" +
            "                \"terms\": {\n" +
            "                  \"Vrvirksomhed.beliggenhedsadresse.kommune.kommuneKode\":[954, 955, 956, 957, 958, 959, 960, 961, 962]\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"range\": {\n" +
            "                  \"Vrvirksomhed.sidstOpdateret\": {\n" +
            "                    \"gte\": \"%s\"\n" +
            "                  }\n" +
            "                }\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"bool\": {\n" +
            "            \"must\": [\n" +
            "              {\n" +
            "                \"terms\": {\n" +
            "                  \"Vrvirksomhed.cvrNummer\":%s\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"range\": {\n" +
            "                  \"Vrvirksomhed.sidstOpdateret\": {\n" +
            "                    \"gte\": \"%s\"\n" +
            "                  }\n" +
            "                }\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    @Column
    private String companyRegisterDirectLookupCertificate = "";

    @Column
    private String companyRegisterDirectLookupPassword = "";

    @Column
    private byte[] companyRegisterDirectLookupPasswordEncrypted;

    @Column
    private String companyRegisterDirectLookupAddress = "";


    public RegisterType getCompanyRegisterType() {
        return this.companyRegisterType;
    }

    public String getCompanyRegisterStartAddress() {
        return this.companyRegisterStartAddress;
    }

    public String getCompanyRegisterScrollAddress() {
        return this.companyRegisterScrollAddress;
    }

    public String getCompanyRegisterUsername() {
        return this.companyRegisterUsername;
    }

    public String getCompanyRegisterPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.companyRegisterPasswordEncryptionFile, this.companyRegisterPasswordEncrypted);
    }

    public String getCompanyRegisterQuery() {
        return this.companyRegisterQuery;
    }

    public String getCompanyRegisterDirectLookupCertificate() {
        return this.companyRegisterDirectLookupCertificate;
    }

    public String getCompanyRegisterDirectLookupPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.companyRegisterPasswordEncryptionFile, this.companyRegisterDirectLookupPasswordEncrypted);
    }

    public String getCompanyRegisterDirectLookupAddress() {
        return this.companyRegisterDirectLookupAddress;
    }

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType companyUnitRegisterType = RegisterType.DISABLED;

    @Column
    private String companyUnitRegisterStartAddress = "http://distribution.virk.dk/cvr-permanent/produktionsenhed/_search";

    @Column
    private String companyUnitRegisterScrollAddress = "http://distribution.virk.dk/_search/scroll";

    @Column
    private String companyUnitRegisterUsername = "";

    @Column
    private String companyUnitRegisterPassword = "";

    @Column
    private byte[] companyUnitRegisterPasswordEncrypted;

    @Transient
    private File companyUnitRegisterPasswordEncryptionFile;

    public void setCompanyUnitRegisterPasswordEncryptionFile(File companyUnitRegisterPasswordEncryptionFile) {
        this.companyUnitRegisterPasswordEncryptionFile = companyUnitRegisterPasswordEncryptionFile;
    }

    @Column(length = 8192)
    private String companyUnitRegisterQuery = "{\n" +
            "    \"query\": {\n" +
            "        \"filtered\": {\n" +
            "            \"filter\": {\n" +
            "                \"range\": {\n" +
            "                    \"VrproduktionsEnhed.sidstOpdateret\": {\n" +
            "                        \"gte\": \"%s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    public RegisterType getCompanyUnitRegisterType() {
        return this.companyUnitRegisterType;
    }

    public String getCompanyUnitRegisterStartAddress() {
        return this.companyUnitRegisterStartAddress;
    }

    public String getCompanyUnitRegisterScrollAddress() {
        return this.companyUnitRegisterScrollAddress;
    }

    public String getCompanyUnitRegisterUsername() {
        return this.companyUnitRegisterUsername;
    }

    public String getCompanyUnitRegisterPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.companyUnitRegisterPasswordEncryptionFile, this.companyUnitRegisterPasswordEncrypted);
    }

    public String getCompanyUnitRegisterQuery() {
        return this.companyUnitRegisterQuery;
    }




    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType participantRegisterType = RegisterType.DISABLED;

    @Column
    private String participantRegisterStartAddress = "http://distribution.virk.dk/cvr-permanent/deltager/_search";

    @Column
    private String participantRegisterScrollAddress = "http://distribution.virk.dk/_search/scroll";

    @Column
    private String participantRegisterUsername = "";

    @Column
    private String participantRegisterPassword = "";

    @Column
    private byte[] participantRegisterPasswordEncrypted;

    @Transient
    private File participantRegisterPasswordEncryptionFile;

    public void setParticipantRegisterPasswordEncryptionFile(File participantRegisterPasswordEncryptionFile) {
        this.participantRegisterPasswordEncryptionFile = participantRegisterPasswordEncryptionFile;
    }

    @Column(length = 8192)
    private String participantRegisterQuery = "{\n" +
            "    \"query\": {\n" +
            "        \"filtered\": {\n" +
            "            \"filter\": {\n" +
            "                \"range\": {\n" +
            "                    \"Vrdeltagerperson.sidstOpdateret\": {\n" +
            "                        \"gte\": \"%s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";


    @Column
    private String participantRegisterDirectLookupCertificate = "";

    @Column
    private String participantRegisterDirectLookupPassword = "";

    @Column
    private byte[] participantRegisterDirectLookupPasswordEncrypted;

    @Column
    private String participantRegisterDirectLookupAddress = "";

    public RegisterType getParticipantRegisterType() {
        return this.participantRegisterType;
    }

    public String getParticipantRegisterStartAddress() {
        return this.participantRegisterStartAddress;
    }

    public String getParticipantRegisterScrollAddress() {
        return this.participantRegisterScrollAddress;
    }

    public String getParticipantRegisterUsername() {
        return this.participantRegisterUsername;
    }

    public String getParticipantRegisterPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.participantRegisterPasswordEncryptionFile, this.participantRegisterPasswordEncrypted);
    }

    public String getParticipantRegisterQuery() {
        return this.participantRegisterQuery;
    }

    public String getParticipantRegisterDirectLookupCertificate() {
        return this.participantRegisterDirectLookupCertificate;
    }

    public String getParticipantRegisterDirectLookupPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.participantRegisterPasswordEncryptionFile, this.participantRegisterDirectLookupPasswordEncrypted);
    }

    public String getParticipantRegisterDirectLookupAddress() {
        return this.participantRegisterDirectLookupAddress;
    }

    public String getPullCronSchedule() {
        return this.pullCronSchedule;
    }




    public String getQuery(String schema) {
        switch (schema) {
            case CompanyRecord.schema:
                return this.getCompanyRegisterQuery();
            case CompanyUnitRecord.schema:
                return this.getCompanyUnitRegisterQuery();
            case ParticipantRecord.schema:
                return this.getParticipantRegisterQuery();
        }
        return null;
    }

    public RegisterType getRegisterType(String schema) {
        switch (schema) {
            case CompanyRecord.schema:
                return this.getCompanyRegisterType();
            case CompanyUnitRecord.schema:
                return this.getCompanyUnitRegisterType();
            case ParticipantRecord.schema:
                return this.getParticipantRegisterType();
        }
        return null;
    }

    public String getStartAddress(String schema) {
        switch (schema) {
            case CompanyRecord.schema:
                return this.getCompanyRegisterStartAddress();
            case CompanyUnitRecord.schema:
                return this.getCompanyUnitRegisterStartAddress();
            case ParticipantRecord.schema:
                return this.getParticipantRegisterStartAddress();
        }
        return null;
    }

    public String getScrollAddress(String schema) {
        switch (schema) {
            case CompanyRecord.schema:
                return this.getCompanyRegisterScrollAddress();
            case CompanyUnitRecord.schema:
                return this.getCompanyUnitRegisterScrollAddress();
            case ParticipantRecord.schema:
                return this.getParticipantRegisterScrollAddress();
        }
        return null;
    }

    public String getUsername(String schema) {
        switch (schema) {
            case CompanyRecord.schema:
                return this.getCompanyRegisterUsername();
            case CompanyUnitRecord.schema:
                return this.getCompanyUnitRegisterUsername();
            case ParticipantRecord.schema:
                return this.getParticipantRegisterUsername();
        }
        return null;
    }

    public String getPassword(String schema) throws GeneralSecurityException, IOException {
        switch (schema) {
            case CompanyRecord.schema:
                return this.getCompanyRegisterPassword();
            case CompanyUnitRecord.schema:
                return this.getCompanyUnitRegisterPassword();
            case ParticipantRecord.schema:
                return this.getParticipantRegisterPassword();
        }
        return null;
    }




    public boolean encryptCompanyRegisterPassword() {
        if (
                this.companyRegisterPasswordEncryptionFile != null &&
                !(this.companyRegisterPassword == null || this.companyRegisterPassword.isEmpty()) &&
                (this.companyRegisterPasswordEncrypted == null || this.companyRegisterPasswordEncrypted.length == 0)
                ) {
            try {
                this.companyRegisterPasswordEncrypted = Encryption.encrypt(this.companyRegisterPasswordEncryptionFile, this.companyRegisterPassword);
                return true;
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean encryptCompanyUnitRegisterPassword() {
        if (
                this.companyUnitRegisterPasswordEncryptionFile != null &&
                !(this.companyUnitRegisterPassword == null || this.companyUnitRegisterPassword.isEmpty()) &&
                (this.companyUnitRegisterPasswordEncrypted == null || this.companyUnitRegisterPasswordEncrypted.length == 0)
                ) {
            try {
                this.companyUnitRegisterPasswordEncrypted = Encryption.encrypt(this.companyUnitRegisterPasswordEncryptionFile, this.companyUnitRegisterPassword);
                return true;
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean encryptParticipantRegisterPassword() {
        if (
                this.participantRegisterPasswordEncryptionFile != null &&
                !(this.participantRegisterPassword == null || this.participantRegisterPassword.isEmpty()) &&
                (this.participantRegisterPasswordEncrypted == null || this.participantRegisterPasswordEncrypted.length == 0)
                ) {
            try {
                this.participantRegisterPasswordEncrypted = Encryption.encrypt(this.participantRegisterPasswordEncryptionFile, this.participantRegisterPassword);
                return true;
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }






    public boolean encryptCompanyDirectRegisterPassword() {

        if (
                this.companyRegisterPasswordEncryptionFile != null &&
                        !(this.companyRegisterDirectLookupPassword == null || this.companyRegisterDirectLookupPassword.isEmpty()) &&
                        (this.companyRegisterDirectLookupPasswordEncrypted == null || this.companyRegisterDirectLookupPasswordEncrypted.length == 0)
                ) {
            try {
                this.companyRegisterDirectLookupPasswordEncrypted = Encryption.encrypt(this.companyRegisterPasswordEncryptionFile, this.companyRegisterDirectLookupPassword);
                return true;
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean encryptParticipantDirectRegisterPassword() {
        if (
                this.participantRegisterPasswordEncryptionFile != null &&
                        !(this.participantRegisterDirectLookupPassword == null || this.participantRegisterDirectLookupPassword.isEmpty()) &&
                        (this.participantRegisterDirectLookupPasswordEncrypted == null || this.participantRegisterDirectLookupPasswordEncrypted.length == 0)
                ) {
            try {
                this.participantRegisterDirectLookupPasswordEncrypted = Encryption.encrypt(this.participantRegisterPasswordEncryptionFile, this.participantRegisterDirectLookupPassword);
                return true;
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
