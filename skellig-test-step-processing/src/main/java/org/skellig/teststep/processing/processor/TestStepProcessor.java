package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.model.TestStep;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public interface TestStepProcessor<T extends TestStep> extends AutoCloseable {

    TestStepRunResult process(T testStep);

    Class<T> getTestStepClass();

    @Override
    default void close() {

    }

    final class TestStepRunResult {

        private TestStep testStep;
        private Consumer<Object, RuntimeException> consumer;
        private Object response;
        private RuntimeException error;
        private CountDownLatch countDownLatch;

        public TestStepRunResult(TestStep testStep) {
            this.testStep = testStep;
            countDownLatch = new CountDownLatch(1);
        }

        public void subscribe(Consumer<Object, RuntimeException> consumer) {
            this.consumer = consumer;
            if (countDownLatch.getCount() == 0) {
                notify(response, error);
            }
        }

        public void notify(Object response, RuntimeException error) {
            this.response = response;
            this.error = error;
            countDownLatch.countDown();

            if (consumer != null) {
                consumer.accept(testStep, response, error);
            }
        }

        public void awaitResult() throws TestStepProcessingException {
            if (testStep != null) {
                try {
                    countDownLatch.await(testStep.getTimeout(), TimeUnit.SECONDS);
                    if (error != null) {
                        error = new TestStepProcessingException(
                                String.format("Failed to process test step '%s'", testStep.getName()), error);
                    }
                } catch (InterruptedException ex) {
                    error = new TestStepProcessingException(
                            String.format("Failed to get response from test step '%s' within %d seconds",
                                    testStep.getName(), testStep.getTimeout()), ex);
                    notify(null, error);
                }

                if (error != null) {
                    throw error;
                }
            }
        }

        public interface Consumer<R, E> {
            void accept(TestStep testStep, R result, E error);
        }
    }
}
