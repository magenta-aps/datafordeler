package dk.magenta.datafordeler.cpr.configuration;

import dk.magenta.datafordeler.core.configuration.Configuration;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.util.Encryption;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.residence.ResidenceEntityManager;
import dk.magenta.datafordeler.cpr.data.road.RoadEntityManager;
/*import dk.magenta.datafordeler.cpr.parsers.PersonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;*/

import javax.persistence.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@javax.persistence.Entity
@Table(name="cpr_config")
public class CprConfiguration implements Configuration {

    //private Logger log = LogManager.getLogger(PersonParser.class);

    public enum Charset {
        US_ASCII(0),
        ISO_8859_1(1),
        UTF_8(2),
        UTF_16BE(3),
        UTF_16LE(4),
        UTF_16(5);

        private int value;
        Charset(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum RegisterType {
        DISABLED(0),
        LOCAL_FILE(1),
        REMOTE_FTP(2),
        TEST_LOCAL_FILE(3);

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
    private final String plugin = CprPlugin.class.getName();




    // Midnight every january 1st
    @Column
    private String personRegisterPullCronSchedule = "0 0 0 1 1 ?";

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType personRegisterType = RegisterType.LOCAL_FILE;

    @Column
    private String personRegisterFtpAddress = "ftps://ftp.cpr.dk";

    @Column
    private String personRegisterFtpDownloadFolder = "/";

    @Column
    private String personRegisterFtpUploadFolder = "/";

    @Column
    private String personRegisterFtpUsername = "";

    @Column
    private String personRegisterFtpPassword = "";

    @Column
    private String personRegisterLocalFile = "cache/d170608.l534902";

    @Column
    @Enumerated(EnumType.ORDINAL)
    private Charset personRegisterDataCharset = Charset.ISO_8859_1;

    @Column
    private byte[] personRegisterPasswordEncrypted;

    @Transient
    private File personRegisterPasswordEncryptionFile;

    public void setPersonRegisterPasswordEncryptionFile(File personRegisterPasswordEncryptionFile) {
        this.personRegisterPasswordEncryptionFile = personRegisterPasswordEncryptionFile;
    }


    @Column
    private String roadRegisterPullCronSchedule = null;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType roadRegisterType = RegisterType.DISABLED;

    @Column
    private String roadRegisterFtpAddress = null;

    @Column
    private String roadRegisterFtpDownloadFolder = "/";

    @Column
    private String roadRegisterFtpUploadFolder = "/";

    @Column
    private String roadRegisterFtpUsername = null;

    @Column
    private String roadRegisterFtpPassword = null;

    @Column
    private String roadRegisterLocalFile = "data/cprroaddata.txt";

    @Column
    @Enumerated(EnumType.ORDINAL)
    private Charset roadRegisterDataCharset = Charset.ISO_8859_1;

    @Column
    private byte[] roadRegisterPasswordEncrypted;

    @Transient
    private File roadRegisterPasswordEncryptionFile;

    public void setRoadRegisterPasswordEncryptionFile(File roadRegisterPasswordEncryptionFile) {
        this.roadRegisterPasswordEncryptionFile = roadRegisterPasswordEncryptionFile;
    }


    @Column
    private String residenceRegisterPullCronSchedule = null;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType residenceRegisterType = RegisterType.DISABLED;

    @Column
    private String residenceRegisterFtpAddress = null;

    @Column
    private String residenceRegisterFtpDownloadFolder = "/";

    @Column
    private String residenceRegisterFtpUploadFolder = "/";

    @Column
    private String residenceRegisterFtpUsername = null;

    @Column
    private String residenceRegisterFtpPassword = null;

    @Column
    private String residenceRegisterLocalFile = "data/cprroaddata.txt";

    @Column
    @Enumerated(EnumType.ORDINAL)
    private Charset residenceRegisterDataCharset = Charset.ISO_8859_1;


    @Column
    private byte[] residenceRegisterPasswordEncrypted;

    @Transient
    private File residenceRegisterPasswordEncryptionFile;

    public void setResidenceRegisterPasswordEncryptionFile(File residenceRegisterPasswordEncryptionFile) {
        this.residenceRegisterPasswordEncryptionFile = residenceRegisterPasswordEncryptionFile;
    }




    @Column
    private String directTransactionCode = "OFF4";

    @Column
    private int directCustomerNumber = 0;

    @Column
    private String directUsername = "";

    @Column
    private byte[] directPasswordEncrypted;

    @Transient
    private File directPasswordPasswordEncryptionFile;

    public void setDirectPasswordPasswordEncryptionFile(File directPasswordPasswordEncryptionFile) {
        this.directPasswordPasswordEncryptionFile = directPasswordPasswordEncryptionFile;
    }

    @Column
    private String directHost = "direkte.cpr.dk";

    @Column
    private short directPort = 5000;







    public String getPersonRegisterPullCronSchedule() {
        return this.personRegisterPullCronSchedule;
    }

    public RegisterType getPersonRegisterType() {
        return this.personRegisterType;
    }

    public String getPersonRegisterFtpAddress() {
        return this.personRegisterFtpAddress;
    }

    public String getPersonRegisterFtpDownloadFolder() {
        return this.personRegisterFtpDownloadFolder;
    }

    public String getPersonRegisterFtpUploadFolder() {
        return this.personRegisterFtpUploadFolder;
    }

    public String getPersonRegisterFtpUsername() {
        return this.personRegisterFtpUsername;
    }

    public String getPersonRegisterFtpPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.personRegisterPasswordEncryptionFile, this.personRegisterPasswordEncrypted);
    }

