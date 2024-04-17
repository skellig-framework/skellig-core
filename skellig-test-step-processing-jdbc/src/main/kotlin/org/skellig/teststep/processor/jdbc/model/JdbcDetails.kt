package org.skellig.teststep.processor.jdbc.model

import org.skellig.teststep.processor.db.model.DatabaseDetails


/**
 * Represents the details of a JDBC connection.
 * It extends the [DatabaseDetails] class and adds the driver name and JDBC URL.
 *
 * @param serverName The name of the database server.
 * @param driverName The name of the JDBC driver.
 * @param url The JDBC URL to connect to the database server.
 * @param userName The username to connect to the database server. Can be null.
 * @param password The password to connect to the database server. Can be null.
 */
class JdbcDetails(
    serverName: String,
    val driverName: String,
    val url: String,
    userName: String?,
    password: String?
) : DatabaseDetails(serverName, userName, password) {

    override fun toString(): String {
        return "(${super.toString()}, driverName = $driverName, url = $url)"
    }
}