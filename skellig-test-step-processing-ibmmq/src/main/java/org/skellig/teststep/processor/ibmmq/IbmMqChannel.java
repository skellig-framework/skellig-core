package org.skellig.teststep.processor.ibmmq;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import org.apache.commons.lang3.StringUtils;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.ibmmq.model.IbmMqManagerDetails;
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.skellig.task.TaskUtils.runTask;

class IbmMqChannel implements AutoCloseable {

    private static final int DEFAULT_MSG_EXPIRY = 100;
    private static final IbmMqManagerFactory queueManagerFactory = new IbmMqManagerFactory();

    private IbmMqQueueDetails ibmMqQueueDetails;
    private MQQueueManager queueManager;
    private MQQueue queue;

    public IbmMqChannel(IbmMqQueueDetails ibmMqQueueDetails) {
        this.ibmMqQueueDetails = ibmMqQueueDetails;
        connectQueue();
    }

    void send(Object request) {
        try {
            MQMessage mqMessage = convertMqMessage(request);
            queue.put(mqMessage);
        } catch (Exception e) {
            throw new TestStepProcessingException(e.getMessage(), e);
        }
    }

    Object read(int periodicDelay, int timeout) {
        try {
            MQMessage message = new MQMessage();

            runTask(() -> {
                queue.get(message);
                return null;
            }, r -> {
                try {
                    return message.getDataLength() > 0 || !queue.isOpen();
                } catch (IOException e) {
                    return false;
                }
            }, periodicDelay, timeout);

            return getMessageBody(message);
        } catch (Exception ex) {
            return null;
        }
    }

    private MQMessage convertMqMessage(Object request) throws IOException {
        MQMessage mqMessage = new MQMessage();
        mqMessage.expiry = DEFAULT_MSG_EXPIRY;
        mqMessage.format = MQConstants.MQFMT_STRING;

        if (request instanceof String) {
            mqMessage.writeString((String) request);
        } else {
            mqMessage.writeObject(request);
        }

        return mqMessage;
    }

    private byte[] getMessageBody(MQMessage message) throws IOException {
        byte[] buffer = new byte[message.getDataLength()];
        message.readFully(buffer);
        return buffer;
    }

    private void connectQueue() {
        try {
            queueManager = queueManagerFactory.createQueueManagerFromDetails(ibmMqQueueDetails.getIbmMqManagerDetails());
            queue = queueManager.accessQueue(ibmMqQueueDetails.getQueueName(),
                    CMQC.MQOO_OUTPUT | CMQC.MQOO_INQUIRE | CMQC.MQOO_INPUT_SHARED);
        } catch (MQException e) {
            throw new TestStepProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            queueManager.disconnect();
            queueManager.close();
            queue.close();
        } catch (Exception e) {
            //log later
        }
    }

    static class IbmMqManagerFactory {

        private final Map<String, MQQueueManager> queueManagers = new HashMap<>();

        public MQQueueManager createQueueManagerFromDetails(IbmMqManagerDetails mqManagerDetails) {
            Objects.requireNonNull(mqManagerDetails, "Queue details cannot be null");
            String mqManagerName = mqManagerDetails.getName();
            try {
                synchronized (queueManagers) {
                    if (!queueManagers.containsKey(mqManagerName) || !queueManagers.get(mqManagerName).isConnected()) {
                        MQQueueManager mqManager = createQueueManagerFromProperties(mqManagerDetails);
                        queueManagers.put(mqManagerName, mqManager);
                    }
                }
                return queueManagers.get(mqManagerName);
            } catch (MQException e) {
                throw new TestStepProcessingException(String.format("Could not connect to queue manager: %s", mqManagerDetails.getName()), e);
            }
        }

        private MQQueueManager createQueueManagerFromProperties(IbmMqManagerDetails mqManagerDetails) throws MQException {
            MQEnvironment.hostname = mqManagerDetails.getHost();
            MQEnvironment.port = mqManagerDetails.getPort();
            MQEnvironment.sharingConversations = 1;

            MQEnvironment.channel = mqManagerDetails.getChannel();
            setUserCredentialsForMQ(mqManagerDetails);
            return createQueueManager(mqManagerDetails.getName());
        }

        protected MQQueueManager createQueueManager(String name) throws MQException {
            return new MQQueueManager(name);
        }

        private void setUserCredentialsForMQ(IbmMqManagerDetails mqManagerDetails) {
            if (mqManagerDetails.getUserCredentials() != null) {
                if (StringUtils.isNotBlank(mqManagerDetails.getUserCredentials().getUsername())) {
                    MQEnvironment.userID = mqManagerDetails.getUserCredentials().getUsername();
                }

                if (StringUtils.isNotBlank(mqManagerDetails.getUserCredentials().getPassword())) {
                    MQEnvironment.password = mqManagerDetails.getUserCredentials().getPassword();
                }
            } else {
                MQEnvironment.userID = null;
                MQEnvironment.password = null;
            }
        }

    }

}
