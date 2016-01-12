package org.skellig.runner;

import org.junit.runner.RunWith;

@RunWith(SkelligRunner.class)
@SkelligOptions(
        features = {"/feature"},
        testSteps = {"/feature"}
)
public class DefaultSkelligRunner {


}
