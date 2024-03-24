package org.skellig.feature

interface SkelligTestEntity {

    fun getId(): Int = hashCode()

    fun getEntityName(): String

    fun getEntityTags(): Set<String>? = null
}