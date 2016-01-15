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

import net.bpiwowar.xpm.commands.Redirect;
import net.bpiwowar.xpm.exceptions.LaunchException;
import net.bpiwowar.xpm.exceptions.LockException;
import net.bpiwowar.xpm.locks.FileLock;
import net.bpiwowar.xpm.locks.Lock;
import net.bpiwowar.xpm.manager.scripting.Expose;
import net.bpiwowar.xpm.manager.scripting.Exposed;
import net.bpiwowar.xpm.scheduler.TypeIdentifier;
import net.bpiwowar.xpm.utils.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * A local host connector provides access to the current machine
 *
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 */
@Exposed
@TypeIdentifier("local")
public class LocalhostConnector extends SingleHostConnector {

    static final private Logger LOGGER = Logger.getLogger();
    private static final String TMPDIR = System.getProperty("java.io.tmpdir").toString();

    public static final String IDENTIFIER = "file://";

    /**
     * Used for serialization
     */
    public LocalhostConnector(Long id, String uri) {
        super(id);
    }

    public LocalhostConnector() {
        super(IDENTIFIER);
    }

    @Override
    public Path resolve(String path) {
        return new File(path).toPath();
    }

    @Override
    public Path defaultTemporaryPath() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    @Override
    public AbstractProcessBuilder processBuilder() {
        return new ProcessBuilder();
    }

    @Override
    protected FileSystem doGetFileSystem() throws FileSystemException {
        return FileSystems.getDefault();
    }

    @Override
    protected boolean contains(FileSystem fileSystem) throws IOException {
        return fileSystem.equals(getFileSystem());
    }

    @Override
    public Lock createLockFile(Path path, boolean wait) throws LockException {
        return new FileLock(path, wait);
    }

    @Override
    public String getHostName() {
        return "localhost";
    }

    @Override
    protected Path getTemporaryDirectory() throws IOException {
        return getFileSystem().getPath(TMPDIR);
    }

    /**
     * Localhost process builder
     */
    static private class ProcessBuilder extends AbstractProcessBuilder {

        static private File convert(Path file) throws FileSystemException {
            return file.toAbsolutePath().toFile();
        }

        static private java.lang.ProcessBuilder.Redirect convert(Redirect redirect) throws FileSystemException {
            if (redirect == null) redirect = Redirect.INHERIT;

            switch (redirect.type()) {
                case PIPE:
                    return java.lang.ProcessBuilder.Redirect.PIPE;
                case INHERIT:
                    return java.lang.ProcessBuilder.Redirect.INHERIT;
                case WRITE:
                    return java.lang.ProcessBuilder.Redirect.to(convert(redirect.file()));
                case APPEND:
                    return java.lang.ProcessBuilder.Redirect.appendTo(convert(redirect.file()));
                case READ:
                    return java.lang.ProcessBuilder.Redirect.from(convert(redirect.file()));
            }
            throw new AssertionError("Should not be here - enum not handled: " + redirect.type());
        }


        @Override
        public XPMProcess start(boolean fake) throws LaunchException, IOException {
            if (fake) return null;
            java.lang.ProcessBuilder builder = new java.lang.ProcessBuilder();

            // Set the environment
            Map<String, String> builderEnvironment = builder.environment();

            if (this.environment() != null)
                for (Map.Entry<String, String> entry : this.environment().entrySet()) {
                    builderEnvironment.put(entry.getKey(), entry.getValue());
                }

            if (LOGGER.isDebugEnabled()) {
                for (Map.Entry<String, String> entry : builderEnvironment.entrySet()) {
                    LOGGER.debug("[*] %s=%s", entry.getKey(), entry.getValue());
                }
            }

            builder.redirectError(convert(error));
            builder.redirectOutput(convert(output));
            builder.redirectInput(convert(input));

            builder.command(command());

            final Process process = builder.start();
            LOGGER.info("Started local job with command [%s]", command().get(0));

            return new LocalProcess(job, process, detach());
        }
    }

    @Expose
    public String env(String key) {
        return System.getenv(key);
    }

}

