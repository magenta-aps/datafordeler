package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.util.CloseDetectInputStream;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import it.sauronsoftware.ftp4j.*;
import it.sauronsoftware.ftp4j.connectors.HTTPTunnelConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.StatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class to fetch files by FTP. Files are fetched to a local folder, and an ImportInputStream is returned
 * for the fetched files.
 * Files are optionally kept and optionally marked to help determine what has already been imported.
 */
public abstract class FtpCommunicator implements Communicator {

    public static final String DONE_FILE_ENDING = ".done";

    private static final Logger log = LogManager.getLogger(FtpCommunicator.class.getCanonicalName());

    protected String username;
    protected String password;
    protected boolean useFtps;
    protected String proxyString;
    private final Path localCopyFolder;
    private SSLSocketFactory sslSocketFactory;
    private boolean keepFiles = false;
    private boolean markLocalFiles = true;
    private boolean markRemoteFiles = true;

    public FtpCommunicator(String username, String password, boolean useFtps) throws DataStreamException {
        this(username, password, useFtps, null, null, false, true, true);
    }

    /**
     * Construct an instance
     *
     * @param username        Login username
     * @param password        Login password
     * @param useFtps         Connect with FTPS
     * @param proxyString     Optional proxy
     * @param localCopyFolder Optional folder to hold fetched files in. If unset, a temporary folder will be created
     * @param keepFiles       Flag to keep fetched files after stream close
     * @param markLocalFiles  Flag to mark local files with the ".done" extension after stream close
     * @param markRemoteFiles Flag to mark remote files with the ".done" extension after stream close
     * @throws DataStreamException
     */
    public FtpCommunicator(String username, String password, boolean useFtps,
                           String proxyString, String localCopyFolder, boolean keepFiles, boolean markLocalFiles, boolean markRemoteFiles) throws DataStreamException {
        this.username = username;
        this.password = password;
        this.useFtps = useFtps;
        this.proxyString = proxyString;

        if (localCopyFolder != null) {
            this.localCopyFolder = Paths.get(localCopyFolder);
        } else {
            try {
                this.localCopyFolder = createTempFolder();
            } catch (IOException e) {
                throw new DataStreamException("Unable to create temporary folder", e);
            }
        }
        this.keepFiles = keepFiles;
        this.markLocalFiles = markLocalFiles;
        this.markRemoteFiles = markRemoteFiles;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    private static Path createTempFolder() throws IOException {
        File temp = File.createTempFile("dafo-" + UUID.randomUUID(), "");
        temp.delete();
        temp.mkdir();
        return Paths.get(temp.getAbsolutePath());
    }

    protected void setupProxy(FTPClient ftpClient) throws DataStreamException {
        if (Strings.isEmpty(this.proxyString)) {
            log.info("No proxy");
        } else {
            log.info("Using proxy " + this.proxyString);
            try {
                URI proxyURI = new URI(this.proxyString);
                HTTPTunnelConnector connector = new HTTPTunnelConnector(
                        proxyURI.getHost(), proxyURI.getPort()
                );
                ftpClient.setConnector(connector);
            } catch (Exception e) {
                throw new DataStreamException("Could not add proxy to FTP connection", e);
            }
        }
    }

    protected FTPClient performConnect(URI uri) throws IOException, DataStreamException {
        FTPClient ftpClient = new FTPClient();
        if (this.useFtps) {
            ftpClient.setSecurity(FTPClient.SECURITY_FTPS);
            log.info("Connecting to " + uri.toString()+", using ftps");
        } else {
            log.info("Connecting to " + uri.toString()+", not using ftps");
        }
        setupProxy(ftpClient);
        if (this.sslSocketFactory != null) {
            ftpClient.setSSLSocketFactory(this.sslSocketFactory);
        }

        try {
            if (uri.getPort() > 0) {
                ftpClient.connect(uri.getHost(), uri.getPort());
            } else {
                ftpClient.connect(uri.getHost());
            }
            ftpClient.login(this.username, this.password);
            return ftpClient;
        } catch (FTPIllegalReplyException | FTPException e) {
            throw new DataStreamException(e);
        }
    }

    public ImportInputStream fetchLocal() throws DataStreamException {
        ArrayList<File> files = new ArrayList<>(Arrays.asList(localCopyFolder.toFile().listFiles()));
        files.sort(File::compareTo);
        log.info("Fetching from local files in "+localCopyFolder);
        try {
            InputStream inputStream = this.buildChainedInputStream(files);
            if (inputStream != null) {
                return new ImportInputStream(new CloseDetectInputStream(inputStream), files);
            } else {
                return new ImportInputStream(new ByteArrayInputStream("".getBytes()));
            }
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    public Path getLocalCopyFolder() {
        return this.localCopyFolder;
    }


    public void listDownloadable(URI uri) {
        FTPClient ftpClient = null;
        try {
            ftpClient = performConnect(uri);
            ftpClient.changeDirectory(uri.getPath());
            String[] list = ftpClient.listNames();
            List<String> remotePaths = Arrays.asList(list);
            remotePaths.sort(Comparator.naturalOrder());
            this.filterFilesToDownload(remotePaths);
        } catch (IOException | DataStreamException | FTPIllegalReplyException | FTPDataTransferException |
                 FTPAbortedException | FTPListParseException | FTPException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fetches all yet-unfetched files from the folder denoted by the uri.
     */
    @Override
    public ImportInputStream fetch(URI uri) throws HttpStatusException, DataStreamException {
        try {
            FTPClient ftpClient = performConnect(uri);

            ftpClient.changeDirectory(uri.getPath());
            String[] list = ftpClient.listNames();
            List<String> remotePaths = Arrays.asList(list);
            remotePaths.sort(Comparator.naturalOrder());
            List<String> downloadPaths = this.filterFilesToDownload(remotePaths);

            ArrayList<File> currentFiles = new ArrayList<>();
            for (String path : downloadPaths) {
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                File outputFile = Files.createFile(Paths.get(localCopyFolder.toString(), fileName)).toFile();
                log.info("Downloading " + path + " to " + outputFile.getAbsolutePath());
                ftpClient.download(path, outputFile);
                currentFiles.add(outputFile);
            }
            log.info("Downloaded "+currentFiles.size()+" files");

            this.onBeforeBuildStream(ftpClient, currentFiles, uri, downloadPaths);
            InputStream inputStream = this.buildChainedInputStream(currentFiles);

            if (inputStream != null) {
                CloseDetectInputStream inputCloser = new CloseDetectInputStream(inputStream);
                inputCloser.addAfterCloseListener(() -> {
                    try {
                        log.info("Input stream closed. Cleaning up");
                        onStreamClose(ftpClient, currentFiles, uri, downloadPaths);
                    } finally {
                        try {
                            ftpClient.disconnect(true);
                            log.info("FTP connection closed");
                        } catch (IOException | FTPIllegalReplyException | FTPException e) {
                            log.error(e);
                        }
                    }
                });
                return new ImportInputStream(inputCloser, currentFiles);
            } else {
                return new ImportInputStream(new ByteArrayInputStream("".getBytes()));
            }
        } catch (FTPException | FTPIllegalReplyException | FTPAbortedException | FTPDataTransferException | FTPListParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        return null;
    }

    protected void onBeforeBuildStream(FTPClient ftpClient, List<File> localFiles, URI uri, List<String> remoteFiles) {
    }

    protected void onStreamClose(FTPClient ftpClient, List<File> localFiles, URI uri, List<String> remoteFiles) {
        this.markRemoteFilesDone(ftpClient, uri, remoteFiles);
        this.markLocalFilesDone(localFiles);
        this.deleteLocalFiles(localFiles);
    }

    @Override
    public StatusLine send(URI endpoint, String payload) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void send(URI endpoint, File payload) throws IOException, DataStreamException {
        Objects.requireNonNull(payload);
        if (!payload.exists()) {
            throw new DataStreamException("File " + payload.getAbsolutePath() + " does not exist");
        }
        if (payload.isDirectory()) {
            for (File sub : payload.listFiles()) {
                this.send(endpoint, sub);
            }
        }
        FTPClient ftpClient = this.performConnect(endpoint);
        try {
            ftpClient.changeDirectory(endpoint.getPath());
            ftpClient.upload(payload);
        } catch (FTPIllegalReplyException | FTPException | FTPAbortedException | FTPDataTransferException e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect(true);
            } catch (FTPIllegalReplyException | FTPException e) {
                log.error(e);
            }
        }
    }

    protected Set<String> getLocalFilenameList() throws IOException {
        Set<String> knownFiles = new HashSet<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.localCopyFolder);
        for (Path path : directoryStream) {
            if (Files.isRegularFile(path)) {
                knownFiles.add(path.getFileName().toString());
            }
        }
        return knownFiles;
    }

    protected InputStream buildChainedInputStream(List<File> files) throws IOException {
        List<File> filesToProcess = new ArrayList<>(files);
        filesToProcess.sort(Comparator.naturalOrder());

        List<Pair<String, InputStream>> streams = new Vector<>();
        for (File file : filesToProcess) {
            if (!file.getName().endsWith(DONE_FILE_ENDING)) {
                Path filePath = Paths.get(file.toURI());
                InputStream newInputStream = Files.newInputStream(filePath);
                streams.add(Pair.of(file.getName(), newInputStream));
            }
        }
        return new LabeledSequenceInputStream(streams);
    }


    protected abstract List<String> filterFilesToDownload(List<String> paths) throws IOException;

    private void deleteLocalFiles(List<File> localFiles) {
        if (!this.keepFiles) {
            for (File file : localFiles) {
                file.delete();
            }
        }
    }

    private void markLocalFilesDone(List<File> localFiles) {
        if (this.markLocalFiles) {
            for (File file : localFiles) {
                String filename = file.getName();
                if (!filename.endsWith(DONE_FILE_ENDING)) {
                    String doneFileName = filename + DONE_FILE_ENDING;
                    try {
                        Files.move(
                                Paths.get(file.toURI()),
                                Paths.get(file.getParent(), doneFileName)
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void markRemoteFilesDone(FTPClient ftpClient, URI folder, List<String> remoteFiles) {
        if (this.markRemoteFiles) {
            try {
                ftpClient.changeDirectory(folder.getPath());
                for (String file : remoteFiles) {
                    ftpClient.rename(file, file + DONE_FILE_ENDING);
                }
            } catch (IOException | FTPIllegalReplyException | FTPException e) {
                e.printStackTrace();
            }
        }
    }
}
