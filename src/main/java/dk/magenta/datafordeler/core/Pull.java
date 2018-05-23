package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.InterruptedPull;
import dk.magenta.datafordeler.core.database.InterruptedPullFile;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.ImportInterruptedException;
import dk.magenta.datafordeler.core.exception.SimilarJobRunningException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.core.util.MonitorLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDataMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;

/**
 * A Runnable that performs a pull with a given RegisterManager.
 * As a Worker subclass, it is started by the run() method and halted with the end() method.
 * If halted with end(), it signals the called-upon EntityManager to cleanly cease,
 * and report back a point that can be used for resuming.
 */
public class Pull extends Worker implements Runnable {

    /**
     * Helper class to construct a Pull from data embedded in a JobDataMap,
     * which is used to set up jobs running on a CRON
     */
    public static class Task extends AbstractTask<Pull> {
        public static final String DATA_ENGINE = "engine";
        public static final String DATA_REGISTERMANAGER = "registerManager";

        @Override
        protected Pull createWorker(JobDataMap dataMap) {
            Engine engine = (Engine) dataMap.get(DATA_ENGINE);
            RegisterManager registerManager = (RegisterManager) dataMap.get(DATA_REGISTERMANAGER);
            return new Pull(engine, registerManager);
        }
    }

    private Logger log = LogManager.getLogger(Pull.class);

    private RegisterManager registerManager;
    private Engine engine;
    private ObjectNode importConfiguration;

    public Pull(Engine engine, RegisterManager registerManager) {
        this(engine, registerManager, null);
    }

    public Pull(Engine engine, RegisterManager registerManager, ObjectNode importConfiguration) {
        this.engine = engine;
        this.registerManager = registerManager;
        this.importConfiguration = importConfiguration;
    }

    public Pull(Engine engine, Plugin plugin) {
        this(engine, plugin.getRegisterManager());
    }

    public Pull(Engine engine, Plugin plugin, ObjectNode importConfiguration) {
        this(engine, plugin.getRegisterManager(), importConfiguration);
    }

    private ImportMetadata importMetadata = null;

