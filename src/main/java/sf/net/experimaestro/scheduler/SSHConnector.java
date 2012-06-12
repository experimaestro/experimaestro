/*
 *
 *  This file is part of experimaestro.
 *  Copyright (c) 2012 B. Piwowarski <benjamin@bpiwowar.net>
 *
 *  experimaestro is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  experimaestro is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package sf.net.experimaestro.scheduler;

import com.jcraft.jsch.*;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import com.sleepycat.persist.model.Persistent;
import sf.net.experimaestro.locks.Lock;
import sf.net.experimaestro.locks.UnlockableException;
import sf.net.experimaestro.utils.Output;
import sf.net.experimaestro.utils.arrays.ListAdaptator;
import sf.net.experimaestro.utils.log.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 * @date 7/6/12
 */
@Persistent
public class SSHConnector implements Connector {

    static final private Logger LOGGER = Logger.getLogger();

    /** Username */
    String username;

    /** Hostname to connect to */
    String hostname;

    /** Port */
    int port = 22;

    /** Used for serialization */
    public SSHConnector() {

    }

    public SSHConnector(String username, String hostname) {
        this.username = username;
        this.hostname = hostname;
    }

    @Override
    public PrintWriter printWriter(String identifier) throws JSchException, IOException {
        boolean ptimestamp = true;
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + identifier;
        final ChannelExec channel = newExecChannel();
        channel.setCommand(command);
        OutputStream out = channel.getOutputStream();
        return new PrintWriter(out) {
            @Override
            public void close() {
                channel.disconnect();
                super.close();
            }
        };
    }



    @Override
    public int exec(String identifier, String command, ArrayList<Lock> locks) throws Exception {
        final ChannelExec channel = newExecChannel();
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err, true);
        channel.setOutputStream(System.out, true);

        LOGGER.info("Starting the task [%s]", identifier);
        channel.connect();

        LOGGER.info("Waiting for task to finish");
        while (!channel.isClosed()) {
            Thread.sleep(1000);
        }


        int code = channel.getExitStatus();
        LOGGER.info("Task finished [code %d]", code);

        channel.getInputStream().close();
        channel.disconnect();
        LOGGER.info("Disconnected");
        return code;
    }


    @Override
    public Lock createLockFile(final String lockIdentifier) throws UnlockableException {
        try {
            ChannelExec channel = newExecChannel();
            LOGGER.info("Creating SSH lock [%s]", lockIdentifier);
            channel.setCommand(String.format("lockfile \"%s\"", CommandLineTask.protect(lockIdentifier, "\"")));
            channel.setInputStream(null);
            channel.setErrStream(System.err, true);
            channel.setOutputStream(System.out, true);
            channel.connect();
            LOGGER.info("Waiting for task to finish");
            while (!channel.isClosed()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error(e);
                }
            }
            channel.disconnect();
        } catch (JSchException e) {
            throw new UnlockableException(e);
        }

        return new Lock() {
            @Override
            public boolean dispose() {
                final ChannelExec channel;
                try {
                    ChannelSftp sftp = newSftpChannel();
                    LOGGER.info("Disposing of SSH lock [%s] / %s", lockIdentifier, sftp);
                    sftp.connect();
                    sftp.chmod(644, lockIdentifier);
                    sftp.rm(lockIdentifier);
                    sftp.disconnect();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    new RuntimeException(e);
                }

                return false;
            }

            @Override
            public void changeOwnership(int pid) {
            }
        };
    }

    @Override
    public void touchFile(String identifier) throws IOException, JSchException {
        final ChannelExec channel = newExecChannel();
        channel.setCommand(String.format("touch \"%s\"", CommandLineTask.protect(identifier, "\"")));
        channel.setInputStream(null);
        channel.setErrStream(System.err, true);
        channel.setOutputStream(System.out, true);
        channel.disconnect();
    }

    @Override
    public boolean fileExists(String identifier) throws JSchException {
        ChannelSftp channel = newSftpChannel();
        try {
            final SftpATTRS stat = channel.stat(identifier);
        } catch (SftpException e) {
            return false;
        } finally {
            channel.disconnect();
        }


        return true;
    }

    @Override
    public long getLastModifiedTime(String identifier) throws JSchException, SftpException {
        ChannelSftp channel = newSftpChannel();
        channel.connect();
        try {
            final SftpATTRS stat = channel.stat(identifier);
            return stat.getMTime();
        } finally {
            channel.disconnect();
        }

    }

    @Override
    public InputStream getInputStream(String identifier) throws IOException, JSchException {
        String command = "scp  -f " + identifier;
        final ChannelExec channel = newExecChannel();

        OutputStream out = channel.getOutputStream();
        final InputStream in = channel.getInputStream();
        channel.connect();

        return new BufferedInputStream(in) {
            @Override
            public void close() throws IOException {
                channel.disconnect();
                super.close();
            }
        };
    }

    @Override
    public void renameFile(String from, String to) throws JSchException, SftpException {
        ChannelSftp channel = newSftpChannel();
        channel.rename(from, to);
        channel.connect();
        channel.disconnect();
    }

    static final int S_IXUSR = 00100; // execute/search by owner

    @Override
    public void setExecutable(String path, boolean flag) throws JSchException, SftpException {
        ChannelSftp channel = newSftpChannel();
        final SftpATTRS attr = channel.stat(path);
        if (flag)
            attr.setPERMISSIONS(attr.getPermissions() | S_IXUSR);
        else
            attr.setPERMISSIONS(attr.getPermissions() & ~S_IXUSR);

        channel.setStat(path, attr);
        channel.connect();
        channel.disconnect();
    }


