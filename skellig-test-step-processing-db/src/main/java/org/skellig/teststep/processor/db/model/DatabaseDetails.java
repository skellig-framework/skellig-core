package org.skellig.teststep.processor.db.model;

import java.util.Optional;

public class DatabaseDetails {

    private String serverName;
    private String userName;
    private String password;

    public DatabaseDetails() {
    }

    public DatabaseDetails(String serverName, String userName, String password) {
        this.serverName = serverName;
        this.userName = userName;
        this.password = password;
    }

    public String getServerName() {
        return serverName;
    }

    public Optional<String> getUserName() {
        return Optional.ofNullable(userName);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }
}
