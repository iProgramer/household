package com.household.domain.model

import java.time.LocalDate
import java.util.UUID

@JvmInline value class TaskId(val value: UUID)
@JvmInline value class HouseholdId(val value: UUID)

data class Task(
    val id: TaskId,
    val householdId: HouseholdId,
    val title: String,
    val date: LocalDate?,
    val assignedTo: MemberId?,
    val status: TaskStatus,
    val recurrenceRule: RecurrenceRule? = null,
) {
    companion object {
        fun create(
            householdId: HouseholdId,
            title: String,
            date: LocalDate?,
            assignedTo: MemberId? = null,
            recurrenceRule: RecurrenceRule? = null,
        ): Task {
            require(title.isNotBlank()) { "Title must not be blank" }
            return Task(
                id = TaskId(UUID.randomUUID()),
                householdId = householdId,
                title = title.trim(),
                date = date,
                assignedTo = assignedTo,
                status = TaskStatus.OPEN,
                recurrenceRule = recurrenceRule,
            )
        }
    }

    fun complete(): Task = copy(status = TaskStatus.DONE)
    fun reschedule(newDate: LocalDate?): Task = copy(date = newDate)
    fun reassign(memberId: MemberId?): Task = copy(assignedTo = memberId)

    fun nextOccurrence(): Task? {
        if (recurrenceRule == null || date == null) return null
        return Task(
            id = TaskId(UUID.randomUUID()),
            householdId = householdId,
            title = title,
            date = recurrenceRule.nextDate(date),
            assignedTo = assignedTo,
            status = TaskStatus.OPEN,
            recurrenceRule = recurrenceRule,
        )
    }
}

enum class TaskStatus { OPEN, DONE }
