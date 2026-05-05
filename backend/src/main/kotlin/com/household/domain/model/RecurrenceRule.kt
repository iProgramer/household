package com.household.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

sealed class RecurrenceRule {
    object Daily : RecurrenceRule()
    object Weekly : RecurrenceRule()
    object Biweekly : RecurrenceRule()
    object Monthly : RecurrenceRule()
    data class OnWeekday(val dayOfWeek: DayOfWeek) : RecurrenceRule()

    fun nextDate(from: LocalDate): LocalDate = when (this) {
        is Daily -> from.plusDays(1)
        is Weekly -> from.plusWeeks(1)
        is Biweekly -> from.plusWeeks(2)
        is Monthly -> from.plusMonths(1)
        is OnWeekday -> from.with(TemporalAdjusters.next(dayOfWeek))
    }
}
