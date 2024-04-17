package org.skellig.teststep.processing.value.function

/**
 * Interface representing an executor for Skellig functions or used in Skellig Test Step files.
 *
 * There are 3 types of functions:
 * 1) Individual - not called from a 'value' in the method [FunctionValueExecutor.execute]. These type of functions only
 * work with arguments provided in this method and ignore the 'value', thus they can be called individually from it.
 * 2) Chained - called from the 'value' provided in the method [FunctionValueExecutor.execute]. These type of functions
 *    are called for the value with arguments provided in this method. For example, in a Skellig Test Steps, these functions are called
 *    this way:
 *    ```
 *     ${propertyA}.getValues().size()
 *    ```
 *      where 'getValues' and 'size' functions are called for the specific value returned from the previous execution.
 * 3) Combination of both 1) and 2), so they can be called individually as well as for the 'value' depending on what is provided
 * in the method [FunctionValueExecutor.execute]
 */
interface FunctionValueExecutor {

    /**
     * Executes a function with the given name and arguments.
     *
     * @param name the name of the function to execute
     * @param value the value associated with the function (optional). If provided, then the function is executed as a
     * method of the 'value'.
     * @param args the arguments to pass to the function
     * @return the result of the function execution
     */
    fun execute(name: String, value: Any?, args: Array<Any?>): Any?

    /**
     * Gets the name of the function. Used in registration of [FunctionValueExecutor].
     */
    fun getFunctionName(): String
}