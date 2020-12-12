package org.skellig.teststep.processor.unix.model;

public class UnixShellHostDetails {

    private String hostName;
    private String hostAddress;
    private int port;
    private String sshKeyPath;
    private String userName;
    private String password;

    private UnixShellHostDetails(String hostName, String hostAddress, int port, String sshKeyPath,
                                 String userName, String password) {
        this.hostName = hostName;
        this.hostAddress = hostAddress;
        this.port = port;
        this.sshKeyPath = sshKeyPath;
        this.userName = userName;
        this.password = password;
    }

    public String getHostName() {
        return hostName;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    public String getSshKeyPath() {
        return sshKeyPath;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public static class Builder {
        private String hostName;
        private String hostAddress;
        private int port;
        private String sshKeyPath;
        private String userName;
        private String password;

        public Builder withHostName(String name) {
            this.hostName = name;
            return this;
        }

        public Builder withHostAddress(String host) {
            this.hostAddress = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withSshKeyPath(String sshKeyPath) {
            this.sshKeyPath = sshKeyPath;
            return this;
        }

        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UnixShellHostDetails build() {
            return new UnixShellHostDetails(hostName, hostAddress, port, sshKeyPath, userName, password);
        }
    }
}
