package org.skellig.teststep.processor.db.model

open class DatabaseDetails(val serverName: String,
                           val userName: String?,
                           val password: String?) {
    override fun toString(): String {
        return "serverName = '$serverName', userName = $userName, password = $password"
    }
}