//    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
//        String passwd;
//        JTextField passwordField = (JTextField) new JPasswordField(20);
//
//        MyUserInfo() {
//        }
//
//        public String getPassword() {
//            return passwd;
//        }
//
//        public boolean promptYesNo(String str) {
//            return false;
//        }
//
//
//        public String getPassphrase() {
//            return null;
//        }
//
//        public boolean promptPassphrase(String message) {
//            return true;
//        }
//
//        public boolean promptPassword(String message) {
//            return false;
//        }
//
//        public void showMessage(String message) {
//        }
//
//
//        public String[] promptKeyboardInteractive(String destination,
//                                                  String name,
//                                                  String instruction,
//                                                  String[] prompt,
//                                                  boolean[] echo) {
//            return null;
//        }
//    }



    class SSHSession {
        public Session session;

        SSHSession() throws JSchException {
            init();
        }

        void init() throws JSchException {
            JSch jsch = new JSch();

            try {
                com.jcraft.jsch.agentproxy.Connector con = null;
                if (SSHAgentConnector.isConnectorAvailable()) {
                    USocketFactory usf = new JNAUSocketFactory();
                    con = new SSHAgentConnector(usf);
                    if (PageantConnector.isConnectorAvailable())
                        con = new PageantConnector();
                    IdentityRepository irepo = new RemoteIdentityRepository(con);
                    jsch.setIdentityRepository(irepo);
                    jsch.setConfig("PreferredAuthentications", "publickey");
                }
            } catch (AgentProxyException e) {
                LOGGER.warn("Could not create ssh-agent proxy: %s", e);
            }

            session = jsch.getSession(username, hostname, 22);
            jsch.setKnownHosts(new File(new File(System.getProperty("user.home"), ".ssh"), "known_hosts").getAbsolutePath());
            session.connect();
        }
    }

    ChannelSftp newSftpChannel() throws JSchException {
        return (ChannelSftp) getSession().openChannel("sftp");
    }

    ChannelExec newExecChannel() throws JSchException {
        return (ChannelExec) getSession().openChannel("exec");
    }

    /** Get the session (creates it if necessary) */
    private Session getSession() throws JSchException {
        if (_session == null) {
            _session = sessions.get(this);
            if (_session == null) {
                sessions.put(this, _session = new SSHSession());
            }
        }

        // If we are not connected, do it now
        if (!_session.session.isConnected())
            _session.session.connect();

        // Returns
        return _session.session;
    }

    /** Static map to sessions
     * This is necessary since the SSHConnector object can be serialized (within a resource)
     */
    static private HashMap<SSHConnector, SSHSession> sessions = new HashMap<SSHConnector, SSHSession>();

    /**
     * Cached SSH session
     */
    static transient private SSHSession _session;

}
