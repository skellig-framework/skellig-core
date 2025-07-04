package org.skellig.feature.event

/**
 * ExecutionSequenceType is an enum class that represents the types of TestStep execution:
 * - NORMAL - to be run in a test scenario
 * - BEFORE - to be run before a test scenario
 * - AFTER - to be run after a test scenario
 */
enum class ExecutionSequenceType {
    NORMAL,
    BEFORE,
    AFTER
}