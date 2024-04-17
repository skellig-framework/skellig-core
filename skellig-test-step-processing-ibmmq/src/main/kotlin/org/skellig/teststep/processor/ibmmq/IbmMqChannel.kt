package org.skellig.teststep.processor.ibmmq

import com.ibm.mq.*
import com.ibm.mq.constants.CMQC
import com.ibm.mq.constants.MQConstants
import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.ibmmq.model.IbmMqManagerDetails
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails
import java.io.Closeable
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Represents an IBM MQ Channel for sending and receiving messages.
 * The channel can also consume incoming messages and respond to them asynchronously.
 *
 * @property ibmMqQueueDetails The details of the IBM MQ Queue associated with this channel.
 * @constructor Creates a new instance of the `IbmMqChannel` class.
 */
open class IbmMqChannel(private val ibmMqQueueDetails: IbmMqQueueDetails) : Closeable {

    companion object {
        private const val DEFAULT_MSG_EXPIRY = 100
        private val queueManagerFactory = IbmMqManagerFactory()
    }

    private val log = logger<IbmMqTestStepProcessor>()
    private var queueManager: MQQueueManager? = null
    private var queue: MQQueue? = null
    private var consumerThread: ExecutorService? = null

    init {
        connectQueue()
    }

    /**
     * Sends a message to the IBMMQ queue.
     * It logs an error if fails to deliver message or if there is an error converting the message or putting it in the queue.
     *
     * @param request The message to send. It can be a String or any other object.
     */
    fun send(request: Any) {
        try {
            val mqMessage = convertMqMessage(request)
            queue!!.put(mqMessage)
        } catch (e: Exception) {
            log.error("Failed to send a message to IBMMQ queue '${ibmMqQueueDetails.id}'", e)
        }
    }

    /**
     * Reads a message from the IBM MQ queue with the specified timeout.
     *
     * @param timeout The timeout value in milliseconds.
     * @return The message read from the queue, or null if an error occurs.
     */
    fun read(timeout: Int): Any? =
        try {
            val message = MQMessage()
            val options = MQGetMessageOptions()
            options.options = MQConstants.MQGMO_WAIT
            options.waitInterval = timeout

            queue!![message, options]

            getMessageBody(message)
        } catch (e: Exception) {
            log.error("Failed to read a message from IBMMQ queue '${ibmMqQueueDetails.id}'", e)
            null
        }

