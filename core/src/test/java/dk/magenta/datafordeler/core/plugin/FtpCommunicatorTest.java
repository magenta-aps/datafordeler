package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FtpCommunicatorTest {

    private static final String TEST_FILE_ENDING = ".test";

    @Test
    public void testDownloadNoNewFiles() throws Exception {
        //TODO implement test for correct handling of no new files

        int port = 2102;
        String contents = null;
        File tempFile = File.createTempFile("ftpdownload2102", FtpCommunicator.DONE_FILE_ENDING);
        tempFile.createNewFile();

        Writer fileWriter = this.getFileWriter(tempFile);

        fileWriter.write("this is a test æøåÆØÅ!");
        fileWriter.close();
        try {
            ftpTransferTest(port, Collections.singletonList(tempFile), "", contents);
        } catch (ExecutionException e) {

        }
        tempFile.delete();
    }

    @Test
    public void testDownloadSingleFile() throws Exception {

        int port = 2102;
        String contents = "this is a test æøåÆØÅ!";
        File tempFile = File.createTempFile("ftpdownload2102", TEST_FILE_ENDING);
        tempFile.createNewFile();

        FileUtils.writeByteArrayToFile(tempFile, contents.getBytes("UTF-8"));

        ftpTransferTest(port, Collections.singletonList(tempFile), "", contents);

        tempFile.delete();
    }


    @Test
    public void testDownloadIgnoreDoneFiles() throws Exception {

        int port = 2103;

        //Create file which should be read
        String contents = "this is a test æøåÆØÅ!";
        File tempFile1 = File.createTempFile("ftpdownload2103", TEST_FILE_ENDING);
        tempFile1.createNewFile();

        Writer fileWriter1 = this.getFileWriter(tempFile1);
        fileWriter1.write(contents);
        fileWriter1.close();

        //Create another file, which should not be read
        File tempFile2 = File.createTempFile("ftpdownload21032", FtpCommunicator.DONE_FILE_ENDING);
        tempFile2.createNewFile();

        Writer fileWriter2 = this.getFileWriter(tempFile2);
        fileWriter2.write("text that should not be read");
        fileWriter2.close();

        //Make list of files
        List<File> tempFiles = new ArrayList<>();
        tempFiles.add(tempFile1);
        tempFiles.add(tempFile2);

        //Setup server and use ftpCommunicator
        ftpTransferTest(port, tempFiles, "", contents);


        tempFile1.delete();
        tempFile2.delete();

    }

    @Test
    public void testDownloadTwoFiles() throws Exception {

        int port = 2104;

        //Create file which should be read
        String contents = "this is a test æøåÆØÅ!";
        File tempFile1 = File.createTempFile("ftpdownload21041", TEST_FILE_ENDING);
        tempFile1.createNewFile();

        Writer fileWriter1 = this.getFileWriter(tempFile1);
        //The substring is splitting the contents into 2 files, it should then be concatenated
        fileWriter1.write(contents.substring(0, 7));
        fileWriter1.close();

        //Create another file
        File tempFile2 = File.createTempFile("ftpdownload21042", TEST_FILE_ENDING);
        tempFile2.createNewFile();

        Writer fileWriter2 = this.getFileWriter(tempFile2);
        fileWriter2.write(contents.substring(7));
        fileWriter2.close();

        //Make list of files
        List<File> tempFiles = new ArrayList<>();
        tempFiles.add(tempFile1);
        tempFiles.add(tempFile2);

        //Setup server and use ftpCommunicator
        ftpTransferTest(port, tempFiles, "", contents);


        tempFile1.delete();
        tempFile2.delete();

    }

    @Test
    public void testDownloadMultipleFiles() throws Exception {

        int port = 2105;

        //Create file which should be read
        String contents = "this is a test æøåÆØÅ!";
        File tempFile1 = File.createTempFile("ftpdownload21041", TEST_FILE_ENDING);
        tempFile1.createNewFile();

        Writer fileWriter1 = this.getFileWriter(tempFile1);
        //The substring is splitting the contents into 2 files, it should then be concatenated
        fileWriter1.write(contents.substring(0, 3));
        fileWriter1.close();

        //Create another file
        File tempFile2 = File.createTempFile("ftpdownload21042", TEST_FILE_ENDING);
        tempFile2.createNewFile();

        Writer fileWriter2 = this.getFileWriter(tempFile2);
        fileWriter2.write(contents.substring(3, 8));
        fileWriter2.close();

        //Create another file
        File tempFile3 = File.createTempFile("ftpdownload21043", TEST_FILE_ENDING);
        tempFile3.createNewFile();

        Writer fileWriter3 = this.getFileWriter(tempFile3);
        fileWriter3.write(contents.substring(8));
        fileWriter3.close();

        //Make list of files
        List<File> tempFiles = new ArrayList<>();
        tempFiles.add(tempFile1);
        tempFiles.add(tempFile2);
        tempFiles.add(tempFile3);


        //Setup server and use ftpCommunicator
        ftpTransferTest(port, tempFiles, "", contents);


        tempFile1.delete();
        tempFile2.delete();
        tempFile3.delete();

    }

    private static SSLSocketFactory getTrustAllSSLSocketFactory() {
        TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManager, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext.getSocketFactory();
    }

    private void ftpTransferTest(int port, List<File> tempFiles, String path, String contents) throws Exception {
        String username = "test";
        String password = "test";

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                FtpCommunicatorTest.this.startServer(username, password, port, tempFiles);
                try {
                    FtpCommunicator ftpCommunicator = new FileProgressFtpCommunicator(username, password, true);
                    ftpCommunicator.setSslSocketFactory(getTrustAllSSLSocketFactory());
                    InputStream inputStream = ftpCommunicator.fetch(new URI("ftp://localhost:" + port + "/" + path));
                    String data = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
                    inputStream.close();
                    Assert.assertEquals(contents, data);
                    Assert.assertTrue(allFilesEndsWithDone());
                } finally {
                    FtpCommunicatorTest.this.stopServer();
                }
                return true;
            }
        });
        future.get(30, TimeUnit.SECONDS);
        executorService.shutdownNow();
        this.stopServer();
    }

    private boolean allFilesEndsWithDone() {
        String[] fileNames = tempDir.list();
        for (int i = 0; i < fileNames.length; i++) {
            if (!fileNames[i].endsWith(FtpCommunicator.DONE_FILE_ENDING)) {
                return false;
            }
        }
        return true;
    }


    private FtpServer server = null;
    private File usersFile = null;
    private File tempDir = null;

    private void startServer(String username, String password, int port, List<File> files) throws Exception {
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
        ssl.setKeystoreFile(new File("keystores/dafo.keystore"));
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

        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            this.tempDir = Files.createTempDirectory(
                    null,
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwxrwxrwx")
                    )
            ).toFile();
        } else {
            this.tempDir = Files.createTempDirectory(null).toFile();
        }


        for (File sourcefile : files) {
            File destFile = new File(this.tempDir, sourcefile.getName());
            FileUtils.copyFile(sourcefile, destFile);
        }

        user.setHomeDirectory(this.tempDir.toString());
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        UserManager um = userManagerFactory.createUserManager();
        um.save(user);//Save the user to the user list on the filesystem
        serverFactory.setUserManager(um);
        this.server = serverFactory.createServer();
        this.server.start();
    }

    private void stopServer() {
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

    private Writer getFileWriter(File outputFile) throws FileNotFoundException {
        try {
            return new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile), "UTF-8"
            ));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
