package net.bpiwowar.xpm.connectors;

/*
 * This file is part of experimaestro.
 * Copyright (c) 2014 B. Piwowarski <benjamin@bpiwowar.net>
 *
 * experimaestro is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * experimaestro is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
 */

import net.bpiwowar.xpm.exceptions.CloseException;
import net.bpiwowar.xpm.exceptions.ConnectorException;
import net.bpiwowar.xpm.exceptions.LockException;
import net.bpiwowar.xpm.exceptions.XPMRuntimeException;
import net.bpiwowar.xpm.locks.Lock;
import net.bpiwowar.xpm.scheduler.ConstructorRegistry;
import net.bpiwowar.xpm.scheduler.DatabaseObjects;
import net.bpiwowar.xpm.scheduler.EndOfJobMessage;
import net.bpiwowar.xpm.scheduler.Job;
import net.bpiwowar.xpm.scheduler.Resource;
import net.bpiwowar.xpm.scheduler.Scheduler;
import net.bpiwowar.xpm.utils.CloseableIterable;
import net.bpiwowar.xpm.utils.GsonConverter;
import net.bpiwowar.xpm.utils.JsonAbstract;
import net.bpiwowar.xpm.utils.JsonSerializationInputStream;
import net.bpiwowar.xpm.utils.db.SQLInsert;
import net.bpiwowar.xpm.utils.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * A process monitor.
 * <p/>
 * <p>
 * A job monitor task is to... monitor the execution of a job, wherever
 * the job is running (remote, local, etc.).
 * It is returned when a job starts, contains information (ID, state, etc.) and control methods (stop)
 * </p>
 *
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 */
@JsonAbstract
public abstract class XPMProcess {
    static ConstructorRegistry<XPMProcess> REGISTRY
            = new ConstructorRegistry(new Class[]{}).add(LocalProcess.class, SSHProcess.class, OARProcess.class);

    static private Logger LOGGER = Logger.getLogger();

    /**
     * True when the process is stored in DB
     */
    transient boolean inDatabase = false;

    /**
     * The checker
     */
    protected transient ScheduledFuture<?> checker = null;

    /**
     * The job to notify when finished with this
     */
    transient protected Job job;

    /**
     * Our process ID
     */
    String pid;

    /**
     * The connector for this job
     */
    private SingleHostConnector connector;

    /**
     * Progress
     */
    private double progress = -1;

    /**
     * Last update (UNIX timestamp)
     */
    private long last_update = -1;

    /**
     * The associated locks to release when the process has ended
     */
    private List<Lock> locks = null;

    /**
     * Creates a new job monitor from a process
     *
     * @param job The attached job (If any)
     * @param pid The process ID
     */
    protected XPMProcess(SingleHostConnector connector, String pid, final Job job) {
        this.connector = connector;
        this.pid = pid;
        this.job = job;
    }

    /**
     * Constructs a XPMProcess without an underlying process
     */
    protected XPMProcess(SingleHostConnector connector, final Job job, String pid) {
        this(connector, pid, job);
    }

    /**
     * Used for serialization
     *
     * @see {@linkplain #init(net.bpiwowar.xpm.scheduler.Job)}
     */
    protected XPMProcess() {
    }

    /**
     * Set up a notifiction using {@linkplain java.lang.Process#waitFor()}.
     */
    protected void startWaitProcess() {
        LOGGER.debug("XPM Process %s constructed", getConnector());

        // Set up the notification thread if needed
        if (job != null && job.isActiveWaiting()) {
            new Thread(String.format("job monitor [%s]", job.getId())) {
                @Override
                public void run() {
                    int code;
                    while (true) {
                        try {
                            LOGGER.info("Waiting for job [%s] process to finish", job);
                            code = waitFor();
                            LOGGER.info("Job [%s] process has finished running [code %d]", job, code);
                            break;
                        } catch (InterruptedException e) {
                            LOGGER.error("Interrupted: %s", e);
                        }
                    }

                    final int _code = code;
                    Scheduler.get().sendMessage(job, new EndOfJobMessage(_code, System.currentTimeMillis()));
                }
            }.start();
        }
    }

    /**
     * Initialization of the job monitor (when restoring from database)
     * <p>
     * The method also sets up a status checker at regular intervals.
     */
    public void init(Job job) {
        // TODO: use connector & job dependent times for checking
        checker = Scheduler.get().schedule(this, 30, TimeUnit.SECONDS);
    }

