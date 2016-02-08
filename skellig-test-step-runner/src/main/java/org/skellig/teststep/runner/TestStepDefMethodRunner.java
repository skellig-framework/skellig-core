package org.skellig.teststep.runner;

import org.skellig.teststep.runner.exception.TestStepMethodInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TestStepDefMethodRunner {

    void invoke(String testStepName, ClassTestStepsRegistry.TestStepDefDetails testStepDefDetails,
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

        Object testStepDefInstance = testStepDefDetails.getTestStepDefInstance();

        try {
            testStepMethod.invoke(testStepDefInstance, methodParameters);
        } catch (IllegalAccessException e) {
            throw new TestStepMethodInvocationException("Unexpected failure when running a test step method", e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e;
            if (e.getTargetException() != null) {
                targetException = e.getTargetException();
            }
            throw new TestStepMethodInvocationException(targetException.getMessage(), targetException);
        }
    }
}
