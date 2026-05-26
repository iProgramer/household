package com.household.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

class FixedEventTest {

    private val householdId = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    private val monday = LocalDate.of(2026, 5, 4) // a Monday

    @Test
    fun `create with valid data`() {
        val event = FixedEvent.create(householdId, "Müllabfuhr", monday, RecurrenceRule.Weekly)

        assertEquals("Müllabfuhr", event.title)
        assertEquals(monday, event.date)
        assertEquals(householdId, event.householdId)
    }

    @Test
    fun `create trims whitespace from title`() {
        val event = FixedEvent.create(householdId, "  Müllabfuhr  ", monday, RecurrenceRule.Weekly)
        assertEquals("Müllabfuhr", event.title)
    }

    @Test
    fun `create with blank title throws`() {
        assertThrows<IllegalArgumentException> {
            FixedEvent.create(householdId, "   ", monday, RecurrenceRule.Weekly)
        }
    }

    // occursOn – non-recurring (legacy DB records without recurrence)

    private fun eventWithoutRecurrence(title: String, date: LocalDate) =
        FixedEvent(FixedEventId(UUID.randomUUID()), householdId, title, date, recurrenceRule = null)

    @Test
    fun `non-recurring event occurs on its own date`() {
        val event = eventWithoutRecurrence("Handwerker", monday)
        assertTrue(event.occursOn(monday))
    }

    @Test
    fun `non-recurring event does not occur on other dates`() {
        val event = eventWithoutRecurrence("Handwerker", monday)
        assertFalse(event.occursOn(monday.plusDays(1)))
        assertFalse(event.occursOn(monday.minusDays(1)))
    }

    @Test
    fun `occursOn returns false for dates before start`() {
        val event = FixedEvent.create(householdId, "Termin", monday, RecurrenceRule.Daily)
        assertFalse(event.occursOn(monday.minusDays(1)))
    }

    // occursOn – Daily

    @Test
    fun `Daily recurrence occurs every day from start`() {
        val event = FixedEvent.create(householdId, "Medizin", monday, RecurrenceRule.Daily)
        assertTrue(event.occursOn(monday))
        assertTrue(event.occursOn(monday.plusDays(1)))
        assertTrue(event.occursOn(monday.plusDays(30)))
    }

    // occursOn – Weekly

    @Test
    fun `Weekly recurrence occurs every 7 days`() {
        val event = FixedEvent.create(householdId, "Gelber Sack", monday, RecurrenceRule.Weekly)
        assertTrue(event.occursOn(monday))
        assertTrue(event.occursOn(monday.plusWeeks(1)))
        assertTrue(event.occursOn(monday.plusWeeks(4)))
        assertFalse(event.occursOn(monday.plusDays(1)))
        assertFalse(event.occursOn(monday.plusDays(6)))
    }

    // occursOn – Biweekly

    @Test
    fun `Biweekly recurrence occurs every 14 days`() {
        val event = FixedEvent.create(householdId, "Papiertonne", monday, RecurrenceRule.Biweekly)
        assertTrue(event.occursOn(monday))
        assertTrue(event.occursOn(monday.plusWeeks(2)))
        assertFalse(event.occursOn(monday.plusWeeks(1)))
    }

    // occursOn – Monthly

    @Test
    fun `Monthly recurrence occurs on same day each month`() {
        val event = FixedEvent.create(householdId, "Miete", monday, RecurrenceRule.Monthly)
        assertTrue(event.occursOn(monday))
        assertTrue(event.occursOn(monday.plusMonths(1)))
        assertTrue(event.occursOn(monday.plusMonths(6)))
        assertFalse(event.occursOn(monday.plusDays(1)))
    }

    // occursOn – OnWeekday

    @Test
    fun `OnWeekday recurrence occurs every week on that weekday`() {
        val event = FixedEvent.create(householdId, "Markt", monday, RecurrenceRule.OnWeekday(DayOfWeek.MONDAY))
        assertTrue(event.occursOn(monday))
        assertTrue(event.occursOn(monday.plusWeeks(1)))
        assertTrue(event.occursOn(monday.plusWeeks(3)))
        assertFalse(event.occursOn(monday.plusDays(1))) // Tuesday
    }

    @Test
    fun `OnWeekday does not occur on other weekdays`() {
        val event = FixedEvent.create(householdId, "Sport", monday, RecurrenceRule.OnWeekday(DayOfWeek.FRIDAY))
        assertFalse(event.occursOn(monday)) // Monday
        assertTrue(event.occursOn(monday.plusDays(4))) // Friday
    }
}
