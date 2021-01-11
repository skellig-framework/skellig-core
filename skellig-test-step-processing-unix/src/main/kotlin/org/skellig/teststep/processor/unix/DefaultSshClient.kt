package org.skellig.teststep.processor.unix;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.skellig.teststep.processing.exception.TestStepProcessingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

class DefaultSshClient extends SSHClient {

    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;

    private String host;
    private int port;
    private String user;
    private String password;
    private String privateSshKeyPath;
    private Session sshSession;

    private DefaultSshClient(String host, int port, String user, String password, String privateSshKeyPath) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.privateSshKeyPath = privateSshKeyPath;
        setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
    }

    public String runShellCommand(String command, int timeoutSec) {
        startSshSessionLazy();

        String response = "";
        try {
            Session.Command cmd = sshSession.exec(command);
            cmd.join(timeoutSec, TimeUnit.SECONDS);

            try (ByteArrayOutputStream outputStream = IOUtils.readFully(cmd.getInputStream())) {
                response = outputStream.toString();
            }
        } catch (Exception ex) {
            //log later
        }
        return response;

    }

    @Override
    public synchronized void close() {
        try {
            if (sshSession != null) {
                sshSession.close();
                sshSession = null;
            }
            super.close();
        } catch (Exception ex) {
            //log later
        }
    }

    private synchronized void startSshSessionLazy() {
        if (sshSession == null || !sshSession.isOpen()) {
            try {
                if (!(isConnected() && isAuthenticated())) {
                    createAndConnectSshClient();
                }
                sshSession = super.startSession();
                if (sshSession != null) {
                    sshSession.allocateDefaultPTY();
                }
            } catch (Exception ex) {
                throw new TestStepProcessingException(ex.getMessage(), ex);
            }
        }
    }

    private void createAndConnectSshClient() throws IOException {
        addHostKeyVerifier((host, port, key) -> true);
        connect(host, port);
        if (privateSshKeyPath != null) {
            authPublickey(user, privateSshKeyPath);
        } else {
            this.authPassword(user, password);
        }
    }

    static class Builder {

        private String host;
        private int port;
        private String user;
        private String password;
        private String privateSshKeyPath;

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withUser(String user) {
            this.user = user;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withPrivateSshKeyPath(String privateSshKeyPath) {
            this.privateSshKeyPath = privateSshKeyPath;
            return this;
        }

        DefaultSshClient build() {
            return new DefaultSshClient(host, port, user, password, privateSshKeyPath);
        }
    }

}
