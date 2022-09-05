package dk.magenta.datafordeler.geo.data;

import java.util.HashMap;
import java.util.Map;

public class GeoHardcode {

    private static Map<Integer, Map<Integer, HardcodedAdressStructure>> munipialicities = new HashMap<Integer, Map<Integer, HardcodedAdressStructure>>();

    static {
        munipialicities.put(961, new HashMap<Integer, HardcodedAdressStructure>());
        munipialicities.get(961).put(4030, new HardcodedAdressStructure("Thule Air Base",3970,"Pituffik","2070"));
        munipialicities.get(961).put(9901, new HardcodedAdressStructure("Grønland Hjemmestyre",3900,"Skattestyrelsen","2000"));
        munipialicities.get(961).put(9999, new HardcodedAdressStructure("Grønland Hjemmestyre",3900,"Skattestyrelsen","2000"));

        munipialicities.put(957, new HashMap<Integer, HardcodedAdressStructure>());
        munipialicities.get(957).put(9901, new HardcodedAdressStructure("Folkeregister 1",3911,"Sisimiut","800"));
        munipialicities.get(957).put(9903, new HardcodedAdressStructure("Folkeregister 3",3911,"Sisimiut","800"));
        munipialicities.get(957).put(9902, new HardcodedAdressStructure("Folkeregister 2",3911,"Sisimiut","800"));
        munipialicities.get(957).put(9904, new HardcodedAdressStructure("Folkeregister 4",3911,"Sisimiut","800"));
        munipialicities.get(957).put(9905, new HardcodedAdressStructure("Folkeregister 5",3911,"Sisimiut","800"));
        munipialicities.get(957).put(9908, new HardcodedAdressStructure("Uden Fast Bopæl",3911,"Sisimiut","800"));
        munipialicities.get(957).put(9909, new HardcodedAdressStructure("Ukendt Adresse",3911,"Sisimiut","800"));
        munipialicities.get(957).put(9912, new HardcodedAdressStructure("Folkeregister 2",3912,"Maniitsoq","700"));
        munipialicities.get(957).put(9913, new HardcodedAdressStructure("Folkeregister 3",3912,"Maniitsoq","700"));
        munipialicities.get(957).put(9915, new HardcodedAdressStructure("Folkeregister 5",3912,"Maniitsoq","700"));
        munipialicities.get(957).put(9918, new HardcodedAdressStructure("Uden Fast Bopæl 3912",3912,"Maniitsoq","700"));
        munipialicities.get(957).put(9919, new HardcodedAdressStructure("Ukendt Adresse 3912",3912,"Maniitsoq","700"));
        munipialicities.get(957).put(9929, new HardcodedAdressStructure("Ukendt Adresse",3910,"Kangerlussuaq","820"));
        munipialicities.get(957).put(9998, new HardcodedAdressStructure("Uden Vejnavn",3912,"Maniitsoq","700"));

        munipialicities.put(956, new HashMap<Integer, HardcodedAdressStructure>());
        munipialicities.get(956).put(9997, new HardcodedAdressStructure("Internordiske Tilfl",3900,"Nuuk","600"));
        munipialicities.get(956).put(9998, new HardcodedAdressStructure("Færinger På Trawler",3900,"Nuuk","600"));
        munipialicities.get(956).put(9900, new HardcodedAdressStructure("Administrativ",3900,"Nuuk","600"));//Efter aftale med Arne Sermersooq
        munipialicities.get(956).put(9901, new HardcodedAdressStructure("Administrativ",3900,"Nuuk","600"));//Tilføjes yderligere administrative adresser
        munipialicities.get(956).put(9902, new HardcodedAdressStructure("Administrativ",3900,"Nuuk","600"));
        munipialicities.get(956).put(9903, new HardcodedAdressStructure("Administrativ",3900,"Nuuk","600"));
        munipialicities.get(956).put(9984, new HardcodedAdressStructure("Administrativ",3900,"Nuuk","600"));

        munipialicities.put(958, new HashMap<Integer, HardcodedAdressStructure>());
        munipialicities.get(958).put(9901, new HardcodedAdressStructure("Skattesty,Qaasuitsup",3952,"Ilulissat","1200"));
    }

    public static HardcodedAdressStructure getHardcodedRoadname(int municipality, int roadcode) {
        if(munipialicities.get(municipality)==null || munipialicities.get(municipality).get(roadcode)==null) {
            return null;
        }
        return munipialicities.get(municipality).get(roadcode);
    }


    public static class HardcodedAdressStructure {
        private String vejnavn = "";
        private Integer postcode = 0;
        private String cityname = "";
        private String locationcode = "";

        public HardcodedAdressStructure(String vejnavn, Integer postcode, String cityname, String locationcode) {
            this.vejnavn = vejnavn;
            this.postcode = postcode;
            this.cityname = cityname;
            this.locationcode = locationcode;
        }

        public String getVejnavn() {
            return vejnavn;
        }

        public void setVejnavn(String vejnavn) {
            this.vejnavn = vejnavn;
        }

        public Integer getPostcode() {
            return postcode;
        }

        public void setPostcode(Integer postcode) {
            this.postcode = postcode;
        }

        public String getCityname() {
            return cityname;
        }

        public void setCityname(String cityname) {
            this.cityname = cityname;
        }

        public String getLocationcode() {
            return locationcode;
        }

        public void setLocationcode(String locationcode) {
            this.locationcode = locationcode;
        }
    }
}
