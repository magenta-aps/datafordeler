package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.WrongSubclassException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.*;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.CprEntityManager;
import dk.magenta.datafordeler.cpr.synchronization.CprSourceData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Component
public class CprRegisterManager extends RegisterManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CprConfigurationManager configurationManager;

    @Autowired
    private CprPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger("CprRegisterManager");

    @Value("${dafo.cpr.proxy-url:}")
    private String proxyString;

    @Value("${dafo.cpr.local-copy-folder:}")
    private String localCopyFolder;


    public CprRegisterManager() {

    }

    /**
    * RegisterManager initialization; set up configuration and source fetcher.
    * We store fetched data in a local cache, so create a random folder for that.
    */
    @PostConstruct
    public void init() throws IOException {
        if (this.localCopyFolder == null || this.localCopyFolder.isEmpty()) {
            File temp = File.createTempFile("datafordeler-cache","");
            temp.delete();
            temp.mkdir();
            this.localCopyFolder = temp.getAbsolutePath();
        }
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    public URI getBaseEndpoint() {
        return null;
    }



    @Override
    protected Communicator getEventFetcher() {
        return null;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public URI getEventInterface(EntityManager entityManager) throws DataFordelerException {
        if (entityManager instanceof CprEntityManager) {
            CprConfiguration configuration = this.configurationManager.getConfiguration();
            return configuration.getRegisterURI((CprEntityManager) entityManager);
        }
        return null;
    }

    @Override
    protected Communicator getChecksumFetcher() {
        return null;
    }

    @Override
    public URI getListChecksumInterface(String schema, OffsetDateTime from) {
        return null;
    }

    @Override
    public boolean pullsEventsCommonly() {
        return false;
    }

    public String getPullCronSchedule() {
        // TODO: make entitymanager specific
        return this.configurationManager.getConfiguration().getPersonRegisterPullCronSchedule();
    }

    public FtpCommunicator getFtpCommunicator(URI eventInterface, CprEntityManager cprEntityManager) throws DataStreamException {
        CprConfiguration configuration = this.configurationManager.getConfiguration();
        return new FtpCommunicator(
                configuration.getRegisterFtpUsername(cprEntityManager),
                configuration.getRegisterFtpPassword(cprEntityManager),
                eventInterface != null && "ftps".equals(eventInterface.getScheme()),
                this.proxyString,
                this.localCopyFolder,
                true,
                false,
                false
        );
    }

    public void setProxyString(String proxyString) {
        this.proxyString = proxyString;
    }

    /**
    * Pull data from the data source denoted by eventInterface, using the 
    * mechanism appropriate for the source.
    * For CPR, this is done using a LocalCopyFtpCommunicator, where we fetch all 
    * files in a remote folder (known to be text files).
    * We then package chunks of lines into Events, and feed them into a stream for 
    * returning.
    */
    @Override
    public ImportInputStream pullRawData(URI eventInterface, EntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {
        if (!(entityManager instanceof CprEntityManager)) {
            throw new WrongSubclassException(CprEntityManager.class, entityManager);
        }
        if (eventInterface == null) {
            this.log.info("Not pulling for "+entityManager.toString());
            return null;
        }
        this.log.info("Pulling from "+eventInterface.toString() + " for entitymanager "+entityManager);
        CprEntityManager cprEntityManager = (CprEntityManager) entityManager;
        ImportInputStream responseBody = null;
        String scheme = eventInterface.getScheme();
        this.log.info("scheme: "+scheme);
        this.log.info("eventInterface: "+eventInterface);
        switch (scheme) {
            case "file":
                try {
                    File file = new File(eventInterface);
                    responseBody = new ImportInputStream(new FileInputStream(file));
                    responseBody.addCacheFile(file);
                } catch (FileNotFoundException e) {
                    this.log.error(e);
                    throw new DataStreamException(e);
                }
                break;

            case "ftp":
            case "ftps":
                try {
                    FtpCommunicator ftpFetcher = this.getFtpCommunicator(eventInterface, cprEntityManager);
                    responseBody = ftpFetcher.fetch(eventInterface);
                } catch (DataStreamException e) {
                    this.log.error(e);
                    throw e;
                }
                break;
        }
        if (responseBody == null) {
            throw new DataStreamException("No data received from source " + eventInterface.toString());
        }

        return responseBody;
    }

    @Override
    protected ItemInputStream<? extends PluginSourceData> parseEventResponse(InputStream rawData, EntityManager entityManager) throws DataFordelerException {
        if (!(entityManager instanceof CprEntityManager)) {
            throw new WrongSubclassException(CprEntityManager.class, entityManager);
        }

        final int linesPerEvent = 1000;
        PipedInputStream inputStream = new PipedInputStream();

        try {
            final PipedOutputStream outputStream = new PipedOutputStream(inputStream);
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            final int dataIdBase = rawData.hashCode();
            final String schema = entityManager.getSchema();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader responseReader = new BufferedReader(new InputStreamReader(rawData, Charset.forName("iso-8859-1")));
                    int eventCount = 0;
                    long totalLines = 0;
                    try {
                        String line;
                        int lineCount = 0;
                        ArrayList<String> lines = new ArrayList<>();
                        while ((line = responseReader.readLine()) != null) {
                            lines.add(line);
                            lineCount++;
                            if (lineCount >= linesPerEvent) {
                                objectOutputStream.writeObject(CprRegisterManager.this.wrap(lines, schema, dataIdBase, eventCount));
                                lines.clear();
                                lineCount = 0;
                                eventCount++;
                                totalLines++;
                            }
                        }
                        if (lineCount > 0) {
                            objectOutputStream.writeObject(CprRegisterManager.this.wrap(lines, schema, dataIdBase, eventCount));
                            eventCount++;
                        }
                        CprRegisterManager.this.log.info("Packed "+eventCount+" data objects with " + totalLines + " lines");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        responseReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        objectOutputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            t.start();

            return new ItemInputStream<>(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private CprSourceData wrap(List<String> lines, String schema, int base, int index) {
        StringJoiner s = new StringJoiner("\n");
        for (String line : lines) {
            s.add(line);
        }
        return new CprSourceData(schema, s.toString(), base + ":" + index);
    }

}
