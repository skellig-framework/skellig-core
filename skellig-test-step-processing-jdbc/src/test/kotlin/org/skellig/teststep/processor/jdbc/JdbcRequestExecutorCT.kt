package org.skellig.teststep.processor.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.db.model.DatabaseRequest;
import org.skellig.teststep.processor.jdbc.model.JdbcDetails;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class JdbcRequestExecutorCT {

    private static final String TABLE = "skellig_info";
    private static final String USER_NAME = "skellig";
    private static final String PASSWORD = "skellig";

    @Container
    private PostgreSQLContainer mysqlContainer =
            (PostgreSQLContainer) new PostgreSQLContainer("postgres:13.1")
                    .withDatabaseName("skellig")
                    .withUsername(USER_NAME)
                    .withPassword(PASSWORD)
                    .withExposedPorts(5432)
                    .withClasspathResourceMapping("sqls/test/init.sql",
                            "/docker-entrypoint-initdb.d/init-postgres.sql", BindMode.READ_ONLY);

    @AfterEach
    void tearDown() {
        mysqlContainer.close();
    }

    @Test
    @DisplayName("Insert and read data from table Then verify response is correct")
    void testInsertAndReadFromDb() {
        String url = mysqlContainer.getJdbcUrl();
        JdbcRequestExecutor requestExecutor = new JdbcRequestExecutor(createJdbcDetails(url));

        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", 1);
        insertParams.put("create_date", LocalDate.now());
        insertParams.put("description", "test");

        requestExecutor.execute(new DatabaseRequest("insert", TABLE, insertParams));

        Map<String, Object> selectParams = new HashMap<>();
        selectParams.put("id", 1);
        Object response = requestExecutor.execute(new DatabaseRequest("select", TABLE, selectParams));

        Map row = (Map) ((List) response).get(0);
        assertAll(
                () -> assertEquals(insertParams.get("id"), row.get("id")),
                () -> assertEquals((Date.valueOf((LocalDate) insertParams.get("create_date"))), row.get("create_date")),
                () -> assertEquals(insertParams.get("description"), row.get("description"))
        );
    }

    private JdbcDetails createJdbcDetails(String url) {
        return new JdbcDetails("s1", "org.postgresql.Driver", url, USER_NAME, PASSWORD);
    }
}