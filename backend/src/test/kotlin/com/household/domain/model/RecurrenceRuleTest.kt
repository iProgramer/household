package com.household.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RecurrenceRuleTest {

    private val saturday = LocalDate.of(2026, 5, 9) // a Saturday

    @Test
    fun `Daily adds one day`() {
        assertEquals(saturday.plusDays(1), RecurrenceRule.Daily.nextDate(saturday))
    }

    @Test
    fun `Weekly adds seven days`() {
        assertEquals(saturday.plusWeeks(1), RecurrenceRule.Weekly.nextDate(saturday))
    }

    @Test
    fun `Biweekly adds fourteen days`() {
        assertEquals(saturday.plusWeeks(2), RecurrenceRule.Biweekly.nextDate(saturday))
    }

    @Test
    fun `Monthly adds one month`() {
        assertEquals(saturday.plusMonths(1), RecurrenceRule.Monthly.nextDate(saturday))
    }

    @Test
    fun `OnWeekday gives next occurrence of that weekday`() {
        // Saturday → next Monday = May 11
        val nextMonday = RecurrenceRule.OnWeekday(DayOfWeek.MONDAY).nextDate(saturday)
        assertEquals(LocalDate.of(2026, 5, 11), nextMonday)
    }

    @Test
    fun `OnWeekday same weekday as from gives next week`() {
        // Saturday → next Saturday = May 16 (not same day)
        val nextSaturday = RecurrenceRule.OnWeekday(DayOfWeek.SATURDAY).nextDate(saturday)
        assertEquals(LocalDate.of(2026, 5, 16), nextSaturday)
    }
}
