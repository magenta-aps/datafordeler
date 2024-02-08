package dk.magenta.datafordeler.statistik.services;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.setup.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;
import dk.magenta.datafordeler.geo.GeoLookupService;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.statistik.utils.Filter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * This might be a naive implementation, but so far it does not look like it
 * It does not take bitemporality or noe to many relations on locality into account
 */
@RestController
@RequestMapping("/statistik/locality_data")
public class LocalityDataService extends StatisticsService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    private CsvMapper csvMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @PostConstruct
    public void init() {
        this.setWriteToLocalFile(true);
    }


    private final Logger log = LogManager.getLogger(GeoLookupService.class.getCanonicalName());

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public void get(HttpServletRequest request, HttpServletResponse response)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, IOException, HttpNotFoundException, MissingParameterException, InvalidCertificateException {
        log.info("Service called");
        super.handleRequest(request, response, ServiceName.LOCALITY);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/")
    public void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, IOException, MissingParameterException, InvalidClientInputException, HttpNotFoundException, InvalidCertificateException {
        super.handleRequest(request, response, ServiceName.LOCALITY);
    }

    @Override
    protected List<String> getColumnNames() {
        return Arrays.asList(MUNICIPALITY_CODE, MUNICIPALITY_SHORT_NAME, MUNICIPALITY_NAME, LOCALITY_CODE, LOCALITY_ABBREVIATION, LOCALITY_NAME,
                LOC_TYPE_CODE, LOC_TYPE_NAME, LOC_STATUS_CODE, LOC_STATUS_NAME, "Void");
    }

    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected CsvMapper getCsvMapper() {
        return this.csvMapper;
    }

    @Override
    protected DafoUserManager getDafoUserManager() {
        return this.dafoUserManager;
    }

    @Override
    protected Logger getLogger() {
        return this.log;
    }

    public int run(Filter filter, OutputStream outputStream, String reportUuid) {

        final Session primarySession = this.getSessionManager().getSessionFactory().openSession();
        final Session secondarySession = this.getSessionManager().getSessionFactory().openSession();

        try {

            primarySession.setDefaultReadOnly(true);
            secondarySession.setDefaultReadOnly(true);

            List<Map<String, String>> concatenation = new ArrayList<>();
            List<GeoLocalityEntity> localityEntities = QueryManager.getAllEntities(primarySession, GeoLocalityEntity.class);
            for (GeoLocalityEntity localityEntity : localityEntities) {

                //The testdataset indicates that we can expect to find one of each record
                Integer KomKod = localityEntity.getMunicipality().iterator().next().getCode();
                String KomKortNavn = municipalityIdToMapName(KomKod);
                String KomNavn = municipalityIdToName(KomKod);

                String localityCode = Optional.ofNullable(localityEntity.getMunicipality().iterator().next().getEntity().getCode()).orElse("");
                String LokKortNavn = Optional.ofNullable(localityEntity.getAbbreviation().iterator().next().getName()).orElse("");
                String localityName = Optional.ofNullable(localityEntity.getName().iterator().next().getName()).orElse("");

                Integer LokBetegn = Optional.ofNullable(localityEntity.getBetegnelse().iterator().next().getBetegn()).orElse(0);
                Integer LokStatus = Optional.ofNullable(localityEntity.getStatus().iterator().next().getStatus()).orElse(0);
                String LokTypeNavn = typeCodeToName(LokBetegn);
                String LokStatusNavn = typeCodeToStatusName(LokStatus);

                HashMap<String, String> csvRow = new HashMap<>();
                csvRow.put(MUNICIPALITY_CODE, Integer.toString(KomKod));
                csvRow.put(MUNICIPALITY_SHORT_NAME, KomKortNavn);
                csvRow.put(MUNICIPALITY_NAME, KomNavn);
                csvRow.put(LOCALITY_CODE, localityCode);
                csvRow.put(LOCALITY_ABBREVIATION, LokKortNavn);
                csvRow.put(LOCALITY_NAME, localityName);
                csvRow.put(LOC_TYPE_CODE, LokBetegn + "");
                csvRow.put(LOC_TYPE_NAME, LokTypeNavn);
                csvRow.put(LOC_STATUS_NAME, LokStatusNavn);
                concatenation.add(csvRow);
            }

            if (outputStream != null) {
                return this.writeItems(concatenation.iterator(), outputStream, item -> {

                });
            }

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            primarySession.close();
            secondarySession.close();
        }
        return 0;
    }

    private static String municipalityIdToName(Integer code) {
        switch (code) {
            case 955:
                return "Kujalleq";
            case 956:
                return "Sermersooq";
            case 957:
                return "Qeqqata";
            case 959:
                return "Qeqertalik";
            case 960:
                return "Avannaata";
            case 961:
                return "udenf.komm.ind.i.Grl";
            default:
                return "";
        }
    }


    private static String municipalityIdToMapName(Integer code) {
        switch (code) {
            case 955:
                return "KU";
            case 956:
                return "SE";
            case 957:
                return "QE";
            case 959:
                return "KQ";
            case 960:
                return "AK";
            case 961:
                return "SDI";
            default:
                return "";
        }
    }


    private static String typeCodeToName(Integer lokalitetBetegn) {

        switch (lokalitetBetegn) {
            case 0:
                return "Ukendt";
            case 1:
                return "Hovedstad";
            case 2:
                return "Hovedbosted";
            case 3:
                return "Større bosted";
            case 4:
                return "Bosted";
            case 5:
                return "Mindre bosted";
            case 6:
                return "Mindste bosted";
            default:
                return "Ukendt";
        }
    }

    private static String typeCodeToStatusCode(Integer typeCode) {
        switch (typeCode) {
            case 1:
                return "15";
            case 2:
                return "20";
            case 3:
                return "15";
            case 4:
                return "20";
            case 5:
                return "15";
            case 6:
                return "20";
            case 7:
                return "15";
            case 8:
                return "20";
            case 9:
                return "15";
            case 10:
                return "20";
            case 11:
                return "15";
            case 12:
                return "20";
            case 13:
                return "15";
            default:
                return "20";
        }
    }

    private static String typeCodeToStatusName(Integer typeCode) {
        switch (typeCode) {
            case 0:
                return "Inaktiv";
            case 1:
                return "Aktiv";
            default:
                return "Undefined";
        }
    }


    protected int writeItems(Iterator<Map<String, String>> items, OutputStream outputStream, Consumer<Object> afterEach) throws IOException {
        CsvSchema.Builder builder = new CsvSchema.Builder();
        builder.setColumnSeparator(';');

        CsvMapper mapper = this.getCsvMapper();
        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS, true);

        List<String> keys = this.getColumnNames();
        for (int i = 0; i < keys.size(); i++) {
            builder.addColumn(new CsvSchema.Column(
                    i, keys.get(i),
                    CsvSchema.ColumnType.STRING
            ));
        }
        CsvSchema schema = builder.build().withHeader();
        int written = 0;

        if (items.hasNext()) {
            ObjectWriter writerobj = mapper.writer(schema);

            SequenceWriter writer = writerobj.writeValues(outputStream);
            for (written = 0; items.hasNext(); written++) {
                Object item = items.next();
                if (item != null) {
                    writer.write(item);
                }
                afterEach.accept(item);
            }
            writer.close();
        }
        return written;
    }


    @Override
    protected Filter getFilter(HttpServletRequest request) {
        Filter filter = new Filter();

        InputStream inputStream = null;
        String fieldName = "file"; // Matches the file field in addressServiceForm.html
        try {
            Part filePart = request.getPart(fieldName);
            if (filePart != null) {
                inputStream = filePart.getInputStream();
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {

            ArrayList<String> pnrs = new ArrayList<>();
            Stream<String> stream = new BufferedReader(new InputStreamReader(inputStream)).lines();

            try {
                stream.forEach(pnrs::add);
            } finally {
                stream.close();
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("IOException", e);
                }
            }
            filter.onlyPnr = pnrs;
        }

        filter.effectAt = OffsetDateTime.now();
        return filter;
    }


    private static final Comparator bitemporalComparator = Comparator.comparing(PersonStatisticsService::getBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprNontemporalRecord::getDafoUpdated)
            .thenComparing(DatabaseEntry::getId);


    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestUnclosed(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null &&
                r.getOriginDate() != null).max(bitemporalComparator).orElse(null);
    }


}
