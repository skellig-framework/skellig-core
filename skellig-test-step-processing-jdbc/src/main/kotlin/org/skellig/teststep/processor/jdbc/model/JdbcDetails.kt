package org.skellig.teststep.processor.jdbc.model

import org.skellig.teststep.processor.db.model.DatabaseDetails

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