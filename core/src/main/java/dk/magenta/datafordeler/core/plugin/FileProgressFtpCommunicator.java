package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileProgressFtpCommunicator extends FtpCommunicator {

    public FileProgressFtpCommunicator(String username, String password, boolean useFtps) throws DataStreamException {
        super(username, password, useFtps);
    }

    public FileProgressFtpCommunicator(String username, String password, boolean useFtps, String proxyString, String localCopyFolder, boolean keepFiles, boolean markLocalFiles, boolean markRemoteFiles) throws DataStreamException {
        super(username, password, useFtps, proxyString, localCopyFolder, keepFiles, markLocalFiles, markRemoteFiles);
    }

    @Override
    protected List<String> filterFilesToDownload(List<String> paths) throws IOException {
        Set<String> knownFiles = getLocalFilenameList();
        List<String> result = new ArrayList<>();
        for (String path : paths) {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (!knownFiles.contains(fileName) && !knownFiles.contains(fileName + DONE_FILE_ENDING)) {
                result.add(path);
            }
        }
        return result;
    }
}
