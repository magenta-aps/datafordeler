package dk.magenta.datafordeler.ger.parser;

import dk.magenta.datafordeler.ger.data.RawData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 26-11-15.
 */
public abstract class SpreadsheetConverter {

    protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger log = LogManager.getLogger(SpreadsheetConverter.class.getCanonicalName());

    protected String[] getApplicableContentTypes() {
        return new String[0];
    }

    protected String[] getApplicableFileExtensions() {
        return new String[0];
    }

    public boolean applies(String contentType) {
        for (String type : this.getApplicableContentTypes()) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, List<RawData>> convert(InputStream data) throws Exception {
        return null;
    }

    public Map<String, List<RawData>> convert(File data) throws Exception {
        return null;
    }

    protected static boolean isRowdataEmpty(List<String> rowData) {
        if (rowData != null && !rowData.isEmpty()) {
            for (String s : rowData) {
                if (s != null && !s.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static HashMap<String, SpreadsheetConverter> mimetypeMap = null;
    private static HashMap<String, SpreadsheetConverter> extensionMap = null;

    private static void loadConverters() {
        ArrayList<SpreadsheetConverter> converterList = new ArrayList<SpreadsheetConverter>();
        converterList.add(new OdfConverter());
        converterList.add(new XlsConverter());
        converterList.add(new XlsxConverter());
        mimetypeMap = new HashMap<>();
        extensionMap = new HashMap<>();
        for (SpreadsheetConverter converter : converterList) {
            for (String contentType : converter.getApplicableContentTypes()) {
                mimetypeMap.put(contentType, converter);
            }
            for (String contentType : converter.getApplicableFileExtensions()) {
                extensionMap.put(contentType, converter);
            }
        }
    }

    public static SpreadsheetConverter getConverterByMimetype(String contentType) {
        if (mimetypeMap == null) {
            loadConverters();
        }
        if (!mimetypeMap.containsKey(contentType)) {
            log.error("Could not find converter for mimetype '" + contentType + "'");
        }
        return mimetypeMap.get(contentType);
    }

    public static SpreadsheetConverter getConverterByExtension(String extension) {
        if (extensionMap == null) {
            loadConverters();
        }
        if (!extensionMap.containsKey(extension)) {
            log.error("Could not find converter for extension '" + extension + "'");
        }
        return extensionMap.get(extension);
    }

    public static Map<String, List<RawData>> getSpreadsheetConversion(InputStream data, String contentType) throws Exception {
        return getConverterByMimetype(contentType).convert(data);
    }

    public static Map<String, List<RawData>> getSpreadsheetConversion(File data, String contentType) throws Exception {
        return getConverterByMimetype(contentType).convert(data);
    }

}
