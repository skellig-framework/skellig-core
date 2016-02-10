package org.skellig.connection.database.model;

import org.skellig.connection.channel.model.BaseChannelDetails;

public class DatabaseChannelDetails extends BaseChannelDetails {

    private String driverName;
    private String url;
    private String userName;
    private String password;


    public DatabaseChannelDetails(String driverName, String url, String userName, String password) {
        super(url);
        this.driverName = driverName;
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
