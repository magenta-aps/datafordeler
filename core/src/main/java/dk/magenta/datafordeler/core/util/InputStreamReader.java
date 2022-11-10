package dk.magenta.datafordeler.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public final class InputStreamReader {

    private static final String STREAM_DELIMITER = "\\A";

    public static String readInputStream(InputStream stream) {
        return readInputStream(stream, "UTF-8");
    }

    public static String readInputStream(InputStream stream, String charsetName) {
        String data = new Scanner(stream, charsetName).useDelimiter(STREAM_DELIMITER).next();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
