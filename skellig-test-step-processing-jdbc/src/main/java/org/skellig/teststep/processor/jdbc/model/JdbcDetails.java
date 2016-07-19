package org.skellig.teststep.processor.jdbc.model;

import org.skellig.teststep.processor.db.model.DatabaseDetails;

public class JdbcDetails extends DatabaseDetails {

    private String driverName;
    private String url;

    public JdbcDetails(String serverName, String driverName, String url, String userName, String password) {
        super(serverName, userName, password);
        this.driverName = driverName;
        this.url = url;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getUrl() {
        return url;
    }

}
