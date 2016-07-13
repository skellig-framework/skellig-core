package org.skellig.teststep.processor.rmq;

import com.typesafe.config.Config;
import org.skellig.teststep.processor.rmq.model.RmqDetails;
import org.skellig.teststep.processor.rmq.model.RmqExchangeDetails;
import org.skellig.teststep.processor.rmq.model.RmqHostDetails;
import org.skellig.teststep.processor.rmq.model.RmqQueueDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


class RmqDetailsConfigReader {

    private static final String RMQ_CONFIG_KEYWORD = "rmq";

    Collection<RmqDetails> read(Config config) {
        Objects.requireNonNull(config, "RMQ config cannot be null");

        Collection<RmqDetails> rmqDetails = Collections.emptyList();
        if (config.hasPath(RMQ_CONFIG_KEYWORD)) {
            List<Map> anyRefList = (List<Map>) config.getAnyRefList(RMQ_CONFIG_KEYWORD);
            rmqDetails = anyRefList.stream()
                    .map(this::createRmqDetails)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return rmqDetails;
    }

    private Collection<RmqDetails> createRmqDetails(Map rawRmqDetails) {
        RmqHostDetails hostDetails = createRmqHostDetails(rawRmqDetails);
        List<Map> queues = (List<Map>) rawRmqDetails.get("queues");
        Map<String, RmqExchangeDetails> exchanges = createExchanges(rawRmqDetails);

        return queues.stream()
                .map(queue -> createQueueDetails(queue, exchanges, hostDetails))
                .collect(Collectors.toList());
    }

    private RmqHostDetails createRmqHostDetails(Map rawRmqDetails) {
        String host = (String) rawRmqDetails.get("host");
        int port = (int) rawRmqDetails.get("port");
        String user = (String) rawRmqDetails.get("username");
        String password = (String) rawRmqDetails.get("password");
        return new RmqHostDetails(host, port, user, password);
    }

    private Map<String, RmqExchangeDetails> createExchanges(Map rawExchangesDetails) {
        List<Map> exchanges = (List<Map>) rawExchangesDetails.get("exchanges");

        Objects.requireNonNull(exchanges, "No exchanges were declared for RMQ");

        return exchanges.stream()
                .map(this::createExchange)
                .collect(Collectors.toMap(RmqExchangeDetails::getName, e -> e));
    }

    private RmqExchangeDetails createExchange(Map rawExchangeDetails) {
        String name = (String) rawExchangeDetails.get("name");
        String type = (String) rawExchangeDetails.get("type");

        Objects.requireNonNull(name, "Name was not declared for RMQ Exchange");

        return new RmqExchangeDetails.Builder()
                .withName(name)
                .withType(type)
                .withDurable(extractIsDurable(rawExchangeDetails))
                .withAutoDelete(extractIsAutoDelete(rawExchangeDetails))
                .withCreateIfNew(extractCreateIfNew(rawExchangeDetails))
                .withParameters(extractParameters(rawExchangeDetails))
                .build();
    }

    private RmqDetails createQueueDetails(Map item, Map<String, RmqExchangeDetails> exchanges,
                                          RmqHostDetails hostDetails) {
        String channelId = (String) item.get("channelId");
        String name = (String) item.get("name");
        String exchange = (String) item.get("exchange");

        Objects.requireNonNull(channelId, "Channel ID was not declared for RMQ details. " +
                "It can be any unique name which you would use in tests as a reference");
        Objects.requireNonNull(name, "Queue name was not declared for RMQ details");
        Objects.requireNonNull(exchange, "Exchange name was not declared for RMQ details");
        Objects.requireNonNull(exchanges.get(exchange), String.format("No exchange name '%s' was declared", exchange));

        RmqQueueDetails queue = new RmqQueueDetails.Builder()
                .withName(name)
                .withRoutingKey(extractRoutingKey(item))
                .withDurable(extractIsDurable(item))
                .withAutoDelete(extractIsAutoDelete(item))
                .withCreateIfNew(extractCreateIfNew(item))
                .withExclusive(extractIsExclusive(item))
                .withParameters(extractParameters(item))
                .build();

        return new RmqDetails.Builder()
                .withChannelId(channelId)
                .withHostDetails(hostDetails)
                .withQueue(queue)
                .withExchange(exchanges.get(exchange))
                .build();
    }

    private String extractRoutingKey(Map rawQueueDetails) {
        String routingKey = (String) rawQueueDetails.get("routingKey");
        return routingKey == null ? "#" : routingKey;
    }

    private boolean extractIsExclusive(Map rawQueueDetails) {
        Boolean isExclusive = (Boolean) rawQueueDetails.get("exclusive");
        return isExclusive == null ? false : isExclusive;
    }

    private boolean extractIsAutoDelete(Map rawQueueDetails) {
        Boolean autoDelete = (Boolean) rawQueueDetails.get("autoDelete");
        return autoDelete == null ? false : autoDelete;
    }

    private boolean extractCreateIfNew(Map rawQueueDetails) {
        Boolean create = (Boolean) rawQueueDetails.get("create");
        return create == null ? false : create;
    }

    private boolean extractIsDurable(Map rawQueueDetails) {
        Boolean isDurable = (Boolean) rawQueueDetails.get("durable");
        return isDurable == null ? false : isDurable;
    }

    private Map<String, Object> extractParameters(Map rawQueueDetails) {
        Map<String, Object> parameters = (Map<String, Object>) rawQueueDetails.get("parameters");
        return parameters == null ? Collections.emptyMap() : parameters;
    }
}
