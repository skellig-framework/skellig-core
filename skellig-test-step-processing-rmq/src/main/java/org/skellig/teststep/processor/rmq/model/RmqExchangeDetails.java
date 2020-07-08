package org.skellig.teststep.processor.rmq.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RmqExchangeDetails {

    private String name;
    private String type;
    private boolean isDurable;
    private boolean isAutoDelete;
    private boolean createIfNew;
    private Map<String, Object> parameters;

    public RmqExchangeDetails(String name, String type, boolean isDurable,
                              boolean isAutoDelete, boolean createIfNew, Map<String, Object> parameters) {
        this.name = name;
        this.type = type;
        this.isDurable = isDurable;
        this.isAutoDelete = isAutoDelete;
        this.createIfNew = createIfNew;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isDurable() {
        return isDurable;
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
        private String type;
        private boolean isDurable;
        private boolean isAutoDelete;
        private boolean createIfNew;
        private Map<String, Object> parameters;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withDurable(boolean durable) {
            isDurable = durable;
            return this;
        }

        public Builder withAutoDelete(boolean autoDelete) {
            isAutoDelete = autoDelete;
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
            if(parameters == null){
                parameters = new HashMap<>();
            }
            parameters.put(name, value);
            return this;
        }

        public RmqExchangeDetails build() {
            if(parameters == null){
                parameters = Collections.emptyMap();
            }
            return new RmqExchangeDetails(name, type, isDurable, isAutoDelete, createIfNew, parameters);
        }
    }
}
