package com.household.application.service

import com.household.domain.model.FixedEvent
import com.household.domain.model.HouseholdId
import com.household.domain.model.RecurrenceRule
import com.household.domain.port.`in`.CreateFixedEventCommand
import com.household.domain.port.out.FixedEventRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

class FixedEventServiceTest {

    private val repo = mockk<FixedEventRepository>()
    private val service = FixedEventService(repo)

    private val monday = LocalDate.of(2026, 5, 4)

    @Test
    fun `create saves event and returns it`() {
        val command = CreateFixedEventCommand(HOUSEHOLD_ID, "Müllabfuhr", monday)
        every { repo.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals("Müllabfuhr", result.title)
        assertEquals(monday, result.date)
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `getForDate returns non-recurring event on exact date`() {
        val event = FixedEvent.create(HOUSEHOLD_ID, "Handwerker", monday)
        every { repo.findAllByHouseholdId(HOUSEHOLD_ID) } returns listOf(event)

        val result = service.getForDate(HOUSEHOLD_ID, monday)

        assertEquals(1, result.size)
    }

    @Test
    fun `getForDate excludes non-recurring event on wrong date`() {
        val event = FixedEvent.create(HOUSEHOLD_ID, "Handwerker", monday)
        every { repo.findAllByHouseholdId(HOUSEHOLD_ID) } returns listOf(event)

        val result = service.getForDate(HOUSEHOLD_ID, monday.plusDays(1))

        assertEquals(0, result.size)
    }

    @Test
    fun `getForDate includes weekly recurring event on matching day`() {
        val event = FixedEvent.create(HOUSEHOLD_ID, "Gelber Sack", monday, RecurrenceRule.Weekly)
        every { repo.findAllByHouseholdId(HOUSEHOLD_ID) } returns listOf(event)

        assertEquals(1, service.getForDate(HOUSEHOLD_ID, monday.plusWeeks(2)).size)
        assertEquals(0, service.getForDate(HOUSEHOLD_ID, monday.plusDays(1)).size)
    }

    @Test
    fun `getForWeek returns events occurring in any of the 7 days`() {
        val friday = LocalDate.of(2026, 5, 8)
        val mondayEvent = FixedEvent.create(HOUSEHOLD_ID, "Montag", monday)
        val fridayEvent = FixedEvent.create(HOUSEHOLD_ID, "Freitag", friday)
        val nextWeekEvent = FixedEvent.create(HOUSEHOLD_ID, "Nächste Woche", monday.plusWeeks(1))
        every { repo.findAllByHouseholdId(HOUSEHOLD_ID) } returns listOf(mondayEvent, fridayEvent, nextWeekEvent)

        val result = service.getForWeek(HOUSEHOLD_ID, monday)

        assertEquals(2, result.size)
        val titles = result.map { it.title }
        assert("Montag" in titles)
        assert("Freitag" in titles)
    }

    @Test
    fun `getForWeek includes recurring event that falls within week`() {
        val saturdayRule = RecurrenceRule.OnWeekday(DayOfWeek.SATURDAY)
        val event = FixedEvent.create(HOUSEHOLD_ID, "Markt", monday, saturdayRule)
        every { repo.findAllByHouseholdId(HOUSEHOLD_ID) } returns listOf(event)

        // Week Mon-Sun starting May 4 includes Saturday May 9
        val result = service.getForWeek(HOUSEHOLD_ID, monday)

        assertEquals(1, result.size)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}
