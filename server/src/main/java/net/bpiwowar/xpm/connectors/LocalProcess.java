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

import net.bpiwowar.xpm.exceptions.LaunchException;
import net.bpiwowar.xpm.scheduler.Job;
import net.bpiwowar.xpm.scheduler.Scheduler;
import net.bpiwowar.xpm.scheduler.TypeIdentifier;
import net.bpiwowar.xpm.utils.ProcessUtils;
import net.bpiwowar.xpm.utils.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystemException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Wrapper for a local thread
 */
@TypeIdentifier("local")
public class LocalProcess extends XPMProcess {
    final static private Logger LOGGER = Logger.getLogger();

    /**
     * The running process: if we have it, easier to monitor
     */
    transient private Process process;
    transient private Thread destroyThread;

    /**
     * Stores the exit value
     */
    transient private Integer exitValue;

    @SuppressWarnings("unused")
    public LocalProcess() {
    }

    // Check on Windows:
    // http://stackoverflow.com/questions/2318220/how-to-programmatically-detect-if-a-process-is-running-with-java-under-windows

    public LocalProcess(Job job, Process process, boolean detach) {
        super(Scheduler.get().getLocalhostConnector(), String.valueOf(ProcessUtils.getPID(process)), job);
        this.process = process;
        if (!detach) {
            // If we need to destroy this process
            destroyThread = new Thread() {
                @Override
                public void run() {
                    try {
                        LocalProcess.this.destroy();
                    } catch (FileSystemException e) {
                        LOGGER.error("Process %s could not be destroyed", LocalProcess.this);
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(destroyThread);
        }

        startWaitProcess();
    }

    @Override
    public boolean isRunning(boolean checkFiles) throws Exception {
        if (!checkFiles && process != null) {
            return ProcessUtils.isRunning(process);
        }

        return super.isRunning(checkFiles);
    }

    @Override
    public int exitValue(boolean checkFile) {
        // Try the easy way
        if (process != null && !checkFile) {
            return process.exitValue();
        }

        return super.exitValue(checkFile);
    }


    @Override
    public OutputStream getOutputStream() {
        return process == null ? null : process.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return process == null ? null : process.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return process == null ? null : process.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        if (process != null) {
            exitValue = process.waitFor();
            return exitValue;
        }
        if (exitValue != null) {
            return exitValue;
        }
        return super.waitFor();
    }

    @Override
    public void destroy() throws FileSystemException {
        if (exitValue != null) {
            return;
        }

        if (process != null) {
            LOGGER.info("Killing job [%s] with PID [%s]", getJob(), getPID());
            // TODO: send a signal first?
            process.destroy();

            if (destroyThread != null)
                try {
                    Runtime.getRuntime().removeShutdownHook(destroyThread);
                } catch (IllegalStateException e) {/* Ignore */}
            process = null;
        } else {
            LOGGER.info("Process was not started by server: killing it externally with PID %s", getPID());
            AbstractProcessBuilder killingProcessBuilder = Scheduler.get().getLocalhostConnector().processBuilder();
            killingProcessBuilder.command("kill", getPID()).detach(false);
            try {
                final XPMProcess killingProcess = killingProcessBuilder.detach(false).start();
                int code = killingProcess.waitFor();
                if (code != 0)
                    throw new RuntimeException(format("Could not kill local process [%s]: error code %d", getPID(), code));
            } catch (LaunchException | IOException | InterruptedException e) {
                throw new RuntimeException(format("Could not kill local process [%s]", getPID()), e);
            }
        }

        // TODO: finish the implementation ???
//            throw new NotImplementedException();
    }


}
