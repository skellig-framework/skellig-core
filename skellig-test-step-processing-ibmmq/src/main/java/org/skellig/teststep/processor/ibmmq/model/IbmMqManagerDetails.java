package org.skellig.teststep.processor.ibmmq.model;

import java.util.Objects;

public class IbmMqManagerDetails {

    private final String host;
    private final int port;
    private final String name;
    private final String channel;
    private final IbmMqUserCredentials userCredentials;

    private IbmMqManagerDetails(String name, String channel, String host, int port, IbmMqUserCredentials userCredentials) {
        Objects.requireNonNull(name, "MQ Manager name cannot be null");
        Objects.requireNonNull(channel, "MQ Manager channel cannot be null");
        Objects.requireNonNull(host, "MQ Manager host cannot be null");
        this.name = name;
        this.channel = channel;
        this.host = host;
        this.port = port;
        this.userCredentials = userCredentials;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getChannel() {
        return channel;
    }

    public IbmMqUserCredentials getUserCredentials() {
        return userCredentials;
    }

    public static class Builder {
        private String host;
        private int port;
        private String name;
        private String channel;
        private IbmMqUserCredentials userCredentials;

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withChannel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder withUserCredentials(IbmMqUserCredentials userCredentials) {
            this.userCredentials = userCredentials;
            return this;
        }

        public IbmMqManagerDetails build() {
            return new IbmMqManagerDetails(name, channel, host, port, userCredentials);
        }
    }

    public static class IbmMqUserCredentials {

        private final String username;
        private final String password;

        public IbmMqUserCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

    }
}
