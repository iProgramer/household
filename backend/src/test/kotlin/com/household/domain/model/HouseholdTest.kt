package com.household.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class HouseholdTest {

    @Test
    fun `create household generates unique id`() {
        val a = Household.create()
        val b = Household.create()

        assertNotEquals(a.id, b.id)
    }

    @Test
    fun `create household generates 8-character uppercase invite code`() {
        val household = Household.create()

        assertEquals(8, household.inviteCode.length)
        assertEquals(household.inviteCode, household.inviteCode.uppercase())
    }

    @Test
    fun `invite codes are unique across households`() {
        val codes = (1..20).map { Household.create().inviteCode }.toSet()

        assertEquals(20, codes.size)
    }
}
