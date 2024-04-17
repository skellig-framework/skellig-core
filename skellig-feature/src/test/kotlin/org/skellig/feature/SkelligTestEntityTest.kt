package org.skellig.feature

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SkelligTestEntityTest {

    private val entity = object : SkelligTestEntity {
        override fun getEntityName(): String {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `test default impl of getId is a hashcode of the instance`() {
        assertEquals(entity.hashCode(), entity.getId())
    }

    @Test
    fun `test default impl of getEntityTags is returning null`() {
        assertNull(entity.getEntityTags())
    }
}