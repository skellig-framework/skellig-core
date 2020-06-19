package org.skellig.runner;

import org.skellig.teststep.runner.context.SkelligTestContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SkelligOptions {

    String[] features();

    String[] testSteps();

    Class<? extends SkelligTestContext> context() default SkelligTestContext.class;
}
