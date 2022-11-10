package dk.magenta.datafordeler.cpr;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FtpService {

    private FtpServer server = null;
    private File usersFile = null;
    private File tempDir = null;

    public void startServer(String username, String password, int port, List<File> files) throws Exception {
        /**
         * Cribbed from https://stackoverflow.com/questions/8969097/writing-a-java-ftp-server#8970126
         */
        if (this.server != null) {
            throw new Exception("Server is already running");
        }
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);

        SslConfigurationFactory ssl = new SslConfigurationFactory();
        ssl.setKeystoreFile(new File(ClassLoader.getSystemResource("dafo.keystore").toURI()));
        ssl.setKeystorePassword("password");
        factory.setSslConfiguration(ssl.createSslConfiguration());
        factory.setImplicitSsl(true);

        serverFactory.addListener("default", factory.createListener());
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        this.usersFile = File.createTempFile("ftpusers", ".properties");
        userManagerFactory.setFile(this.usersFile);//choose any. We're telling the FTP-server where to read it's user list
        userManagerFactory.setPasswordEncryptor(new PasswordEncryptor() {//We store clear-text passwords in this example
            @Override
            public String encrypt(String password) {
                return password;
            }

            @Override
            public boolean matches(String passwordToCheck, String storedPassword) {
                return passwordToCheck.equals(storedPassword);
            }
        });
        //Let's add a user, since our myusers.properties files is empty on our first test run
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);

        this.tempDir = Files.createTempDirectory(null).toFile();

        for (File sourcefile : files) {
            File destFile = new File(this.tempDir, sourcefile.getName());
            FileUtils.copyFile(sourcefile, destFile);
        }

        user.setHomeDirectory(this.tempDir.toString());
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        UserManager um = userManagerFactory.createUserManager();
        try {
            um.save(user);//Save the user to the user list on the filesystem
        } catch (FtpException e1) {
            //Deal with exception as you need
        }
        serverFactory.setUserManager(um);
        this.server = serverFactory.createServer();
        try {
            this.server.start();//Your FTP server starts listening for incoming FTP-connections, using the configuration options previously set
        } catch (FtpException ex) {
        }
    }

    public File getTempDir() {
        return tempDir;
    }

    public void stopServer() {
        if (this.server != null) {
            this.server.stop();
            this.server = null;
        }
        if (this.usersFile != null) {
            this.usersFile.delete();
        }
        if (this.tempDir != null) {
            this.tempDir.delete();
        }
    }
}
