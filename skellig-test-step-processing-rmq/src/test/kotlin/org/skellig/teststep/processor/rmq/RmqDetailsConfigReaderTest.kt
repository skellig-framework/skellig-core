package org.skellig.teststep.processor.rmq;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.rmq.model.RmqDetails;
import org.skellig.teststep.processor.rmq.model.RmqHostDetails;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Read RMQ details")
class RmqDetailsConfigReaderTest {

    private RmqDetailsConfigReader rmqDetailsConfigReader;

    @BeforeEach
    public void setUp() {
        rmqDetailsConfigReader = new RmqDetailsConfigReader();
    }

    @Test
    @DisplayName("xWhen null provided Then throw exception")
    public void testReadRmqDetailsWhenConfigIsNull() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> rmqDetailsConfigReader.read(null));

        assertEquals("RMQ config cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("When valid config provided Then verify all read correctly")
    public void testReadRmqDetails() {
        Config config = ConfigFactory.load("rmq-details.conf");

        Collection<RmqDetails> mqDetails = rmqDetailsConfigReader.read(config);

        assertEquals(3, mqDetails.size());
        assertTrue(mqDetails.stream()
                .anyMatch(item -> {
                    RmqHostDetails hostDetails = item.getHostDetails();
                    return item.getExchange().getName().equals("exchange1") &&
                            item.getExchange().getType().equals("topic") &&
                            item.getExchange().isCreateIfNew() &&
                            item.getExchange().isAutoDelete() &&
                            item.getExchange().isDurable() &&

                            item.getChannelId().equals("Q1") &&

                            item.getQueue().getName().equals("queue1") &&
                            item.getQueue().getRoutingKey().equals("any") &&
                            item.getQueue().isCreateIfNew() &&
                            item.getQueue().isAutoDelete() &&
                            item.getQueue().isDurable() &&
                            item.getQueue().isExclusive() &&

                            hostDetails.getHost().equals("localhost") &&
                            hostDetails.getPort() == 5672 &&

                            hostDetails.getUser().equals("usr1") &&
                            hostDetails.getPassword().equals("pswd1");
                }));

        assertTrue(mqDetails.stream()
                .anyMatch(item -> {
                    RmqHostDetails hostDetails = item.getHostDetails();
                    return item.getChannelId().equals("Q2") &&
                            item.getQueue().getName().equals("queue1") &&
                            item.getQueue().getRoutingKey().equals("#") &&
                            !item.getQueue().isCreateIfNew() &&
                            !item.getQueue().isAutoDelete() &&
                            !item.getQueue().isDurable() &&
                            !item.getQueue().isExclusive() &&

                            item.getExchange().getName().equals("exchange2") &&

                            hostDetails.getHost().equals("localhost") &&
                            hostDetails.getPort() == 5673 &&

                            hostDetails.getUser().equals("usr2") &&
                            hostDetails.getPassword().equals("pswd2");
                }));

        assertTrue(mqDetails.stream()
                .anyMatch(item -> {
                    RmqHostDetails hostDetails = item.getHostDetails();
                    return item.getChannelId().equals("Q3") &&
                            item.getQueue().getName().equals("queue2") &&

                            item.getExchange().getName().equals("exchange2") &&
                            item.getExchange().getType() == null &&

                            hostDetails.getHost().equals("localhost") &&
                            hostDetails.getPort() == 5673 &&

                            hostDetails.getUser().equals("usr2") &&
                            hostDetails.getPassword().equals("pswd2");
                }));
    }

}