package com.household.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@JvmInline value class FixedEventId(val value: UUID)

data class FixedEvent(
    val id: FixedEventId,
    val householdId: HouseholdId,
    val title: String,
    val date: LocalDate,
    val recurrenceRule: RecurrenceRule? = null,
) {
    companion object {
        fun create(
            householdId: HouseholdId,
            title: String,
            date: LocalDate,
            recurrenceRule: RecurrenceRule,
        ): FixedEvent {
            require(title.isNotBlank()) { "Title must not be blank" }
            return FixedEvent(
                id = FixedEventId(UUID.randomUUID()),
                householdId = householdId,
                title = title.trim(),
                date = date,
                recurrenceRule = recurrenceRule,
            )
        }
    }

    fun update(newTitle: String, newDate: LocalDate, newRecurrenceRule: RecurrenceRule): FixedEvent {
        require(newTitle.isNotBlank()) { "Title must not be blank" }
        return copy(title = newTitle.trim(), date = newDate, recurrenceRule = newRecurrenceRule)
    }

    fun occursOn(targetDate: LocalDate): Boolean {
        if (targetDate < date) return false
        if (recurrenceRule == null) return date == targetDate
        return when (recurrenceRule) {
            is RecurrenceRule.Daily -> true
            is RecurrenceRule.Weekly -> ChronoUnit.DAYS.between(date, targetDate) % 7 == 0L
            is RecurrenceRule.Biweekly -> ChronoUnit.DAYS.between(date, targetDate) % 14 == 0L
            is RecurrenceRule.Monthly -> date.dayOfMonth == targetDate.dayOfMonth
            is RecurrenceRule.OnWeekday -> targetDate.dayOfWeek == recurrenceRule.dayOfWeek
        }
    }
}
