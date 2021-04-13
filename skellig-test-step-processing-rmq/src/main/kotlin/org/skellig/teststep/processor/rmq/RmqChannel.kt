package org.skellig.teststep.processor.rmq

import com.rabbitmq.client.*
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.rmq.model.RmqDetails
import java.io.Closeable
import java.io.IOException
import java.nio.charset.StandardCharsets

class RmqChannel(private val rmqDetails: RmqDetails) : Closeable {

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
        } catch (ex: Exception) {
            //log later
        }
    }

    fun read(acknowledgeResponse: Any?): ByteArray? {
        var response: ByteArray? = null
        try {
            val msg = channel!!.basicGet(rmqDetails.queue.name, true)
            if (msg != null) {
                response = msg.body

                acknowledgeResponse?.let { sendResponse(msg.props, it) }
                        ?: channel!!.basicAck(msg.envelope.deliveryTag, true)
            }
        } catch (e: Exception) {
            //log later
        }
        return response
    }

    private fun sendResponse(properties: AMQP.BasicProperties, message: Any) {
        try {
            channel!!.basicPublish(
                    "",
                    properties.replyTo,
                    properties,
                    convertRequestToBytes(message)
            )
        } catch (e: IOException) {
            // log later
            e.printStackTrace()
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
        } catch (e: Exception) {
            throw TestStepProcessingException(e.message, e)
        }
    }

    override fun close() {
        try {
            channel!!.close()
            conn!!.close()
        } catch (e: Exception) {
            // log later
        }
    }
}