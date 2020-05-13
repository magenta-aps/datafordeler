package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.cpr.records.road.RoadRecordQuery;
import dk.magenta.datafordeler.cpr.records.road.data.RoadNameBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadPostalcodeBitemporalRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CprLookupService {

    private Logger log = LogManager.getLogger("dk.magenta.datafordeler.geo.CprLookupService");

    private static Pattern houseNumberPattern = Pattern.compile("(\\d+)(.*)");

    private static HashMap<Integer, String> municipalityCacheDK = new HashMap<>();

    /**
     * Hardcoding the mapping between kommune -name and -code
     */
    static {
        municipalityCacheDK.put(101,"Københavns");
        municipalityCacheDK.put(147,"Frederiksberg");
        municipalityCacheDK.put(151,"Ballerup");
        municipalityCacheDK.put(153,"Brøndby");
        municipalityCacheDK.put(155,"Dragør");
        municipalityCacheDK.put(0157,"Gentofte");
        municipalityCacheDK.put(159,"Gladsaxe");
        municipalityCacheDK.put(161,"Glostrup");
        municipalityCacheDK.put(163,"Herlev");
        municipalityCacheDK.put(165,"Albertslund");
        municipalityCacheDK.put(167,"Hvidovre");
        municipalityCacheDK.put(169,"Høje Taastrup");
        municipalityCacheDK.put(173,"Lyngby-Taarbæk");
        municipalityCacheDK.put(175,"Rødovre");
        municipalityCacheDK.put(183,"Ishøj");
        municipalityCacheDK.put(185,"Tårnby");
        municipalityCacheDK.put(187,"Vallensbæk");
        municipalityCacheDK.put(190,"Furesø");
        municipalityCacheDK.put(201,"Allerød");
        municipalityCacheDK.put(210,"Fredensborg");
        municipalityCacheDK.put(217,"Helsingør");
        municipalityCacheDK.put(219,"Hillerød");
        municipalityCacheDK.put(223,"Hørsholm");
        municipalityCacheDK.put(230,"Rudersdal");
        municipalityCacheDK.put(240,"Egedal");
        municipalityCacheDK.put(250,"Frederikssund");
        municipalityCacheDK.put(253,"Greve");
        municipalityCacheDK.put(259,"Køge");
        municipalityCacheDK.put(260,"Halsnæs");
        municipalityCacheDK.put(265,"Roskilde");
        municipalityCacheDK.put(269,"Solrød");
        municipalityCacheDK.put(270,"Gribskov");
        municipalityCacheDK.put(306,"Odsherred");
        municipalityCacheDK.put(316,"Holbæk");
        municipalityCacheDK.put(320,"Faxe");
        municipalityCacheDK.put(326,"Kalundborg");
        municipalityCacheDK.put(329,"Ringsted");
        municipalityCacheDK.put(330,"Slagelse");
        municipalityCacheDK.put(336,"Stevns");
        municipalityCacheDK.put(340,"Sorø");
        municipalityCacheDK.put(350,"Lejre");
        municipalityCacheDK.put(360,"Lolland");
        municipalityCacheDK.put(370,"Næstved");
        municipalityCacheDK.put(376,"Guldborgsund");
        municipalityCacheDK.put(390,"Vordingborg");
        municipalityCacheDK.put(400,"Bornholms Regionskommune");
        municipalityCacheDK.put(410,"Middelfart");
        municipalityCacheDK.put(420,"Assens");
        municipalityCacheDK.put(430,"Faaborg-Midtfyn");
        municipalityCacheDK.put(440,"Kerteminde");
        municipalityCacheDK.put(450,"Nyborg");
        municipalityCacheDK.put(461,"Odense");
        municipalityCacheDK.put(479,"Svendborg");
        municipalityCacheDK.put(480,"Nordfyns");
        municipalityCacheDK.put(482,"Langeland");
        municipalityCacheDK.put(492,"Ærø");
        municipalityCacheDK.put(510,"Haderslev");
        municipalityCacheDK.put(530,"Billund");
        municipalityCacheDK.put(540,"Sønderborg");
        municipalityCacheDK.put(550,"Tønder");
        municipalityCacheDK.put(561,"Esbjerg");
        municipalityCacheDK.put(563,"Fanø");
        municipalityCacheDK.put(573,"Varde");
        municipalityCacheDK.put(575,"Vejen");
        municipalityCacheDK.put(580,"Aabenraa");
        municipalityCacheDK.put(607,"Fredericia");
        municipalityCacheDK.put(615,"Horsens");
        municipalityCacheDK.put(621,"Kolding");
        municipalityCacheDK.put(630,"Vejle");
        municipalityCacheDK.put(657,"Herning");
        municipalityCacheDK.put(661,"Holstebro");
        municipalityCacheDK.put(665,"Lemvig");
        municipalityCacheDK.put(671,"Struer");
        municipalityCacheDK.put(706,"Syddjurs");
        municipalityCacheDK.put(707,"Norddjurs");
        municipalityCacheDK.put(710,"Favrskov");
        municipalityCacheDK.put(727,"Odder");
        municipalityCacheDK.put(730,"Randers");
        municipalityCacheDK.put(740,"Silkeborg");
        municipalityCacheDK.put(741,"Samsø");
        municipalityCacheDK.put(746,"Skanderborg");
        municipalityCacheDK.put(751,"Aarhus");
        municipalityCacheDK.put(756,"Ikast-Brande");
        municipalityCacheDK.put(760,"Ringkøbing-Skjern");
        municipalityCacheDK.put(766,"Hedensted");
        municipalityCacheDK.put(773,"Morsø");
        municipalityCacheDK.put(779,"Skive");
        municipalityCacheDK.put(787,"Thisted");
        municipalityCacheDK.put(791,"Viborg");
        municipalityCacheDK.put(810,"Brønderslev");
        municipalityCacheDK.put(813,"Frederikshavn");
        municipalityCacheDK.put(820,"Vesthimmerlands");
        municipalityCacheDK.put(825,"Læsø");
        municipalityCacheDK.put(840,"Rebild");
        municipalityCacheDK.put(846,"Mariagerfjord");
        municipalityCacheDK.put(849,"Jammerbugt");
        municipalityCacheDK.put(851,"Aalborg");
        municipalityCacheDK.put(860,"Hjørring");
    }

    protected SessionManager sessionManager;

    public CprLookupService(SessionManager sessionManager) {
        this.sessionManager =sessionManager;
    }

    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    public synchronized CprLookupDTO doLookup(int municipalityCode, int roadCode) {
        return this.doLookup(municipalityCode, roadCode, null);
    }

    /**
     *
     * @param municipalityCode
     * @param roadCode
     * @param houseNumber
     * @return
     */
    public synchronized CprLookupDTO doLookup(int municipalityCode, int roadCode, String houseNumber) {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            CprLookupDTO cprLookupDTO = new CprLookupDTO();
            cprLookupDTO.setMunicipalityName(municipalityCacheDK.get(municipalityCode));
            this.populateRoadDK(cprLookupDTO, session, municipalityCode, roadCode, houseNumber);

            return cprLookupDTO;
        }
    }

    private void populateRoadDK(CprLookupDTO lookup, Session session, int municipalityCode, int roadCode, String houseNumber) {
        dk.magenta.datafordeler.cpr.records.road.data.RoadEntity roadEntity = this.getRoadDK(session, municipalityCode, roadCode);
        if (roadEntity != null) {
            OffsetDateTime now = OffsetDateTime.now();
            RoadNameBitemporalRecord nameRecord = CprRecordFilter.filterRecordsByRegistrationAndEffectReturnNewest(roadEntity.getName(), now);
            lookup.setRoadName(nameRecord.getRoadName());

            RoadPostalcodeBitemporalRecord postCode = this.getRoadPostalCodeDK(roadEntity, houseNumber);
            if (postCode != null) {
                lookup.setPostalCode(postCode.getPostalCode());
                lookup.setPostalDistrict(postCode.getPostalDistrict());
            }
        }
    }


    private dk.magenta.datafordeler.cpr.records.road.data.RoadEntity getRoadDK(Session session, int municipalityCode, int roadCode) {
        try {
            RoadRecordQuery roadQuery = new RoadRecordQuery();
            setQueryNow(roadQuery);
            roadQuery.setVejkode(roadCode);
            roadQuery.addKommunekode(municipalityCode);
            List<dk.magenta.datafordeler.cpr.records.road.data.RoadEntity> roadEntities = QueryManager.getAllEntities(session, roadQuery, dk.magenta.datafordeler.cpr.records.road.data.RoadEntity.class);
            if(roadEntities.size()>0) {
                return roadEntities.get(0);
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            log.error("Failure parsing CPR adress-record", e);
        }
        return null;
    }

    /**
     * Some roads in Denmark is goes through more than one kommune
     * @param roadEntity
     * @param houseNumber
     * @return
     */
    private RoadPostalcodeBitemporalRecord getRoadPostalCodeDK(dk.magenta.datafordeler.cpr.records.road.data.RoadEntity roadEntity, String houseNumber) {
        OffsetDateTime now = OffsetDateTime.now();
        List<RoadPostalcodeBitemporalRecord> postalCodeRecords = CprRecordFilter.filterRecordsByRegistrationAndEffect(roadEntity.getPostcode(), now);
        for (RoadPostalcodeBitemporalRecord postcodeRecord : postalCodeRecords) {
            if (houseNumber != null) {//All this is probably ok, I copied it from working functionality after refactoring gladdreg out of the project
                Matcher m = houseNumberPattern.matcher(houseNumber);
                if (m.find()) {
                    int numberPart = Integer.parseInt(m.group(1));
                    String letterPart = m.group(2).toLowerCase();
                    Matcher from = houseNumberPattern.matcher(postcodeRecord.getFromHousenumber());
                    Matcher to = houseNumberPattern.matcher(postcodeRecord.getToHousenumber());
                    if (from.find() && to.find()) {
                        int fromNumber = Integer.parseInt(from.group(1));
                        int toNumber = Integer.parseInt(to.group(1));
                        if (fromNumber < numberPart && numberPart < toNumber) {
                            return postcodeRecord;
                        } else if (fromNumber == numberPart || numberPart == toNumber) {
                            String fromLetter = from.group(2).toLowerCase();
                            String toLetter = from.group(2).toLowerCase();
                            if ((fromNumber < numberPart || fromLetter.isEmpty() || fromLetter.compareTo(letterPart) <= 0) && (numberPart < toNumber || toLetter.isEmpty() || letterPart.compareTo(toLetter) < 0)) {
                                return postcodeRecord;
                            }
                        }
                    }
                } else {
                    return postcodeRecord;
                }
            }
        }
        return null;
    }

    private static void setQueryNow(BaseQuery query) {
        OffsetDateTime now = OffsetDateTime.now();
        query.setRegistrationFrom(now);
        query.setRegistrationTo(now);
        query.setEffectFrom(now);
        query.setEffectTo(now);
    }
}