    @Override
    public String toString() {
        return String.format("[Process of %s]", job);
    }

    /**
     * Get the underlying job
     *
     * @return The job
     */
    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }


    /**
     * Adopt the locks
     */
    public void adopt(List<Lock> locks) throws SQLException {
        // Changing the ownership of the different logs
        try (PreparedStatement st = Scheduler.prepareStatement("INSERT INTO ProcessLocks(process, lock) VALUES(?,?)")) {
            st.setLong(1, job.getId());

            for (Lock lock : locks) {
                try {
                    lock.changeOwnership(pid);
                    if (inDatabase) {
                        lock.save();
                        st.setLong(2, lock.getId());
                        st.execute();
                    }
                } catch (Throwable e) {
                    LOGGER.error(e, "Could not adopt lock %s", lock);
                }
            }
        }
        this.locks = locks;
    }

    /**
     * Dispose of this job monitor
     */
    synchronized public void dispose() throws LockException {
        close();

        try {
            locks = loadLocks();
            LOGGER.info("Disposing of %d locks for %s", locks.size(), this);
            while (!locks.isEmpty()) {
                final Lock lock = locks.get(locks.size() - 1);
                lock.close();
                locks.remove(locks.size() - 1);
            }
            locks = null;

            if (inDatabase) {
                Scheduler.statement("DELETE FROM Processes WHERE resource=?")
                        .setLong(1, job.getId())
                        .execute().close();
            }
        } catch (SQLException | CloseException e) {
            throw new LockException(e);
        }

    }

    /**
     * Close this job monitor
     */
    void close() {
        if (checker != null) {
            LOGGER.info("Cancelling the checker for %s", this);
            if (!checker.cancel(true)) {
                checker = null;
            } else
                LOGGER.warn("Could not cancel the checker");

        }
    }

    /**
     * Check if a job is running but allow some latency
     * @param latency The latency (time to last check) in seconds
     * @return True if the process is running, false otherwise
     * @throws ConnectorException If something goes wrong while checking
     */
    public boolean isRunning(long latency) throws ConnectorException {
        if ((System.currentTimeMillis() - last_update) > latency * 1000) {
            boolean running = isRunning();
            try (PreparedStatement st = Scheduler.prepareStatement("UPDATE Processes SET last_update=? WHERE resource=?")) {
                st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                st.setLong(2, job.getId());
                final long count = st.executeUpdate();
                if (count != 1) {
                    LOGGER.error("Could not update last_update for %s [count is not 1 but %d]", job, count);
                }
            } catch (SQLException e) {
                LOGGER.error("Could not update last_update for %s", job);
            }
            return running;
        }

        // Process is still running since there was a status update
        return true;
    }

    /**
     * Check if the job is running
     *
     * @return True if the job is running
     */
    public boolean isRunning() throws ConnectorException {
        // We have no process, check
        final boolean exists = Files.exists(Job.LOCK_EXTENSION.transform(job.getLocator()));
        LOGGER.debug("Job %s is running: %s", this.job, exists);
        return exists;
    }

    /**
     * Returns the error code
     *
     * @param checkFile Whether to ask the process (if available) or look at the file
     */
    public int exitValue(boolean checkFile) {
        // Check for done file
        try {
            if (Files.exists(Resource.DONE_EXTENSION.transform(job.getLocator())))
                return 0;

            // If the job is not done, check the ".code" file to get the error code
            // and otherwise return -1
            final Path codeFile = Resource.CODE_EXTENSION.transform(job.getLocator());
            if (Files.exists(codeFile)) {
                final InputStream stream = Files.newInputStream(codeFile);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String s = reader.readLine();
                int code = s != null ? Integer.parseInt(s) : -1;
                reader.close();
                return code;
            }
        } catch (IOException e) {
            throw new IllegalThreadStateException();
        }

        // The process failed, but we do not know how
        return -1;

    }

    /**
     * Asynchronous check the state of the job monitor
     *
     * @param checkFiles Whether files should be checked rather than the process
     * @param latency The latency in seconds to check the job
     * @return true if the process was stopped
     */
    synchronized public boolean check(boolean checkFiles, long latency) throws Exception {
        if (!isRunning(latency)) {
            // We are not running: send a message
            LOGGER.info("End of job [%s]", job);
            final Path file = Resource.CODE_EXTENSION.transform(job.getLocator());
            final long time = Files.exists(file) ? Files.getLastModifiedTime(file).toMillis() : -1;
            Scheduler.get().sendMessage(job, new EndOfJobMessage(exitValue(checkFiles), time));
            return true;
        }
        return false;
    }

    /**
     * @see {@linkplain Process#getOutputStream()}
     */
    abstract public OutputStream getOutputStream();

    /**
     * @see {@linkplain Process#getErrorStream()}
     */
    abstract public InputStream getErrorStream();

    /**
     * @see {@linkplain Process#getInputStream()}
     */
    abstract public InputStream getInputStream();

    /**
     * Periodic checks
     *
     * @see {@linkplain Process#waitFor()}
     */
    public int waitFor() throws InterruptedException {
        synchronized (this) {
            // Wait 1 second
            while (true) {
                try {
                    try {
                        if (!isRunning()) {
                            return exitValue(false);
                        }
                    } catch (Exception e) {
                        // TODO: what to do here?
                        // Delay?
                        LOGGER.warn("Error while checking if running");
                    }
                    wait(1000);

                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    /**
     * @see {@linkplain Process#destroy()}
     */
    abstract public void destroy() throws FileSystemException;

    public String getPID() {
        return pid;
    }

    /**
     * The host where this process is running (or give an access to the process, e.g. for OAR processes)
     */
    protected SingleHostConnector getConnector() {
        return connector;
    }

    final private static SQLInsert SQL_INSERT = new SQLInsert("Processes", false, "resource", "type", "connector", "pid", "data", "progress", "last_update");

    /**
     * Save the process
     */
    public void save() throws SQLException {
        // Save the process
        final Connection connection = Scheduler.getConnection();
        SQL_INSERT.execute(connection, false, job.getId(),
                DatabaseObjects.getTypeValue(getClass()), connector.getId(), pid,
                JsonSerializationInputStream.of(this, GsonConverter.defaultBuilder),
                progress, new Timestamp(last_update));
        inDatabase = true;

        // Save the locks
        if (locks != null) {
            try (PreparedStatement st = connection.prepareStatement("INSERT INTO ProcessLocks(process, lock) VALUES(?,?)")) {
                // Insert into DB
                st.setLong(1, job.getId());

                for (Lock lock : locks) {
                    // Save lock
                    lock.save();

                    // Save relationship
                    st.setLong(2, lock.getId());
                    st.execute();
                }
            }
        }
    }

    public static XPMProcess load(Job job) throws SQLException {
        try (PreparedStatement st = Scheduler.prepareStatement("SELECT type, connector, pid, data, progress, last_update FROM Processes WHERE resource=?")) {
            st.setLong(1, job.getId());
            st.execute();
            final ResultSet rs = st.getResultSet();
            if (!rs.next()) {
                return null;
            }

            long type = rs.getLong(1);


            final XPMProcess process = REGISTRY.newInstance(type);
            process.connector = (SingleHostConnector) Connector.findById(rs.getLong(2));
            process.pid = rs.getString(3);
            process.progress = rs.getDouble(5);
            process.last_update = rs.getTimestamp(6).getTime();
            process.job = job;
            process.inDatabase = true;
            DatabaseObjects.loadFromJson(GsonConverter.defaultBuilder, process, rs.getBinaryStream(4));
            return process;
        }
    }

    private List<Lock> loadLocks() throws SQLException, CloseException {
        if (locks == null) {
            List<Lock> locks = new ArrayList<>();
            try (final CloseableIterable<Lock> it = Scheduler.get().locks().find(Lock.PROCESS_SELECT_QUERY, st -> st.setLong(1, job.getId()))) {
                it.forEach(locks::add);
            }
            return locks;
        }
        return locks;
    }

    public long exitTime() {
        // FIXME: implement this
        return System.currentTimeMillis();
    }

    /**
     * Progress
     * <p>
     * The value is negative if not set
     */
    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        if (progress != this.progress) {
            try {
                Scheduler.statement(format("UPDATE Processes SET progress=?, last_update=? WHERE resource=?"))
                        .setDouble(1, progress)
                        .setTimestamp(2, new Timestamp(System.currentTimeMillis()))
                        .setLong(3, job.getId())
                        .execute().close();
                this.progress = progress;
            } catch (SQLException e) {
                throw new XPMRuntimeException(e, "Could not set value in database");
            }
        }

        this.progress = progress;
    }
}