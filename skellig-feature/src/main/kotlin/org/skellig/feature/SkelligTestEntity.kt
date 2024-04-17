package org.skellig.feature

/**
 * The SkelligTestEntity interface represents a generic entity for a Skellig Test.
 * It provides methods to retrieve the ID, name, and tags of the entity.
 *
 * Main implementations but not limited to: [Feature], [TestScenario] and [TestStep].
 */
interface SkelligTestEntity {

    /**
     * Retrieves the ID of the entity. It is used by runners (ex. jUnit runner) to identify a unique Feature, Test Scenario
     * or Test Step
     *
     * By default, it is a hashcode.
     */
    fun getId(): Int = hashCode()

    /**
     * Retrieves the name of the entity.
     */
    fun getEntityName(): String

    /**
     * Retrieves the tags associated with the entity.
     *
     * @return The set of tags associated with the entity, or null if there are no tags.
     */
    fun getEntityTags(): Set<String>? = null
}