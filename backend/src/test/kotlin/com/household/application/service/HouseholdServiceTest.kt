package com.household.application.service

import com.household.domain.model.Household
import com.household.domain.model.HouseholdId
import com.household.domain.port.out.HouseholdRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class HouseholdServiceTest {

    private val householdRepository = mockk<HouseholdRepository>()
    private val service = HouseholdService(householdRepository)

    @Test
    fun `getInviteCode returns invite code for household`() {
        val household = Household(HOUSEHOLD_ID, "ABCD1234")
        every { householdRepository.findById(HOUSEHOLD_ID) } returns household

        val result = service.getInviteCode(HOUSEHOLD_ID)

        assertEquals("ABCD1234", result)
    }

    @Test
    fun `getInviteCode throws when household not found`() {
        every { householdRepository.findById(HOUSEHOLD_ID) } returns null

        assertThrows<IllegalStateException> { service.getInviteCode(HOUSEHOLD_ID) }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}
