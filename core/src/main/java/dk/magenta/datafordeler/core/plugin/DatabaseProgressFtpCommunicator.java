package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import it.sauronsoftware.ftp4j.FTPClient;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseProgressFtpCommunicator extends FtpCommunicator {

    private Session session;
    private String type;

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
    public DatabaseProgressFtpCommunicator(Session session, String type, String username, String password, boolean useFtps,
                                           String proxyString, String localCopyFolder, boolean keepFiles, boolean markLocalFiles, boolean markRemoteFiles) throws DataStreamException {
        super(username, password, useFtps, proxyString, localCopyFolder, keepFiles, markLocalFiles, markRemoteFiles);
        this.session = session;
        this.type = type;
    }

    @Override
    protected List<String> filterFilesToDownload(List<String> paths) throws IOException {
        System.out.println("Filtering files to download");
        System.out.println("files:");
        for (String path : paths) {
            System.out.println("\t"+path);
        }
        HashSet<String> found = new HashSet<>(existing(this.session, this.type, paths));
        HashSet<String> newSet = new HashSet<>(paths);
        System.out.println("Already registered files:");
        for (String path : found) {
            System.out.println("\t"+path);
        }
        newSet.removeAll(found);
        System.out.println("Previously unseen:");
        for (String path : newSet) {
            System.out.println("\t"+path);
        }
        if (newSet.size() > 5) {
            System.out.println("Something is wrong. Not downloading");
            newSet.clear();
        }
        return new ArrayList<>(newSet);
    }

    @Override
    protected void onStreamClose(FTPClient ftpClient, List<File> localFiles, URI uri, List<String> remoteFiles) {
        this.saveList(localFiles.stream().map(File::getName).collect(Collectors.toList()));
    }

    private void saveList(List<String> filenames) {
        Transaction transaction = this.session.beginTransaction();
        try {
            for (String filename : filter(this.session, this.type, filenames)) {
                this.session.save(new FtpPulledFile(this.type, filename));
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    private static Collection<String> existing(Session session, String type, List<String> filenames) {
        return session.createQuery(
                "select item."+FtpPulledFile.DB_FIELD_FILENAME+" from "+ FtpPulledFile.class.getCanonicalName()+" item where "+FtpPulledFile.DB_FIELD_TYPE+" = :type and "+FtpPulledFile.DB_FIELD_FILENAME+" in :list",
                        String.class
                )
                .setParameter("list", filenames)
                .setParameter("type", type)
                .list();
    }

    private static Collection<String> filter(Session session, String type, List<String> newFilenames) {
        HashSet<String> found = new HashSet<>(existing(session, type, newFilenames));
        HashSet<String> newSet = new HashSet<>(newFilenames);
        newSet.removeAll(found);
        return newSet;
    }

    public void insertFromFolder() {
        System.out.println("Inserting names of fetched files into database");
        if (this.getLocalCopyFolder() != null) {
            File folder = this.getLocalCopyFolder().toFile();
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    System.out.println("Files to register ("+files.length+"):");
                    for (File file : files) {
                        System.out.println("\t"+file.getName());
                    }
                    this.saveList(Arrays.stream(files).map(File::getName).collect(Collectors.toList()));
                }
            } else {
                System.out.println("Folder " + folder.getName() + " does not exist");
            }
        } else {
            System.out.println("Local copy folder is null");
        }
    }

}
