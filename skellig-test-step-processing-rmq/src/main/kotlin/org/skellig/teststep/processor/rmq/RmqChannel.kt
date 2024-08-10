package org.skellig.teststep.processor.rmq

import com.rabbitmq.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.rmq.model.RmqDetails
import java.io.Closeable
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.coroutines.CoroutineContext

/**
 * Represents a RMQ channel for sending and receiving messages.
 * The channel can also consume incoming messages and respond to them asynchronously.
 *
 * @property rmqDetails The [RmqDetails] of the RMQ connection, exchange, and queue.
 */
class RmqChannel(private val rmqDetails: RmqDetails) : Closeable {

    private val log = logger<RmqChannel>()
    private lateinit var conn: Connection
    private lateinit var channel: Channel

    init {
        connectToQueue(createConnectionFactory(rmqDetails))
    }

    /**
     * Sends a message to the RMQ server.
     * If fails to deliver message, it logs an error.
     *
     * @param request The message to send.
     * @param routingKey The routing key for the message. When null, falls back to the default routing key from [RmqDetails.queue].
     * @param properties The additional properties for the message. When null, falls back to default properties which is
     * [MessageProperties.TEXT_PLAIN].
     */
    fun send(request: Any?, routingKey: String?, properties: AMQP.BasicProperties? = null) {
        try {
            channel.basicPublish(
                rmqDetails.exchange.name, routingKey ?: rmqDetails.queue.routingKey, properties ?: MessageProperties.TEXT_PLAIN, convertRequestToBytes(request)
            )
        } catch (ex: Exception) {
            log.error("Failed to send a message to RMQ '$rmqDetails'", ex)
        }
    }

    /**
     * Reads a message from the RMQ server and returns it as a byte array.
     * If fails to read a message or send an ack response, it logs an error.
     *
     * @param acknowledgeResponse The response object to acknowledge with. If provided, the method will send the response
     *                            using the properties from the received message. If not provided, the method will
     *                            acknowledge the message by calling `channel.basicAck()` with the delivery tag of the
     *                            received message.
     *
     * @return The byte array representing the message body, or null if no message was received.
     */
    fun read(acknowledgeResponse: Any?): ByteArray? {
        var response: ByteArray? = null
        try {
            val msg = channel.basicGet(rmqDetails.queue.name, true)
            if (msg != null) {
                response = msg.body

                acknowledgeResponse?.let { sendResponse(msg.props, it) } ?: channel.basicAck(msg.envelope.deliveryTag, true)
            }
        } catch (e: Exception) {
            log.error("Failed to read a message from RMQ '$rmqDetails'", e)
        }
        return response
    }

    /**
     * Listens for messages from the RMQ server in a specific queue and invokes the specified callback function for each received message.
     * The callback is called asynchronously then right after it - ack response is sent.
     *
     * @param acknowledgeResponse The response object to acknowledge with. If provided, the method will send the response using the properties from the received message. If not provided,
     * the method will acknowledge the message by calling `channel.basicAck()` with the delivery tag of the received message.
     * @param callback The function to be invoked for each received message. The function takes one parameter, `message`, which represents the received message.
     * @return The consumer tag if the consume operation is successful, otherwise null.
     */
    fun consume(acknowledgeResponse: Any?, callback: (message: ByteArray) -> Unit): String? {
        log.info("Start listener for RMQ queue: ${rmqDetails.queue.id}")
        return channel.basicConsume(rmqDetails.queue.name, acknowledgeResponse == null, 
            RmqConsumer(channel, acknowledgeResponse, callback))
    }

    private fun sendResponse(properties: AMQP.BasicProperties, message: Any) {
        try {
            channel.basicPublish(
                "", properties.replyTo, properties, convertRequestToBytes(message)
            )
            log.debug { "Response has been sent to RMQ '${properties.replyTo}': $message" }
        } catch (e: IOException) {
            log.error("Failed to reply to '${properties.replyTo}' with message: $message", e)
        }
    }

    private fun convertRequestToBytes(request: Any?): ByteArray {
        return if (request is ByteArray) {
            request
        } else {
            request?.toString()?.toByteArray(StandardCharsets.UTF_8) ?: byteArrayOf()
        }
    }

    private fun createConnectionFactory(rmqDetails: RmqDetails): ConnectionFactory {
        log.debug { "Start to connect to RMQ queue '$rmqDetails'" }
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
            channel = conn.createChannel()
            val exchange = rmqDetails.exchange
            if (exchange.isCreateIfNew) {
                channel.exchangeDeclare(
                    exchange.name, exchange.type, exchange.isDurable, exchange.isAutoDelete, exchange.parameters
                )
            }
            val queueDetails = rmqDetails.queue
            if (queueDetails.isCreateIfNew) {
                channel.queueDeclare(
                    queueDetails.name, queueDetails.isDurable, queueDetails.isExclusive, queueDetails.isAutoDelete, queueDetails.parameters
                )
            }
            channel.queueBind(queueDetails.name, exchange.name, queueDetails.routingKey)

            log.debug { "Successfully connected to RMQ queue '$rmqDetails'" }
        } catch (e: Exception) {
            throw TestStepProcessingException(e.message, e)
        }
    }

    /**
     * Closes the RMQ queue, channel, and connection.
     * If an exception occurs during the process, it logs a warning message.
     */
    override fun close() {
        try {
            log.debug { "Closing RMQ queue '$rmqDetails'" }
            channel.close()
            conn.close()
        } catch (e: Exception) {
            log.warn("Could not manually close RMQ channel '$rmqDetails'. Reason: ${e.message}")
        }
    }

    private inner class RmqConsumer(
        channel: Channel,
        private val acknowledgeResponse: Any?,
        callback: (message: ByteArray) -> Unit
    ) : DefaultConsumer(channel) {
        private val consumerCallbackJob = ConsumerCallbackJob(callback)

        override fun handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: ByteArray) {
            consumerCallbackJob.execute(body)

            acknowledgeResponse?.let {
                log.debug { "Reply with '$it' to RMQ queue '${rmqDetails.queue.id}'" }
                sendResponse(properties, it)
            }
        }
    }

    private class ConsumerCallbackJob(val callback: (message: ByteArray) -> Unit) : CoroutineScope {
        private var job: Job = Job()

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        fun execute(body: ByteArray) = launch {
            callback(body)
        }
    }

}