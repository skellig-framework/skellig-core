package org.skellig.teststep.processor.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.cassandra.model.CassandraDetails;
import org.skellig.teststep.processor.db.model.DatabaseRequest;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.InetSocketAddress;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class CassandraRequestExecutorCT {

    private static final String TABLE = "books.book";
    private static final String USER_NAME = "cassandra";
    private static final String PASSWORD = "cassandra";

    @Container
    private CassandraContainer cassandraContainer =
            new CassandraContainer<>("cassandra:4.0")
                    .withExposedPorts(9042);

    @AfterEach
    void tearDown() {
        cassandraContainer.close();
    }

    @Test
    @DisplayName("Insert and read data from table Then verify response is correct")
    void testInsertAndReadFromDb() {
        initDatabase();

        CassandraRequestExecutor requestExecutor = new CassandraRequestExecutor(createCassandraDetails());

        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", 1);
        insertParams.put("date_created", LocalDateTime.now());
        insertParams.put("name", "test");

        requestExecutor.execute(new DatabaseRequest("insert", TABLE, insertParams));

        Map<String, Object> selectParams = new HashMap<>();
        selectParams.put("id", 1);
        Object response = requestExecutor.execute(new DatabaseRequest("select", TABLE, selectParams));

        Map row = (Map) ((List) response).get(0);
        assertAll(
                () -> assertEquals(insertParams.get("id"), row.get("id")),
                () -> assertEquals(Date.from(((LocalDateTime) insertParams.get("date_created")).toInstant(ZoneOffset.UTC)), row.get("date_created")),
                () -> assertEquals(insertParams.get("name"), row.get("name"))
        );
    }

    private void initDatabase() {
        Cluster cluster = cassandraContainer.getCluster();

        try (Session session = cluster.connect()) {

            session.execute("CREATE KEYSPACE IF NOT EXISTS books WITH replication = \n" +
                    "{'class':'SimpleStrategy','replication_factor':'1'};");

            session.execute("create table books.book" +
                    "(" +
                    "id int primary key," +
                    "date_created timestamp," +
                    "name text" +
                    ")" +
                    "with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}");
        }
    }

    private CassandraDetails createCassandraDetails() {
        return new CassandraDetails("s1", Collections.singletonList(new InetSocketAddress("localhost", cassandraContainer.getMappedPort(9042))),
                USER_NAME, PASSWORD);
    }
}