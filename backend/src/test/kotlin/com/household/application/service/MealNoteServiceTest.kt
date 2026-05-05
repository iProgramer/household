package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.MealNote
import com.household.domain.port.`in`.SetMealNoteCommand
import com.household.domain.port.out.MealNoteRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class MealNoteServiceTest {

    private val repo = mockk<MealNoteRepository>()
    private val service = MealNoteService(repo)

    private val today = LocalDate.now()

    @Test
    fun `set saves meal note and returns it`() {
        val command = SetMealNoteCommand(HOUSEHOLD_ID, today, "Pasta Bolognese")
        every { repo.save(any()) } answers { firstArg() }

        val result = service.set(command)

        assertEquals("Pasta Bolognese", result.note)
        assertEquals(today, result.date)
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `getForDate returns note when present`() {
        val note = MealNote(HOUSEHOLD_ID, today, "Salat")
        every { repo.findByHouseholdIdAndDate(HOUSEHOLD_ID, today) } returns note

        assertEquals("Salat", service.getForDate(HOUSEHOLD_ID, today)?.note)
    }

    @Test
    fun `getForDate returns null when absent`() {
        every { repo.findByHouseholdIdAndDate(HOUSEHOLD_ID, today) } returns null

        assertNull(service.getForDate(HOUSEHOLD_ID, today))
    }

    @Test
    fun `getForWeek delegates with correct date range`() {
        val start = LocalDate.of(2026, 5, 4)
        every { repo.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, start, start.plusDays(6)) } returns emptyList()

        service.getForWeek(HOUSEHOLD_ID, start)

        verify { repo.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, start, start.plusDays(6)) }
    }

    @Test
    fun `getForWeek returns sparse list of present notes`() {
        val start = LocalDate.of(2026, 5, 4)
        val notes = listOf(
            MealNote(HOUSEHOLD_ID, start, "Pizza"),
            MealNote(HOUSEHOLD_ID, start.plusDays(3), "Suppe"),
        )
        every { repo.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, start, start.plusDays(6)) } returns notes

        val result = service.getForWeek(HOUSEHOLD_ID, start)

        assertEquals(2, result.size)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}
