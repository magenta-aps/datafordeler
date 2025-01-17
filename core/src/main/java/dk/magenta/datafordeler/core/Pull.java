package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.database.InterruptedPull;
import dk.magenta.datafordeler.core.database.InterruptedPullFile;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.ImportInterruptedException;
import dk.magenta.datafordeler.core.exception.SimilarJobRunningException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.JobDataMap;

import java.io.File;
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

    private final Logger log = LogManager.getLogger(Pull.class.getCanonicalName());

    private final RegisterManager registerManager;
    private final Engine engine;
    private final ObjectNode importConfiguration;
    private final String prefix;

    public Pull(Engine engine, RegisterManager registerManager) {
        this(engine, registerManager, engine.objectMapper.createObjectNode());
    }

    public Pull(Engine engine, RegisterManager registerManager, ObjectNode importConfiguration) {
        this.engine = engine;
        this.registerManager = registerManager;
        this.importConfiguration = importConfiguration;
        this.prefix = "Worker " + this.getId() + ": ";
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
     */
    @Override
    public void run() {
        String pluginName = this.registerManager.getPlugin().getName();
        Session session = this.engine.sessionManager.getSessionFactory().openSession();
        try {
            this.log.info(this.prefix + "Beginning pull for " + pluginName);

            if (runningPulls.containsKey(this.registerManager)) {
                throw new SimilarJobRunningException(this.prefix + "Another pull job is already running for RegisterManager " + this.registerManager.getClass().getCanonicalName() + " (" + this.registerManager.hashCode() + ")");
            }

            this.log.info(this.prefix + "Adding lock for " + this.registerManager.getClass().getCanonicalName() + " (" + this.registerManager.hashCode() + ")");
            runningPulls.put(this.registerManager, this);

            // See if there's a prior pull that was interrupted, and resume it.
            InterruptedPull interruptedPull = this.getLastInterrupt();
            if (interruptedPull != null) {
                this.log.info(this.prefix + "A prior pull (started at " + interruptedPull.getStartTime() + ") was interrupted at " + interruptedPull.getInterruptTime() + ". Resuming.");
                EntityManager entityManager = this.registerManager.getEntityManager(interruptedPull.getSchemaName());
                if (entityManager == null) {
                    this.log.error(this.prefix + "Unknown schema: " + interruptedPull.getSchemaName() + ". Cannot resume");
                } else {
                    this.log.error(this.prefix + "Schema: " + interruptedPull.getSchemaName() + ". Resuming with entitymanager " + entityManager.getClass().getCanonicalName());
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
                        this.log.error(this.prefix + "Got no stream from cache");
                    } else {
                        StringJoiner sj = new StringJoiner("\n");
                        for (File file : files) {
                            sj.add(file.getAbsolutePath());
                        }
                        this.log.info(this.prefix + "Got stream from files: \n" + sj);
                        this.importMetadata = new ImportMetadata();
                        this.importMetadata.setSession(session);
                        this.importMetadata.setImportTime(interruptedPull.getStartTime());
                        this.importMetadata.setStartChunk(interruptedPull.getChunk());
                        String importConfiguration = interruptedPull.getImportConfiguration();
                        if (importConfiguration != null) {
                            this.importMetadata.setImportConfiguration((ObjectNode) this.engine.objectMapper.readTree(importConfiguration));
                        }
                        this.deleteInterrupt(interruptedPull);

                        try {
                            this.log.info(this.prefix + "Resuming at chunk " + interruptedPull.getChunk() + "...");
                            entityManager.parseData(cacheStream, this.importMetadata);
                        } catch (Exception e) {
                            if (!this.doCancel) {
                                throw e;
                            }
                        } finally {
                            QueryManager.clearCaches();
                        }
                    }
                }
            }


            this.importMetadata = new ImportMetadata();
            this.importMetadata.setSession(session);
            this.importMetadata.setImportConfiguration(importConfiguration);

            boolean error = false;
            boolean skip = false;
            for (EntityManager entityManager : this.registerManager.getEntityManagers()) {
                if (this.doCancel) {
                    break;
                }
                if (!entityManager.pullEnabled()) {
                    this.log.info(this.prefix + "Entitymanager " + entityManager.getClass().getSimpleName() + " is disabled");
                    continue;
                }

                OffsetDateTime lastUpdate = entityManager.getLastUpdated(session);
                if (lastUpdate != null && importMetadata.getImportTime().toLocalDate().isEqual(lastUpdate.toLocalDate()) && importConfiguration.size() == 0) {
                    this.log.info(this.prefix + "Already pulled data for " + entityManager.getClass().getSimpleName() + " at " + lastUpdate + ", no need to re-pull today");
                    continue;
                }

                this.log.info(this.prefix + "Pulling data for " + entityManager.getClass().getSimpleName());

                this.registerManager.beforePull(entityManager, this.importMetadata);
                InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, this.importMetadata);

                if (Environment.getEnv("SKIP_PULL_LOAD", false)) {
                    stream = null;
                    log.info("Skipping actual data load");
                }

                if (stream != null) {
                    try {
                        entityManager.parseData(stream, importMetadata);
                        if (!entityManager.shouldSkipLastUpdate(importMetadata)) {
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
                        stream.close();
                    }
                }
            }

            if (this.doCancel) {
                this.log.info(this.prefix + "Pull for " + pluginName + " interrupted");
            } else if (error) {
                this.log.info(this.prefix + "Pull for " + pluginName + " errored");
            } else if (skip) {
                this.log.info(this.prefix + "Pull for " + pluginName + " skipped");
            } else {
                this.log.info(this.prefix + "Pull for " + pluginName + " complete");
            }
            this.onComplete();

            this.log.info(this.prefix + " removing lock for " + this.registerManager.getClass().getCanonicalName() + " (" + this.registerManager.hashCode() + ") on " + OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } catch (ImportInterruptedException e) {
            this.log.info(this.prefix + "Pull for " + pluginName + " interrupted");
            if (e.getChunk() != null && e.getFiles() != null) {
                this.saveInterrupt(e);
            }
            this.onComplete();
        } catch (Throwable e) {
            this.log.error(this.prefix + "Pull errored", e);
            this.onError(e);
            throw new RuntimeException(e);
        } finally {
            if (this.importMetadata != null) {
                this.importMetadata.setSession(null);
            }
            session.close();
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
     *
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
     *
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
     *
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

    private static final HashMap<RegisterManager, Pull> runningPulls = new HashMap<>();

}
