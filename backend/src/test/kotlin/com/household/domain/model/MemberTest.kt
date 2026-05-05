package com.household.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class MemberTest {

    @Test
    fun `create member normalizes email to lowercase`() {
        val member = Member.create(HOUSEHOLD_ID, "Alice@Example.COM", "hash")

        assertEquals("alice@example.com", member.email)
    }

    @Test
    fun `create member trims email whitespace`() {
        val member = Member.create(HOUSEHOLD_ID, "  alice@example.com  ", "hash")

        assertEquals("alice@example.com", member.email)
    }

    @Test
    fun `each created member has a unique id`() {
        val a = Member.create(HOUSEHOLD_ID, "alice@example.com", "hash")
        val b = Member.create(HOUSEHOLD_ID, "bob@example.com", "hash")

        assertNotEquals(a.id, b.id)
    }

    @Test
    fun `member belongs to given household`() {
        val member = Member.create(HOUSEHOLD_ID, "alice@example.com", "hash")

        assertEquals(HOUSEHOLD_ID, member.householdId)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}
