package dk.magenta.datafordeler.cpr.parsers;

import dk.magenta.datafordeler.cpr.records.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    /*public T parseLine(String line) {
        return this.parseLine(line.substring(0, 3), line);
    }*/

    public T parseLine(String line) {
        int colonIndex = line.indexOf(':');
        String origin = null;
        if (colonIndex != -1) {
            origin = line.substring(2, colonIndex);
            line = line.substring(colonIndex+1);
        }
        T record = this.parseLine(line.substring(0, 3), line);
        if (origin != null && record != null) {
            record.setOrigin(origin);
        }
        return record;
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
    public List<T> parse(LinkedHashMap<String, ArrayList<String>> lineMap, String encoding) {
        ArrayList<T> records = new ArrayList<>();

        this.log.info("Reading data");
        int batchSize = 0, batchCount = 0;

        for (String origin : lineMap.keySet()) {
            List<String> lines = lineMap.get(origin);

            for (String line : lines) {
                line = line.trim();
                if (line.length() > 3) {
                    try {
                        T record = this.parseLine(line);
                        if (record != null) {
                            record.setOrigin(origin);
                            records.add(record);
                        }
                    } catch (OutOfMemoryError e) {
                        log.error("OutOfMemoryError", e);
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
        }
        int count = records.size();
        this.log.info("Parse complete (" + count + " usable entries found)");
        return records;
    }

}
