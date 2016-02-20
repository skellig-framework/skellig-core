package org.skellig.teststep.processor.rmq.model;

import java.util.HashMap;
import java.util.Map;

public class RmqQueueDetails {

    private String name;
    private String routingKey;
    private boolean isDurable;
    private boolean isExclusive;
    private boolean isAutoDelete;
    private boolean createIfNew;
    private Map<String, Object> parameters;

    public RmqQueueDetails(String name, String routingKey, boolean isDurable,
                           boolean isExclusive, boolean isAutoDelete, boolean createIfNew,
                           Map<String, Object> parameters) {
        this.name = name;
        this.routingKey = routingKey;
        this.isDurable = isDurable;
        this.isExclusive = isExclusive;
        this.isAutoDelete = isAutoDelete;
        this.createIfNew = createIfNew;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public boolean isDurable() {
        return isDurable;
    }

    public boolean isExclusive() {
        return isExclusive;
    }

    public boolean isAutoDelete() {
        return isAutoDelete;
    }

    public boolean isCreateIfNew() {
        return createIfNew;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static class Builder {
        private String name;
        private String routingKey;
        private boolean isDurable = true;
        private boolean isExclusive;
        private boolean isAutoDelete;
        private boolean createIfNew;
        private Map<String, Object> parameters;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withRoutingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        public Builder withDurable(boolean isDurable) {
            this.isDurable = isDurable;
            return this;
        }

        public Builder withExclusive(boolean isExclusive) {
            this.isExclusive = isExclusive;
            return this;
        }

        public Builder withAutoDelete(boolean isAutoDelete) {
            this.isAutoDelete = isAutoDelete;
            return this;
        }

        public Builder withCreateIfNew(boolean createIfNew) {
            this.createIfNew = createIfNew;
            return this;
        }

        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withParameter(String name, Object value) {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            parameters.put(name, value);
            return this;
        }

        public RmqQueueDetails build() {
            return new RmqQueueDetails(name, routingKey, isDurable, isExclusive, isAutoDelete, createIfNew, parameters);
        }
    }
}
