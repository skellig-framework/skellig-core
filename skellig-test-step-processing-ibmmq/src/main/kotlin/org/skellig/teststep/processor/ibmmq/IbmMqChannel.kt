package org.skellig.teststep.processor.ibmmq

import com.ibm.mq.*
import com.ibm.mq.constants.CMQC
import com.ibm.mq.constants.MQConstants
import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.ibmmq.model.IbmMqManagerDetails
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class IbmMqChannel(private val ibmMqQueueDetails: IbmMqQueueDetails) : Closeable {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(IbmMqChannel::class.java)

        private const val DEFAULT_MSG_EXPIRY = 100
        private val queueManagerFactory = IbmMqManagerFactory()
    }

    private var queueManager: MQQueueManager? = null
    private var queue: MQQueue? = null
    private var consumerThread: ExecutorService? = null

    init {
        connectQueue()
    }

    fun send(request: Any) {
        try {
            val mqMessage = convertMqMessage(request)
            queue!!.put(mqMessage)
            LOGGER.debug("Message sent to IBMMQ '${ibmMqQueueDetails.queueName}'")
        } catch (e: Exception) {
            LOGGER.error("Failed to send a message to IBMMQ '${ibmMqQueueDetails.queueName}'", e)
        }
    }

    fun read(timeout: Int): Any? =
            try {
                val message = MQMessage()
                val options = MQGetMessageOptions()
                options.options = MQConstants.MQGMO_WAIT
                options.waitInterval = timeout

                queue!![message, options]

                val messageBody = getMessageBody(message)

                LOGGER.debug("Received message from IBMMQ '${ibmMqQueueDetails.queueName}'")

                messageBody
            } catch (e: Exception) {
                LOGGER.error("Failed to read a message from IBMMQ '${ibmMqQueueDetails.queueName}'", e)
                null
            }

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
        LOGGER.info("Start listener for IbmMq queue: ${ibmMqQueueDetails.queueName}")
        consumerThread?.execute {
            while (!consumerThread!!.isShutdown) {
                val data = read(timeout)
                responseHandler(data)
                response?.let { send(it) }
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
            queueManager = queueManagerFactory.createQueueManagerFromDetails(ibmMqQueueDetails.ibmMqManagerDetails)
            queue = queueManager!!.accessQueue(ibmMqQueueDetails.queueName,
                    CMQC.MQOO_OUTPUT or CMQC.MQOO_INQUIRE or CMQC.MQOO_INPUT_SHARED)
        } catch (e: MQException) {
            throw TestStepProcessingException(e.message, e)
        }
    }

    override fun close() {
        try {
            consumerThread?.shutdownNow()
            queueManager!!.disconnect()
            queueManager!!.close()
            queue!!.close()
        } catch (e: Exception) {
            LOGGER.warn("Could not safely close IBMMQ channel ${ibmMqQueueDetails.queueName}", e)
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
                throw TestStepProcessingException(String.format("Could not connect to queue manager: %s", mqManagerDetails.name), e)
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