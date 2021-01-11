package org.skellig.teststep.processor.jdbc

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.skellig.teststep.processor.jdbc.model.JdbcDetails
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Date
import java.time.LocalDate

@Testcontainers
internal class JdbcRequestExecutorCT {

    companion object {
        private const val TABLE = "skellig_info"
        private const val USER_NAME = "skellig"
        private const val PASSWORD = "skellig"
    }

    @Container
    private val mysqlContainer = PostgreSQLContainer<PostgreSQLContainer<*>>("postgres:13.1")
            .withDatabaseName("skellig")
            .withUsername(USER_NAME)
            .withPassword(PASSWORD)
            .withExposedPorts(5432)
            .withClasspathResourceMapping("sqls/test/init.sql",
                    "/docker-entrypoint-initdb.d/init-postgres.sql", BindMode.READ_ONLY)

    @AfterEach
    fun tearDown() {
        mysqlContainer.close()
    }

    @Test
    @DisplayName("Insert and read data from table Then verify response is correct")
    fun testInsertAndReadFromDb() {
        val url = mysqlContainer.jdbcUrl
        val requestExecutor = JdbcRequestExecutor(createJdbcDetails(url))
        val insertParams = mapOf(
                Pair("id", 1),
                Pair("create_date", LocalDate.now()),
                Pair("description", "test"))

        requestExecutor.execute(DatabaseRequest("insert", TABLE, insertParams))

        val response = requestExecutor.execute(DatabaseRequest("select", TABLE, mapOf(Pair("id", 1))))

        val row = (response as List<*>?)!![0] as Map<*, *>
        assertAll(
                { assertEquals(insertParams["id"], row["id"]) },
                { assertEquals(Date.valueOf(insertParams["create_date"] as LocalDate?), row["create_date"]) },
                { assertEquals(insertParams["description"], row["description"]) }
        )
    }

    private fun createJdbcDetails(url: String): JdbcDetails {
        return JdbcDetails("s1", "org.postgresql.Driver", url, USER_NAME, PASSWORD)
    }
}