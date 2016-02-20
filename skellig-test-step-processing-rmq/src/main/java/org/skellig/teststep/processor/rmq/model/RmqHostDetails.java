package org.skellig.teststep.processor.rmq.model;

public class RmqHostDetails {

    private String host;
    private int port;
    private String user;
    private String password;

    public RmqHostDetails(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public static class Builder {
        private String host;
        private int port;
        private String user;
        private String password;

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

        public RmqHostDetails build() {
            return new RmqHostDetails(host, port, user, password);
        }
    }

}
