package dk.magenta.datafordeler.prisme;

import java.util.HashMap;
import java.util.Map;

public class HardcodedCountrycodeForGERCompany {

    //http://www.experit.dk/ouali/web/Dst/Dokumentation/databeskrivelser/Alle%20lande.htm
    public static Map<Integer, String> countrycodes = new HashMap<Integer, String>();

    static {
        countrycodes.put(3, "NL");//Nederlandene
        countrycodes.put(6, "GB");//UK
        countrycodes.put(7, "IE");//Irland
        countrycodes.put(24, "IS");//Island
        countrycodes.put(28, "NO");//Norge
        countrycodes.put(30, "SE");//Sverige
        countrycodes.put(32, "FI");//Finland
        countrycodes.put(41, "FO");//Færøerne
        countrycodes.put(63, "SK");//Slovakiet
        countrycodes.put(75, "RU");//Rusland
        countrycodes.put(91, "SI");//Slovenien
        countrycodes.put(400, "US");//USA
        countrycodes.put(404, "CA");//Canada
        countrycodes.put(406, "GL");//Grønland
        countrycodes.put(442, "PA");//Panama
        countrycodes.put(600, "CY");//Cypern
        countrycodes.put(647, "AE");//For.Arab.Emirat
        countrycodes.put(664, "IN");//Indien
        countrycodes.put(706, "SG");//Singapore
        countrycodes.put(720, "CN");//Kina
        countrycodes.put(800, "AU");//Australien
    }
}
