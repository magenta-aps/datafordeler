package dk.magenta.datafordeler.cpr.synchronization;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.plugin.FtpCommunicator;
import dk.magenta.datafordeler.core.util.CloseDetectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.net.ftp.FTPClient;

/**
 * Created by jubk on 05-07-2017.
 */
public class LocalCopyFtpCommunicator extends FtpCommunicator {

  private Path localCopyFolder;

  public LocalCopyFtpCommunicator(String username, String password, boolean useFtps,
      String proxyString, String localCopyFolder) throws IOException {
    super(username, password, useFtps, proxyString);
    this.localCopyFolder = Paths.get(localCopyFolder);
    if (!Files.isDirectory(this.localCopyFolder)) {
      throw new IOException("Local copy folder for FTP download " +
          localCopyFolder + " is not a directory");
    }
  }

  @Override
  public InputStream fetch(URI uri) throws HttpStatusException, DataStreamException {
    System.out.println("step 1");
    System.out.println(uri);

    try {
      FTPClient ftpClient = this.performConnect(uri);

      System.out.println("step 2");
      List<String> remotePaths = Arrays.asList(ftpClient.listNames(uri.getPath()));
      remotePaths.sort(Comparator.naturalOrder());
      List<String> downloadPaths = this.filterFilesToDownload(remotePaths);
      System.out.println("step 3");

      for (String path : downloadPaths) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        Path outputFile = Files.createFile(Paths.get(localCopyFolder.toString(), fileName));
        OutputStream outputStream = Files.newOutputStream(outputFile);
        ftpClient.retrieveFile(path, outputStream);
        // ftpClient.completePendingCommand();
        outputStream.close();
      }
      System.out.println("step 4");
      ftpClient.disconnect();
      System.out.println("step 5");

      InputStream inputStream = this.buildChainedInputStream();
      System.out.println("step 6");

      if (inputStream != null) {
        CloseDetectInputStream inputCloser = new CloseDetectInputStream(inputStream);
        inputCloser.addAfterCloseListener(new Runnable() {
          @Override
          public void run() {
            try {
              markLocalFilesAsDone();
              ftpClient.disconnect();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
        inputStream = inputCloser;
      } else {
        inputStream = new ByteArrayInputStream("".getBytes());
      }

      return inputStream;

    } catch (IOException e) {
      System.out.println("whoa, fail!");
      e.printStackTrace();
      throw new DataStreamException(e);
    }
  }

  private void markLocalFilesAsDone() throws IOException {
    for(String fileName : this.getLocalFilenameList()) {
      if(!fileName.endsWith(DONE_FILE_ENDING)) {
        String doneFileName = fileName + DONE_FILE_ENDING;
        Files.move(
            Paths.get(localCopyFolder.toString(), fileName),
            Paths.get(localCopyFolder.toString(), doneFileName)
        );
      }
    }
  }

  protected Set<String> getLocalFilenameList() throws IOException {
    Set<String> knownFiles = new HashSet<>();
    DirectoryStream<Path> directoryStream = Files.newDirectoryStream(localCopyFolder);
    for(Path path : directoryStream) {
      if(Files.isRegularFile(path)) {
        knownFiles.add(path.getFileName().toString());
      }
    }
    return knownFiles;
  }

  protected InputStream buildChainedInputStream() throws IOException {
    List<String> fileNamesToProcess = new ArrayList<>(getLocalFilenameList());
    fileNamesToProcess.sort(Comparator.naturalOrder());

    InputStream inputStream = null;

    for(String fileName : fileNamesToProcess) {
      if(!fileName.endsWith(DONE_FILE_ENDING)) {
        Path filePath = Paths.get(localCopyFolder.toString(), fileName);
        InputStream newInputStream = Files.newInputStream(filePath);
        if(inputStream == null) {
          inputStream = newInputStream;
        } else {
          inputStream = new SequenceInputStream(inputStream, newInputStream);
        }
      }
    }

    return inputStream;
  }

  protected List<String> filterFilesToDownload(List<String> paths) throws IOException {
    Set<String> knownFiles = getLocalFilenameList();
    List<String> result = new ArrayList<>();
    for(String path : paths) {
      String fileName = path.substring(path.lastIndexOf('/') + 1);
      if(!knownFiles.contains(fileName) && !knownFiles.contains(fileName + DONE_FILE_ENDING)) {
        result.add(path);
      }
    }
    return result;
  }



}