    /**
     * Fetches data from the remote register, at first checking whether a previous Pull for the plugin was interrupted,
     * and in that case resuming it, before fetching for each EntityManager is succession.
     * How the data is fetched, parsed and inserted in the database is ultimately up to each plugin. This method just oversees the workflow by:
     * * Looping over EntityManagers in the plugin, and for each:
     * * Calling on the EntityManager to fetch data as an inputstream
     * * Calling on the EntityManager to parse the stream and save the result to the database
     * * In case of an orderly interruption (such as an aborted command), store how far in the process went. It is up to the EntityManager to measure and report these figures.
     * */
    @Override
    public void run() {
        try {
            if (runningPulls.keySet().contains(this.registerManager)) {
                throw new SimilarJobRunningException("Another pull job is already running for RegisterManager " + this.registerManager.getClass().getCanonicalName() + " (" + this.registerManager.hashCode() + ")");
            }

            this.log.info("Worker " + this.getId() + " adding lock for " + this.registerManager.getClass().getCanonicalName() + " (" + this.registerManager.hashCode() + ")");
            runningPulls.put(this.registerManager, this);

            this.log.info("Worker " + this.getId() + " fetching events with " + this.registerManager.getClass().getCanonicalName());

            // See if there's a prior pull that was interrupted, and resume it.
            InterruptedPull interruptedPull = this.getLastInterrupt();
            if (interruptedPull != null) {
                this.log.info("A prior pull (started at "+interruptedPull.getStartTime()+") was interrupted at "+interruptedPull.getInterruptTime()+". Resuming.");
                EntityManager entityManager = this.registerManager.getEntityManager(interruptedPull.getSchemaName());
                if (entityManager == null) {
                    this.log.error("Unknown schema: "+interruptedPull.getSchemaName()+". Cannot resume");
                } else {
                    this.log.error("Schema: "+interruptedPull.getSchemaName()+". Resuming with entitymanager "+entityManager.getClass().getCanonicalName());
                    ArrayList<File> files = new ArrayList<>();
                    HashSet<String> fileNames = new HashSet<>();
                    for (InterruptedPullFile interruptedPullFile : interruptedPull.getFiles()) {
                        String filename = interruptedPullFile.getFilename();
                        if (!fileNames.contains(filename)) {
                            files.add(new File(filename));
                            fileNames.add(filename);
                        }
                    }
                    InputStream cacheStream = this.registerManager.getCacheStream(files);
                    if (cacheStream == null) {
                        this.log.error("Got no stream from cache");
                    } else {
                        StringJoiner sj = new StringJoiner("\n");
                        for (File file : files) {
                            sj.add(file.getAbsolutePath());
                        }
                        this.log.info("Got stream from files: \n" + sj.toString());
                        this.importMetadata = new ImportMetadata();
                        this.importMetadata.setImportTime(interruptedPull.getStartTime());
                        this.importMetadata.setStartChunk(interruptedPull.getChunk());
                        this.importMetadata.setImportConfiguration((ObjectNode) this.engine.objectMapper.readTree(interruptedPull.getImportConfiguration()));
                        this.deleteInterrupt(interruptedPull);

                        Session session = this.engine.sessionManager.getSessionFactory().openSession();
                        this.importMetadata.setSession(session);
                        try {
                            this.log.info("Resuming at chunk "+interruptedPull.getChunk()+"...");
                            entityManager.parseData(cacheStream, this.importMetadata);
                        } catch (Exception e) {
                            if (!this.doCancel) {
                                throw e;
                            }
                        } finally {
                            QueryManager.clearCaches();
                            session.close();
                        }
                    }
                }
            }



            this.importMetadata = new ImportMetadata();
            this.importMetadata.setImportConfiguration(importConfiguration);

            boolean error = false;
            boolean skip = false;
            if (this.registerManager.pullsEventsCommonly()) {
                this.log.info("Pulling data for " + this.registerManager.getClass().getSimpleName());
                ItemInputStream<? extends PluginSourceData> stream = this.registerManager.pullEvents(this.importMetadata);
                if (stream != null) {
                    this.doPull(importMetadata, stream);
                    Session session = this.engine.sessionManager.getSessionFactory().openSession();
                    importMetadata.setSession(session);
                    this.registerManager.setLastUpdated(null, importMetadata);
                    session.close();
                } else {
                    skip = true;
                }
            } else {
                for (EntityManager entityManager : this.registerManager.getEntityManagers()) {
                    if (this.doCancel) {
                        break;
                    }
                    if (!entityManager.pullEnabled()) {
                        this.log.info("Entitymanager "+entityManager.getClass().getSimpleName()+" is disabled");
                        continue;
                    }

                    Session session = this.engine.sessionManager.getSessionFactory().openSession();
                    OffsetDateTime lastUpdate = entityManager.getLastUpdated(session);
                    session.close();
                    if (lastUpdate != null && importMetadata.getImportTime().toLocalDate().isEqual(lastUpdate.toLocalDate()) && importConfiguration.size() == 0) {
                        this.log.info("Already pulled data for " + entityManager.getClass().getSimpleName()+" at "+lastUpdate+", no need to re-pull today");
                        continue;
                    }

                    this.log.info("Pulling data for " + entityManager.getClass().getSimpleName());

                    InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, this.importMetadata);
                    if (stream != null) {
                        session = this.engine.sessionManager.getSessionFactory().openSession();
                        this.importMetadata.setSession(session);

                        try {
                            entityManager.parseData(stream, importMetadata);
                            if (importMetadata.getImportConfiguration() == null || importMetadata.getImportConfiguration().size() == 0) {
                                this.registerManager.setLastUpdated(entityManager, importMetadata);
                            }
                        } catch (Exception e) {
                            if (this.doCancel) {
                                break;
                            } else {
                                throw e;
                            }
                        } finally {
                            QueryManager.clearCaches();
                            session.close();
                            stream.close();
                        }
                    }
                }
            }

            String prefix = "Worker " + this.getId() + ": ";
            if (this.doCancel) {
                this.log.info(prefix + "Pull interrupted");
            } else if (error) {
                this.log.info(prefix + "Pull errored");
            } else if (skip) {
                this.log.info(prefix + "Pull skipped");
            } else {
                this.log.info(prefix + "Pull complete");
            }
            this.onComplete();

            this.log.info("Worker " + this.getId() + " removing lock for " + this.registerManager.getClass().getCanonicalName() + " (" + this.registerManager.hashCode() + ") on " + OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } catch (ImportInterruptedException e) {
            String prefix = "Worker " + this.getId() + ": ";
            this.log.info(prefix + "Pull interrupted");

            if (e.getChunk() != null && e.getFiles() != null) {
                this.saveInterrupt(e);
            }

            this.onComplete();

        } catch (Throwable e) {
            e.printStackTrace();
            this.log.error(e);
            this.onError(e);
            MonitorLogger.logMonitoredError(e);
            throw new RuntimeException(e);
        } finally {
            runningPulls.remove(this.registerManager);
        }
    }