    public String getPersonRegisterLocalFile() {
        return this.personRegisterLocalFile;
    }

    public String getPersonRegisterCharset() {
        return this.formatCharset(this.personRegisterDataCharset);
    }

    public URI getPersonRegisterURI() throws ConfigurationException {
        return this.formatURI(
                this.personRegisterType,
                this.personRegisterLocalFile,
                (this.personRegisterFtpAddress != null && this.personRegisterFtpDownloadFolder != null) ?
                        (this.personRegisterFtpAddress + this.personRegisterFtpDownloadFolder) : null
        );
    }

    public URI getPersonRegisterSubscriptionURI() throws ConfigurationException {
        return this.formatURI(
                this.personRegisterType,
                this.personRegisterLocalFile,
                (this.personRegisterFtpAddress != null && this.personRegisterFtpUploadFolder != null) ?
                        (this.personRegisterFtpAddress + this.personRegisterFtpUploadFolder) : null
        );
    }








    public String getRoadRegisterPullCronSchedule() {
        return this.roadRegisterPullCronSchedule;
    }

    public RegisterType getRoadRegisterType() {
        return this.roadRegisterType;
    }

    public String getRoadRegisterFtpAddress() {
        return this.roadRegisterFtpAddress;
    }

    public String getRoadRegisterFtpUsername() {
        return this.roadRegisterFtpUsername;
    }

