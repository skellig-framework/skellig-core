package org.skellig.teststep.runner;

import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.runner.exception.TestStepMethodInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TestStepDefMethodRunner {

    TestStepProcessor.TestStepRunResult invoke(String testStepName, ClassTestStepsRegistry.TestStepDefDetails testStepDefDetails,
                                               Map<String, String> parameters) throws TestStepMethodInvocationException {
        Method testStepMethod = testStepDefDetails.getTestStepMethod();
        Pattern testStepNamePattern = testStepDefDetails.getTestStepNamePattern();
        Matcher matcher = testStepNamePattern.matcher(testStepName);
        Object[] methodParameters = new Object[testStepMethod.getParameterCount()];
        int index = 0;
        if (matcher.find() && matcher.groupCount() > 0) {
            index += 1;
            methodParameters[index - 1] = matcher.group(index);
            if (matcher.groupCount() < testStepMethod.getParameterCount()) {
                methodParameters[index] = parameters;
            }
        }

        TestStepProcessor.TestStepRunResult result = new TestStepProcessor.TestStepRunResult(null);
        Object testStepDefInstance = testStepDefDetails.getTestStepDefInstance();
        Object response = null;
        TestStepMethodInvocationException error = null;
        try {
            response = testStepMethod.invoke(testStepDefInstance, methodParameters);
        } catch (IllegalAccessException e) {
            error = new TestStepMethodInvocationException("Unexpected failure when running a test step method", e);
            throw error;
        } catch (InvocationTargetException e) {
            Throwable targetException = e;
            if (e.getTargetException() != null) {
                targetException = e.getTargetException();
            }
            error = new TestStepMethodInvocationException(targetException.getMessage(), targetException);
            throw error;
        } finally {
            result.notify(response, error);
        }
        return result;
    }
}
