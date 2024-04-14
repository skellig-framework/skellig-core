package org.skellig.teststep.runner.context

/**
 * SkelligTestContextAware is an interface that defines a method for setting the SkelligTestContext.
 * Classes implementing this interface can be aware of the Skellig test context, assigned by [SkelligTestContext]
 * and interact with it.
 */
interface SkelligTestContextAware {

    /**
     * Sets the [SkelligTestContext] for the object.
     * This method is called by [SkelligTestContext] on initialization (ex. [SkelligTestContext.initialize] is called).
     *
     * @param context the [SkelligTestContext] to be set
     */
    fun setSkelligTestContext(context: SkelligTestContext)
}