    public String getRoadRegisterFtpPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.roadRegisterPasswordEncryptionFile, this.roadRegisterPasswordEncrypted);
    }

    public String getRoadRegisterLocalFile() {
        return this.roadRegisterLocalFile;
    }

    public String getRoadRegisterCharset() {
        return this.formatCharset(this.roadRegisterDataCharset);
    }

    public URI getRoadRegisterURI() throws ConfigurationException {
        return this.formatURI(
                this.roadRegisterType,
                this.roadRegisterLocalFile,
                (this.roadRegisterFtpAddress != null && this.roadRegisterFtpDownloadFolder != null) ?
                        (this.roadRegisterFtpAddress + this.roadRegisterFtpDownloadFolder) : null
        );
    }

    public URI getRoadRegisterSubscriptionURI() throws ConfigurationException {
        return this.formatURI(
                this.roadRegisterType,
                this.roadRegisterLocalFile,
                (this.roadRegisterFtpAddress != null && this.roadRegisterFtpUploadFolder != null) ?
                        (this.roadRegisterFtpAddress + this.roadRegisterFtpUploadFolder) : null
        );
    }





    public String getResidenceRegisterPullCronSchedule() {
        return this.residenceRegisterPullCronSchedule;
    }

    public RegisterType getResidenceRegisterType() {
        return this.residenceRegisterType;
    }

    public String getResidenceRegisterFtpAddress() {
        return this.residenceRegisterFtpAddress;
    }

    public String getResidenceRegisterFtpUsername() {
        return this.residenceRegisterFtpUsername;
    }

    public String getResidenceRegisterFtpPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.residenceRegisterPasswordEncryptionFile, this.residenceRegisterPasswordEncrypted);
    }

    public String getResidenceRegisterLocalFile() {
        return this.residenceRegisterLocalFile;
    }

    public String getResidenceRegisterCharset() {
        return this.formatCharset(this.residenceRegisterDataCharset);
    }

    public URI getResidenceRegisterURI() throws ConfigurationException {
        return this.formatURI(
                this.residenceRegisterType,
                this.residenceRegisterLocalFile,
                (this.residenceRegisterFtpAddress != null && this.residenceRegisterFtpDownloadFolder != null) ?
                        (this.residenceRegisterFtpAddress + this.residenceRegisterFtpDownloadFolder) : null
        );
    }

    public URI getResidenceRegisterSubscriptionURI() throws ConfigurationException {
        return this.formatURI(
                this.residenceRegisterType,
                this.residenceRegisterLocalFile,
                (this.residenceRegisterFtpAddress != null && this.residenceRegisterFtpUploadFolder != null) ?
                        (this.residenceRegisterFtpAddress + this.residenceRegisterFtpUploadFolder) : null
        );
    }





    public String getRegisterPullCronSchedule(EntityManager entityManager) {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterPullCronSchedule();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterPullCronSchedule();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterPullCronSchedule();
        }
        return null;
    }

    public RegisterType getRegisterType(EntityManager entityManager) {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterType();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterType();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterType();
        }
        return null;
    }

    public String getRegisterFtpAddress(EntityManager entityManager) {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterFtpAddress();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterFtpAddress();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterFtpAddress();
        }
        return null;
    }

    public String getRegisterFtpUsername(EntityManager entityManager) {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterFtpUsername();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterFtpUsername();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterFtpUsername();
        }
        return null;
    }

    public String getRegisterFtpPassword(EntityManager entityManager) throws GeneralSecurityException, IOException {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterFtpPassword();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterFtpPassword();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterFtpPassword();
        }
        return null;
    }

    public String getRegisterLocalFile(EntityManager entityManager) {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterLocalFile();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterLocalFile();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterLocalFile();
        }
        return null;
    }

    public String getRegisterCharset(EntityManager entityManager) {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterCharset();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterCharset();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterCharset();
        }
        return null;
    }

    public URI getRegisterURI(EntityManager entityManager) throws ConfigurationException {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterURI();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterURI();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterURI();
        }
        return null;
    }

    public URI getRegisterSubscriptionURI(EntityManager entityManager) throws ConfigurationException {
        if (entityManager instanceof PersonEntityManager) {
            return this.getPersonRegisterSubscriptionURI();
        }
        if (entityManager instanceof RoadEntityManager) {
            return this.getRoadRegisterSubscriptionURI();
        }
        if (entityManager instanceof ResidenceEntityManager) {
            return this.getResidenceRegisterSubscriptionURI();
        }
        return null;
    }


    public String getDirectTransactionCode() {
        return this.directTransactionCode;
    }

    public int getDirectCustomerNumber() {
        return this.directCustomerNumber;
    }

    public String getDirectUsername() {
        return this.directUsername;
    }

    public byte[] getEncryptedDirectPassword() throws GeneralSecurityException, IOException {
        return this.directPasswordEncrypted;
    }

    public String getDirectPassword() throws GeneralSecurityException, IOException {
        return Encryption.decrypt(this.directPasswordPasswordEncryptionFile, this.directPasswordEncrypted);
    }

    public String getDirectHost() {
        return this.directHost;
    }

    public short getDirectPort() {
        return this.directPort;
    }



    private String formatCharset(Charset charset) {
        if (charset != null) {
            return charset.name().replaceAll("_", "-");
        }
        return null;
    }

    private URI formatURI(RegisterType registerType, String localFile, String ftpAddress) throws ConfigurationException {
        if (registerType == RegisterType.DISABLED) {
            return null;
        } else if (registerType == RegisterType.LOCAL_FILE) {
            File file = new File(localFile);
            if (!file.exists()) {
                String full = "";
                try {
                    full = " (" + file.getAbsolutePath() + ")";
                } catch (SecurityException e) {}
                throw new ConfigurationException("Configured file not found: " + localFile + full);
            }
            return file.toURI();
        } else if (registerType == RegisterType.REMOTE_FTP) {
            try {
                return new URI(ftpAddress);
            } catch (URISyntaxException|NullPointerException e) {
                throw new ConfigurationException("Invalid FTP address configured: " + ftpAddress);
            }
        } else if (registerType == RegisterType.TEST_LOCAL_FILE) {
            File file = new File(personRegisterLocalFile);
            if (!file.exists()) {
                throw new ConfigurationException("Configured file not found: " + personRegisterLocalFile);
            }
            return file.toURI();
        }
        return null;
    }


    /**
     * For testing - test methods will set these, but not save the entity to DB
     */

    public void setPersonRegisterPullCronSchedule(String personRegisterPullCronSchedule) {
        this.personRegisterPullCronSchedule = personRegisterPullCronSchedule;
    }

    public void setPersonRegisterType(RegisterType personRegisterType) {
        this.personRegisterType = personRegisterType;
    }

    public void setPersonRegisterFtpAddress(String personRegisterFtpAddress) {
        this.personRegisterFtpAddress = personRegisterFtpAddress;
    }

    public void setPersonRegisterFtpUsername(String personRegisterFtpUsername) {
        this.personRegisterFtpUsername = personRegisterFtpUsername;
    }

    public void setPersonRegisterFtpPassword(String personRegisterFtpPassword) throws GeneralSecurityException, IOException {
        this.personRegisterPasswordEncrypted = Encryption.encrypt(this.personRegisterPasswordEncryptionFile, personRegisterFtpPassword);
    }

    public void setPersonRegisterLocalFile(String personRegisterLocalFile) {
        this.personRegisterLocalFile = personRegisterLocalFile;
    }

    public void setPersonRegisterDataCharset(Charset personRegisterDataCharset) {
        this.personRegisterDataCharset = personRegisterDataCharset;
    }

    public void setRoadRegisterPullCronSchedule(String roadRegisterPullCronSchedule) {
        this.roadRegisterPullCronSchedule = roadRegisterPullCronSchedule;
    }

    public void setRoadRegisterType(RegisterType roadRegisterType) {
        this.roadRegisterType = roadRegisterType;
    }

    public void setRoadRegisterFtpAddress(String roadRegisterFtpAddress) {
        this.roadRegisterFtpAddress = roadRegisterFtpAddress;
    }

    public void setRoadRegisterFtpUsername(String roadRegisterFtpUsername) {
        this.roadRegisterFtpUsername = roadRegisterFtpUsername;
    }

    public void setRoadRegisterFtpPassword(String roadRegisterFtpPassword) throws GeneralSecurityException, IOException {
        this.roadRegisterPasswordEncrypted = Encryption.encrypt(this.roadRegisterPasswordEncryptionFile, roadRegisterFtpPassword);
    }

    public void setRoadRegisterLocalFile(String roadRegisterLocalFile) {
        this.roadRegisterLocalFile = roadRegisterLocalFile;
    }

    public void setRoadRegisterDataCharset(Charset roadRegisterDataCharset) {
        this.roadRegisterDataCharset = roadRegisterDataCharset;
    }

    public void setResidenceRegisterPullCronSchedule(String residenceRegisterPullCronSchedule) {
        this.residenceRegisterPullCronSchedule = residenceRegisterPullCronSchedule;
    }

    public void setResidenceRegisterType(RegisterType residenceRegisterType) {
        this.residenceRegisterType = residenceRegisterType;
    }

    public void setResidenceRegisterFtpAddress(String residenceRegisterFtpAddress) {
        this.residenceRegisterFtpAddress = residenceRegisterFtpAddress;
    }

    public void setResidenceRegisterFtpUsername(String residenceRegisterFtpUsername) {
        this.residenceRegisterFtpUsername = residenceRegisterFtpUsername;
    }

    public void setResidenceRegisterFtpPassword(String residenceRegisterFtpPassword) throws GeneralSecurityException, IOException {
        this.residenceRegisterPasswordEncrypted = Encryption.encrypt(this.residenceRegisterPasswordEncryptionFile, residenceRegisterFtpPassword);
    }

    public void setResidenceRegisterLocalFile(String residenceRegisterLocalFile) {
        this.residenceRegisterLocalFile = residenceRegisterLocalFile;
    }

    public void setResidenceRegisterDataCharset(Charset residenceRegisterDataCharset) {
        this.residenceRegisterDataCharset = residenceRegisterDataCharset;
    }

    public void setDirectTransactionCode(String directTransactionCode) {
        this.directTransactionCode = directTransactionCode;
    }

    public void setDirectCustomerNumber(int directCustomerNumber) {
        this.directCustomerNumber = directCustomerNumber;
    }

    public void setDirectUsername(String directUsername) {
        this.directUsername = directUsername;
    }

    public void setDirectPassword(String directPassword) throws GeneralSecurityException, IOException {
        this.directPasswordEncrypted = Encryption.encrypt(this.directPasswordPasswordEncryptionFile, directPassword);
    }

    public void setDirectHost(String directHost) {
        this.directHost = directHost;
    }

    public void setDirectPort(short directPort) {
        this.directPort = directPort;
    }



    public boolean encryptPersonRegisterPassword() {
        if (
                this.personRegisterPasswordEncryptionFile != null &&
                !(this.personRegisterFtpPassword == null || this.personRegisterFtpPassword.isEmpty()) &&
                (this.personRegisterPasswordEncrypted == null || this.personRegisterPasswordEncrypted.length == 0)
                ) {
            try {
                this.personRegisterPasswordEncrypted = Encryption.encrypt(this.personRegisterPasswordEncryptionFile, this.personRegisterFtpPassword);
                return true;
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean encryptRoadRegisterPassword() {
        if (
                this.roadRegisterPasswordEncryptionFile != null &&
                !(this.roadRegisterFtpPassword == null || this.roadRegisterFtpPassword.isEmpty()) &&
                (this.roadRegisterPasswordEncrypted == null || this.roadRegisterPasswordEncrypted.length == 0)
                ) {
            try {
                this.roadRegisterPasswordEncrypted = Encryption.encrypt(this.roadRegisterPasswordEncryptionFile, this.roadRegisterFtpPassword);
                return true;
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean encryptResidenceRegisterPassword() {
        if (
                this.residenceRegisterPasswordEncryptionFile != null &&
                !(this.residenceRegisterFtpPassword == null || this.residenceRegisterFtpPassword.isEmpty()) &&
                (this.residenceRegisterPasswordEncrypted == null || this.residenceRegisterPasswordEncrypted.length == 0)
                ) {
            try {
                this.residenceRegisterPasswordEncrypted = Encryption.encrypt(this.residenceRegisterPasswordEncryptionFile, this.residenceRegisterFtpPassword);
                return true;
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
