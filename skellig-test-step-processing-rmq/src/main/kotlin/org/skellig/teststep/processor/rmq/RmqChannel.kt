package org.skellig.teststep.processor.rmq

import com.rabbitmq.client.*
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.rmq.model.RmqDetails
import java.io.Closeable
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class RmqChannel(private val rmqDetails: RmqDetails) : Closeable {

    private var conn: Connection? = null
    private var channel: Channel? = null

    init {
        connectToQueue(createConnectionFactory(rmqDetails))
    }

    fun send(request: Any?, routingKey: String?) {
        try {
            channel!!.basicPublish(
                    rmqDetails.exchange.name,
                    routingKey ?: rmqDetails.queue.routingKey,
                    MessageProperties.TEXT_PLAIN,
                    convertRequestToBytes(request)
            )
        } catch (ex: Exception) {
            //log later
        }
    }

    fun read(acknowledgeResponse: Any?, timeout: Int): ByteArray? {
        val response = AtomicReference<ByteArray?>()
        try {
            val countDownLatch = CountDownLatch(1)
            channel!!.basicConsume(rmqDetails.queue.name, true,
                    createConsumer(acknowledgeResponse, response, countDownLatch))

            countDownLatch.await(timeout.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            //log later
        }
        return response.get()
    }

    private fun createConsumer(acknowledgeResponse: Any?, response: AtomicReference<ByteArray?>, countDownLatch: CountDownLatch): DefaultConsumer {
        return object : DefaultConsumer(channel) {
            override fun handleDelivery(consumerTag: String, envelope: Envelope,
                                        properties: AMQP.BasicProperties, body: ByteArray) {
                if (response.get() == null) {
                    response.set(body)
                    try {
                        acknowledgeResponse?.let { sendResponse(properties, it) }
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }
        }
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