    /**
     * Consumes messages from an IBM MQ queue and invokes a response handler for each received message.
     *
     * @param response The response message to send to the same queue on each consumed message. It can be null if no response is required.
     * @param timeout The timeout value in milliseconds for reading messages from the queue.
     * @param responseHandler The callback function to handle the received message.
     */
    fun consume(response: Any?, timeout: Int, responseHandler: (message: Any?) -> Unit) {
        /*
         TODO: consider this later
         val cf = MQQueueConnectionFactory()
         cf.hostName = ibmMqQueueDetails.ibmMqManagerDetails.host;
         cf.port = ibmMqQueueDetails.ibmMqManagerDetails.port;
         cf.queueManager = ibmMqQueueDetails.ibmMqManagerDetails.name;
         cf.channel = ibmMqQueueDetails.ibmMqManagerDetails.channel;
         cf.transportType = WMQConstants.WMQ_CM_CLIENT;
         val conn = cf.createQueueConnection() as MQQueueConnection
         val session = conn.createSession(false, 1) as MQQueueSession

         val queue = session.createQueue(ibmMqQueueDetails.queueName)

         val receiver = session.createReceiver(queue) as MQQueueReceiver

         receiver.messageListener = MessageListener {
             responseHandler(it)
             response?.let {
                 send(response)
             }
         }

         conn.start()*/

        if (consumerThread == null || consumerThread!!.isShutdown) {
            consumerThread = Executors.newCachedThreadPool()
        }
        log.info("Start listener for IBMMQ queue: ${ibmMqQueueDetails.id}")
        consumerThread?.execute {
            while (!consumerThread!!.isShutdown) {
                val data = read(timeout)
                responseHandler(data)
                response?.let {
                    log.debug { "Respond with '$response' to IBMMQ queue '${ibmMqQueueDetails.id}'" }
                    send(it)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun convertMqMessage(request: Any): MQMessage {
        val mqMessage = MQMessage()
        mqMessage.expiry = DEFAULT_MSG_EXPIRY
        mqMessage.format = MQConstants.MQFMT_STRING

        if (request is String) {
            mqMessage.writeString(request)
        } else {
            mqMessage.writeObject(request)
        }
        return mqMessage
    }

    @Throws(IOException::class)
    private fun getMessageBody(message: MQMessage): ByteArray {
        val buffer = ByteArray(message.dataLength)
        message.readFully(buffer)
        return buffer
    }

    private fun connectQueue() {
        try {
            log.debug { "Start to connect to IBMMQ queue ${ibmMqQueueDetails.id}" }
            queueManager = queueManagerFactory.createQueueManagerFromDetails(ibmMqQueueDetails.ibmMqManagerDetails)
            queue = queueManager!!.accessQueue(
                ibmMqQueueDetails.queueName,
                CMQC.MQOO_OUTPUT or CMQC.MQOO_INQUIRE or CMQC.MQOO_INPUT_SHARED
            )
            log.debug { "Successfully connected to IBMMQ queue ${ibmMqQueueDetails.id}" }
        } catch (e: MQException) {
            throw TestStepProcessingException(e.message, e)
        }
    }

    /**
     * Closes the IBMMQ channel, disconnects from the queue manager, and closes the queue.
     * If an exception occurs while closing, it logs a warning message.
     */
    override fun close() {
        try {
            consumerThread?.shutdownNow()
            queueManager!!.disconnect()
            queueManager!!.close()
            queue!!.close()
        } catch (e: Exception) {
            log.warn("Could not safely close IBMMQ queue ${ibmMqQueueDetails.id}. Reason: ${e.message}")
        }
    }

    internal class IbmMqManagerFactory {

        private val queueManagers = mutableMapOf<String, MQQueueManager>()

        fun createQueueManagerFromDetails(mqManagerDetails: IbmMqManagerDetails): MQQueueManager? {
            Objects.requireNonNull(mqManagerDetails, "Queue details cannot be null")

            val mqManagerName = mqManagerDetails.name
            return try {
                synchronized(queueManagers) {
                    if (!queueManagers.containsKey(mqManagerName) || !queueManagers[mqManagerName]!!.isConnected()) {
                        val mqManager = createQueueManagerFromProperties(mqManagerDetails)
                        queueManagers[mqManagerName] = mqManager
                    }
                }
                queueManagers[mqManagerName]
            } catch (e: MQException) {
                throw TestStepProcessingException("Could not connect to queue manager: ${mqManagerDetails.name}", e)
            }
        }

        @Throws(MQException::class)
        private fun createQueueManagerFromProperties(mqManagerDetails: IbmMqManagerDetails): MQQueueManager {
            MQEnvironment.hostname = mqManagerDetails.host
            MQEnvironment.port = mqManagerDetails.port
            MQEnvironment.sharingConversations = 1
            MQEnvironment.channel = mqManagerDetails.channel

            setUserCredentialsForMQ(mqManagerDetails)
            return createQueueManager(mqManagerDetails.name)
        }

        @Throws(MQException::class)
        private fun createQueueManager(name: String?): MQQueueManager {
            return MQQueueManager(name)
        }

        private fun setUserCredentialsForMQ(mqManagerDetails: IbmMqManagerDetails) {
            if (mqManagerDetails.userCredentials != null) {
                if (StringUtils.isNotBlank(mqManagerDetails.userCredentials.username)) {
                    MQEnvironment.userID = mqManagerDetails.userCredentials.username
                }
                if (StringUtils.isNotBlank(mqManagerDetails.userCredentials.password)) {
                    MQEnvironment.password = mqManagerDetails.userCredentials.password
                }
            } else {
                MQEnvironment.userID = null
                MQEnvironment.password = null
            }
        }
    }
}