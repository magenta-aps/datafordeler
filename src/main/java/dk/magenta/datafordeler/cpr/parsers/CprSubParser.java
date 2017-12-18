package dk.magenta.datafordeler.cpr.parsers;

import dk.magenta.datafordeler.cpr.records.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser class for the CPR format, where each line is one record.
 */
public abstract class CprSubParser<T extends Record> {

    public CprSubParser() {
    }

    private Logger log = LogManager.getLogger(CprSubParser.class);

    public Logger getLog() {
        return this.log;
    }

    public T parseLine(String line) {
        return this.parseLine(line.substring(0, 3), line);
    }

    protected void logType(String recordType) {
        this.getLog().debug("Parsing record of type "+recordType);
    }

    public abstract T parseLine(String recordType, String line);

    // Maybe override in subclass?
    protected String getEncoding() {
        return null;
    }

    // TODO: output an objectInputStream
    public List<T> parse(List<String> lines, String encoding) {
        ArrayList<T> records = new ArrayList<>();

        this.log.info("Reading data");
        int batchSize = 0, batchCount = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.length() > 3) {
                try {
                    T record = this.parseLine(line);
                    if (record != null) {
                        records.add(record);
                    }
                } catch (OutOfMemoryError e) {
                    System.out.println(line);
                }
            }
            batchSize++;
            if (batchSize >= 100000) {
                batchCount++;
                System.gc();
                this.log.trace("    parsed " + (batchCount * batchSize) + " lines");
                batchSize = 0;
            }
        }
        int count = records.size();
        this.log.info("Parse complete (" + count + " usable entries found)");
        return records;
    }

}
