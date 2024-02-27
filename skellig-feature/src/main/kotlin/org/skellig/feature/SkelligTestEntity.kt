package org.skellig.feature

interface SkelligTestEntity {

    fun getEntityName(): String

    fun getEntityTags(): Set<String>? = null
}