    /**
     * Ask a running Pull to stop. It's up to the called EntityManagers to actually respect this flag.
     * During its run, an EntityManager must regularly check for this flag, and throw an ImportInterruptedException
     * when it is set.
     */
    @Override
    public void end() {
        if (this.importMetadata != null) {
            this.importMetadata.setStop();
        }
    }

    /**
     * With an interruption, save the state (cached files, offset) to the database, so the pull can be resumed later
     * @param exception
     */
    private void saveInterrupt(ImportInterruptedException exception) {
        if (this.importMetadata != null) {
            this.log.info("Saving interrupt");
            InterruptedPull interruptedPull = new InterruptedPull();
            interruptedPull.setChunk(exception.getChunk());
            interruptedPull.setFiles(exception.getFiles());
            interruptedPull.setStartTime(this.importMetadata.getImportTime());
            interruptedPull.setInterruptTime(OffsetDateTime.now());
            interruptedPull.setSchemaName(exception.getEntityManager().getSchema());
            interruptedPull.setPlugin(this.registerManager.getPlugin());
            try {
                interruptedPull.setImportConfiguration(this.engine.objectMapper.writeValueAsString(this.importConfiguration));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            Session session = this.engine.configurationSessionManager.getSessionFactory().openSession();
            session.beginTransaction();
            session.save(interruptedPull);
            session.getTransaction().commit();
            session.close();
        }
    }

    /**
     * Get data on last interrupted pull from the database
     * @return
     */
    private InterruptedPull getLastInterrupt() {
        Session session = this.engine.configurationSessionManager.getSessionFactory().openSession();
        HashMap<String, Object> filter = new HashMap<>();
        filter.put("plugin", this.registerManager.getPlugin().getName());
        InterruptedPull interruptedPull = QueryManager.getItem(session, InterruptedPull.class, filter);
        if (interruptedPull != null) {
            interruptedPull.getFiles();
        }
        session.close();
        return interruptedPull;
    }

    /**
     * When an interrupted pull is being resumed, it should be cleared from the database
     * @param interruptedPull
     */
    private void deleteInterrupt(InterruptedPull interruptedPull) {
        this.log.info("Deleting interrupt");
        Session session = this.engine.configurationSessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.delete(interruptedPull);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    private void doPull(ImportMetadata importMetadata, ItemInputStream<? extends PluginSourceData> eventStream) throws DataStreamException, IOException {
        this.log.info("doPull");
        int count = 0;
        long last_time = System.currentTimeMillis();

        try {
            PluginSourceData event;
            Plugin plugin = this.registerManager.getPlugin();
            while ((event = eventStream.next()) != null && !this.doCancel) {
                boolean success = this.engine.handleEvent(event, plugin, importMetadata);
                if (!success) {
                    this.log.warn("Worker " + this.getId() + " failed handling event " + event.getId() + ", not processing further events");
                    eventStream.close();
                    break;
                }

                count++;

                if (count % 100 == 0) {
                    long now = System.currentTimeMillis();
                    log.info(
                        "%d: %fms per event",
                        count, (now - last_time) / 100.0
                    );
                    last_time = now;
                }
            }

        } catch (IOException e) {
            throw new DataStreamException(e);
        } finally {
            this.log.info("Worker " + this.getId() + " processed " + count + " events. Closing stream.");
            eventStream.close();
        }
    }

    private static HashMap<RegisterManager, Pull> runningPulls = new HashMap<>();

}
