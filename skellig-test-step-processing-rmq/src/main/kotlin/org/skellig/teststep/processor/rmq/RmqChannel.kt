package org.skellig.teststep.processor.rmq

import com.rabbitmq.client.*
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.rmq.model.RmqDetails
import java.io.Closeable
import java.io.IOException
import java.nio.charset.StandardCharsets
import com.rabbitmq.client.AMQP

import com.rabbitmq.client.DefaultConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class RmqChannel(private val rmqDetails: RmqDetails) : Closeable {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(RmqChannel::class.java)
    }

    private var conn: Connection? = null
    private var channel: Channel? = null

    init {
        connectToQueue(createConnectionFactory(rmqDetails))
    }

    fun send(request: Any?, routingKey: String?, properties: AMQP.BasicProperties? = null) {
        try {
            channel!!.basicPublish(
                rmqDetails.exchange.name,
                routingKey ?: rmqDetails.queue.routingKey,
                properties ?: MessageProperties.TEXT_PLAIN,
                convertRequestToBytes(request)
            )
            LOGGER.debug("Message sent to RMQ '$rmqDetails': $request")
        } catch (ex: Exception) {
            LOGGER.error("Failed to send a message to RMQ '$rmqDetails'", ex)
        }
    }

    fun read(acknowledgeResponse: Any?): ByteArray? {
        var response: ByteArray? = null
        try {
            val msg = channel!!.basicGet(rmqDetails.queue.name, true)
            if (msg != null) {
                response = msg.body
                LOGGER.debug("Received message from RMQ '{}': {}", rmqDetails, response)

                acknowledgeResponse?.let { sendResponse(msg.props, it) }
                    ?: channel!!.basicAck(msg.envelope.deliveryTag, true)
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to read a message from RMQ '$rmqDetails'", e)
        }
        return response
    }

    fun consume(acknowledgeResponse: Any?, callback: (message: Any) -> Unit): String? {
        return channel!!.basicConsume(
            rmqDetails.queue.name, acknowledgeResponse == null,
            object : DefaultConsumer(channel) {
                override fun handleDelivery(consumerTag: String, envelope: Envelope,
                                            properties: AMQP.BasicProperties, body: ByteArray) {
                    callback(body)
                    acknowledgeResponse?.let { sendResponse(properties, it) }
                }
            })
    }

    private fun sendResponse(properties: AMQP.BasicProperties, message: Any) {
        try {
            channel!!.basicPublish(
                "",
                properties.replyTo,
                properties,
                convertRequestToBytes(message)
            )
            LOGGER.debug("Response has been sent to RMQ '${properties.replyTo}': $message")
        } catch (e: IOException) {
            LOGGER.error("Failed to respond with message to RMQ '${properties.replyTo}': $message", e)
        }
    }

    private fun convertRequestToBytes(request: Any?): ByteArray {
        return if (request is ByteArray) {
            request
        } else {
            request.toString().toByteArray(StandardCharsets.UTF_8)
        }
    }

    private fun createConnectionFactory(rmqDetails: RmqDetails): ConnectionFactory {
        val hostDetails = rmqDetails.hostDetails
        val factory = ConnectionFactory()
        factory.username = hostDetails.user
        factory.password = hostDetails.password
        factory.host = hostDetails.host
        factory.port = hostDetails.port

        return factory
    }

    private fun connectToQueue(connectionFactory: ConnectionFactory) {
        try {
            conn = connectionFactory.newConnection()
            channel = conn!!.createChannel()
            val exchange = rmqDetails.exchange
            if (exchange.isCreateIfNew) {
                channel!!.exchangeDeclare(exchange.name,
                                          exchange.type,
                                          exchange.isDurable,
                                          exchange.isAutoDelete,
                                          exchange.parameters)
            }
            val queueDetails = rmqDetails.queue
            if (queueDetails.isCreateIfNew) {
                channel!!.queueDeclare(queueDetails.name,
                                       queueDetails.isDurable,
                                       queueDetails.isExclusive,
                                       queueDetails.isAutoDelete,
                                       queueDetails.parameters)
            }
            channel!!.queueBind(queueDetails.name, exchange.name, queueDetails.routingKey)

            LOGGER.info("Connected to RMQ channel '$rmqDetails'")
        } catch (e: Exception) {
            throw TestStepProcessingException(e.message, e)
        }
    }

    override fun close() {
        try {
            channel!!.close()
            conn!!.close()
        } catch (e: Exception) {
            LOGGER.warn("Could not manually close RMQ channel '$rmqDetails'. Reason: ${e.message}")
        }
    }
}