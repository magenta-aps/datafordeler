package dk.magenta.datafordeler.core.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ImportInputStream extends WrappedInputStream {

    public ImportInputStream(InputStream inner) {
        super(inner);
    }

    public ImportInputStream(InputStream inner, File cacheFile) {
        this(inner);
        this.cacheFiles.add(cacheFile);
        this.countLines();
    }

    public ImportInputStream(InputStream inner, List<File> cacheFiles) {
        this(inner);
        this.cacheFiles.addAll(cacheFiles);
        this.countLines();
    }


    private final ArrayList<File> cacheFiles = new ArrayList<>();

    public void addCacheFile(File cacheFile) {
        this.cacheFiles.add(cacheFile);
        this.countLines();
    }

    public ArrayList<File> getCacheFiles() {
        return this.cacheFiles;
    }


    private int lineCount = 0;

    public int getLineCount() {
        return this.lineCount;
    }

    private void countLines() {
        int count = 0;
        for (File file : this.cacheFiles) {
            try {
                count += countLines(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.lineCount = count;
    }

    private static int countLines(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; i++) {
                    if (c[i] == '\n') {
                        count++;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }
}
