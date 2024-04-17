package org.skellig.teststep.processor.db.model

/**
 * Represents the details of a database server.
 * It contains the server name, username, and password.
 *
 * @param serverName The name of the database server.
 * @param userName The username to connect to the database server. Can be null.
 * @param password The password to connect to the database server. Can be null.
 */
open class DatabaseDetails(val serverName: String,
                           val userName: String?,
                           val password: String?) {
    override fun toString(): String {
        return "serverName = '$serverName', userName = $userName, password = $password"
    }
}